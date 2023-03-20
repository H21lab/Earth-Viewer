/*
 * DownloadTexturesXplanet class
 *
 * This file is part of Earth Viewer
 * Copyright 2023, Martin Kacer, H21 lab
 *
 * Earth Viewer is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Earth Viewer is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Earth Viewer.  If not, see <http://www.gnu.org/licenses/>.
 */

package mobile.application.forfree.earth;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.TimeZone;

import android.graphics.Bitmap;
import android.util.Log;

public class DownloadTexturesXplanet extends DownloadTextures {
	private OpenGLES20Renderer gles20Renderer = null;

	public DownloadTexturesXplanet(OpenGLES20Renderer mGLES20Renderer) {
		super(mGLES20Renderer);
		gles20Renderer = mGLES20Renderer;
	}

	@Override
	protected String doInBackground(String... urls) {
		gles20Renderer.downloadedTextures = 0;
		gles20Renderer.reloadedTextures = true;

		char tag = 'X';
		gles20Renderer.mTag = tag;

		InputStream is2 = null;
		Bitmap b = null;

		URLConnection ucon = null;
		URL url = null;

		// load image from cache

		// find latest image but not older then 3 hours
		String filename = null;
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));

		cal.set(Calendar.HOUR_OF_DAY, 24 * (int) (cal.get(Calendar.HOUR_OF_DAY) / 24));
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		//File dir = gles20Renderer.mContext.getFilesDir();
		File dir = gles20Renderer.mContext.getCacheDir();
		File[] subFiles;
		long epoch = cal.getTimeInMillis();
		long current = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")).getTimeInMillis();

		int files_to_download = 1;
		progressDialogSetMax(files_to_download);

		boolean exists = false;

		filename = OpenGLES20Renderer.getNameFromEpoch(tag, epoch);

		subFiles = dir.listFiles();
		if (subFiles != null) {
			for (File file : subFiles) {
				if (filename.equals(file.getName())) {
					exists = true;
					break;
				}
			}
		}

		if (exists == false) {
			// download from internet
			try {
				/* Open a connection to that URL. */
				url = new URL("http://xplanetclouds.com/free/local/clouds_2048.jpg");
				Log.d("H21lab", "Downloading: " + url.toString());

				ucon = url.openConnection();
				ucon.setUseCaches(false);
				ucon.connect();

				is2 = ucon.getInputStream();

				ByteArrayOutputStream mis2 = new ByteArrayOutputStream();
				byte data[] = new byte[1024];
				int count;
				while ((count = is2.read(data, 0, 1024)) != -1) {
					mis2.write(data, 0, count);
				}
				mis2.flush();
				is2.close();

				// b = BitmapFactory.decodeStream(is2);
				byte[] ba = mis2.toByteArray();
				gles20Renderer.saveTexture(filename, ba, 2048, 1024);

				mis2.close();

			} catch (Exception e) {

				Log.e("H21lab", "Unable to connect to " + ucon.getURL().toString() + " " + e.getMessage());

				if (ucon != null) {

					try {
						url = new URL(ucon.getURL().toString().replace(".nyud.net:8080", ""));
						Log.d("H21lab", "Downloading: " + url.toString());

						ucon = url.openConnection();
						ucon.setUseCaches(false);
						ucon.connect();

						is2 = ucon.getInputStream();

						ByteArrayOutputStream mis2 = new ByteArrayOutputStream();
						byte data[] = new byte[1024];
						int count;
						while ((count = is2.read(data, 0, 1024)) != -1) {
							mis2.write(data, 0, count);
						}
						mis2.flush();
						is2.close();

						byte[] ba = mis2.toByteArray();
						gles20Renderer.saveTexture(filename, ba, 2048, 1024);

						mis2.close();

					} catch (Exception e2) {
						if (ucon != null) {
							Log.e("H21lab", "Unable to connect to " + ucon.getURL().toString() + " " + e2.getMessage());
						} else if (url != null) {
							Log.e("H21lab", "Unable to connect to " + url.toString() + " " + e2.getMessage());
						}
					}
				}

			}
		}

		progressDialogUpdate();

		return "";
	}

}