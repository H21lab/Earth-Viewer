/*
 * OpenGLErrorDialog class
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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

public class OpenGLErrorDialog extends AsyncTask<String, Void, String> 
{
	
	AlertDialog.Builder dlgAlert;
	String errorMsg = null;
	
    @Override
    protected void onPreExecute() 
    {
	
    	dlgAlert  = new AlertDialog.Builder(OpenGLES20Renderer.mContext);
		
        super.onPreExecute();
        
	}
    
    @Override
    protected String doInBackground(String... urls) 
    {
    	errorMsg = urls[0];
    	return "";
    }

    @Override
    protected void onPostExecute(String response1) 
    {
    	
    	Log.e("H21lab", errorMsg);
    	
    	dlgAlert.setMessage(errorMsg);
		dlgAlert.setTitle("Rendering Device Error");
		dlgAlert.setNegativeButton("Crash & Report",
			    new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) {
			          //dismiss the dialog  
			    		throw new IllegalArgumentException(errorMsg); 
			        }
			    });
		dlgAlert.setPositiveButton("Ignore Error",
			    new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) {
			          //dismiss the dialog  
			    	
			        }
			    });
		dlgAlert.setCancelable(true);
		dlgAlert.create().show();
	
    }
}