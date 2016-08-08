/*
 * M3DMATRIX class
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

public class M3DMATRIX {
	float m[][] = new float[4][4];

	M3DMATRIX() {
	}

	M3DMATRIX(float []_m) {
		m[0][0] = _m[0];	m[0][1] = _m[1];	m[0][2] = _m[2];	m[0][3] = _m[3];
		m[1][0] = _m[4];	m[1][1] = _m[5];	m[1][2] = _m[6];	m[1][3] = _m[7];
		m[2][0] = _m[8];	m[2][1] = _m[9];	m[2][2] = _m[10];	m[2][3] = _m[11];
		m[3][0] = _m[12];	m[3][1] = _m[13];	m[3][2] = _m[14];	m[3][3] = _m[15];
	}

	M3DMATRIX(float _m00, float _m01, float _m02, float _m03, float _m10, float _m11, float _m12, float _m13, float _m20, float _m21, float _m22, float _m23,
	          float _m30, float _m31, float _m32, float _m33) {
		m[0][0] = _m00;		m[0][1] = _m01;		m[0][2] = _m02;		m[0][3] = _m03;
		m[1][0] = _m10;		m[1][1] = _m11;		m[1][2] = _m12;		m[1][3] = _m13;
		m[2][0] = _m20;		m[2][1] = _m21;		m[2][2] = _m22;		m[2][3] = _m23;
		m[3][0] = _m30;		m[3][1] = _m31;		m[3][2] = _m32;		m[3][3] = _m33;
	}

	static M3DMATRIX MUL(M3DMATRIX a, M3DMATRIX b) {
		M3DMATRIX ret = new M3DMATRIX();
		ret.m[0][0] = a.m[0][0] * b.m[0][0] + a.m[0][1] * b.m[1][0] + a.m[0][2] * b.m[2][0] + a.m[0][3] * b.m[3][0];
		ret.m[0][1] = a.m[0][0] * b.m[0][1] + a.m[0][1] * b.m[1][1] + a.m[0][2] * b.m[2][1] + a.m[0][3] * b.m[3][1];
		ret.m[0][2] = a.m[0][0] * b.m[0][2] + a.m[0][1] * b.m[1][2] + a.m[0][2] * b.m[2][2] + a.m[0][3] * b.m[3][2];
		ret.m[0][3] = a.m[0][0] * b.m[0][3] + a.m[0][1] * b.m[1][3] + a.m[0][2] * b.m[2][3] + a.m[0][3] * b.m[3][3];
		ret.m[1][0] = a.m[1][0] * b.m[0][0] + a.m[1][1] * b.m[1][0] + a.m[1][2] * b.m[2][0] + a.m[1][3] * b.m[3][0];
		ret.m[1][1] = a.m[1][0] * b.m[0][1] + a.m[1][1] * b.m[1][1] + a.m[1][2] * b.m[2][1] + a.m[1][3] * b.m[3][1];
		ret.m[1][2] = a.m[1][0] * b.m[0][2] + a.m[1][1] * b.m[1][2] + a.m[1][2] * b.m[2][2] + a.m[1][3] * b.m[3][2];
		ret.m[1][3] = a.m[1][0] * b.m[0][3] + a.m[1][1] * b.m[1][3] + a.m[1][2] * b.m[2][3] + a.m[1][3] * b.m[3][3];
		ret.m[2][0] = a.m[2][0] * b.m[0][0] + a.m[2][1] * b.m[1][0] + a.m[2][2] * b.m[2][0] + a.m[2][3] * b.m[3][0];
		ret.m[2][1] = a.m[2][0] * b.m[0][1] + a.m[2][1] * b.m[1][1] + a.m[2][2] * b.m[2][1] + a.m[2][3] * b.m[3][1];
		ret.m[2][2] = a.m[2][0] * b.m[0][2] + a.m[2][1] * b.m[1][2] + a.m[2][2] * b.m[2][2] + a.m[2][3] * b.m[3][2];
		ret.m[2][3] = a.m[2][0] * b.m[0][3] + a.m[2][1] * b.m[1][3] + a.m[2][2] * b.m[2][3] + a.m[2][3] * b.m[3][3];
		ret.m[3][0] = a.m[3][0] * b.m[0][0] + a.m[3][1] * b.m[1][0] + a.m[3][2] * b.m[2][0] + a.m[3][3] * b.m[3][0];
		ret.m[3][1] = a.m[3][0] * b.m[0][1] + a.m[3][1] * b.m[1][1] + a.m[3][2] * b.m[2][1] + a.m[3][3] * b.m[3][1];
		ret.m[3][2] = a.m[3][0] * b.m[0][2] + a.m[3][1] * b.m[1][2] + a.m[3][2] * b.m[2][2] + a.m[3][3] * b.m[3][2];
		ret.m[3][3] = a.m[3][0] * b.m[0][3] + a.m[3][1] * b.m[1][3] + a.m[3][2] * b.m[2][3] + a.m[3][3] * b.m[3][3];

		return ret;
	}

	static M3DMATRIX IdentityMatrix() {
		M3DMATRIX mtx = new M3DMATRIX();
		for (int a = 0; a < 4; a++) {
			for (int b = 0; b < 4; b++) {
				mtx.m[a][b] = (a == b) ? 1.0f : 0.0f;
			}
		}
		return mtx;
	}

	static M3DMATRIX ZeroMatrix() {
		M3DMATRIX mtx = new M3DMATRIX();
		for (int a = 0; a < 4; a++) {
			for (int b = 0; b < 4; b++) {
				mtx.m[a][b] = 0.0f;
			}
		}
		return mtx;
	}

	static M3DMATRIX POINTROTATE_MATRIX(M3DVECTOR A, M3DVECTOR axis, float theta) {
		M3DMATRIX mtx = new M3DMATRIX();
		M3DVECTOR os = axis;
		float CS = (float) Math.cos((double) theta), SN = (float) Math.sin((double) theta), temp;
		// normalization
		temp = M3DVECTOR.SquareMagnitude(axis);
		if (temp == 0.0f) {
			return IdentityMatrix();
		}
		if (temp != 1.0f) {
			temp = (float) Math.sqrt(temp);
			os = M3DVECTOR.MUL(os, 1.0f / temp);
		}

		mtx.m[0][0] = CS + (1 - CS) * os.x * os.x;
		mtx.m[0][1] = SN * os.z + (1 - CS) * os.x * os.y;
		mtx.m[0][2] = -SN * os.y + (1 - CS) * os.x * os.z;
		mtx.m[0][3] = 0.0f;
		mtx.m[1][0] = -SN * os.z + (1 - CS) * os.x * os.y;
		mtx.m[1][1] = CS + (1 - CS) * os.y * os.y;
		mtx.m[1][2] = SN * os.x + (1 - CS) * os.y * os.z;
		mtx.m[1][3] = 0.0f;
		mtx.m[2][0] = SN * os.y + (1 - CS) * os.x * os.z;
		mtx.m[2][1] = -SN * os.x + (1 - CS) * os.y * os.z;
		mtx.m[2][2] = CS + (1 - CS) * os.z * os.z;
		mtx.m[2][3] = 0.0f;
		// if A==(0,0,0), than 3 next rows equals zero - no translation
		mtx.m[3][0] = (1 - CS) * A.x - (1 - CS) * os.x * os.x * A.x - (1 - CS) * os.x * os.y * A.y - (1 - CS) * os.x * os.z * A.z - A.y * os.z * SN + A.z
				                                                                                                                                              * os.y * SN;
		mtx.m[3][1] = (1 - CS) * A.y - (1 - CS) * os.y * os.y * A.y - (1 - CS) * os.x * os.y * A.x - (1 - CS) * os.y * os.z * A.z - A.z * os.x * SN + A.x
				                                                                                                                                              * os.z * SN;
		mtx.m[3][2] = (1 - CS) * A.z - (1 - CS) * os.z * os.z * A.z - (1 - CS) * os.x * os.z * A.x - (1 - CS) * os.y * os.z * A.y - A.x * os.y * SN + A.y
				                                                                                                                                              * os.x * SN;
		mtx.m[3][3] = 1.0f;
		return mtx;
	}

	// Multiplies vector(1x4, vec(0,3)=1.0f) with matrix 4x4
	static M3DVECTOR VxM(M3DVECTOR vec, M3DMATRIX mat) {
		M3DVECTOR ret = new M3DVECTOR();
		float F;
		ret.x = vec.x * mat.m[0][0] + vec.y * mat.m[1][0] + vec.z * mat.m[2][0] + mat.m[3][0];
		ret.y = vec.x * mat.m[0][1] + vec.y * mat.m[1][1] + vec.z * mat.m[2][1] + mat.m[3][1];
		ret.z = vec.x * mat.m[0][2] + vec.y * mat.m[1][2] + vec.z * mat.m[2][2] + mat.m[3][2];
		F = vec.x * mat.m[0][3] + vec.y * mat.m[1][3] + vec.z * mat.m[2][3] + mat.m[3][3];
		ret.x /= F;
		ret.y /= F;
		ret.z /= F;
		return ret;
	}

	/******************* TRANSFORMATIONS ***************/
	static M3DMATRIX CreateProjection(float near_plane, float far_plane, float fov_vert, float fov_horiz) {
		float h, w, Q;
		w = (float) (Math.cos(fov_horiz * 0.5) / Math.sin(fov_horiz * 0.5));
		h = (float) (Math.cos(fov_vert * 0.5) / Math.sin(fov_vert * 0.5));
		Q = far_plane / (far_plane - near_plane);
		M3DMATRIX ret = new M3DMATRIX(w, 0.0f, 0.0f, 0.0f, 0.0f, h, 0.0f, 0.0f, 0.0f, 0.0f, Q, -1.0f, 0.0f, 0.0f, -Q * near_plane, 0.0f);
		return ret;
	}

	static M3DMATRIX CreateCameraMatrix(M3DVECTOR position, M3DVECTOR orient, M3DVECTOR upworld) {
		M3DMATRIX mat = new M3DMATRIX();
		M3DVECTOR vView = new M3DVECTOR();
		vView = M3DVECTOR.MUL(M3DVECTOR.Normalize(orient), -1.0f);
		M3DVECTOR vRight = M3DVECTOR.CrossProduct(upworld, vView);
		M3DVECTOR vUp = M3DVECTOR.CrossProduct(vView, vRight);
		vRight = M3DVECTOR.Normalize(vRight);
		vUp = M3DVECTOR.Normalize(vUp);
		mat.m[0][0] = vRight.x;		mat.m[0][1] = vUp.x;		mat.m[0][2] = vView.x;		mat.m[0][3] = 0.0f;
		mat.m[1][0] = vRight.y;		mat.m[1][1] = vUp.y;		mat.m[1][2] = vView.y;		mat.m[1][3] = 0.0f;
		mat.m[2][0] = vRight.z;		mat.m[2][1] = vUp.z;		mat.m[2][2] = vView.z;		mat.m[2][3] = 0.0f;
		mat.m[3][0] = -M3DVECTOR.DotProduct(position, vRight);		mat.m[3][1] = -M3DVECTOR.DotProduct(position, vUp);		mat.m[3][2] = -M3DVECTOR.DotProduct(position, vView);		mat.m[3][3] = 1.0f;
		return mat;
	}

	static M3DMATRIX CreateWorldMatrix(M3DVECTOR position, M3DVECTOR orientation, M3DVECTOR up) {
		M3DVECTOR osZ = M3DVECTOR.Normalize(orientation);
		M3DVECTOR osX = M3DVECTOR.CrossProduct(up, osZ);
		M3DVECTOR osY = M3DVECTOR.CrossProduct(osZ, osX);
		osX = M3DVECTOR.Normalize(osX);
		osY = M3DVECTOR.Normalize(osY);
		M3DMATRIX mat = IdentityMatrix();
		mat.m[0][0] = osX.x;		mat.m[1][0] = osY.x;		mat.m[2][0] = osZ.x;		mat.m[3][0] = position.x;
		mat.m[0][1] = osX.y;		mat.m[1][1] = osY.y;		mat.m[2][1] = osZ.y;		mat.m[3][1] = position.y;
		mat.m[0][2] = osX.z;		mat.m[1][2] = osY.z;		mat.m[2][2] = osZ.z;		mat.m[3][2] = position.z;
		return mat;
	}

	public float[] values() {
		float[] res = new float[16];
		for (int a = 0; a < 4; a++) {
			for (int b = 0; b < 4; b++) {
				res[a*4 + b] = m[a][b];
			}
		}
		return res;
	}

	/***************************************************************/
}
