/*
 * DownloadTexturesCCI class
 *
 * This file is part of Earth Viewer
 * Copyright 2016, Martin Kacer, H21 lab
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

public class DownloadTexturesCCI extends DownloadTextures
{           


    @Override
    protected String doInBackground(String... urls) 
    {
    	OpenGLES20Renderer.downloadedTextures = 0;
    	OpenGLES20Renderer.reloadedTextures = true;
    	
    	//String myUri = "http://pamola.um.maine.edu/DailySummary/frames/GFS-025deg/WORLD-CED/PRCP/";
		// Format: https://climatereanalyzer.org/wx_frames/gfs/world-ced/t2/2018-08-08-00z/00.png
		String myUri = "https://climatereanalyzer.org/wx_frames/gfs/world-ced/prcp/";


		char tag = 'C';
    	
    	if (urls[0].equals("CLOUDS")) {
    		//myUri = "http://pamola.um.maine.edu/DailySummary/frames/GFS-025deg/WORLD-CED/PRCP/";
		    //myUri = "http://traveler.um.maine.edu/fcst_frames/GFS-025deg/WORLD-CED/PRCP/";
			myUri = "https://climatereanalyzer.org/wx_frames/gfs/world-ced/prcp-tcld-topo/";
    		tag = 'C';
    	} else if (urls[0].equals("TEMP")) {
    		//myUri = "http://pamola.um.maine.edu/DailySummary/frames/GFS-025deg/WORLD-CED/T2/";
    		//myUri = "http://traveler.um.maine.edu/fcst_frames/GFS-025deg/WORLD-CED/T2/";
			myUri = "https://climatereanalyzer.org/wx_frames/gfs/world-ced/t2/";
		    tag = 'T';
    	} else if (urls[0].equals("TEMP_AN")) {
    		//myUri = "http://pamola.um.maine.edu/DailySummary/frames/GFS-025deg/WORLD-CED/T2_anom/";
		    //myUri = "http://traveler.um.maine.edu/fcst_frames/GFS-025deg/WORLD-CED/T2_anom/";
			myUri = "https://climatereanalyzer.org/wx_frames/gfs/world-ced/t2anom/";
    		tag = 't';
    	}
    	else if (urls[0].equals("WATER")) {
    		//myUri = "http://pamola.um.maine.edu/DailySummary/frames/GFS-025deg/WORLD-CED/PWTR/";
		    //myUri = "http://traveler.um.maine.edu/fcst_frames/GFS-025deg/WORLD-CED/PWTR/";
			myUri = "https://climatereanalyzer.org/wx_frames/gfs/world-ced/pwtr/";
		    tag = 'w';
    	}
    	else if (urls[0].equals("WIND")) {
    		//myUri = "http://pamola.um.maine.edu/DailySummary/frames/GFS-025deg/WORLD-CED/WS10/";
		    //myUri = "http://traveler.um.maine.edu/fcst_frames/GFS-025deg/WORLD-CED/WS10/";
			myUri = "https://climatereanalyzer.org/wx_frames/gfs/world-ced/ws10/";
    		tag = 'v';
    	}
    	else if (urls[0].equals("JET")) {
    		//myUri = "http://pamola.um.maine.edu/DailySummary/frames/GFS-025deg/WORLD-CED/WS250/";
		    //myUri = "http://traveler.um.maine.edu/fcst_frames/GFS-025deg/WORLD-CED/WS250/";
			myUri = "https://climatereanalyzer.org/wx_frames/gfs/world-ced/ws250-snowc-topo/";
    		tag = 'j';
    	}
    	else if (urls[0].equals("SNOW")) {
    		//myUri = "http://pamola.um.maine.edu/DailySummary/frames/GFS-025deg/WORLD-CED/SNOW/";
		    //myUri = "http://traveler.um.maine.edu/fcst_frames/GFS-025deg/WORLD-CED/SNOW/";
			myUri = "https://climatereanalyzer.org/wx_frames/gfs/world-ced/snowd-mslp/";
    		tag = 's';
    	}
    	
    	OpenGLES20Renderer.mTag = tag;
    	
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
		
		File dir = OpenGLES20Renderer.mContext.getFilesDir();
		File[] subFiles;
		long epoch;
		long current_real = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")).getTimeInMillis();
		long data_generated_epoch;
		
		HashMap<Integer, String>  iKeys = new HashMap<Integer, String>();
		HashMap<Integer, Long>  eKeys = new HashMap<Integer, Long>();
		HashMap<Integer, Long> mKeys = new HashMap<Integer, Long>();
		
		
		int files_to_download = (48)/3;

		progressDialogSetMax(files_to_download);
		
		
		// Download the older files if possible
		epoch = cal.getTimeInMillis();
		
		// assume that if is less than 9:00 am UTC, the images are old from previous day
		long reload = 0L;
		
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
		if (c.get(Calendar.HOUR_OF_DAY) < 9) {
			epoch -= 24*3600*1000;			
			
			c.set(Calendar.HOUR_OF_DAY, 9);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			reload = c.getTimeInMillis();
			reload -= 24*3600*1000;	
			
		} else {
			c.set(Calendar.HOUR_OF_DAY, 9);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			reload = c.getTimeInMillis();
			
		}
		
		// Download the older files if possible
		epoch = cal.getTimeInMillis();
		
		// if is less than 8:00 am UTC, assume the images are old from previous day
		c = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
		data_generated_epoch = current_real;
		if (c.get(Calendar.HOUR_OF_DAY) < 8) {
			epoch -= 24*3600*1000;
			data_generated_epoch -= 24*3600*1000;
		}
				
		
		for (int h = 0; h < files_to_download; h+=1) {
			
			if (isCancelled() == true) {
				break;
			}
			
			
			boolean exists = false;
			
			// each 3h
			epoch = epoch + 3*3600*1000;
			

			Log.d("H21lab", "h = " + h);

			
			// do not download too new data
			if (epoch - current_real > (48)*3600*1000) {

				Log.d("H21lab", "Data fom eKeys too new h = " + h);
				
				continue;
			} 
			// do not download old data
			if (epoch - current_real < -3*3600*1000) {

				Log.d("H21lab", "Data fom eKeys old h = " + h);	
				
				files_to_download++;
				continue;
			}
			filename = OpenGLES20Renderer.getNameFromEpoch(tag, epoch);
			exists = false;
			subFiles = dir.listFiles();
			if (subFiles != null) {
			    for (File file : subFiles) {
			    	// file exist and is not newer file
			    	
			    	if ( filename.equals(file.getName()) && (file.lastModified() - reload > 0) ) {
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

				url = new URL(myUri
						+ getDirectoryNameFromEpoch(data_generated_epoch) + "/"
						+ String.format("%02d", h) + ".png");

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
				            OpenGLES20Renderer.mContext.deleteFile(file.getName());
				        }
				    }
				}

				//save texture
				OpenGLES20Renderer.saveTexture(filename, ba, 1024, 1024);

				mis2.close();

			} catch (MalformedURLException e1) {
				Log.e("H21lab", "Connection error " + "MalformedURLException "+ e1.getMessage());
			} catch (Exception e2) {
				Log.e("H21lab", "Connection error " + "Unable to connect to " + ucon.getURL().toString() + " " + e2.getMessage());
			}

			progressDialogUpdate();
			
		}
		
		
        return "";
    }

	// 2018-08-07-00z
	public static String getDirectoryNameFromEpoch(long epoch) {

		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
		c.setTimeInMillis(epoch);

		String year = String.format("%04d", (int)(c.get(Calendar.YEAR)));
		String month = String.format("%02d", (int)(c.get(Calendar.MONTH) + 1));
		String day = String.format("%02d", (int)(c.get(Calendar.DAY_OF_MONTH)));

		String filename = Integer.toString(c.get(Calendar.YEAR)) + "-" + month + "-" + day + "-00z";

		return filename;

	}

}