/*
 * EarthActivity class
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

import java.util.Calendar;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class EarthActivity extends Activity {

	private final int ID_MENU_TIME_RESET = 1;
	private final int ID_MENU_LIGHT = 2;
	private final int ID_MENU_LIGHT_SPECULAR = 50;
	private final int ID_MENU_PLAY = 3;
	private final int ID_MENU_COPYRIGHT = 4;
	private final int ID_MENU_IMAGERY_AIRMASS = 5;
	private final int ID_MENU_IMAGERY_INFRARED = 7;
	private final int ID_MENU_IMAGERY_VISIBLE_INFRARED = 8;
	private final int ID_MENU_IMAGERY_WATER_VAPOR = 9;
	private final int ID_MENU_IMAGERY_MPE = 10;
	private final int ID_MENU_IMAGERY_IODC = 11;
	private final int ID_MENU_IMAGERY_XPLANET = 12;
	private final int ID_MENU_IMAGERY_SSEC_IR = 13;
	private final int ID_MENU_IMAGERY_SSEC_WATER = 14;
	private final int ID_MENU_IMAGERY_GOES_EAST = 15;
	private final int ID_MENU_IMAGERY_GOES_WEST = 16;
	private final int ID_MENU_IMAGERY_MTSAT = 17;
	private final int ID_MENU_IMAGERY_CCI_CLOUDS = 18;
	private final int ID_MENU_IMAGERY_CCI_TEMP = 19;
	private final int ID_MENU_IMAGERY_CCI_TEMP_AN = 20;
	private final int ID_MENU_IMAGERY_CCI_WATER = 21;
	private final int ID_MENU_IMAGERY_CCI_WIND = 22;
	private final int ID_MENU_IMAGERY_CCI_JET = 23;
	private final int ID_MENU_IMAGERY_CCI_SNOW = 24;
	private final int ID_MENU_IMAGERY_CCI_OISST_V2 = 25;
	private final int ID_MENU_IMAGERY_CCI_OISST_V2_1Y = 26;
	private final int ID_MENU_IMAGERY_CCI_TEMP_AN_1Y = 27;
	private final int ID_MENU_IMAGERY_CCI_ERSST_V5 = 28;
	private final int ID_MENU_IMAGERY_AIRMASS_HD = 30;
	private final int ID_MENU_IMAGERY_MPE_HD = 31;
	private final int ID_MENU_IMAGERY_NRL_RAINRATE = 41;

	private final int ID_MENU_IMAGERY_NOAA_AURORA_N = 51;
	private final int ID_MENU_IMAGERY_NOAA_AURORA_S = 52;


	private OpenGLES20SurfaceView mGLView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Create a GLSurfaceView instance and set it
		// as the ContentView for this Activity
		mGLView = new OpenGLES20SurfaceView(this);
		setContentView(mGLView);

		mGLView.getOpenGLES20Renderer().mEpoch = Calendar.getInstance().getTimeInMillis();
		mGLView.getOpenGLES20Renderer()._e1 = 0L;
		mGLView.getOpenGLES20Renderer()._e2 = 0L;
		mGLView.getOpenGLES20Renderer()._e3 = 0L;
		mGLView.getOpenGLES20Renderer()._e4 = 0L;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, ID_MENU_PLAY, Menu.NONE, R.string.play);
		menu.add(Menu.NONE, ID_MENU_TIME_RESET, Menu.NONE, R.string.reset);
		menu.add(Menu.NONE, ID_MENU_LIGHT, Menu.NONE, R.string.light);
		menu.add(Menu.NONE, ID_MENU_LIGHT_SPECULAR, Menu.NONE, R.string.light_specular);
		//menu.add(Menu.NONE, ID_MENU_IMAGERY_XPLANET, Menu.NONE, R.string.xplanet);
		menu.add(Menu.NONE, ID_MENU_IMAGERY_CCI_CLOUDS, Menu.NONE, R.string.cci_clouds);
		menu.add(Menu.NONE, ID_MENU_IMAGERY_CCI_TEMP, Menu.NONE, R.string.cci_temp);
		menu.add(Menu.NONE, ID_MENU_IMAGERY_CCI_TEMP_AN, Menu.NONE, R.string.cci_temp_an);
		menu.add(Menu.NONE, ID_MENU_IMAGERY_CCI_WATER, Menu.NONE, R.string.cci_water);
		menu.add(Menu.NONE, ID_MENU_IMAGERY_CCI_WIND, Menu.NONE, R.string.cci_wind);
		menu.add(Menu.NONE, ID_MENU_IMAGERY_CCI_JET, Menu.NONE, R.string.cci_jet);
		//menu.add(Menu.NONE, ID_MENU_IMAGERY_CCI_TEMP_AN_1Y, Menu.NONE, R.string.cci_temp_an_1y);
		menu.add(Menu.NONE, ID_MENU_IMAGERY_CCI_OISST_V2_1Y, Menu.NONE, R.string.cci_oisst_v2_1y);
		//menu.add(Menu.NONE, ID_MENU_IMAGERY_CCI_OISST_V2, Menu.NONE, R.string.cci_oisst_v2);
		//menu.add(Menu.NONE, ID_MENU_IMAGERY_CCI_ERSST_V5, Menu.NONE, R.string.cci_ersst_v5);
		//menu.add(Menu.NONE, ID_MENU_IMAGERY_CCI_SNOW, Menu.NONE, R.string.cci_snow);
		//menu.add(Menu.NONE, ID_MENU_IMAGERY_NRL_RAINRATE, Menu.NONE, R.string.nrl_rainrate);
		menu.add(Menu.NONE, ID_MENU_IMAGERY_AIRMASS, Menu.NONE, R.string.meteosat_0_airmass);
		menu.add(Menu.NONE, ID_MENU_IMAGERY_AIRMASS_HD, Menu.NONE, R.string.meteosat_0_airmass_hd);
		//menu.add(Menu.NONE, ID_MENU_IMAGERY_MPE, Menu.NONE, R.string.meteosat_0_mpe);
		menu.add(Menu.NONE, ID_MENU_IMAGERY_INFRARED, Menu.NONE, R.string.meteosat_0_infrared);
		menu.add(Menu.NONE, ID_MENU_IMAGERY_IODC, Menu.NONE, R.string.meteosat_iodc_mpe);
		//menu.add(Menu.NONE, ID_MENU_IMAGERY_GOES_EAST, Menu.NONE, R.string.goes_east);
		//menu.add(Menu.NONE, ID_MENU_IMAGERY_GOES_WEST, Menu.NONE, R.string.goes_west);
		//menu.add(Menu.NONE, ID_MENU_IMAGERY_MTSAT, Menu.NONE, R.string.mtsat);
		menu.add(Menu.NONE, ID_MENU_IMAGERY_SSEC_IR, Menu.NONE, R.string.ssec_ir);
		menu.add(Menu.NONE, ID_MENU_IMAGERY_SSEC_WATER, Menu.NONE, R.string.ssec_water);
		menu.add(Menu.NONE, ID_MENU_IMAGERY_NOAA_AURORA_N, Menu.NONE, R.string.noaa_aurora_n);
		menu.add(Menu.NONE, ID_MENU_IMAGERY_NOAA_AURORA_S, Menu.NONE, R.string.noaa_aurora_s);
		menu.add(Menu.NONE, ID_MENU_COPYRIGHT, Menu.NONE, "Copyright");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// The following call pauses the rendering thread.
		// If your OpenGL application is memory intensive,
		// you should consider de-allocating objects that
		// consume significant memory here.
		mGLView.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// The following call resumes a paused rendering thread.
		// If you de-allocated graphic objects for onPause()
		// this is a good place to re-allocate them.
		mGLView.onResume();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//check selected menu item
		if (item.getItemId() == ID_MENU_TIME_RESET) {
			mGLView.getOpenGLES20Renderer().mTimeRotate = 0;
			mGLView.getOpenGLES20Renderer().mEpoch = Calendar.getInstance().getTimeInMillis();
			mGLView.getOpenGLES20Renderer()._e1 = 0L;
			mGLView.getOpenGLES20Renderer()._e2 = 0L;
			mGLView.getOpenGLES20Renderer()._e3 = 0L;
			mGLView.getOpenGLES20Renderer()._e4 = 0L;

		} else if (item.getItemId() == ID_MENU_LIGHT) {
			if (mGLView.getOpenGLES20Renderer().mLiveLight) {
				mGLView.getOpenGLES20Renderer().mLiveLight = false;
				mGLView.getOpenGLES20Renderer().DEV.Light[0].Pos = new M3DVECTOR(mGLView.getOpenGLES20Renderer().DEV.Light[1].Pos);
			} else {
				mGLView.getOpenGLES20Renderer().mLiveLight = true;
				mGLView.getOpenGLES20Renderer().DEV.Light[0].Pos = new M3DVECTOR(mGLView.getOpenGLES20Renderer().DEV.Light[2].Pos);
			}

			return true;
		} else if (item.getItemId() == ID_MENU_LIGHT_SPECULAR) {
			if (mGLView.getOpenGLES20Renderer().mLightSpecular) {
				mGLView.getOpenGLES20Renderer().mLightSpecular = false;
				mGLView.getOpenGLES20Renderer().DEV.Light[0].SR = mGLView.getOpenGLES20Renderer().DEV.Light[1].SR = mGLView.getOpenGLES20Renderer().DEV.Light[2].SR = 0.0f;
				mGLView.getOpenGLES20Renderer().DEV.Light[0].SG = mGLView.getOpenGLES20Renderer().DEV.Light[1].SG = mGLView.getOpenGLES20Renderer().DEV.Light[2].SG = 0.0f;
				mGLView.getOpenGLES20Renderer().DEV.Light[0].SB = mGLView.getOpenGLES20Renderer().DEV.Light[1].SB = mGLView.getOpenGLES20Renderer().DEV.Light[2].SB = 0.0f;
			} else {
				mGLView.getOpenGLES20Renderer().mLightSpecular = true;
				mGLView.getOpenGLES20Renderer().DEV.Light[0].SR = mGLView.getOpenGLES20Renderer().DEV.Light[1].SR = mGLView.getOpenGLES20Renderer().DEV.Light[2].SR = 1.0f;
				mGLView.getOpenGLES20Renderer().DEV.Light[0].SG = mGLView.getOpenGLES20Renderer().DEV.Light[1].SG = mGLView.getOpenGLES20Renderer().DEV.Light[2].SG = 0.7f;
				mGLView.getOpenGLES20Renderer().DEV.Light[0].SB = mGLView.getOpenGLES20Renderer().DEV.Light[1].SB = mGLView.getOpenGLES20Renderer().DEV.Light[2].SB = 0.4f;
			}

			return true;
		} else if (item.getItemId() == ID_MENU_PLAY) {

			mGLView.getOpenGLES20Renderer()._e1 = 0L;
			mGLView.getOpenGLES20Renderer()._e2 = 0L;
			mGLView.getOpenGLES20Renderer()._e3 = 0L;
			mGLView.getOpenGLES20Renderer()._e4 = 0L;

			mGLView.getOpenGLES20Renderer().DEV.Light[0].Pos = new M3DVECTOR(mGLView.getOpenGLES20Renderer().DEV.Light[2].Pos);
			if (mGLView.getOpenGLES20Renderer().mPlay) {
				mGLView.getOpenGLES20Renderer().mPlay = false;
			} else if (!mGLView.getOpenGLES20Renderer().mPlay) {
				mGLView.getOpenGLES20Renderer().mPlay = true;

			}

			return true;
		} else if (item.getItemId() == ID_MENU_COPYRIGHT) {
			AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
			dlgAlert.setMessage(""
					+ "CCI DATA: Data obtained using Climate Reanalyzer (http://cci-reanalyzer.org), Climate Change Institute, University of Maine, USA.\nhttp://pamola.um.maine.edu/DailySummary/frames/GFS-025deg/WORLD-CED/\n\n"
					//+ "NRL DATA: Data obtained using United States Naval Research Laboratory, Marine Meteorology Division (http://www.nrlmry.navy.mil)\n\n"
					+ "METEOSAT DATA: All METEOSAT images shown in the application are subject to EUMETSAT copyright. " + "Copyright EUMETSAT " + cal.get(Calendar.YEAR) + "\nhttp://oiswww.eumetsat.org/IPPS/html/MSG/\n\n"
					//+ "GOES DATA: Credit to NOAA-NASA GOES Project\nhttp://goes.gsfc.nasa.gov/goeseast/fulldisk/\n\n"
					//+ "MTSAT DATA: Credit to Japan Meteorological Agency\nhttp://www.jma.go.jp/en/gms/\n\n"
					+ "SSEC DATA: Provided courtesy of University of Wisconsin-Madison Space Science and Engineering Center\nhttp://www.ssec.wisc.edu/data/comp/\n\n"
					//+ "XPLANET DATA: Many thanks to Hari Nair author of Xplanet\nhttp://xplanet.sourceforge.net\n\n"
					+ "NOAA DATA: Data are obtained from NOAA SPACE WEATHER PREDICTION CENTER \nhttps://www.swpc.noaa.gov/\n\n"
					+ "Developed by Martin Kacer\n");
			dlgAlert.setTitle("Copyright");
			dlgAlert.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							//dismiss the dialog
						}
					});
			dlgAlert.setCancelable(true);
			dlgAlert.create().show();

		} else if (item.getItemId() == ID_MENU_IMAGERY_XPLANET) {
			if (mGLView.getOpenGLES20Renderer().mDownloadTextures.getStatus() == AsyncTask.Status.FINISHED) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures = new DownloadTexturesXplanet(mGLView.getOpenGLES20Renderer());
				mGLView.getOpenGLES20Renderer().mDownloadTextures.execute();
			} else if (mGLView.getOpenGLES20Renderer().mDownloadTextures.isCancelled() != true) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures.progressDialogShow();
			}
		} else if (item.getItemId() == ID_MENU_IMAGERY_NRL_RAINRATE) {
			if (mGLView.getOpenGLES20Renderer().mDownloadTextures.getStatus() == AsyncTask.Status.FINISHED) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures = new DownloadTexturesNRL(mGLView.getOpenGLES20Renderer());
				mGLView.getOpenGLES20Renderer().mDownloadTextures.execute("RAINRATE");
			} else if (mGLView.getOpenGLES20Renderer().mDownloadTextures.isCancelled() != true) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures.progressDialogShow();
			}
		} else if (item.getItemId() == ID_MENU_IMAGERY_AIRMASS) {
			if (mGLView.getOpenGLES20Renderer().mDownloadTextures.getStatus() == AsyncTask.Status.FINISHED) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures = new DownloadTexturesEumetsat(mGLView.getOpenGLES20Renderer());
				mGLView.getOpenGLES20Renderer().mDownloadTextures.execute("AIRMASS");
			} else if (mGLView.getOpenGLES20Renderer().mDownloadTextures.isCancelled() != true) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures.progressDialogShow();
			}
		} else if (item.getItemId() == ID_MENU_IMAGERY_AIRMASS_HD) {
			if (mGLView.getOpenGLES20Renderer().mDownloadTextures.getStatus() == AsyncTask.Status.FINISHED) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures = new DownloadTexturesEumetsat(mGLView.getOpenGLES20Renderer());
				mGLView.getOpenGLES20Renderer().mDownloadTextures.execute("AIRMASS_HD");
			} else if (mGLView.getOpenGLES20Renderer().mDownloadTextures.isCancelled() != true) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures.progressDialogShow();
			}
		} else if (item.getItemId() == ID_MENU_IMAGERY_MPE_HD) {
			if (mGLView.getOpenGLES20Renderer().mDownloadTextures.getStatus() == AsyncTask.Status.FINISHED) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures = new DownloadTexturesEumetsat(mGLView.getOpenGLES20Renderer());
				mGLView.getOpenGLES20Renderer().mDownloadTextures.execute("MPE_HD");
			} else if (mGLView.getOpenGLES20Renderer().mDownloadTextures.isCancelled() != true) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures.progressDialogShow();
			}
		} else if (item.getItemId() == ID_MENU_IMAGERY_CCI_CLOUDS) {
			if (mGLView.getOpenGLES20Renderer().mDownloadTextures.getStatus() == AsyncTask.Status.FINISHED) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures = new DownloadTexturesCCI(mGLView.getOpenGLES20Renderer());
				mGLView.getOpenGLES20Renderer().mDownloadTextures.execute("CLOUDS");
			} else if (mGLView.getOpenGLES20Renderer().mDownloadTextures.isCancelled() != true) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures.progressDialogShow();
			}
		} else if (item.getItemId() == ID_MENU_IMAGERY_CCI_TEMP) {
			if (mGLView.getOpenGLES20Renderer().mDownloadTextures.getStatus() == AsyncTask.Status.FINISHED) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures = new DownloadTexturesCCI(mGLView.getOpenGLES20Renderer());
				mGLView.getOpenGLES20Renderer().mDownloadTextures.execute("TEMP");
			} else if (mGLView.getOpenGLES20Renderer().mDownloadTextures.isCancelled() != true) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures.progressDialogShow();
			}
		} else if (item.getItemId() == ID_MENU_IMAGERY_CCI_TEMP_AN) {
			if (mGLView.getOpenGLES20Renderer().mDownloadTextures.getStatus() == AsyncTask.Status.FINISHED) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures = new DownloadTexturesCCI(mGLView.getOpenGLES20Renderer());
				mGLView.getOpenGLES20Renderer().mDownloadTextures.execute("TEMP_AN");
			} else if (mGLView.getOpenGLES20Renderer().mDownloadTextures.isCancelled() != true) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures.progressDialogShow();
			}
		} else if (item.getItemId() == ID_MENU_IMAGERY_CCI_WATER) {
			if (mGLView.getOpenGLES20Renderer().mDownloadTextures.getStatus() == AsyncTask.Status.FINISHED) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures = new DownloadTexturesCCI(mGLView.getOpenGLES20Renderer());
				mGLView.getOpenGLES20Renderer().mDownloadTextures.execute("WATER");
			} else if (mGLView.getOpenGLES20Renderer().mDownloadTextures.isCancelled() != true) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures.progressDialogShow();
			}
		} else if (item.getItemId() == ID_MENU_IMAGERY_CCI_WIND) {
			if (mGLView.getOpenGLES20Renderer().mDownloadTextures.getStatus() == AsyncTask.Status.FINISHED) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures = new DownloadTexturesCCI(mGLView.getOpenGLES20Renderer());
				mGLView.getOpenGLES20Renderer().mDownloadTextures.execute("WIND");
			} else if (mGLView.getOpenGLES20Renderer().mDownloadTextures.isCancelled() != true) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures.progressDialogShow();
			}
		} else if (item.getItemId() == ID_MENU_IMAGERY_CCI_JET) {
			if (mGLView.getOpenGLES20Renderer().mDownloadTextures.getStatus() == AsyncTask.Status.FINISHED) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures = new DownloadTexturesCCI(mGLView.getOpenGLES20Renderer());
				mGLView.getOpenGLES20Renderer().mDownloadTextures.execute("JET");
			} else if (mGLView.getOpenGLES20Renderer().mDownloadTextures.isCancelled() != true) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures.progressDialogShow();
			}
		} else if (item.getItemId() == ID_MENU_IMAGERY_CCI_SNOW) {
			if (mGLView.getOpenGLES20Renderer().mDownloadTextures.getStatus() == AsyncTask.Status.FINISHED) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures = new DownloadTexturesCCI(mGLView.getOpenGLES20Renderer());
				mGLView.getOpenGLES20Renderer().mDownloadTextures.execute("SNOW");
			} else if (mGLView.getOpenGLES20Renderer().mDownloadTextures.isCancelled() != true) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures.progressDialogShow();
			}
		} else if (item.getItemId() == ID_MENU_IMAGERY_CCI_TEMP_AN_1Y) {
			if (mGLView.getOpenGLES20Renderer().mDownloadTextures.getStatus() == AsyncTask.Status.FINISHED) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures = new DownloadTexturesCCI(mGLView.getOpenGLES20Renderer());
				mGLView.getOpenGLES20Renderer().mDownloadTextures.execute("TEMP_AN_1Y");
			} else if (mGLView.getOpenGLES20Renderer().mDownloadTextures.isCancelled() != true) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures.progressDialogShow();
			}
		} else if (item.getItemId() == ID_MENU_IMAGERY_CCI_OISST_V2_1Y) {
			if (mGLView.getOpenGLES20Renderer().mDownloadTextures.getStatus() == AsyncTask.Status.FINISHED) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures = new DownloadTexturesCCI(mGLView.getOpenGLES20Renderer());
				mGLView.getOpenGLES20Renderer().mDownloadTextures.execute("OISST_V2_1Y");
			} else if (mGLView.getOpenGLES20Renderer().mDownloadTextures.isCancelled() != true) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures.progressDialogShow();
			}
		} else if (item.getItemId() == ID_MENU_IMAGERY_CCI_OISST_V2) {
			if (mGLView.getOpenGLES20Renderer().mDownloadTextures.getStatus() == AsyncTask.Status.FINISHED) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures = new DownloadTexturesCCI(mGLView.getOpenGLES20Renderer());
				mGLView.getOpenGLES20Renderer().mDownloadTextures.execute("OISST_V2");
			} else if (mGLView.getOpenGLES20Renderer().mDownloadTextures.isCancelled() != true) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures.progressDialogShow();
			}
		} else if (item.getItemId() == ID_MENU_IMAGERY_CCI_ERSST_V5) {
			if (mGLView.getOpenGLES20Renderer().mDownloadTextures.getStatus() == AsyncTask.Status.FINISHED) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures = new DownloadTexturesCCI(mGLView.getOpenGLES20Renderer());
				mGLView.getOpenGLES20Renderer().mDownloadTextures.execute("ERSST_V5");
			} else if (mGLView.getOpenGLES20Renderer().mDownloadTextures.isCancelled() != true) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures.progressDialogShow();
			}
		} else if (item.getItemId() == ID_MENU_IMAGERY_INFRARED) {
			if (mGLView.getOpenGLES20Renderer().mDownloadTextures.getStatus() == AsyncTask.Status.FINISHED) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures = new DownloadTexturesEumetsat(mGLView.getOpenGLES20Renderer());
				mGLView.getOpenGLES20Renderer().mDownloadTextures.execute("IR108_BW");
			} else if (mGLView.getOpenGLES20Renderer().mDownloadTextures.isCancelled() != true) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures.progressDialogShow();
			}
		} else if (item.getItemId() == ID_MENU_IMAGERY_VISIBLE_INFRARED) {
			if (mGLView.getOpenGLES20Renderer().mDownloadTextures.getStatus() == AsyncTask.Status.FINISHED) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures = new DownloadTexturesEumetsat(mGLView.getOpenGLES20Renderer());
				mGLView.getOpenGLES20Renderer().mDownloadTextures.execute("VIS006_BW");
			} else if (mGLView.getOpenGLES20Renderer().mDownloadTextures.isCancelled() != true) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures.progressDialogShow();
			}
		} else if (item.getItemId() == ID_MENU_IMAGERY_WATER_VAPOR) {
			if (mGLView.getOpenGLES20Renderer().mDownloadTextures.getStatus() == AsyncTask.Status.FINISHED) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures = new DownloadTexturesEumetsat(mGLView.getOpenGLES20Renderer());
				mGLView.getOpenGLES20Renderer().mDownloadTextures.execute("WV062_BW");
			} else if (mGLView.getOpenGLES20Renderer().mDownloadTextures.isCancelled() != true) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures.progressDialogShow();
			}
		} else if (item.getItemId() == ID_MENU_IMAGERY_MPE) {
			if (mGLView.getOpenGLES20Renderer().mDownloadTextures.getStatus() == AsyncTask.Status.FINISHED) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures = new DownloadTexturesEumetsat(mGLView.getOpenGLES20Renderer());
				mGLView.getOpenGLES20Renderer().mDownloadTextures.execute("MPE");
			} else if (mGLView.getOpenGLES20Renderer().mDownloadTextures.isCancelled() != true) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures.progressDialogShow();
			}
		} else if (item.getItemId() == ID_MENU_IMAGERY_IODC) {
			if (mGLView.getOpenGLES20Renderer().mDownloadTextures.getStatus() == AsyncTask.Status.FINISHED) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures = new DownloadTexturesEumetsat(mGLView.getOpenGLES20Renderer());
				mGLView.getOpenGLES20Renderer().mDownloadTextures.execute("IODC");
			} else if (mGLView.getOpenGLES20Renderer().mDownloadTextures.isCancelled() != true) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures.progressDialogShow();
			}
		} else if (item.getItemId() == ID_MENU_IMAGERY_GOES_EAST) {
			if (mGLView.getOpenGLES20Renderer().mDownloadTextures.getStatus() == AsyncTask.Status.FINISHED) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures = new DownloadTexturesGoes(mGLView.getOpenGLES20Renderer());
				mGLView.getOpenGLES20Renderer().mDownloadTextures.execute("GOES_EAST");
			} else if (mGLView.getOpenGLES20Renderer().mDownloadTextures.isCancelled() != true) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures.progressDialogShow();
			}
		} else if (item.getItemId() == ID_MENU_IMAGERY_GOES_WEST) {
			if (mGLView.getOpenGLES20Renderer().mDownloadTextures.getStatus() == AsyncTask.Status.FINISHED) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures = new DownloadTexturesGoes(mGLView.getOpenGLES20Renderer());
				mGLView.getOpenGLES20Renderer().mDownloadTextures.execute("GOES_WEST");
			} else if (mGLView.getOpenGLES20Renderer().mDownloadTextures.isCancelled() != true) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures.progressDialogShow();
			}
		} else if (item.getItemId() == ID_MENU_IMAGERY_MTSAT) {
			if (mGLView.getOpenGLES20Renderer().mDownloadTextures.getStatus() == AsyncTask.Status.FINISHED) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures = new DownloadTexturesMTSAT(mGLView.getOpenGLES20Renderer());
				mGLView.getOpenGLES20Renderer().mDownloadTextures.execute("MTSAT");
			} else if (mGLView.getOpenGLES20Renderer().mDownloadTextures.isCancelled() != true) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures.progressDialogShow();
			}
		} else if (item.getItemId() == ID_MENU_IMAGERY_SSEC_IR) {
			if (mGLView.getOpenGLES20Renderer().mDownloadTextures.getStatus() == AsyncTask.Status.FINISHED) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures = new DownloadTexturesSSEC(mGLView.getOpenGLES20Renderer());
				mGLView.getOpenGLES20Renderer().mDownloadTextures.execute("IR");
			} else if (mGLView.getOpenGLES20Renderer().mDownloadTextures.isCancelled() != true) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures.progressDialogShow();
			}
		} else if (item.getItemId() == ID_MENU_IMAGERY_SSEC_WATER) {
			if (mGLView.getOpenGLES20Renderer().mDownloadTextures.getStatus() == AsyncTask.Status.FINISHED) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures = new DownloadTexturesSSEC(mGLView.getOpenGLES20Renderer());
				mGLView.getOpenGLES20Renderer().mDownloadTextures.execute("WATER");
			} else if (mGLView.getOpenGLES20Renderer().mDownloadTextures.isCancelled() != true) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures.progressDialogShow();
			}
		} else if (item.getItemId() == ID_MENU_IMAGERY_NOAA_AURORA_N) {
			if (mGLView.getOpenGLES20Renderer().mDownloadTextures.getStatus() == AsyncTask.Status.FINISHED) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures = new DownloadTexturesNoaa(mGLView.getOpenGLES20Renderer());
				mGLView.getOpenGLES20Renderer().mDownloadTextures.execute("NOAA_AURORA_NORTH");
			} else if (mGLView.getOpenGLES20Renderer().mDownloadTextures.isCancelled() != true) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures.progressDialogShow();
			}
		}   else if (item.getItemId() == ID_MENU_IMAGERY_NOAA_AURORA_S) {
			if (mGLView.getOpenGLES20Renderer().mDownloadTextures.getStatus() == AsyncTask.Status.FINISHED) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures = new DownloadTexturesNoaa(mGLView.getOpenGLES20Renderer());
				mGLView.getOpenGLES20Renderer().mDownloadTextures.execute("NOAA_AURORA_SOUTH");
			} else if (mGLView.getOpenGLES20Renderer().mDownloadTextures.isCancelled() != true) {
				mGLView.getOpenGLES20Renderer().mDownloadTextures.progressDialogShow();
			}
		}

		return false;
	}
}