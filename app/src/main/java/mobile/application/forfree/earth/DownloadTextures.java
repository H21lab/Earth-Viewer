/*
 * DownloadTextures class
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

import java.io.File;
import java.io.InputStream;
import java.util.Calendar;
import java.util.TimeZone;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.opengl.ETC1Util.ETC1Texture;
import android.os.AsyncTask;
import android.util.Log;

public class DownloadTextures extends AsyncTask<String, Void, String> {
	private OpenGLES20Renderer gles20Renderer = null;

	public DownloadTextures(OpenGLES20Renderer mGLES20Renderer) {
		super();
		gles20Renderer = mGLES20Renderer;
	}

	private ProgressDialog pd = null;
	private int pd_progress;

	public void progressDialogShow() {
		try {
			if (pd != null && this.getStatus() == AsyncTask.Status.RUNNING) {
				pd.show();
			}
		} catch (Exception e) {
			Log.e("H21lab", "Unable to create ProgressDialog " + e.getMessage());
		}
	}

	public void progressDialogSetMax(int files_to_download) {
		try {
			if (pd != null) {
				pd.setMax(files_to_download);
			}
		} catch (Exception e) {
			Log.e("H21lab", "Unable to create ProgressDialog " + e.getMessage());
		}
	}

	public void progressDialogUpdate() {
		try {
			pd_progress++;
			if (pd != null) {
				pd.setProgress(pd_progress);
			}
			//reloadTextures();
		} catch (Exception e) {
			Log.e("H21lab", "Unable to create ProgressDialog " + e.getMessage());
		}
	}

	@Override
	protected void onPreExecute() {
		pd_progress = 0;

		try {
			pd = new ProgressDialog(gles20Renderer.mContext);

			if (pd != null) {
				pd.setMessage("Please wait...");
				pd.setCancelable(true);

				pd.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						gles20Renderer.mDownloadTextures.cancel(true);
						reloadTextures();

					}
				});

				pd.setMax(100);
				pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				pd.setProgress(pd_progress);
				pd.show();
			}
		} catch (Exception e) {
			Log.e("H21lab", "Unable to create ProgressDialog " + e.getMessage());
		}
		super.onPreExecute();
	}

	@Override
	protected String doInBackground(String... urls) {

		return "";
	}

	void reloadTextures() {
		File[] subFiles;
		Calendar cal;
		Long epoch;
		//File dir = gles20Renderer.mContext.getFilesDir();
		File dir = gles20Renderer.mContext.getCacheDir();
		InputStream is2 = null;

		// clean the internal storage and select actual cloadmap
		subFiles = dir.listFiles();
		cal = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
		epoch = cal.getTimeInMillis();

		for (String f : gles20Renderer.mCloudMapFilename.keySet()) {
			// reload always after download
			Long e = gles20Renderer.mCloudMapFilename.get(f);
			gles20Renderer.mCloudMap.remove(e);
			gles20Renderer.mCloudMapEpochToFilename.remove(e);
			gles20Renderer.mCloudMapFilename.remove(f);
		}

		if (subFiles != null) {
			for (File file : subFiles) {
				if (!file.exists()) {
					continue;
				}

				if (gles20Renderer.mCloudMapFilename.containsKey(file.getName())) {
					continue;
				}

				Log.d("H21lab", "Files: " + file.getName() + " " + OpenGLES20Renderer.getEpochFromName(file.getName()) + " " + file.length());

				if (OpenGLES20Renderer.getTag(file.getName()) == 'm') {
					// delete older than 6h
					if (epoch - OpenGLES20Renderer.getEpochFromName(file.getName()) > (6 + 2) * 3600 * 1000) {
						Log.d("H21lab",
								"Deleting old for tag m: " + file.getName() + " epoch " + epoch + " file epoch " + OpenGLES20Renderer.getEpochFromName(file.getName()));
						file.delete();
						continue;
					}
				} else if (OpenGLES20Renderer.getTag(file.getName()) == 'h') {
					// delete older than 2h
					if (epoch - OpenGLES20Renderer.getEpochFromName(file.getName()) > (2 + 1) * 3600 * 1000) {
						Log.d("H21lab",
								"Deleting old for tag h: " + file.getName() + " epoch " + epoch + " file epoch " + OpenGLES20Renderer.getEpochFromName(file.getName()));
						file.delete();
						continue;
					}
				} else if (OpenGLES20Renderer.getTag(file.getName()) == 'I' || OpenGLES20Renderer.getTag(file.getName()) == 'W') {
					// delete older than 168h
					if (epoch - OpenGLES20Renderer.getEpochFromName(file.getName()) > (168 + 6) * 3600 * 1000) {
						Log.d("H21lab",
								"Deleting old for tag I or W: " + file.getName() + " epoch " + epoch + " file epoch " + OpenGLES20Renderer.getEpochFromName(file.getName()));
						file.delete();
						continue;
					}
				} else if (OpenGLES20Renderer.getTag(file.getName()) == 'a' || OpenGLES20Renderer.getTag(file.getName()) == 'b') {
					// delete older than 1 year
					if (epoch - OpenGLES20Renderer.getEpochFromName(file.getName()) > ((long) ((1) * (365.25 + 6) * 24 * 3600)) * 1000) {
						Log.d("H21lab",
								"Deleting old for tag X, Y: " + file.getName() + " epoch " + epoch + " file epoch " + OpenGLES20Renderer.getEpochFromName(file.getName()));
						file.delete();
						continue;
					}
				} else if (OpenGLES20Renderer.getTag(file.getName()) == 'O') {
					// delete older than 35 years
					if (epoch - OpenGLES20Renderer.getEpochFromName(file.getName()) > ((long) ((35 + 1) * 365.25 * 24 * 3600)) * 1000) {
						Log.d("H21lab",
								"Deleting old for tag O: " + file.getName() + " epoch " + epoch + " file epoch " + OpenGLES20Renderer.getEpochFromName(file.getName()));
						file.delete();
						continue;
					}
				} else if (OpenGLES20Renderer.getTag(file.getName()) == 'e') {
					// delete older than 65 years
					if (epoch - OpenGLES20Renderer.getEpochFromName(file.getName()) > ((long) ((65 + 1) * 365.25 * 24 * 3600)) * 1000) {
						Log.d("H21lab",
								"Deleting old for tag E: " + file.getName() + " epoch " + epoch + " file epoch " + OpenGLES20Renderer.getEpochFromName(file.getName()));
						file.delete();
						continue;
					}
				}

				// default delete older than 24h
				else {
					if (epoch - OpenGLES20Renderer.getEpochFromName(file.getName()) > (24 + 6) * 3600 * 1000) {
						Log.d("H21lab",
								"Deleting old: " + file.getName() + " epoch " + epoch + " file epoch " + OpenGLES20Renderer.getEpochFromName(file.getName()));
						file.delete();
						continue;
					}
				}

				// delete newer files then 7d
				if (OpenGLES20Renderer.getEpochFromName(file.getName()) - epoch > (168 + 24) * 3600 * 1000) {
					Log.d("H21lab",
							"Deleting new: " + file.getName() + " epoch " + epoch + " file epoch " + OpenGLES20Renderer.getEpochFromName(file.getName()));
					file.delete();
					continue;
				}

				if (OpenGLES20Renderer.getTag(file.getName()) != gles20Renderer.mTag) {
					continue;
				}

				ETC1Texture bi = gles20Renderer.loadTexture(file.getName());
				if (bi != null) {
					Log.d("H21lab", "PUT file into HASH = " + " " + OpenGLES20Renderer.getEpochFromName(file.getName()) + " " + file.getName());
					Long e = OpenGLES20Renderer.getEpochFromName(file.getName());
					gles20Renderer.mCloudMap.put(e, bi);
					gles20Renderer.mCloudMapFilename.put(file.getName(), e);
					gles20Renderer.mCloudMapEpochToFilename.put(e, file.getName());
				}

			}
		}


		if (gles20Renderer.mBitmap3 == null) {

			is2 = gles20Renderer.mContext.getResources().openRawResource(R.raw.clouds);
			try {
				gles20Renderer.mBitmap3 = BitmapFactory.decodeStream(is2);
			} finally {
				try {
					is2.close();
				} catch (Exception e) {
					// Ignore.
				}
			}

		}

		gles20Renderer.initializedShaders = false;
		;

		gles20Renderer._e1 = 0L;
		gles20Renderer._e2 = 0L;
		gles20Renderer._e3 = 0L;
		gles20Renderer._e4 = 0L;

		gles20Renderer.downloadedTextures = 1;
		gles20Renderer.reloadedTextures = true;
	}

	@Override
	protected void onPostExecute(String response1) {
		reloadTextures();


		//Some Code.....
		try {
			if (pd != null && pd.isShowing())
				pd.dismiss();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}