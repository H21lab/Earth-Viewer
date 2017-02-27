/*
 * DownloadTexturesGoes class
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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.graphics.Bitmap;
import android.util.Log;

public class DownloadTexturesGoes extends DownloadTextures 
{           


    @Override
    protected String doInBackground(String... urls) 
    {
    	OpenGLES20Renderer.downloadedTextures = 0;
    	OpenGLES20Renderer.reloadedTextures = true;
    	
    	String myUri = "https://goes.gsfc.nasa.gov/goeseast/fulldisk/3band_color/";
    	char tag = 'G';
    	
    	if (urls[0].equals("GOES_EAST")) {
    		myUri = "https://goes.gsfc.nasa.gov/goeseast/fulldisk/3band_color/";
    		tag = 'G';
    	} else if (urls[0].equals("GOES_WEST")) {
    		myUri = "https://goes.gsfc.nasa.gov/goeswest/fulldisk/3band_color/";
    		tag = 'H';
    	}
    	
    	OpenGLES20Renderer.mTag = tag;
    	
	    InputStream is2 = null;
		Bitmap b = null;

		URLConnection ucon = null;
		URL url = null;
		
		// load image from cache
		
		// find latest image but not older then 3 hours
		String filename = null;
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));

	    cal.set(Calendar.HOUR_OF_DAY, 1*((int)(cal.get(Calendar.HOUR_OF_DAY)/1)));
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		File dir = OpenGLES20Renderer.mContext.getFilesDir();
		File[] subFiles;
		long epoch;
		long current = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")).getTimeInMillis();
		
		HashMap<Integer, String>  iKeys = new HashMap<Integer, String>();
		HashMap<Integer, Long>  eKeys = new HashMap<Integer, Long>();
		
		int files_to_download = 0;
		
		if (iKeys.size() == 0 || eKeys.size() == 0) {
			try {
				
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet get = new HttpGet(myUri);
		
				HttpResponse response = httpClient.execute(get);
				
				Log.d("H21lab", "HTTP GET OK");
				
				// Build up result
				String bodyHtml = EntityUtils.toString(response.getEntity());
				
				BufferedReader bufReader = new BufferedReader(new StringReader(bodyHtml));
				
				// <tr><td valign="top"><img src="/icons/image2.gif" alt="[IMG]"></td><td><a href="1501150545G13I04.tif">1501150545G13I04.tif</a></td><td align="right">15-Jan-2015 01:21  </td><td align="right">550K</td></tr>
				Pattern pattern = Pattern.compile("href=\\\"(\\d\\S+)\\.jpg\\\"");
				
				String line;
				int i = 0;
				
				while( (line = bufReader.readLine()) != null )
				{
					Matcher matcher = pattern.matcher(line);
					while (matcher.find()) {
						Log.d("H21lab", "iKeys: " + i + " " + matcher.group(1));
						iKeys.put(i, matcher.group(1));
						i++;
					}
				}
				
				
				//  <tr><td valign="top"><img src="/icons/image2.gif" alt="[IMG]"></td><td><a href="1501150545G13I04.tif">1501150545G13I04.tif</a></td><td align="right">15-Jan-2015 01:21  </td><td align="right">550K</td></tr>
				pattern = Pattern.compile("href=\\\"(\\d\\S+)\\.jpg\\\"");
				
				bufReader = new BufferedReader(new StringReader(bodyHtml));
				
				i = 0;
				while( (line = bufReader.readLine()) != null )
				{
					Matcher matcher = pattern.matcher(line);
					while (matcher.find()) {
						Log.d("H21lab", "eKeys: " + matcher.group(1));
						
						String str = matcher.group(1);
						str = str.trim().replaceAll("\\t+", " ");
						str = str.trim().replaceAll("\\s+", " ");
						SimpleDateFormat df = new SimpleDateFormat("yyMMddHHmm");
						java.util.Date d = df.parse(str);
						long e = d.getTime();

						if (current - e <= (24 + 3)*3600*1000) {
							files_to_download++;
						}
						
						Log.d("H21lab", "eKeys: " + i + " " + e);
						eKeys.put(i, e);
						i++;
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
		for (int h = 0; h < eKeys.size(); h+=1) {
			
			if (isCancelled() == true) {
				break;
			}
			
			boolean exists = false;
			
			Log.d("H21lab", "h = " + h);

			if (!eKeys.containsKey(h)) {

				Log.d("H21lab", "Does not conain eKeys h = " + h);
				
				continue;
			} 
			
			epoch = eKeys.get(h);

			
			// do not download too old data
			if (current - epoch > (24 + 3)*3600*1000) {

				Log.d("H21lab", "Data fom eKeys too old h = " + h);
				
				continue;
			}
			filename = OpenGLES20Renderer.getNameFromEpoch(tag, epoch);
			exists = false;
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

				OpenGLES20Renderer.saveTexture(filename, ba, 2048, 2048);
				
				mis2.close();



			} catch (Exception e) {
				
				Log.e("H21lab", "Unable to connect to " + ucon.getURL().toString() + " " + e.getMessage());		
				
			}

			progressDialogUpdate();
			
		}

        return "";
    }

}