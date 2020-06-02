/*
 * DownloadTexturesNRL class
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
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.TimeZone;
import android.graphics.Bitmap;
import android.util.Log;

import static java.net.HttpURLConnection.HTTP_MOVED_PERM;

public class DownloadTexturesNRL extends DownloadTextures 
{

	// follow redirects
	private HttpURLConnection openConnection(String url) throws IOException {

		if (url == null) {
			Log.e("H21lab", "url is null in method HttpURLConnection.");
			return null;
		}

		HttpURLConnection connection;
		boolean redirected;
		do {
			connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setUseCaches(false);

			int code = connection.getResponseCode();
			redirected = code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP || code == HttpURLConnection.HTTP_SEE_OTHER;
			if (redirected) {
				url = connection.getHeaderField("Location");
				connection.disconnect();
			}
		} while (redirected);
		return connection;
	}



	@Override
    protected String doInBackground(String... urls) 
    {
    	OpenGLES20Renderer.downloadedTextures = 0;
    	OpenGLES20Renderer.reloadedTextures = true;

    	String myUri = "https://www.nrlmry.navy.mil/archdat/global/rain/accumulations/geo/3-hour/";
    	char tag = 'R';
    	
    	if (urls[0].equals("RAINRATE")) {
    		myUri = "https://www.nrlmry.navy.mil/archdat/global/rain/accumulations/geo/3-hour/";
        	tag = 'R';
    	}
    	
    	OpenGLES20Renderer.mTag = tag;
    	
    	
    	InputStream is2 = null;
		Bitmap b = null;

	    HttpURLConnection ucon = null;
		URL url = null;
		
		// load image from cache
		
		// find latest image but not older then 3 hours
		String filename = null;
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));

	    cal.set(Calendar.HOUR_OF_DAY, 3*((int)(cal.get(Calendar.HOUR_OF_DAY))/3));
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		File dir = OpenGLES20Renderer.mContext.getFilesDir();
		File[] subFiles;
		long epoch;
		
		epoch = cal.getTimeInMillis();
		progressDialogSetMax(8 - 0 + 1);
		epoch = epoch - 0*3600*1000;
		
		for (int h = 0; h <= 24; h+=3) {
			
			if (isCancelled() == true) {
				break;
			}

			
			boolean exists = false;
			
			epoch = epoch - 3*3600*1000;
			
			filename = OpenGLES20Renderer.getNameFromEpoch(tag, epoch);
			
			Log.d("H21lab", "h = " + h);
			Log.d("H21lab", "epoch = " + epoch);
			Log.d("H21lab", "filename = " + filename);
			
			
			
			subFiles = dir.listFiles();
			if (subFiles != null) {
			    for (File file : subFiles) {
			    	if ( filename.equals(file.getName()) ) {
			    		exists = true;
			    		break;				    		
			    	}
			    }
			}
			
			// do not download already existing
			if (exists) {
				
				progressDialogUpdate();
				
				continue;
			}
		
			// download from internet
			try {
				String _filename = "";
				
				Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
		    	c.setTimeInMillis(epoch);
		    	
		    	String hour = String.format("%02d", (int)(c.get(Calendar.HOUR_OF_DAY)));
		    	String day = String.format("%02d", (int)(c.get(Calendar.DAY_OF_MONTH)));
		    	String month = String.format("%02d", (int)(c.get(Calendar.MONTH) + 1));
		    	_filename = Integer.toString(c.get(Calendar.YEAR)) + month + day + "." + hour + "00.geo.rainsum.global.3.jpg";
				
				
				url = new URL(myUri + _filename);
				Log.d("H21lab", "Downloading: " + url.toString());
					
				//ucon = (HttpURLConnection)url.openConnection();
				//ucon.setUseCaches(false);
				//ucon.connect();

				// follow redirects
				ucon = openConnection(url.toString());
				if(ucon != null) {
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
					OpenGLES20Renderer.saveTexture(filename, ba, 2048, 512);

					mis2.close();
				}
				
			} catch (IOException e) {

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