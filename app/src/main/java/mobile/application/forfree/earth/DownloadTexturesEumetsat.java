/*
 * DownloadTexturesEumetsat class
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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.util.EntityUtils;

import android.graphics.Bitmap;
import android.util.Log;

public class DownloadTexturesEumetsat extends DownloadTextures 
{           


    @Override
    protected String doInBackground(String... urls) 
    {
    	OpenGLES20Renderer.downloadedTextures = 0;
    	OpenGLES20Renderer.reloadedTextures = true;
    	int tWidth = 1024;
    	int tHeight = 1024;
    	int hBack = 24;
    	
    	String myUri = "https://eumetview.eumetsat.int/static-images/MSG/RGB/AIRMASS/FULLDISC";
    	char tag = 'M';
    	
    	if (urls[0].equals("AIRMASS")) {
    		myUri = "https://eumetview.eumetsat.int/static-images/MSG/RGB/AIRMASS/FULLDISC";
    		tag = 'M';
    	} else if (urls[0].equals("AIRMASS_HD")) {
    		myUri = "https://eumetview.eumetsat.int/static-images/MSG/RGB/AIRMASS/FULLRESOLUTION";
    		tag = 'm';
    		tWidth = 4096;
    		tHeight = 4096;
    		hBack = 3;
    	} else if (urls[0].equals("NATURALCOLOR")) {
    		myUri = "https://eumetview.eumetsat.int/static-images/MSG/RGB/NATURALCOLOR/FULLDISC";
    		tag = 'N';
    	} else if (urls[0].equals("IR108_BW")) {
    		myUri = "https://eumetview.eumetsat.int/static-images/MSG/IMAGERY/IR108/BW/FULLDISC";
    		tag = 'B';
    	} else if (urls[0].equals("VIS006_BW")) {
    		myUri = "https://eumetview.eumetsat.int/static-images/MSG/IMAGERY/VIS006/BW/FULLDISC";
    		tag = 'C';
    	} else if (urls[0].equals("WV062_BW")) {
    		myUri = "https://eumetview.eumetsat.int/static-images/MSG/IMAGERY/WV062/BW/FULLDISC";
    		tag = 'D';
    	}  else if (urls[0].equals("MPE")) {
    		myUri = "https://eumetview.eumetsat.int/static-images/MSG/PRODUCTS/H03B/FULLDISC";
    		tag = 'E';
    	}/* else if (urls[0].equals("MPE_HD")) {
    		myUri = "https://eumetview.eumetsat.int/static-images/MSG/PRODUCTS/MPE/FULLRESOLUTION";
    		tag = 'e';
    		tWidth = 4096;
    		tHeight = 4096;
    		hBack = -1;
    	}*/ else if (urls[0].equals("IODC")) {
    		//myUri = "http://oiswww.eumetsat.org/IPPS/html/MTP/PRODUCTS/MPE/FULLDISC";
		    myUri = "https://eumetview.eumetsat.int/static-images/MSGIODC/IMAGERY/IR108/BW/FULLDISC/";
    		tag = 'F';
    	}
    	
    	OpenGLES20Renderer.mTag = tag;

        BufferedInputStream is2 = null;
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
				
				//HttpClient httpClient = new DefaultHttpClient();
				//HttpGet get = new HttpGet(myUri + "/index.htm");
		
				//HttpResponse response = httpClient.execute(get);

				URL urlObj = new URL(myUri + "/index.htm");
				HttpURLConnection urlConnection = (HttpURLConnection) urlObj.openConnection();

				Log.d("H21lab", "HTTP GET OK");
				
				// Build up result
				//String bodyHtml = EntityUtils.toString(response.getEntity());
				//InputStream is = urlConnection.getInputStream();
				//String bodyHtml = is.toString();
				
				//BufferedReader bufReader = new BufferedReader(new StringReader(bodyHtml));
				BufferedReader bufReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

				// array_nom_imagen[0]="PTwhQddyHWGUL"
				Pattern pattern = Pattern.compile("array_nom_imagen\\[(\\d+)\\]\\s*=\\s*\\\"(\\S+)\\\"");

				//  <option value="0">13/01/15   11:00 UTC</option>
				Pattern pattern2 = Pattern.compile("\\<option value=\\\"(\\d+)\\\"\\>(.*)\\<\\/option\\>");
				
				String line;
				while( (line = bufReader.readLine()) != null )
				{
					Matcher matcher = pattern.matcher(line);
					while (matcher.find()) {
						Log.d("H21lab", "iKeys: " + "array_nom_imagen[" + matcher.group(1) + "] = " + matcher.group(2));
						iKeys.put(Integer.parseInt(matcher.group(1)), matcher.group(2));	
					}

					matcher = pattern2.matcher(line);
					while (matcher.find()) {
						Log.d("H21lab", "eKeys: " + matcher.group(1) + " " + matcher.group(2));

						String str = matcher.group(2);
						str = str.trim().replaceAll("\\t+", " ");
						str = str.trim().replaceAll("\\s+", " ");
						SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm zzz");
						java.util.Date d = df.parse(str);
						long e = d.getTime();

						if (current - e <= (hBack + 3)*3600*1000) {
							files_to_download++;
						}

						eKeys.put(Integer.parseInt(matcher.group(1)), e);
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
				progressDialogUpdate();
					
				Log.d("H21lab", "Does not conain eKeys h = " + h);	
				
				continue;
			} 
			
			epoch = eKeys.get(h);

			
			// do not download too old data
			if (current - epoch > (hBack + 3)*3600*1000) {
				progressDialogUpdate();
				
				Log.d("H21lab", "Data from eKeys too old h = " + h);	
				
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
				Log.e("H21lab", "!!!!!!!!!!!!!!!!!!!!");
				
				//oiswww.eumetsat.org/IPPS/html/MSG/IMAGERY/IR108/BW/FULLDISC/IMAGESDisplay/
				url = new URL(myUri + "/IMAGESDisplay/" + iKeys.get(h));
				
				Log.d("H21lab", "Downloading: " + url.toString());
					
				ucon = url.openConnection();
				ucon.setUseCaches(false);
				ucon.connect();

				//is2 = ucon.getInputStream();
				is2 = new BufferedInputStream(ucon.getInputStream());

				ByteArrayOutputStream mis2 = new ByteArrayOutputStream();
				byte data[] = new byte[1024];
				int count;
				while ((count = is2.read(data, 0, 1024)) != -1) {
					Log.d("H21lab", Integer.toString(data.toString().length()));
					mis2.write(data, 0, count);
				}
				mis2.flush();
				is2.close();
				
				byte[] ba = mis2.toByteArray();

				OpenGLES20Renderer.saveTexture(filename, ba, tWidth, tHeight);
				
				mis2.close();
			
				
				
			} catch (Exception e) {
				
				Log.e("H21lab", "Unable to connect to " + ucon.getURL().toString() + " " + e.getMessage());		

			}
			
			
			
			progressDialogUpdate();
				
		}

        return "";
    }

}