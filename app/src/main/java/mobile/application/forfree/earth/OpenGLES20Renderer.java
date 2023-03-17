/*
 * OpenGLES20Renderer class
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.opengl.ETC1;
import android.opengl.ETC1Util;
import android.opengl.ETC1Util.ETC1Texture;
import android.opengl.GLSurfaceView;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

public class OpenGLES20Renderer implements GLSurfaceView.Renderer {
	final static String TAG = "GLES20Renderer";

	long _t1, _t2;                // used to calculate FPS
	float FPS = 30.0f;
	float Tc = 1.0f / FPS;        // duration of 1 frame
	int Nrenderedframe = 0;

	public M3DM DEV;
	M3DM.mD3DFrame scene;
	public M3DM.mD3DFrame fearth;
	public M3DM.mD3DMesh earth;

	public M3DM.mD3DFrame flabel;
	public M3DM.mD3DMesh label;

	Context mContext;

	public M3DMATRIX mRotation;

	public int mBump = 10;
	public boolean mLiveLight = false;
	public boolean mLightSpecular = true;
	public boolean mPlay = false;

	M3DVECTOR At = new M3DVECTOR(0.0f, 0.0f, 0.0f); // accelleration vector
	M3DVECTOR Gt = new M3DVECTOR(0.0f, 0.0f, 0.0f); // vertical gravity vector m/(s*s)

	boolean reloadedTextures = false;
	int downloadedTextures = 0;
	boolean preferencesChanged = false;
	DownloadTextures mDownloadTextures = null;
	boolean initializedShaders = false;
	boolean initialized = false;
	char mTag = 'X';
	int openGLErrorDetected = 0;


	Bitmap mBitmap1 = null, mBitmap2 = null, mBitmap3 = null, mBitmap4 = null;
	ConcurrentHashMap<Long, ETC1Texture> mCloudMap = new ConcurrentHashMap<Long, ETC1Texture>();
	ConcurrentHashMap<String, Long> mCloudMapFilename = new ConcurrentHashMap<String, Long>();
	ConcurrentHashMap<Long, String> mCloudMapEpochToFilename = new ConcurrentHashMap<Long, String>();
	ConcurrentHashMap<Long, Integer> mCloudMapId = new ConcurrentHashMap<Long, Integer>();
	ConcurrentHashMap<Integer, String> mCloudMapIdFilename = new ConcurrentHashMap<Integer, String>();

	public M3DM.mD3DTexture texture = null;
	public M3DM.mD3DTexture texture1 = null;
	public M3DM.mD3DTexture texture2 = null;
	public M3DM.mD3DTexture texture3 = null;
	public M3DM.mD3DTexture texture4 = null;
	public M3DM.mD3DTexture texture5 = null;
	public M3DM.mD3DTexture texture6 = null;

	public long mTimeRotate = 0;
	public long mEpoch = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")).getTimeInMillis();

	public long _e1 = 0L;    // nearest in past
	public long _e2 = 0L;    // second nearest in past
	public long _e3 = 0L;    // nearest in future
	public long _e4 = 0L;    // second nearest in future

	public int texSizeW = 1024;
	public int texSizeH = 1024;

	public int mWidth = 0;
	public int mHeight = 0;


	public OpenGLES20Renderer(Context context) {
		super();
		mContext = context;

		initialize();

	}

	public void onSurfaceCreated(GL10 unused, EGLConfig config) {

		// reset textures
		_e1 = 0L;    // nearest in past
		_e2 = 0L;    // second nearest in past
		_e3 = 0L;    // nearest in future
		_e4 = 0L;    // second nearest in future

		initializedShaders = false;
		reloadedTextures = true;
		_t2 = System.nanoTime();

		if (DEV == null) {
			initialize();
		}

		DEV.initializeGL();

		// textueres has been cleared by OS
		texture = null;
		texture1 = null;
		texture2 = null;
		texture3 = null;
		texture4 = null;
		texture5 = null;
		texture6 = null;
		mCloudMapId.clear();
		mCloudMapIdFilename.clear();


		initializeShaders();

		earth.setProgram(Shaders.p_cci);
		earth.setBumpLevel((float) mBump);
		initializeGLTextures();

		// Set the background frame color
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		Log.d("H21lab", "GL_VERSION = " + GLES20.glGetString(GLES20.GL_VERSION));
		Log.d("H21lab", "GL_EXTENSIONS = " + GLES20.glGetString(GLES20.GL_EXTENSIONS));
		Log.d("H21lab", "GL_RENDERER = " + GLES20.glGetString(GLES20.GL_RENDERER));

	}

	public void setShaders() {
		if (DEV != null) {

			label.setProgram(Shaders.p_label);
			if (mTag == 'X') {
				earth.setProgram(Shaders.p_xplanet);
				label.setFlags(label.getFlags() | M3DM.MD3DMESHF_DISABLED);
			} else if (mTag == 'R') {
				earth.setProgram(Shaders.p_nrl_rainrate);
				label.setFlags(label.getFlags() | M3DM.MD3DMESHF_DISABLED);
			} else if (mTag == 'I') {
				earth.setProgram(Shaders.p_ssec_ir);
				label.setFlags(label.getFlags() | M3DM.MD3DMESHF_DISABLED);
			} else if (mTag == 'W') {
				earth.setProgram(Shaders.p_ssec_water);
				label.setFlags(label.getFlags() | M3DM.MD3DMESHF_DISABLED);
			} else if (mTag == 'F') {
				earth.setProgram(Shaders.p_meteosat_iodc);
				label.setFlags(label.getFlags() | M3DM.MD3DMESHF_DISABLED);
			} else if (mTag == 'G') {
				earth.setProgram(Shaders.p_goes_east);
				label.setFlags(label.getFlags() | M3DM.MD3DMESHF_DISABLED);
			} else if (mTag == 'H') {
				earth.setProgram(Shaders.p_goes_west);
				label.setFlags(label.getFlags() | M3DM.MD3DMESHF_DISABLED);
			} else if (mTag == 'J') {
				earth.setProgram(Shaders.p_mtsat);
				label.setFlags(label.getFlags() | M3DM.MD3DMESHF_DISABLED);
			} else if (mTag == 'C') {
				earth.setProgram(Shaders.p_cci);
				label.setFlags(label.getFlags() & ~M3DM.MD3DMESHF_DISABLED);
				// change label UV
				label.Vertex[1 * label.VertexSize + 7] = 1.0f - 0.090f;
				label.Vertex[2 * label.VertexSize + 7] = 1.0f - 0.090f;
			} else if (mTag == 'T') {
				earth.setProgram(Shaders.p_cci_temp);
				label.setFlags(label.getFlags() & ~M3DM.MD3DMESHF_DISABLED);
				// change label UV
				label.Vertex[1 * label.VertexSize + 7] = 1.0f - 0.076f;
				label.Vertex[2 * label.VertexSize + 7] = 1.0f - 0.076f;
			} else if (mTag == 't') {
				earth.setProgram(Shaders.p_cci_temp_an);
				label.setFlags(label.getFlags() & ~M3DM.MD3DMESHF_DISABLED);
				// change label UV
				label.Vertex[1 * label.VertexSize + 7] = 1.0f - 0.076f;
				label.Vertex[2 * label.VertexSize + 7] = 1.0f - 0.076f;
			} else if (mTag == 'w') {
				earth.setProgram(Shaders.p_cci_water);
				label.setFlags(label.getFlags() & ~M3DM.MD3DMESHF_DISABLED);
				// change label UV
				label.Vertex[1 * label.VertexSize + 7] = 1.0f - 0.076f;
				label.Vertex[2 * label.VertexSize + 7] = 1.0f - 0.076f;
			} else if (mTag == 'v') {
				earth.setProgram(Shaders.p_cci_wind);
				label.setFlags(label.getFlags() & ~M3DM.MD3DMESHF_DISABLED);
				// change label UV
				label.Vertex[1 * label.VertexSize + 7] = 1.0f - 0.076f;
				label.Vertex[2 * label.VertexSize + 7] = 1.0f - 0.076f;
			} else if (mTag == 'j') {
				earth.setProgram(Shaders.p_cci_jet);
				label.setFlags(label.getFlags() & ~M3DM.MD3DMESHF_DISABLED);
				// change label UV
				label.Vertex[1 * label.VertexSize + 7] = 1.0f - 0.076f;
				label.Vertex[2 * label.VertexSize + 7] = 1.0f - 0.076f;
			} else if (mTag == 's') {
				earth.setProgram(Shaders.p_cci_snow);
				label.setFlags(label.getFlags() & ~M3DM.MD3DMESHF_DISABLED);
				// change label UV
				label.Vertex[1 * label.VertexSize + 7] = 1.0f - 0.076f;
				label.Vertex[2 * label.VertexSize + 7] = 1.0f - 0.076f;
			} else if (mTag == 'a') {
				earth.setProgram(Shaders.p_cci_temp_an_1y);
				label.setFlags(label.getFlags() & ~M3DM.MD3DMESHF_DISABLED);
				// change label UV
				label.Vertex[1 * label.VertexSize + 7] = 1.0f - 0.13f;
				label.Vertex[2 * label.VertexSize + 7] = 1.0f - 0.13f;
			} else if (mTag == 'b') {
				earth.setProgram(Shaders.p_cci_oisst_v2);
				label.setFlags(label.getFlags() & ~M3DM.MD3DMESHF_DISABLED);
				// change label UV
				label.Vertex[1 * label.VertexSize + 7] = 1.0f - 0.076f;
				label.Vertex[2 * label.VertexSize + 7] = 1.0f - 0.076f;
			} else if (mTag == 'O') {
				earth.setProgram(Shaders.p_cci_oisst_v2);
				label.setFlags(label.getFlags() & ~M3DM.MD3DMESHF_DISABLED);
				// change label UV
				label.Vertex[1 * label.VertexSize + 7] = 1.0f - 0.076f;
				label.Vertex[2 * label.VertexSize + 7] = 1.0f - 0.076f;
			} else if (mTag == 'e') {
				earth.setProgram(Shaders.p_cci_oisst_v2);
				label.setFlags(label.getFlags() & ~M3DM.MD3DMESHF_DISABLED);
				// change label UV
				label.Vertex[1 * label.VertexSize + 7] = 1.0f - 0.076f;
				label.Vertex[2 * label.VertexSize + 7] = 1.0f - 0.076f;
			} else if (mTag == 'm') {
				earth.setProgram(Shaders.p_meteosat_0_hd);
				label.setFlags(label.getFlags() | M3DM.MD3DMESHF_DISABLED);
			} else {
				earth.setProgram(Shaders.p_meteosat_0);
				label.setFlags(label.getFlags() | M3DM.MD3DMESHF_DISABLED);
			}

			int p = earth.getProgram();
			earth.flushProgram();
			label.flushProgram();
			if (p != -1 && p != earth.getNewProgram()) {
				DEV.DeleteProgram(p);
			}
		}

	}

	public void initializeShaders() {
		if (DEV != null) {

			// initialize Shaders

			Shaders.p_label = DEV.CompileProgram(Shaders.vsc_label, Shaders.fsc_label);

			if (mTag == 'X') {
				Shaders.p_xplanet = DEV.CompileProgram(Shaders.vsc_xplanet, Shaders.fsc_xplanet);
			} else if (mTag == 'R') {
				Shaders.p_nrl_rainrate = DEV.CompileProgram(Shaders.vsc_nrl_rainrate, Shaders.fsc_nrl_rainrate);
			} else if (mTag == 'I') {
				Shaders.p_ssec_ir = DEV.CompileProgram(Shaders.vsc_ssec_ir, Shaders.fsc_ssec_ir);
			} else if (mTag == 'W') {
				Shaders.p_ssec_water = DEV.CompileProgram(Shaders.vsc_ssec_water, Shaders.fsc_ssec_water);
			} else if (mTag == 'F') {
				Shaders.p_meteosat_iodc = DEV.CompileProgram(Shaders.vsc_meteosat_iodc, Shaders.fsc_meteosat_iodc);
			} else if (mTag == 'G') {
				Shaders.p_goes_east = DEV.CompileProgram(Shaders.vsc_goes_east, Shaders.fsc_goes_east);
			} else if (mTag == 'H') {
				Shaders.p_goes_west = DEV.CompileProgram(Shaders.vsc_goes_west, Shaders.fsc_goes_east);
			} else if (mTag == 'J') {
				Shaders.p_mtsat = DEV.CompileProgram(Shaders.vsc_mtsat, Shaders.fsc_mtsat);
			} else if (mTag == 'C') {
				Shaders.p_cci = DEV.CompileProgram(Shaders.vsc_cci, Shaders.fsc_cci);
			} else if (mTag == 'T') {
				Shaders.p_cci_temp = DEV.CompileProgram(Shaders.vsc_cci_temp, Shaders.fsc_cci_temp);
			} else if (mTag == 't') {
				Shaders.p_cci_temp_an = DEV.CompileProgram(Shaders.vsc_cci_temp_an, Shaders.fsc_cci_temp_an);
			} else if (mTag == 'w') {
				Shaders.p_cci_water = DEV.CompileProgram(Shaders.vsc_cci_water, Shaders.fsc_cci_temp);
			} else if (mTag == 'v') {
				Shaders.p_cci_wind = DEV.CompileProgram(Shaders.vsc_cci_wind, Shaders.fsc_cci_temp);
			} else if (mTag == 'j') {
				Shaders.p_cci_jet = DEV.CompileProgram(Shaders.vsc_cci_jet, Shaders.fsc_cci_temp);
			} else if (mTag == 's') {
				Shaders.p_cci_snow = DEV.CompileProgram(Shaders.vsc_cci_wind, Shaders.fsc_cci_temp);
			} else if (mTag == 'a') {
				Shaders.p_cci_temp_an_1y = DEV.CompileProgram(Shaders.vsc_cci_temp_an_1y, Shaders.fsc_cci_temp_an_1y);
			} else if (mTag == 'b') {
				Shaders.p_cci_oisst_v2 = DEV.CompileProgram(Shaders.vsc_cci_oisst_v2, Shaders.fsc_cci_oisst_v2);
			} else if (mTag == 'O') {
				Shaders.p_cci_oisst_v2 = DEV.CompileProgram(Shaders.vsc_cci_oisst_v2, Shaders.fsc_cci_oisst_v2);
			} else if (mTag == 'e') {
				Shaders.p_cci_oisst_v2 = DEV.CompileProgram(Shaders.vsc_cci_oisst_v2, Shaders.fsc_cci_oisst_v2);
			} else if (mTag == 'm') {
				Shaders.p_meteosat_0_hd = DEV.CompileProgram(Shaders.vsc_meteosat_0_hd, Shaders.fsc_meteosat_0_hd);
			} else {
				Shaders.p_meteosat_0 = DEV.CompileProgram(Shaders.vsc_meteosat_0, Shaders.fsc_meteosat_0);
			}

			GLES20.glFlush();

			setShaders();

			initializedShaders = true;
		}
	}

	public void initializeGLTextures() {

		if (downloadedTextures == 0) {
			return;
		}

		// Texture
		if (texture1 != null) {
			int[] textures1 = new int[1];
			textures1[0] = texture1.id;
			GLES20.glDeleteTextures(1, textures1, 0);
			GLES20.glFlush();
		}

		int[] textures1 = new int[1];
		GLES20.glGenTextures(1, textures1, 0);

		texture1 = new M3DM.mD3DTexture();
		texture1.id = textures1[0];

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture1.id);

		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap2, 0);
		//


		// Texture
		if (texture2 != null) {
			int[] textures2 = new int[1];
			textures2[0] = texture2.id;
			GLES20.glDeleteTextures(1, textures2, 0);
			GLES20.glFlush();
		}
		int[] textures2 = new int[1];
		GLES20.glGenTextures(1, textures2, 0);

		texture2 = new M3DM.mD3DTexture();
		texture2.id = textures2[0];

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture2.id);

		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap3, 0);
		//


		// Texture
		if (texture3 != null) {
			int[] textures3 = new int[1];
			textures3[0] = texture3.id;
			GLES20.glDeleteTextures(1, textures3, 0);
			GLES20.glFlush();
		}
		int[] textures3 = new int[1];
		GLES20.glGenTextures(1, textures3, 0);

		texture3 = new M3DM.mD3DTexture();
		texture3.id = textures3[0];

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture3.id);

		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap4, 0);

		// Texture
		if (texture != null) {
			int[] textures = new int[1];
			textures[0] = texture.id;
			GLES20.glDeleteTextures(1, textures, 0);
			GLES20.glFlush();
		}

		int[] textures = new int[1];
		GLES20.glGenTextures(1, textures, 0);

		texture = new M3DM.mD3DTexture();
		texture.id = textures[0];

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture.id);

		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);


		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap1, 0);
		//


		earth.Textures = 7;
		earth.setTexture(0, texture);
		earth.setTexture(1, texture1);
		earth.setTexture(2, texture2);
		earth.setTexture(3, texture3);
		earth.setTexture(4, texture2);
		earth.setTexture(5, texture2);
		earth.setTexture(6, texture2);

		label.Textures = 1;
		label.setTexture(0, texture1);


		// delete textures
		for (Long e : mCloudMapId.keySet()) {

			if (mCloudMapIdFilename.containsKey(mCloudMapId.get(e))) {

				if (OpenGLES20Renderer.getTag(mCloudMapIdFilename.get(mCloudMapId.get(e))) != this.mTag) {

					int[] t = new int[1];
					t[0] = mCloudMapId.get(e);
					GLES20.glDeleteTextures(1, t, 0);
					mCloudMapId.remove(e);
					mCloudMapIdFilename.remove(e);
				}
			}
		}
		GLES20.glFlush();


		// load textures
		for (Long e : mCloudMap.keySet()) {
			if (mCloudMapId.contains(e)) {
				continue;
			}


			int[] id = new int[1];
			GLES20.glGenTextures(1, id, 0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id[0]);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

			ETC1Texture etc1tex = mCloudMap.get(e);
			if (etc1tex != null) {
				GLES20.glCompressedTexImage2D(GLES20.GL_TEXTURE_2D, 0, ETC1.ETC1_RGB8_OES, etc1tex.getWidth(), etc1tex.getHeight(), 0, etc1tex.getData().capacity(), etc1tex.getData());

				Log.d("H21lab", "e = " + e + " " + id[0] + " " + mCloudMap.get(e));
				mCloudMapId.put(e, id[0]);
				if (mCloudMapEpochToFilename.containsKey(e)) {
					mCloudMapIdFilename.put(id[0], mCloudMapEpochToFilename.get(e));
				} else {
					Log.e("H21lab", "No hash key for mCloudMapEpochToFilename e = " + e);
				}

			}

		}

		// find nearest epoch in past
		Long e1 = 0L;
		for (Long e : mCloudMapId.keySet()) {

			if ((e < mEpoch) && ((mEpoch - e) < (mEpoch - e1))) {
				e1 = e;
			}
		}
		if (mCloudMapId.containsKey(e1)) {
			texture = new M3DM.mD3DTexture();
			texture.id = mCloudMapId.get(e1);
			label.setTexture(0, texture);
			earth.setTexture(2, texture);
			earth.setTexture(4, texture);
			earth.setTexture(5, texture);
			earth.setTexture(6, texture);
		} else {
			// find nearest epoch in future
			Long e3 = 0L;
			for (Long e : mCloudMapId.keySet()) {

				if ((e >= mEpoch) && ((e - mEpoch) <= (e3 - mEpoch))) {
					e3 = e;
				}
			}
			if (mCloudMapId.containsKey(e3)) {
				texture = new M3DM.mD3DTexture();
				texture.id = mCloudMapId.get(e3);
				label.setTexture(0, texture);
				earth.setTexture(2, texture);
				earth.setTexture(4, texture);
				earth.setTexture(5, texture);
				earth.setTexture(6, texture);
			}

		}
		reloadedTextures = true;

		downloadedTextures = 2;
	}

	// 2007006M0600
	public static String getNameFromEpoch(char tag, long epoch) {

		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
		c.setTimeInMillis(epoch);

		String hour = String.format("%02d", (int) (c.get(Calendar.HOUR_OF_DAY)));
		String minute = String.format("%02d", (int) (c.get(Calendar.MINUTE)));
		String day = String.format("%03d", (int) (c.get(Calendar.DAY_OF_YEAR)));

		String filename = Integer.toString(c.get(Calendar.YEAR)) + day + tag + hour + minute;

		return filename;

	}

	// 2007006M0600
	public static Long getEpochFromName(String filename) {

		try {
			Calendar c = Calendar.getInstance();
			c.clear();
			int i;

			i = Integer.parseInt(filename.substring(0, 4));

			c.set(Calendar.YEAR, i);

			i = Integer.parseInt(filename.substring(4, 7));

			c.set(Calendar.DAY_OF_YEAR, i);

			i = Integer.parseInt(filename.substring(8, 10));

			c.set(Calendar.HOUR, i);

			i = Integer.parseInt(filename.substring(10, 12));

			c.set(Calendar.MINUTE, i);

			return c.getTimeInMillis();

		} catch (Exception e) {
			return 0L;
		}
	}

	// 2007006M0600
	public static char getTag(String filename) {

		if (filename == null) {
			return ' ';
		}
		if (filename.length() <= 7) {
			return ' ';
		}
		return filename.charAt(7);

	}


	public static Bitmap scaleBitmap(Bitmap bitmap, int newWidth, int newHeight) {
		Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Config.ARGB_8888);

		float ratioX = (float) newWidth / (float) bitmap.getWidth();
		float ratioY = (float) newHeight / (float) bitmap.getHeight();
		float middleX = (float) newWidth / 2.0f;
		float middleY = (float) newHeight / 2.0f;

		Matrix scaleMatrix = new Matrix();
		scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

		Canvas canvas = new Canvas(scaledBitmap);
		canvas.setMatrix(scaleMatrix);
		canvas.drawBitmap(bitmap, middleX - bitmap.getWidth() / 2, middleY - bitmap.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));
		return scaledBitmap;
	}

	public void saveImage(String filename, byte[] data) {


		Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length);
		b = scaleBitmap(b, texSizeW, texSizeH);

		// save file
		FileOutputStream outputStream;

		try {
			outputStream = mContext.openFileOutput(filename, Context.MODE_PRIVATE);
			b.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);

			outputStream.flush();
			outputStream.close();

			Log.d("H21lab", "Saved Image: " + filename);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveTexture(String filename, byte[] data, int w, int h) {


		Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length);
		if (b == null) {
			return;
		}
		b = scaleBitmap(b, w, h);
		b = convert(b, Bitmap.Config.RGB_565);


		int size = b.getRowBytes() * b.getHeight();
		ByteBuffer inputImage = ByteBuffer.allocateDirect(size); // size is good
		inputImage.order(ByteOrder.nativeOrder());
		b.copyPixelsToBuffer(inputImage);
		inputImage.position(0);

		int encodedImageSize = ETC1.getEncodedDataSize(b.getWidth(), b.getHeight());
		ByteBuffer compressedImage = ByteBuffer.allocateDirect(encodedImageSize).order(ByteOrder.nativeOrder());
		ETC1.encodeImage(inputImage, b.getWidth(), b.getHeight(), 2, 2 * b.getWidth(), compressedImage);
		ETC1Texture etc1tex = new ETC1Texture(b.getWidth(), b.getHeight(), compressedImage);


		// save file
		FileOutputStream outputStream;


		try {
			outputStream = mContext.openFileOutput(filename, Context.MODE_PRIVATE);

			ETC1Util.writeTexture(etc1tex, outputStream);

			outputStream.flush();
			outputStream.close();

			Log.d("H21lab", "Saved Texture: " + filename + " size: " + compressedImage.capacity());

		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	private static Bitmap convert(Bitmap bitmap, Bitmap.Config config) {
		Bitmap convertedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), config);
		Canvas canvas = new Canvas(convertedBitmap);
		Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		canvas.drawBitmap(bitmap, 0, 0, paint);
		return convertedBitmap;
	}

	public Bitmap loadImage(String filename) {

		if (filename == null) {
			return null;
		}
		FileInputStream inputStream;
		Bitmap b = null;
		try {
			inputStream = mContext.openFileInput(filename);
			b = BitmapFactory.decodeStream(inputStream);
			inputStream.close();
			Log.d("H21lab", "Loaded Image: " + filename);
		} catch (Exception e0) {
			e0.printStackTrace();
		}

		b = scaleBitmap(b, texSizeW, texSizeH);
		b = convert(b, Bitmap.Config.RGB_565);

		return b;

	}

	static ByteBuffer readToByteBuffer(InputStream inStream) throws IOException {
		byte[] buffer = new byte[1024];
		ByteArrayOutputStream outStream = new ByteArrayOutputStream(1024);
		int read;

		while (true) {
			read = inStream.read(buffer);
			if (read == -1)
				break;
			outStream.write(buffer, 0, read);
		}


		ByteBuffer byteData = ByteBuffer.wrap(outStream.toByteArray());
		return byteData;
	}

	public ETC1Texture loadTexture(String filename) {

		ETC1Texture etc1tex = null;
		if (filename == null) {
			return null;
		}
		FileInputStream inputStream;
		Bitmap b = null;
		try {
			inputStream = mContext.openFileInput(filename);

			etc1tex = ETC1Util.createTexture(inputStream);

			inputStream.close();
		} catch (Exception e0) {
			e0.printStackTrace();
		}

		return etc1tex;
	}


	public void initialize() {
		// -------------------------------------------

		DEV = new M3DM();

		// create light
		DEV.N_Lights = 1;

		// static light
		DEV.Light[1].AR = 0.0f;
		DEV.Light[1].AG = 0.0f;
		DEV.Light[1].AB = 0.0f;
		DEV.Light[1].DR = 1.0f;
		DEV.Light[1].DG = 1.0f;
		DEV.Light[1].DB = 1.0f;
		DEV.Light[1].SR = 1.0f;
		DEV.Light[1].SG = 0.7f;
		DEV.Light[1].SB = 0.4f;
		DEV.Light[1].AT = 0.0f;
		DEV.Light[1].Pos = new M3DVECTOR(1.5f, 0.7f, 3.0f);

		// live light
		DEV.Light[2].AR = 0.0f;
		DEV.Light[2].AG = 0.0f;
		DEV.Light[2].AB = 0.0f;
		DEV.Light[2].DR = 1.0f;
		DEV.Light[2].DG = 1.0f;
		DEV.Light[2].DB = 1.0f;
		DEV.Light[2].SR = 1.0f;
		DEV.Light[2].SG = 0.7f;
		DEV.Light[2].SB = 0.4f;
		DEV.Light[2].AT = 0.0f;
		DEV.Light[2].Pos = new M3DVECTOR(1.5f, 0.7f, 3.0f);


		DEV.Light[0].AR = DEV.Light[1].AR;
		DEV.Light[0].AG = DEV.Light[1].AG;
		DEV.Light[0].AB = DEV.Light[1].AB;
		DEV.Light[0].DR = DEV.Light[1].DR;
		DEV.Light[0].DG = DEV.Light[1].DG;
		DEV.Light[0].DB = DEV.Light[1].DB;
		DEV.Light[0].SR = DEV.Light[1].SR;
		DEV.Light[0].SG = DEV.Light[1].SG;
		DEV.Light[0].SB = DEV.Light[1].SB;
		DEV.Light[0].AT = DEV.Light[1].AT;
		DEV.Light[0].Pos = new M3DVECTOR(DEV.Light[1].Pos);

		// create scene
		scene = new M3DM.mD3DFrame();

		// create object
		earth = M3DM.createEllipsoid(1.0f, 0.996f, 3, 6, 0.0f, 0.0f, 1.0f, 1.0f);
		//earth.generateNormals();
		earth.generateTangentsBitangets();
		earth.Textures = 0;
		M3DM.M3DMATERIAL material = new M3DM.M3DMATERIAL(1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 3.0f, 0.0f, 1.0f, 1.0f);
		earth.setMaterial(material);

		fearth = new M3DM.mD3DFrame(scene);
		fearth.Position = new M3DVECTOR(0.0f, 0.0f, 0.0f);
		fearth.Up = new M3DVECTOR(0.0f, 1.0f, 0.0f);
		fearth.Orientation = new M3DVECTOR(0.0f, 0.0f, -1.0f);
		fearth.addMesh(earth);

		// create label
		M3DM.mD3DMesh mesh;

		M3DM.M3DVERTEX vertex[] = new M3DM.M3DVERTEX[4];
		short data[] = new short[4];

		float a = 0.35f;
		vertex[0] = new M3DM.M3DVERTEX();
		vertex[0].P.x = -a;
		vertex[0].P.y = -a*0.2f;
		vertex[0].P.z = a;
		vertex[0].u = 0.0f;
		vertex[0].v = 1.0f;

		vertex[1] = new M3DM.M3DVERTEX();
		vertex[1].P.x = -a;
		vertex[1].P.y = a*0.2f;
		vertex[1].P.z = a;
		vertex[1].u = 0.0f;
		vertex[1].v = 1.0f - 0.076f;

		vertex[2] = new M3DM.M3DVERTEX();
		vertex[2].P.x = a;
		vertex[2].P.y = a*0.2f;
		vertex[2].P.z = a;
		vertex[2].u = 1.0f;
		vertex[2].v = 1.0f - 0.076f;

		vertex[3] = new M3DM.M3DVERTEX();
		vertex[3].P.x = a;
		vertex[3].P.y = -a*0.2f;
		vertex[3].P.z = a;
		vertex[3].u = 1.0f;
		vertex[3].v = 1.0f;

		data[0] = 1;
		data[1] = 0;
		data[2] = 2;
		data[3] = 3;

		for (int i = 0; i < vertex.length; i++) {
			vertex[i].N = M3DVECTOR.Normalize(vertex[i].P);
		}

		label = new M3DM.mD3DMesh(vertex, data, data.length);
		label.setFlags(M3DM.MD3DMESHF_DISABLED);

		label.Textures = 1;
		label.setMaterial(material);

		flabel = new M3DM.mD3DFrame(scene);
		flabel.Position = new M3DVECTOR(0.0f, -0.5f, 1.0f);
		flabel.Up = new M3DVECTOR(0.0f, 1.0f, 0.0f);
		flabel.Orientation = new M3DVECTOR(0.0f, 0.0f, 1.0f);
		flabel.addMesh(label);
		flabel.setWorldM();
		label.setFlags(M3DM.MD3DMESHF_UI);

		mRotation = M3DMATRIX.IdentityMatrix();
		// -------------------------------------------


		InputStream is = mContext.getResources().openRawResource(R.raw.normalmap);
		try {
			mBitmap1 = BitmapFactory.decodeStream(is);
		} finally {
			try {
				is.close();
			} catch (Exception e) {
				// Ignore.
			}
		}

		InputStream is1 = mContext.getResources().openRawResource(R.raw.texture);
		try {
			mBitmap2 = BitmapFactory.decodeStream(is1);
		} finally {
			try {
				is1.close();
			} catch (Exception e) {
				// Ignore.
			}
		}

		InputStream is2 = mContext.getResources().openRawResource(R.raw.earth_lights);
		try {
			mBitmap4 = BitmapFactory.decodeStream(is2);
		} finally {
			try {
				is2.close();
			} catch (Exception e) {
				// Ignore.
			}
		}

		if (initialized == false) {
			mDownloadTextures = new DownloadTexturesXplanet(this);
			mDownloadTextures.cancel(true);
			mDownloadTextures.reloadTextures();

			initialized = true;
		} else {
			mDownloadTextures.reloadTextures();
		}

	}


	public void onDrawFrame(GL10 unused) {

		if (initializedShaders == false) {
			initializeShaders();
		}

		if (downloadedTextures == 0) {

		} else if (downloadedTextures == 1) {
			initializeGLTextures();

		}

		// Redraw background color
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);


		// Main Loop
		fearth.Orientation = M3DMATRIX.VxM(fearth.Orientation, mRotation);
		fearth.Up = M3DMATRIX.VxM(fearth.Up, mRotation);

		// movement attenuation
		float a = 0.95f;
		mRotation.m[0][0] = a * mRotation.m[0][0] + (1.0f - a) * 1.0f;
		mRotation.m[1][0] = a * mRotation.m[1][0] + (1.0f - a) * 0.0f;
		mRotation.m[2][0] = a * mRotation.m[2][0] + (1.0f - a) * 0.0f;
		mRotation.m[3][0] = a * mRotation.m[3][0] + (1.0f - a) * 0.0f;

		mRotation.m[0][1] = a * mRotation.m[0][1] + (1.0f - a) * 0.0f;
		mRotation.m[1][1] = a * mRotation.m[1][1] + (1.0f - a) * 1.0f;
		mRotation.m[2][1] = a * mRotation.m[2][1] + (1.0f - a) * 0.0f;
		mRotation.m[3][1] = a * mRotation.m[3][1] + (1.0f - a) * 0.0f;

		mRotation.m[0][2] = a * mRotation.m[0][2] + (1.0f - a) * 0.0f;
		mRotation.m[1][2] = a * mRotation.m[1][2] + (1.0f - a) * 0.0f;
		mRotation.m[2][2] = a * mRotation.m[2][2] + (1.0f - a) * 1.0f;
		mRotation.m[3][2] = a * mRotation.m[3][2] + (1.0f - a) * 0.0f;

		mRotation.m[0][3] = a * mRotation.m[0][3] + (1.0f - a) * 0.0f;
		mRotation.m[1][3] = a * mRotation.m[1][3] + (1.0f - a) * 0.0f;
		mRotation.m[2][3] = a * mRotation.m[2][3] + (1.0f - a) * 0.0f;
		mRotation.m[3][3] = a * mRotation.m[3][3] + (1.0f - a) * 1.0f;


		fearth.setWorldM();
		flabel.setWorldM();


		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
		long now = cal.getTimeInMillis();

		// Live light
		if (mLiveLight) {

			// sun declination calculation. see wiki
			cal.setTimeInMillis(mEpoch);
			double N = cal.get(Calendar.DAY_OF_YEAR) - 1;
			double declination = -((M3DM.PI / 180.0) * 23.44) * Math.cos((M3DM.PI / 180.0) * (360.0 / 365.0) * (N + 10.0));

			// calculate sun/light realtive rotation
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			//long msFromMidnight  = now - cal.getTimeInMillis() + mTimeRotate;
			long msFromMidnight = mEpoch - cal.getTimeInMillis();
			long timeShift = cal.getTimeZone().getOffset(mEpoch);


			DEV.Light[0].Pos = M3DVECTOR.POINTROTATE(new M3DVECTOR(0.0f, 100.0f * (float) Math.tan(declination), -100.0f), new M3DVECTOR(0.0f, 0.0f, 0.0f), new M3DVECTOR(0.0f, 1.0f, 0.0f), M3DM.PI - 2.0f * M3DM.PI * (float) (msFromMidnight - timeShift) / (1000.0f * 60.0f * 60.0f * 24.0f));
			DEV.Light[0].Pos = M3DMATRIX.VxM(DEV.Light[0].Pos, fearth.world);
		}

		// Time calculation
		if (mPlay) {
			// next 48h
			if (mTag == 'C' || mTag == 't' || mTag == 'T' || mTag == 'w' || mTag == 'v' || mTag == 'j' || mTag == 's') {
				mTimeRotate += Tc * 6 * 30 * 60 * 1000;
			}
			// last 35 years
			else if (mTag == 'O') {
				mTimeRotate += 25 * ((long) 365.25 * Tc * 2 * 30 * 60) * 1000;
			}
			// last 65 years
			else if (mTag == 'e') {
				mTimeRotate += 25 * ((long) 365.25 * Tc * 2 * 30 * 60) * 1000;
			}
			// last 0.5 years
			else if (mTag == 'a' || mTag == 'b') {
				mTimeRotate += 0.3 * ((long) 365.25 * Tc * 2 * 30 * 60) * 1000;
			}
			// last 6h
			else if (mTag == 'm') {
				mTimeRotate += Tc * 2 * 30 * 60 * 1000;
			}
			// last 2h
			else if (mTag == 'e') {
				mTimeRotate += Tc * 1 * 30 * 60 * 1000;
			}
			// last 168h
			else if (mTag == 'I' || mTag == 'W') {
				mTimeRotate += Tc * 12 * 30 * 60 * 1000;
			}
			// last 24h
			else {
				mTimeRotate += Tc * 4 * 30 * 60 * 1000;
			}
		}

		// next 48h
		if (mTag == 'C' || mTag == 't' || mTag == 'T' || mTag == 'w' || mTag == 'v' || mTag == 'j' || mTag == 's') {
			while (mTimeRotate > 48 * 3600 * 1000) {
				mTimeRotate -= 48 * 3600 * 1000;
			}
			mEpoch = now + mTimeRotate;
		}
		// last 35 years
		else if (mTag == 'O') {
			while (mTimeRotate > 35 * 365.25 * 24 * 3600 * 1000) {
				mTimeRotate -= 35 * 365.25 * 24 * 3600 * 1000;
			}
			mEpoch = now - (long) (35 * 365.25 * 24 * 3600) * 1000 + mTimeRotate;
		}
		// last 65 years
		else if (mTag == 'e') {
			while (mTimeRotate > 65 * 365.25 * 24 * 3600 * 1000) {
				mTimeRotate -= 65 * 365.25 * 24 * 3600 * 1000;
			}
			mEpoch = now - (long) (65 * 365.25 * 24 * 3600) * 1000 + mTimeRotate;
		}
		// last 1 years
		else if (mTag == 'a' || mTag == 'b') {
			while (mTimeRotate > 0.5 * 365.25 * 24 * 3600 * 1000) {
				mTimeRotate -= 0.5 * 365.25 * 24 * 3600 * 1000;
			}
			mEpoch = now - (long) (0.5 * 365.25 * 24 * 3600) * 1000 + mTimeRotate;
		}
		// last 6h
		else if (mTag == 'm') {
			if (mTimeRotate < 18 * 3600 * 1000) {
				mTimeRotate = 18 * 3600 * 1000;
			}
			while (mTimeRotate > 24 * 3600 * 1000) {
				mTimeRotate -= 6 * 3600 * 1000;
			}
			mEpoch = now - 24 * 3600 * 1000 + mTimeRotate;
		}
		// last 2h
		else if (mTag == 'e') {
			while (mTimeRotate > 2 * 3600 * 1000) {
				mTimeRotate -= 2 * 3600 * 1000;
			}
			mEpoch = now - 2 * 3600 * 1000 + mTimeRotate;
		}
		// last 168h
		else if (mTag == 'I' || mTag == 'W') {
			while (mTimeRotate > 168 * 3600 * 1000) {
				mTimeRotate -= 168 * 3600 * 1000;
			}
			mEpoch = now - 168 * 3600 * 1000 + mTimeRotate;
		}
		// last 24h
		else {
			while (mTimeRotate > 24 * 3600 * 1000) {
				mTimeRotate -= 24 * 3600 * 1000;
			}
			mEpoch = now - 24 * 3600 * 1000 + mTimeRotate;
		}

		// reset shows always current epoch
		if (mTimeRotate == 0) {
			mEpoch = now;
		}


		// Update texture
		long e1 = Long.MIN_VALUE/2 + 1L;    // nearest in past
		long e2 = Long.MIN_VALUE/2 + 1L;    // second nearest in past
		long e3 = Long.MAX_VALUE/2 - 1L;    // nearest in future
		long e4 = Long.MAX_VALUE/2 - 1L;    // second nearest in future

		// find nearest epoch in past
		for (Long e : mCloudMapId.keySet()) {

			if ((e < mEpoch) && ((mEpoch - e) < (mEpoch - e1))) {
				e1 = e;
			}
		}

		// find second nearest in past
		for (Long e : mCloudMapId.keySet()) {
			if ((e < mEpoch) && (e != e1) && ((mEpoch - e) < (mEpoch - e2))) {
				e2 = e;
			}
		}

		// find nearest in future
		for (Long e : mCloudMapId.keySet()) {
			if ((e >= mEpoch) && ((e - mEpoch) < (e3 - mEpoch))) {
				e3 = e;
			}
		}

		// find second nearest in future
		for (Long e : mCloudMapId.keySet()) {
			if ((e >= mEpoch) && (e != e3) && ((e - mEpoch) < (e4 - mEpoch))) {
				e4 = e;
			}
		}

		if (e1 != _e1) {
			if (mCloudMapId.containsKey(e1)) {
				Log.d("H21lab", "Changing texture e1 " + e1 + " " + _e1 + " ID " + mCloudMapId.get(e1));

				texture = new M3DM.mD3DTexture();
				texture.id = mCloudMapId.get(e1);
				label.setTexture(0, texture);
				earth.setTexture(2, texture);

				_e1 = e1;
			}
		}

		if (e2 != _e2) {
			if (mCloudMapId.containsKey(e2)) {
				Log.d("H21lab", "Changing texture e2 " + e2 + " " + _e2 + " ID " + mCloudMapId.get(e2));
				texture = new M3DM.mD3DTexture();
				texture.id = mCloudMapId.get(e2);
				label.setTexture(0, texture);
				earth.setTexture(4, texture);
				_e2 = e2;
			} else if (_e2 != e1 && mCloudMapId.containsKey(e1)) {
				Log.d("H21lab", "Changing texture e1 no e2 " + e1 + " " + _e1 + " ID " + mCloudMapId.get(e1));
				texture = new M3DM.mD3DTexture();
				texture.id = mCloudMapId.get(e1);
				label.setTexture(0, texture);
				earth.setTexture(4, texture);
				_e2 = e1;
			}
		}

		if (e3 != _e3) {
			if (mCloudMapId.containsKey(e3)) {
				Log.d("H21lab", "Changing texture e3 " + e3 + " " + _e3 + " ID " + mCloudMapId.get(e3));
				texture = new M3DM.mD3DTexture();
				texture.id = mCloudMapId.get(e3);
				label.setTexture(0, texture);
				earth.setTexture(5, texture);
				_e3 = e3;
			} else if (_e3 != _e1 && mCloudMapId.containsKey(e1)) {
				Log.d("H21lab", "Changing texture e1 no e3 " + e1 + " " + _e1 + " ID " + mCloudMapId.get(e1));
				texture = new M3DM.mD3DTexture();
				texture.id = mCloudMapId.get(e1);
				label.setTexture(0, texture);
				earth.setTexture(5, texture);
				_e3 = e1;
			}
		}

		if (e4 != _e4) {
			if (mCloudMapId.containsKey(e4)) {
				Log.d("H21lab", "Changing texture e4 " + e4 + " " + _e4 + " ID " + mCloudMapId.get(e4));
				texture = new M3DM.mD3DTexture();
				texture.id = mCloudMapId.get(e4);
				label.setTexture(0, texture);
				earth.setTexture(6, texture);
				_e4 = e4;
			} else if (_e4 != _e3 && mCloudMapId.containsKey(e3)) {
				Log.d("H21lab", "Changing texture e3 no e4 " + e1 + " " + _e3 + " ID " + mCloudMapId.get(e3));
				texture = new M3DM.mD3DTexture();
				texture.id = mCloudMapId.get(e3);
				label.setTexture(0, texture);
				earth.setTexture(6, texture);
				_e4 = e3;
			} else if (_e4 != _e1 && _e4 != _e3 && mCloudMapId.containsKey(e1)) {
				Log.d("H21lab", "Changing texture e1 no e4 " + e1 + " " + _e1 + " ID " + mCloudMapId.get(e1));
				texture = new M3DM.mD3DTexture();
				texture.id = mCloudMapId.get(e1);
				label.setTexture(0, texture);
				earth.setTexture(6, texture);
				_e4 = e1;
			}
		}

		// cloud map weights passed to shaders
		float tw1 = 1.0f;
		if (_e4 - _e1 != 0) {
			tw1 = (float) (1.0 - ((double) (mEpoch - _e1) / (double) (_e4 - _e1)));

			if (tw1 > 1.0f) {
				tw1 = 1.0f;
			}
			if (tw1 < 0.0f) {
				tw1 = 0.0f;
			}
		}
		earth.setCustomAttribute("uTW1", tw1);

		float tw2 = 1.0f;
		if (_e3 - _e2 != 0) {
			tw2 = (float) (1.0 - (double) (mEpoch - _e2) / (double) (_e3 - _e2));

			if (tw2 > 1.0f) {
				tw2 = 1.0f;
			}
			if (tw2 < 0.0f) {
				tw2 = 0.0f;
			}
		}
		earth.setCustomAttribute("uTW2", tw2);

		float tw3 = 1.0f;
		if (_e3 - _e1 != 0) {
			tw3 = (float) (1.0 - (double) (mEpoch - _e1) / (double) (_e3 - _e1));

			if (tw3 > 1.0f) {
				tw3 = 1.0f;
			}
			if (tw3 < 0.0f) {
				tw3 = 0.0f;
			}
		}
		earth.setCustomAttribute("uTW3", tw3);


		DEV.renderFrame(scene);
		if (reloadedTextures) {
			reloadedTextures = false;
			DEV.renderFrame(scene);
		}

		int glError = GLES20.glGetError();
		if (glError != GLES20.GL_NO_ERROR) {
			Log.e("H21lab", "OpenGL error " + "Error code " + glError);

			if (openGLErrorDetected >= 0) {
				initializeShaders();
				openGLErrorDetected++;
			}
		}

		/******** FPS *********/
		Nrenderedframe++;

		if (Nrenderedframe % 5 == 0) {
			// print time
			try {
				Activity act = (Activity) mContext;
				act.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Activity ac = (Activity) mContext;

						Date d = new Date(mEpoch);
						SimpleDateFormat formatter = new SimpleDateFormat("E HH:mm dd-MM-yyyy Z");
						String s = formatter.format(d);

						ac.setTitle(s);
					}
				});
			} catch (Exception e) {
				Log.e("H21lab", "Unable print time" + e.getMessage());
			}
		}
		if (Nrenderedframe % 30 == 0) {

			_t1 = _t2; // pre FPS
			_t2 = System.nanoTime();
			double _t = ((double) (_t2 - _t1)) / 1.0e9;
			Tc = Tc * 0.9f + 0.1f * (float) (_t / ((double) Nrenderedframe));
			if (Tc != 0.0f) {
				FPS = 1.0f / Tc;
			}
			Nrenderedframe = 0;
		}

		if (openGLErrorDetected > 50) {
			OpenGLErrorDialog errorDialog = new OpenGLErrorDialog(this);
			errorDialog.execute(
					"OpenGL error detected. It may be caused by rendering device capabilities. Application will crash now, but in next dialog please help to improve the application by submitting the error report.\n\n"
							+ "OpenGL error " + GLES20.glGetError() + "\n\n"
							+ "GL_VERSION = " + GLES20.glGetString(GLES20.GL_VERSION) + "\n\n"
							+ "GL_EXTENSIONS = " + GLES20.glGetString(GLES20.GL_EXTENSIONS) + "\n\n"
							+ "GL_RENDERER = " + GLES20.glGetString(GLES20.GL_RENDERER) + "\n\n"
							+ "glGetProgramInfoLog = " + GLES20.glGetProgramInfoLog(earth.getProgram()) + "\n\n"
			);
			openGLErrorDetected = -1;
		}

	}

	public void onSurfaceChanged(GL10 unused, int width, int height) {

		mWidth = width;
		mHeight = height;
		GLES20.glViewport(0, 0, width, height);
		DEV.initialize(width, height, 1.0f, 10.0f, new M3DVECTOR(0.0f, 0.0f, 2.7f), new M3DVECTOR(0.0f, 0.0f, -1.0f), new M3DVECTOR(0.0f, 1.0f, 0.0f));

	}

}