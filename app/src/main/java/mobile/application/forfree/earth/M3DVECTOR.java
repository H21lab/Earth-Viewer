/*
 * M3DVECTOR class
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

public class M3DVECTOR {
	float x, y, z;

	M3DVECTOR() {
		x = y = z = 0.0f;
	}

	M3DVECTOR(float d) {
		x = y = z = d;
	}

	M3DVECTOR(float X, float Y, float Z) {
		x = X;
		y = Y;
		z = Z;
	}
	
	M3DVECTOR(M3DVECTOR v) {
		x = v.x;
		y = v.y;
		z = v.z;
	}
	
	float[] values() {
		float[] v = new float[4];
		v[0] = x;
		v[1] = y;
		v[2] = z;
		v[3] = 1.0f;
		return v;
	}
	
	void set(M3DVECTOR v) {
		x = v.x;
		y = v.y;
		z = v.z;
	}
	
	void set(float X, float Y, float Z) {
		x = X;
		y = Y;
		z = Z;
	}
	
	boolean equals(M3DVECTOR v) {
		return x == v.x && y == v.y && z == v.y; 
	}
	

	static M3DVECTOR ADD(M3DVECTOR A, M3DVECTOR B) {
		M3DVECTOR C = new M3DVECTOR(A.x + B.x, A.y + B.y, A.z + B.z);
		return C;
	}

	static M3DVECTOR DIF(M3DVECTOR A, M3DVECTOR B) {
		M3DVECTOR C = new M3DVECTOR(A.x - B.x, A.y - B.y, A.z - B.z);
		return C;
	}

	static M3DVECTOR MUL(M3DVECTOR A, float d) {
		M3DVECTOR C = new M3DVECTOR(A.x * d, A.y * d, A.z * d);
		return C;
	}

	static float SquareMagnitude(M3DVECTOR A) {
		return A.x * A.x + A.y * A.y + A.z * A.z;
	}

	static float Magnitude(M3DVECTOR A) {
		return (float) Math.sqrt(SquareMagnitude(A));
	}

	static M3DVECTOR Normalize(M3DVECTOR A) {
		float M = Magnitude(A);
		M3DVECTOR C;
		if (M == 0.0) {
			C = new M3DVECTOR(0.0f, 0.0f, 0.0f);
			return C;
		}
		C = new M3DVECTOR(A.x / M, A.y / M, A.z / M);
		return C;
	}

	static float DotProduct(M3DVECTOR A, M3DVECTOR B) {
		return A.x * B.x + A.y * B.y + A.z * B.z;
	}

	static M3DVECTOR CrossProduct(M3DVECTOR A, M3DVECTOR B) {
		M3DVECTOR C = new M3DVECTOR(A.y * B.z - A.z * B.y, A.z * B.x - A.x * B.z, A.x * B.y - A.y * B.x);
		return C;
	}

	static M3DVECTOR POINTROTATE(M3DVECTOR B, M3DVECTOR A, M3DVECTOR os, float theta) {
		M3DVECTOR P, osX, osY, BN;
		M3DVECTOR R = new M3DVECTOR(B.x - A.x, B.y - A.y, B.z - A.z);
		float t;

		// ci nahodou bod B nelezi na osi otocenia
		M3DVECTOR test = CrossProduct(os, R);
		if (test.x == 0.0f && test.y == 0.0f && test.z == 0.0f) {
			return B;
		}
		// nelezi
		t = DotProduct(os, R) / (os.x * os.x + os.y * os.y + os.z * os.z);
		P = new M3DVECTOR(A.x + t * os.x, A.y + t * os.y, A.z + t * os.z);
		osX = new M3DVECTOR(B.x - P.x, B.y - P.y, B.z - P.z);
		float L = (float) Math.sqrt(Math.abs(os.x * os.x + os.y * os.y + os.z * os.z));
		R = new M3DVECTOR(os.x / L, os.y / L, os.z / L);
		osY = CrossProduct(R, osX);
		float CS = (float) Math.cos(theta), SN = (float) Math.sin(theta);
		BN = new M3DVECTOR(CS * osX.x + SN * osY.x, CS * osX.y + SN * osY.y, CS * osX.z + SN * osY.z);
		BN = new M3DVECTOR(BN.x + P.x, BN.y + P.y, BN.z + P.z);
		return BN;
	}

	static M3DVECTOR VtoView(M3DVECTOR vec, int SCREEN_WIDTH, int SCREEN_HEIGHT, float P_NPlane, float P_FPlane) {
		return new M3DVECTOR(SCREEN_WIDTH / 2 + vec.x * (float) (SCREEN_WIDTH / 2), SCREEN_HEIGHT / 2 - vec.y * (float) (SCREEN_HEIGHT / 2),
				((P_FPlane - P_NPlane) / 2.0f) * vec.z + (P_NPlane + P_FPlane) / 2.0f);
	}
}
