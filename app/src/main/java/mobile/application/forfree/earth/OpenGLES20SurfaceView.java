/*
 * OpenGLES20SurfaceView class
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

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class OpenGLES20SurfaceView extends GLSurfaceView {

	private OpenGLES20Renderer mRenderer;
	private float mPreviousX = -1.0f;
	private float mPreviousY = -1.0f;

	private float mPreviousX2 = -1.0f;
	private float mPreviousY2 = -1.0f;

	private float mPreviousxtheta = 0.0f;
	private float mPreviousytheta = 0.0f;

	private int previousPointerCount = 0;

	GestureDetector gestureDetector;
	public float test;

	public OpenGLES20SurfaceView(Context context) {
		super(context);

		// Create an OpenGL ES 2.0 context.
		setEGLContextClientVersion(2);
		// Set the Renderer for drawing on the GLSurfaceView

		mRenderer = new OpenGLES20Renderer(context);
		setRenderer(mRenderer);

		gestureDetector = new GestureDetector(context, new GestureListener());
	}

	public OpenGLES20Renderer getOpenGLES20Renderer() {
		return mRenderer;
	}

	private class GestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		// event when double tap occurs
		@Override
		public boolean onDoubleTap(MotionEvent e) {

			// play/stop
			mRenderer.mPlay = !mRenderer.mPlay;

			return true;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {

		gestureDetector.onTouchEvent(e);

		// MotionEvent reports input details from the touch screen
		// and other input controls. In this case, you are only
		// interested in events where the touch position changed.

		float x;
		float y;
		float x2;
		float y2;

		if (mRenderer != null && mRenderer.initialized == true) {

			try {

				switch (e.getAction()) {
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
					case MotionEvent.ACTION_OUTSIDE:
						mPreviousX = -1.0f;
						mPreviousY = -1.0f;
						mPreviousX2 = -1.0f;
						mPreviousY2 = -1.0f;
						break;

					case MotionEvent.ACTION_MOVE:

						if (e.getPointerCount() == 1) {

							if (previousPointerCount >= 2) {
								mPreviousX = -1.0f;
								mPreviousY = -1.0f;
								mPreviousX2 = -1.0f;
								mPreviousY2 = -1.0f;
							}

							x = e.getX(0);
							y = e.getY(0);

							if (mPreviousX == -1.0f) {
								mPreviousX = x;
							}
							if (mPreviousY == -1.0f) {
								mPreviousY = y;
							}

							float ytheta = 0.5f * mPreviousytheta + 0.5f * (1.0f * (float) (y - mPreviousY) / (float) mRenderer.DEV.SCREEN_HEIGHT);
							float xtheta = 0.5f * mPreviousxtheta + 0.5f * (1.0f * (float) (x - mPreviousX) / (float) mRenderer.DEV.SCREEN_WIDTH);

							mRenderer.mRotation = M3DMATRIX.POINTROTATE_MATRIX(new M3DVECTOR(0.0f, 0.0f, 0.0f), new M3DVECTOR(0.0f, 1.0f, 0.0f), xtheta);
							mRenderer.mRotation = M3DMATRIX.MUL(mRenderer.mRotation, M3DMATRIX.POINTROTATE_MATRIX(new M3DVECTOR(0.0f, 0.0f, 0.0f), new M3DVECTOR(1.0f, 0.0f, 0.0f), ytheta));

							mPreviousX = x;
							mPreviousY = y;
							mPreviousX2 = -1.0f;
							mPreviousY2 = -1.0f;
							mPreviousxtheta = xtheta;
							mPreviousytheta = ytheta;

							previousPointerCount = 1;

						} else if (e.getPointerCount() >= 2) {

							x = e.getX(0);
							y = e.getY(0);
							x2 = e.getX(1);
							y2 = e.getY(1);

							if (mPreviousX == -1.0f) {
								mPreviousX = x;
							}
							if (mPreviousY == -1.0f) {
								mPreviousY = y;
							}
							if (mPreviousX2 == -1.0f) {
								mPreviousX2 = x2;
							}
							if (mPreviousY2 == -1.0f) {
								mPreviousY2 = y2;
							}


							float _x = (x - x2) / (float) mRenderer.DEV.SCREEN_WIDTH;
							float _y = (y - y2) / (float) mRenderer.DEV.SCREEN_HEIGHT;

							float __x = (mPreviousX - mPreviousX2) / (float) mRenderer.DEV.SCREEN_WIDTH;
							float __y = (mPreviousY - mPreviousY2) / (float) mRenderer.DEV.SCREEN_HEIGHT;

							if (_x != 0.0f && __x != 0.0f && _y != 0.0f && __y != 0.0f) {

								float _angle = (float) Math.atan((double) _y / _x);
								if (x2 < x) {
									_angle -= M3DM.PI / 1.0f;
								}


								float __angle = (float) Math.atan((double) __y / __x);
								if (mPreviousX2 < mPreviousX) {
									__angle -= M3DM.PI / 1.0f;
								}

								float angle = (_angle - __angle) * 1.0f;

								mRenderer.fearth.Orientation = M3DVECTOR.POINTROTATE(mRenderer.fearth.Orientation, new M3DVECTOR(0.0f, 0.0f, 0.0f), mRenderer.DEV.CameraOrientation, angle);
								mRenderer.fearth.Up = M3DVECTOR.POINTROTATE(mRenderer.fearth.Up, new M3DVECTOR(0.0f, 0.0f, 0.0f), mRenderer.DEV.CameraOrientation, angle);
								mRenderer.fearth.setWorldM();

							}


							// scale
							_x = x - x2;
							_y = y - y2;

							__x = mPreviousX - mPreviousX2;
							__y = mPreviousY - mPreviousY2;

							float _r = (float) Math.sqrt(_x * _x + _y * _y);
							float __r = (float) Math.sqrt(__x * __x + __y * __y);
							float scale = (float) Math.sqrt(__r / _r);

							mRenderer.DEV.P_fov_horiz *= scale;
							if (mRenderer.DEV.P_fov_horiz > M3DM.PI - 1.0f) {
								mRenderer.DEV.P_fov_horiz = M3DM.PI - 1.0f;
							} else if (mRenderer.DEV.P_fov_horiz <= 0.0f) {
								mRenderer.DEV.P_fov_horiz = 0.01f;
							}

							mRenderer.DEV.P_fov_vert *= scale;
							if (mRenderer.DEV.P_fov_vert > M3DM.PI - 1.7f) {
								mRenderer.DEV.P_fov_vert = M3DM.PI - 1.7f;
							} else if (mRenderer.DEV.P_fov_vert <= 0.0f) {
								mRenderer.DEV.P_fov_vert = 0.01f;
							}

							float left = -mRenderer.DEV.P_NPlane * ((float) (mRenderer.DEV.SCREEN_WIDTH) / (float) (mRenderer.DEV.SCREEN_HEIGHT)) * (float) Math.tan(mRenderer.DEV.P_fov_vert / 2.0f);
							float right = mRenderer.DEV.P_NPlane * ((float) (mRenderer.DEV.SCREEN_WIDTH) / (float) (mRenderer.DEV.SCREEN_HEIGHT)) * (float) Math.tan(mRenderer.DEV.P_fov_vert / 2.0f);
							float bottom = -mRenderer.DEV.P_NPlane * (float) Math.tan(mRenderer.DEV.P_fov_vert / 2.0f);
							float top = mRenderer.DEV.P_NPlane * (float) Math.tan(mRenderer.DEV.P_fov_vert / 2.0f);

							Matrix.frustumM(mRenderer.DEV.projectionMatrix, 0, left, right, bottom, top, mRenderer.DEV.P_NPlane, mRenderer.DEV.P_FPlane);

							Matrix.frustumM(mRenderer.DEV.orthoProjectionMatrix, 0, -mRenderer.DEV.P_NPlane * (float) (mRenderer.DEV.SCREEN_WIDTH) / (float) (mRenderer.DEV.SCREEN_HEIGHT) * (float) Math.tan(mRenderer.DEV._P_fov_vert / 2.0f), mRenderer.DEV.P_NPlane * (float) (mRenderer.DEV.SCREEN_WIDTH) / (float) (mRenderer.DEV.SCREEN_HEIGHT) * (float) Math.tan(mRenderer.DEV._P_fov_vert / 2.0f), -mRenderer.DEV.P_NPlane * (float) Math.tan(mRenderer.DEV._P_fov_vert / 2.0f), mRenderer.DEV.P_NPlane * (float) Math.tan(mRenderer.DEV._P_fov_vert / 2.0f), mRenderer.DEV.P_NPlane, mRenderer.DEV.P_FPlane);

							mPreviousX = x;
							mPreviousY = y;
							mPreviousX2 = x2;
							mPreviousY2 = y2;

							previousPointerCount = 2;

						} else {

							mPreviousX = -1.0f;
							mPreviousY = -1.0f;
							mPreviousX2 = -1.0f;
							mPreviousY2 = -1.0f;

							previousPointerCount = 0;
						}

						break;
				}

			} catch (Exception exception) {
				Log.e("H21lab", "Exception detected: " + exception.getMessage());
			}
		}

		return true;
	}
}
