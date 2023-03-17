/*
 * DownloadTexturesGoes class
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

import android.graphics.Bitmap;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadTexturesNoaa extends DownloadTextures {
	private OpenGLES20Renderer gles20Renderer = null;

	public DownloadTexturesNoaa(OpenGLES20Renderer mGLES20Renderer) {
		super(mGLES20Renderer);
		gles20Renderer = mGLES20Renderer;
	}

	@Override
	protected String doInBackground(String... urls) {
		gles20Renderer.downloadedTextures = 0;
		gles20Renderer.reloadedTextures = true;

		String myUri = "https://services.swpc.noaa.gov/images/animations/ovation/north/";
		char tag = 'N';

		if (urls[0].equals("NOAA_AURORA_NORTH")) {
			myUri = "https://services.swpc.noaa.gov/images/animations/ovation/north/";
			tag = 'N';
		} else if (urls[0].equals("NOAA_AURORA_SOUTH")) {
			myUri = "https://services.swpc.noaa.gov/images/animations/ovation/south/";
			tag = 'S';
		}

		gles20Renderer.mTag = tag;

		InputStream is2 = null;
		Bitmap b = null;

		URLConnection ucon = null;
		URL url = null;

		// load image from cache

		// find latest image but not older then 3 hours
		String filename = null;
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));

		cal.set(Calendar.HOUR_OF_DAY, 1 * ((int) (cal.get(Calendar.HOUR_OF_DAY) / 1)));
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		File dir = gles20Renderer.mContext.getFilesDir();
		File[] subFiles;
		long epoch;
		long current = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")).getTimeInMillis();

		HashMap<Integer, String> iKeys = new HashMap<Integer, String>();
		HashMap<Integer, Long> eKeys = new HashMap<Integer, Long>();

		int files_to_download = 0;

		if (iKeys.size() == 0 || eKeys.size() == 0) {
			try {

				//HttpClient httpClient = new DefaultHttpClient();
				//HttpGet get = new HttpGet(myUri);

				//HttpResponse response = httpClient.execute(get);

				URL urlObj = new URL(myUri);
				URLConnection urlConnection = (URLConnection) urlObj.openConnection();
				Log.d("H21lab", "HTTP GET OK");

				urlConnection.setUseCaches(false);
				// unable to connect during the first connect, reduce timeout for this attempt
				urlConnection.setConnectTimeout(300);
				urlConnection.setReadTimeout(300);
				urlConnection.connect();

				urlConnection.setConnectTimeout(5000);
				urlConnection.setReadTimeout(5000);

				// Build up result
				//String bodyHtml = EntityUtils.toString(response.getEntity());
				//InputStream is = urlConnection.getInputStream();
				//String bodyHtml = is.toString();

				//BufferedReader bufReader = new BufferedReader(new StringReader(bodyHtml));
				BufferedReader bufReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
				// <a href="aurora_N_2023-03-15_1450.jpg">aurora_N_2023-03-15_..&gt;</a> 2023-03-15 14:51  249K
				// <a href="aurora_S_2023-03-15_1450.jpg">aurora_S_2023-03-15_..&gt;</a> 2023-03-15 14:51  188K
				// Download only 10 mins intervals
				Pattern pattern = Pattern.compile("href=\\\"(aurora_[N|S]_)(\\S+0)\\.jpg\\\"");

				String line;
				int i = 0;
				int j = 0;
				while ((line = bufReader.readLine()) != null) {
					Matcher matcher = pattern.matcher(line);
					while (matcher.find()) {
						Log.d("H21lab", "iKeys: " + i + " " + matcher.group(1) + matcher.group(2));
						iKeys.put(i, matcher.group(1) + matcher.group(2));
						i++;
					}

					matcher = pattern.matcher(line);
					while (matcher.find()) {
						Log.d("H21lab", "eKeys: " + matcher.group(2));

						String str = matcher.group(2);
						str = str.trim().replaceAll("\\t+", " ");
						str = str.trim().replaceAll("\\s+", " ");
						SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HHmm");
						df.setTimeZone(TimeZone.getTimeZone("GMT"));
						java.util.Date d = df.parse(str);
						long e = d.getTime();
						// add cca 1h, it is forecast data
						e = e + 3600 * 1000;

						if (current - e <= (24 + 2) * 3600 * 1000) {
							files_to_download++;
						}

						Log.d("H21lab", "eKeys: " + j + " " + e);
						eKeys.put(i, e);
						j++;
					}
				}

			} catch (Exception e3) {
				Log.e("H21lab", "Connection error " + e3.getMessage());
				e3.printStackTrace();
			}
		}

		progressDialogSetMax(files_to_download);

		// Download the older files if possible
		epoch = cal.getTimeInMillis();
		for (int h = 0; h < eKeys.size(); h += 1) {

			if (isCancelled() == true) {
				break;
			}

			boolean exists = false;

			Log.d("H21lab", "h = " + h);

			if (!eKeys.containsKey(h)) {

				Log.d("H21lab", "Does not contain eKeys h = " + h);

				continue;
			}

			epoch = eKeys.get(h);


			// do not download too old data
			if (current - epoch > (24 + 2) * 3600 * 1000) {

				Log.d("H21lab", "Data fom eKeys too old h = " + h);

				continue;
			}
			filename = OpenGLES20Renderer.getNameFromEpoch(tag, epoch);
			exists = false;
			subFiles = dir.listFiles();
			if (subFiles != null) {
				for (File file : subFiles) {
					if (filename.equals(file.getName())) {
						exists = true;
						break;
					}
				}
			}
			// do not download already existing
			if (exists) {

				progressDialogUpdate();

				Log.d("H21lab", "File already exists from eKeys h = " + h);

				continue;
			}
			// change filename
			Log.d("H21lab", "New filename = " + filename + " e = " + epoch);


			// download from internet
			try {
				//oiswww.eumetsat.org/IPPS/html/MSG/IMAGERY/IR108/BW/FULLDISC/IMAGESDisplay/
				url = new URL(myUri + iKeys.get(h) + ".jpg");

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

				gles20Renderer.saveTexture(filename, ba, 512, 512);

				mis2.close();


			} catch (Exception e) {

				if (ucon != null) {
					Log.e("H21lab", "Unable to connect to " + ucon.getURL().toString() + " " + e.getMessage());
				} else {
					Log.e("H21lab", "Unable to connect to " + myUri + " " + e.getMessage());
				}

			}

			progressDialogUpdate();

		}

		return "";
	}

}