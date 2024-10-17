/*
 * DownloadTexturesCCI class
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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import android.util.Log;

public class DownloadTexturesCCI extends DownloadTextures {
	private OpenGLES20Renderer gles20Renderer = null;

	public DownloadTexturesCCI(OpenGLES20Renderer mGLES20Renderer) {
		super(mGLES20Renderer);
		gles20Renderer = mGLES20Renderer;
	}

	@Override
	protected String doInBackground(String... urls) {
		gles20Renderer.downloadedTextures = 0;
		gles20Renderer.reloadedTextures = true;

		//String myUri = "http://pamola.um.maine.edu/DailySummary/frames/GFS-025deg/WORLD-CED/PRCP/";
		// Format: https://climatereanalyzer.org/wx_frames/gfs/world-ced/t2/2018-08-08-00z/00.png
		//String myUri = "https://climatereanalyzer.org/wx_frames/gfs/world-ced/prcp/";
		String myUri = "https://climatereanalyzer.org/wx_frames/gfs/world-wt/prcp/";
		String myPrefix = "";


		char tag = 'C';

		if (urls[0].equals("CLOUDS")) {
			//myUri = "http://pamola.um.maine.edu/DailySummary/frames/GFS-025deg/WORLD-CED/PRCP/";
			//myUri = "http://traveler.um.maine.edu/fcst_frames/GFS-025deg/WORLD-CED/PRCP/";
			//myUri = "https://climatereanalyzer.org/wx_frames/gfs/world-ced/prcp-tcld-topo/";
			//myUri = "https://climatereanalyzer.org/wx_frames/gfs/world-wt/prcp-tcld-topo/";
			//myUri = "https://pamola.um.maine.edu/wx_frames/gfs/world-ced/prcp-tcld-topo/";
			myUri = "https://climatereanalyzer.org/wx/fcst/maps/gfs/world-wt/prcp-mslp-gph500/";
			myPrefix = "gfs_world-wt_prcp-mslp-gph500";
			tag = 'C';
		} else if (urls[0].equals("TEMP")) {
			//myUri = "http://pamola.um.maine.edu/DailySummary/frames/GFS-025deg/WORLD-CED/T2/";
			//myUri = "http://traveler.um.maine.edu/fcst_frames/GFS-025deg/WORLD-CED/T2/";
			//myUri = "https://climatereanalyzer.org/wx_frames/gfs/world-ced/t2/";
			//myUri = "https://climatereanalyzer.org/wx_frames/gfs/world-wt/t2/";
			//myUri = "https://pamola.um.maine.edu/wx_frames/gfs/world-ced/t2/";
			myUri = "https://climatereanalyzer.org/wx/fcst/maps/gfs/world-wt/t2/";
			myPrefix = "gfs_world-wt_t2";
			tag = 'T';
		} else if (urls[0].equals("TEMP_AN")) {
			//myUri = "http://pamola.um.maine.edu/DailySummary/frames/GFS-025deg/WORLD-CED/T2_anom/";
			//myUri = "http://traveler.um.maine.edu/fcst_frames/GFS-025deg/WORLD-CED/T2_anom/";
			//myUri = "https://climatereanalyzer.org/wx_frames/gfs/world-ced/t2anom/";
			//myUri = "https://climatereanalyzer.org/wx_frames/gfs/world-wt/t2anom/";
			//myUri = "https://pamola.um.maine.edu/wx_frames/gfs/world-ced/t2anom/";
			myUri = "https://climatereanalyzer.org/wx/fcst/maps/gfs/world-wt/t2anom/";
			myPrefix = "gfs_world-wt_t2anom";
			tag = 't';
		} else if (urls[0].equals("WATER")) {
			//myUri = "http://pamola.um.maine.edu/DailySummary/frames/GFS-025deg/WORLD-CED/PWTR/";
			//myUri = "http://traveler.um.maine.edu/fcst_frames/GFS-025deg/WORLD-CED/PWTR/";
			//myUri = "https://climatereanalyzer.org/wx_frames/gfs/world-ced/pwtr/";
			//myUri = "https://climatereanalyzer.org/wx_frames/gfs/world-wt/pwtr/";
			//myUri = "https://pamola.um.maine.edu/wx_frames/gfs/world-ced/pwtr/";
			myUri = "https://climatereanalyzer.org/wx/fcst/maps/gfs/world-wt/pwtr/";
			myPrefix = "gfs_world-wt_pwtr";
			tag = 'w';
		} else if (urls[0].equals("WIND")) {
			//myUri = "http://pamola.um.maine.edu/DailySummary/frames/GFS-025deg/WORLD-CED/WS10/";
			//myUri = "http://traveler.um.maine.edu/fcst_frames/GFS-025deg/WORLD-CED/WS10/";
			//myUri = "https://climatereanalyzer.org/wx_frames/gfs/world-ced/ws10/";
			//myUri = "https://pamola.um.maine.edu/wx_frames/gfs/world-ced/ws10-mslp/";
			//myUri = "https://climatereanalyzer.org/wx_frames/gfs/world-ced/ws10-mslp/";
			//myUri = "https://climatereanalyzer.org/wx_frames/gfs/world-wt/ws10-mslp/";
			myUri = "https://climatereanalyzer.org/wx/fcst/maps/gfs/world-wt/ws10-mslp/";
			myPrefix = "gfs_world-wt_ws10-mslp";
			tag = 'v';
		} else if (urls[0].equals("JET")) {
			//myUri = "http://pamola.um.maine.edu/DailySummary/frames/GFS-025deg/WORLD-CED/WS250/";
			//myUri = "http://traveler.um.maine.edu/fcst_frames/GFS-025deg/WORLD-CED/WS250/";
			//myUri = "https://climatereanalyzer.org/wx_frames/gfs/world-ced/ws250-mslp/";
			//myUri = "https://climatereanalyzer.org/wx_frames/gfs/world-wt/ws250-mslp/";
			myUri = "https://climatereanalyzer.org/wx/fcst/maps/gfs/world-wt/ws250-mslp/";
			myPrefix = "gfs_world-wt_ws250-mslp";
			tag = 'j';
		} else if (urls[0].equals("SNOW")) {
			//myUri = "http://pamola.um.maine.edu/DailySummary/frames/GFS-025deg/WORLD-CED/SNOW/";
			//myUri = "http://traveler.um.maine.edu/fcst_frames/GFS-025deg/WORLD-CED/SNOW/";
			//myUri = "https://climatereanalyzer.org/wx_frames/gfs/world-ced/snowd-mslp/";
			//myUri = "https://climatereanalyzer.org/wx_frames/gfs/world-wt/snowd-mslp/";
			//myUri = "https://pamola.um.maine.edu/wx_frames/gfs/world-ced/snowd-mslp/";
			myUri = "https://climatereanalyzer.org/wx/fcst/maps/gfs/world-wt/snowd-mslp/";
			myPrefix = "gfs_world-wt_snowd-mslp";
			tag = 's';
		} else if (urls[0].equals("TEMP_AN_1Y")) {
			//myUri = "http://pamola.um.maine.edu/DailySummary/frames/GFS-025deg/WORLD-CED/T2_anom/";
			//myUri = "http://traveler.um.maine.edu/fcst_frames/GFS-025deg/WORLD-CED/T2_anom/";
			//myUri = "https://pamola.um.maine.edu/wxrmaps/clim_frames/t2anom/world-ced/";
			//myUri = "https://climatereanalyzer.org/reanalysis/daily_maps/clim_frames/t2anom/world-ced/";
			myUri = "https://climatereanalyzer.org/reanalysis/daily_maps/clim_frames/t2anom/world-wt/";
			tag = 'a';
		} else if (urls[0].equals("OISST_V2_1Y")) {
			//myUri = "http://pamola.um.maine.edu/DailySummary/frames/GFS-025deg/WORLD-CED/T2_anom/";
			//myUri = "http://traveler.um.maine.edu/fcst_frames/GFS-025deg/WORLD-CED/T2_anom/";
			//myUri = "https://pamola.um.maine.edu/wxrmaps/clim_frames/sstanom/world-ced2/";
			myUri = "https://climatereanalyzer.org/reanalysis/daily_maps/clim_frames/sstanom/world-ced2/";
			tag = 'b';
		} else if (urls[0].equals("OISST_V2")) {
			//myUri = "http://pamola.um.maine.edu/DailySummary/frames/GFS-025deg/WORLD-CED/SNOW/";
			//myUri = "http://traveler.um.maine.edu/fcst_frames/GFS-025deg/WORLD-CED/SNOW/";
			//myUri = "https://pamola.um.maine.edu/cr/clim/sst/frames/oisst2/world-ced2/sstanom/";
			myUri = "https://climatereanalyzer.org/clim/sst/frames/oisst2/world-ced2/sstanom/";
			tag = 'O';
		} else if (urls[0].equals("ERSST_V5")) {
			//myUri = "http://pamola.um.maine.edu/DailySummary/frames/GFS-025deg/WORLD-CED/SNOW/";
			//myUri = "http://traveler.um.maine.edu/fcst_frames/GFS-025deg/WORLD-CED/SNOW/";
			//myUri = "https://pamola.um.maine.edu/cr/clim/sst/frames/ersst5/world-ced2/sstanom/";
			myUri = "https://climatereanalyzer.org/clim/sst/frames/ersst5/world-ced2/sstanom/";
			tag = 'e';
		}

		gles20Renderer.mTag = tag;

		InputStream is2 = null;

		URLConnection ucon = null;
		URL url = null;

		// load image from cache

		// find latest image but not older then 3 hours
		String filename = null;
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		//File dir = gles20Renderer.mContext.getFilesDir();
		File dir = gles20Renderer.mContext.getCacheDir();
		File[] subFiles;
		long epoch;
		long current_real = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")).getTimeInMillis();
		long data_generated_epoch;

		HashMap<Integer, String> iKeys = new HashMap<Integer, String>();
		HashMap<Integer, Long> eKeys = new HashMap<Integer, Long>();
		HashMap<Integer, Long> mKeys = new HashMap<Integer, Long>();


		int files_to_download;

		// last 35 years
		if (tag == 'O') {
			files_to_download = 35;
		}
		// last 65 years
		if (tag == 'e') {
			files_to_download = 65;
		}
		// last 1 year
		else if (tag == 'a' || tag == 'b') {
			files_to_download = 365 - 15;
		}
		// next 7d
		else {
			files_to_download = (168) / 6;
		}

		progressDialogSetMax(files_to_download);


		// Download the older files if possible
		epoch = cal.getTimeInMillis();

		// assume that if is less than 9:00 am UTC, the images are old from previous day
		long reload = 0L;

		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));

		// last 35 years or last 65 years
		if (tag == 'O' || tag == 'e') {
			c.set(Calendar.HOUR_OF_DAY, 9);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);

			// lets day, that can be safely iterated
			c.set(Calendar.DAY_OF_YEAR, 100);

			reload = c.getTimeInMillis();
		}
		// last 1 year
		else if (tag == 'a' || tag == 'b') {
			c.set(Calendar.HOUR_OF_DAY, 9);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			reload = c.getTimeInMillis();
		}
		// next 7d
		else {
			if (c.get(Calendar.HOUR_OF_DAY) < 9) {
				epoch -= 168 * 3600 * 1000;

				c.set(Calendar.HOUR_OF_DAY, 9);
				c.set(Calendar.MINUTE, 0);
				c.set(Calendar.SECOND, 0);
				c.set(Calendar.MILLISECOND, 0);
				reload = c.getTimeInMillis();
				reload -= 168 * 3600 * 1000;

			} else {
				c.set(Calendar.HOUR_OF_DAY, 9);
				c.set(Calendar.MINUTE, 0);
				c.set(Calendar.SECOND, 0);
				c.set(Calendar.MILLISECOND, 0);
				reload = c.getTimeInMillis();

			}
		}

		// Historical data
		if (tag == 'O' || tag == 'e' || tag == 'a' || tag == 'b') {
			epoch = c.getTimeInMillis();

			data_generated_epoch = current_real;
		}
		// Forecast data
		else {
			// Download the older files if possible
			epoch = cal.getTimeInMillis();

			// if is less than 8:00 am UTC, assume the images are old from previous day
			c = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
			data_generated_epoch = current_real;
			if (c.get(Calendar.HOUR_OF_DAY) < 8) {
				epoch -= 168 * 3600 * 1000;
				data_generated_epoch -= 168 * 3600 * 1000;
			}
		}

		// last 35 years
		if (tag == 'O') {
			// start loading from beginning and not backward
			epoch = epoch - 35 * (long) (365.25 * 24 * 3600 * 1000);
		}
		// last 65 years
		else if (tag == 'e') {
			// start loading from beginning and not backward
			epoch = epoch - 65 * (long) (365.25 * 24 * 3600 * 1000);
		}
		// last 1 year
		else if (tag == 'a' || tag == 'b') {
			// start loading from beginning and not backward
			epoch = epoch - (long) (1.0 * 365.25 * 24 * 3600 * 1000);
		}

		for (int h = 0; h < files_to_download; h += 1) {

			if (isCancelled() == true) {
				break;
			}


			boolean exists = false;

			// last 35 years or last 65 years
			if (tag == 'O' || tag == 'e') {
				// each 1 year
				epoch = epoch + (long) (365.25 * 24 * 3600 * 1000);
			}
			// last 1 year
			else if (tag == 'a' || tag == 'b') {
				// each 1 day
				epoch = epoch + 24 * 3600 * 1000;
			}
			// next 7d
			else {
				// each 6h
				epoch = epoch + 6 * 3600 * 1000;
			}


			Log.d("H21lab", "h = " + h);


			// last 35 years
			if (tag == 'O') {
				// do not download too new data
				if (epoch - current_real > ((long) -1 * 365.25 * 24 * 3600 * 1000)) {

					Log.d("H21lab", "Data fom eKeys too new h = " + h);

					continue;
				}
				// do not download old data
				if (epoch - current_real < ((long) -35 * 365.25 * 24 * 3600 * 1000)) {

					Log.d("H21lab", "Data fom eKeys old h = " + h);

					files_to_download++;
					continue;
				}

			}
			// last 65 years
			else if (tag == 'e') {
				// do not download too new data
				if (epoch - current_real > ((long) -1 * 365.25 * 24 * 3600 * 1000)) {

					Log.d("H21lab", "Data fom eKeys too new h = " + h);

					continue;
				}
				// do not download old data
				if (epoch - current_real < ((long) -65 * 365.25 * 24 * 3600 * 1000)) {

					Log.d("H21lab", "Data fom eKeys old h = " + h);

					files_to_download++;
					continue;
				}
			}
			// last 1 year
			else if (tag == 'a' || tag == 'b') {
				// do not download too new data
				if (epoch - current_real > ((long) -15 * 24 * 3600 * 1000)) {

					Log.d("H21lab", "Data fom eKeys too new h = " + h);

					continue;
				}
				// do not download old data
				if (epoch - current_real < ((long) -(1.0 * 365.25) * 24 * 3600 * 1000)) {

					Log.d("H21lab", "Data fom eKeys old h = " + h);

					files_to_download++;
					continue;
				}
			}
			// next 7d
			else {
				// do not download too new data
				if (epoch - current_real > (2 * 168) * 3600 * 1000) {

					Log.d("H21lab", "Data fom eKeys too new h = " + h);

					continue;
				}
				// do not download old data
				if (epoch - current_real < -(3 * 6) * 3600 * 1000) {

					Log.d("H21lab", "Data fom eKeys old h = " + h);

					files_to_download++;
					continue;
				}
			}

			filename = OpenGLES20Renderer.getNameFromEpoch(tag, epoch);

			exists = false;
			subFiles = dir.listFiles();
			if (subFiles != null) {
				for (File file : subFiles) {
					// last 35 years or last 65 years
					if (tag == 'O' || tag == 'e') {
						// file exist

						if (filename.equals(file.getName())) {
							exists = true;
							break;
						}
					}
					// last 1 year
					else if (tag == 'a' || tag == 'b') {
						// file exist

						if (filename.equals(file.getName())) {
							exists = true;
							break;
						}
					}
					// next 7d
					else {
						// file exist and is not newer file

						if (filename.equals(file.getName()) && (file.lastModified() - reload > 0)) {
							exists = true;
							break;
						}
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


				// last 35 years
				if (tag == 'O') {
					url = new URL(myUri + "/"
							+ getOISSTV2NameFromEpoch(epoch) + ".png");
				}
				// last 65 years
				else if (tag == 'e') {
					url = new URL(myUri + "/"
							+ getERSSTV5NameFromEpoch(epoch) + ".png");
				}
				// last 1 year
				else if (tag == 'a') {
					url = new URL(myUri + "/"
							+ getTEMP_AN_1Y_NameFromEpoch(epoch) + ".png");
				}
				// last 1 year
				else if (tag == 'b') {
					url = new URL(myUri + "/"
							+ getOISSTV2_1Y_NameFromEpoch(epoch) + ".png");
				}
				// next 7d
				else {
					url = new URL(myUri
							+ getDirectoryNameFromEpoch(data_generated_epoch) + "/"
							+ myPrefix + "_" + getFileNamePrefixFromEpoch(data_generated_epoch) + "_" + String.format("%03d", 6 * h)
							+ ".png");
							//+ String.format("%02d", h) + ".png");
				}


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

				// delete previous files
				subFiles = dir.listFiles();
				if (subFiles != null) {
					for (File file : subFiles) {
						if (file.getName().startsWith(filename)) {
							gles20Renderer.mContext.deleteFile(file.getName());
						}
					}
				}

				//save texture
				gles20Renderer.saveTexture(filename, ba, 1024, 1024);

				mis2.close();

			} catch (MalformedURLException e1) {
				Log.e("H21lab", "Connection error " + "MalformedURLException " + e1.getMessage());
			} catch (Exception e2) {
				if (ucon != null) {
					Log.e("H21lab", "Unable to connect to " + ucon.getURL().toString() + " " + e2.getMessage());
				} else {
					Log.e("H21lab", "Unable to connect to " + myUri + " " + e2.getMessage());
				}
			}

			progressDialogUpdate();

		}


		return "";
	}

	// 2018-08-07-00z
	public static String getDirectoryNameFromEpoch(long epoch) {

		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
		c.setTimeInMillis(epoch);

		String year = String.format("%04d", (int) (c.get(Calendar.YEAR)));
		String month = String.format("%02d", (int) (c.get(Calendar.MONTH) + 1));
		String day = String.format("%02d", (int) (c.get(Calendar.DAY_OF_MONTH)));

		String filename = Integer.toString(c.get(Calendar.YEAR)) + "-" + month + "-" + day + "-00z";

		return filename;

	}

	// 2024101700_006.png
	public static String getFileNamePrefixFromEpoch(long epoch) {

		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
		c.setTimeInMillis(epoch);

		String year = String.format("%04d", (int) (c.get(Calendar.YEAR)));
		String month = String.format("%02d", (int) (c.get(Calendar.MONTH) + 1));
		String day = String.format("%02d", (int) (c.get(Calendar.DAY_OF_MONTH)));
		//String hour = String.format("%03d", (int) (c.get(Calendar.HOUR_OF_DAY)));

		String filename = Integer.toString(c.get(Calendar.YEAR)) + "" + month + "" + day + "00";

		return filename;

	}

	// 2019/t2anom_world-ced_2019_d151.png
	public static String getTEMP_AN_1Y_NameFromEpoch(long epoch) {
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
		c.setTimeInMillis(epoch);

		String year = String.format("%04d", (int) (c.get(Calendar.YEAR)));
		String day = String.format("%03d", (int) (c.get(Calendar.DAY_OF_YEAR)));

		//String filename = year + "/" + "t2anom_world-ced_" + year + "_d" + day;
		String filename = year + "/" + "t2anom_world-wt_" + year + "_d" + day;

		return filename;

	}

	// 2019/sstanom_world-ced2_2019_d152.png
	public static String getOISSTV2_1Y_NameFromEpoch(long epoch) {
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
		c.setTimeInMillis(epoch);

		String year = String.format("%04d", (int) (c.get(Calendar.YEAR)));
		String day = String.format("%03d", (int) (c.get(Calendar.DAY_OF_YEAR)));

		String filename = year + "/" + "sstanom_world-ced2_" + year + "_d" + day;

		return filename;

	}

	// oisst2_world-ced2_sstanom_2019-03.png
	public static String getOISSTV2NameFromEpoch(long epoch) {
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
		c.setTimeInMillis(epoch);

		String year = String.format("%04d", (int) (c.get(Calendar.YEAR)));
		//String month = String.format("%02d", (int)(c.get(Calendar.MONTH) + 1));
		//String day = String.format("%02d", (int)(c.get(Calendar.DAY_OF_MONTH)));

		String filename = "oisst2_world-ced2_sstanom_" + Integer.toString(c.get(Calendar.YEAR)) + "-" + "13";

		return filename;

	}

	// ersst5_world-ced2_sstanom_1886-13.png
	public static String getERSSTV5NameFromEpoch(long epoch) {
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
		c.setTimeInMillis(epoch);

		String year = String.format("%04d", (int) (c.get(Calendar.YEAR)));
		//String month = String.format("%02d", (int)(c.get(Calendar.MONTH) + 1));
		//String day = String.format("%02d", (int)(c.get(Calendar.DAY_OF_MONTH)));

		String filename = "ersst5_world-ced2_sstanom_" + Integer.toString(c.get(Calendar.YEAR)) + "-" + "13";

		return filename;

	}

}