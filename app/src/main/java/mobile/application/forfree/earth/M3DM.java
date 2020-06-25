/*
 * M3DM class
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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

class M3DM {

	final static String TAG = "M3DM";

	int SCREEN_WIDTH = 200, SCREEN_HEIGHT = 200;

	int ZSORT = 1;      // TODO

	final static float PI = 3.1415926535f;
	/************************* CAMERA ***********************/
	M3DVECTOR CameraPosition, CameraOrientation, CameraUp;
	float[] viewMatrix = new float[16];
	float[] projectionMatrix = new float[16];

	// PROJECTION
	float P_NPlane = 1.0f;
	float P_FPlane = 200.0f;
	float P_fov_vert = 60.0f * (PI / 180.0f); // celkovo
	float P_fov_horiz = ((float) (SCREEN_WIDTH) / (float) (SCREEN_HEIGHT)) * P_fov_vert;
	/************************ FLAGS *************************/
	// TODO some parameters are legacy
	final static int MAXMESHS = 20; // max # of meshs in one mesh or frame
	final static int MAXFRAMES = 50; // max # of frames in one frame
	final static int MAXNFACE = 50; // max # of vertex per face
	final static int MAXNTX = 500; // max # of textures in MOTextureManagment
	final static int MAXOBJ = 100; // max # of objects in the scene - pre Z sort
	final static int MAXBOX = 100; // max # of boxes in the mesh - for collision
	// detect AABB
	final static int NAMELENGHT = 16;// lenght of Name of the Mesh in chars

	/* FLAGS for Mesh */
	final static int MD3DMESHF_SPHERMAP = 1; // mesh has env. mapping
	final static int MD3DMESHF_DISABLED = 2; // mesh is not rendered
	final static int MD3DMESHF_NOCULLING = 16;    // mesh no culling
	final static int MD3DMESHF_FRONTCULLING = 32;    // mesh front culling
	final static int MD3DMESHF_RENDEREDFIRST = 64;  // mesh render first if zsort

	/* FLAGS PRE FLEXIBLE VERTEX FORMAT */
	final static int M3DFVF_XYZ = 0x00000001;
	final static int M3DFVF_NORMAL = 0x00000002;
	final static int M3DFVF_COLOR = 0x00000004;
	final static int M3DFVF_TEX1 = 0x00000008;

	// TODO not used yet
	/* For Z sort*********** */
	class ZSORTstruct implements Comparable<ZSORTstruct> {
		float Z;
		mD3DMesh hMesh;
		M3DMATRIX World;

		@Override
		public int compareTo(ZSORTstruct b) {
			if (this.Z > b.Z) {
				return 1;
			} else if (this.Z < b.Z) {
				return -1;
			} else {
				return 0;
			}
		}
	}

	static List<ZSORTstruct> zsort = new ArrayList<ZSORTstruct>();

	/* OpenGL */
	// TODO only for 1 light and texture required
	private final String vertexShaderCode =
			"uniform mat4 uMVPMatrix;   \n" +
					"uniform mat4 uMVMatrix;   	\n" +
					"uniform mat4 uVMatrix;   	\n" +
					"uniform mat4 uIMVMatrix;	\n" +

					"attribute vec4 aPosition; 	\n" +
					"attribute vec3 aNormal;  	\n" +
					"attribute vec2 aTex;		\n" +

					"varying vec3 vPosition;	\n" +
					"varying vec3 vNormal;		\n" +
					"varying vec3 vMVNormal;	\n" +
					"varying vec2 vTex;			\n" +

					"uniform vec4 uLightPos;  	\n" +
					"varying vec3 vLightPos;	\n" +
					"varying vec3 lightVector;	\n" +


					"varying vec3 vEye;	\n" +

					// Atmosphere
					"varying vec3 vPositionA;	\n" +
					// clouds shadow shift
					"varying vec2 shiftUV;		\n" +

					"void main(){              	\n" +
					" gl_Position = uMVPMatrix * aPosition; \n" +
					" vec4 position = aPosition; \n" +
					" vPosition = vec3(position.x, position.y, position.z); \n" +

					// only rotate the normals
					" vec4 normal = vec4(aNormal, 0.0); \n" +
					" vNormal = vec3(normal.x, normal.y, normal.z); \n" +
					" normal = uMVMatrix*vec4(aNormal, 0.0); \n" +
					" vMVNormal = vec3(normal.x, normal.y, normal.z); \n" +


					" vTex = aTex; \n" +

					" vec4 lightPos = uIMVMatrix * uLightPos; \n" +
					" vLightPos = vec3(lightPos.x, lightPos.y, lightPos.z); \n" +

					" vec4 eye = uIMVMatrix * vec4(0.0, 0.0, 0.0, 1.0); \n" +
					" vEye = vec3(eye.x, eye.y, eye.z); \n" +


					" lightVector = normalize(vLightPos - vPosition);   \n" +

					// clouds shadow
					" vec3 _y = vec3(0.0, 1.0, 0.0); \n" +
					" vec3 _x = cross(_y, vNormal); \n" +
					" vec3 _z = vNormal; \n" +
					" shiftUV = vec2( dot(lightVector, _x), -dot(lightVector, _y) ); \n" +


					"}                         \n";

	private final String fragmentShaderCode =
			"precision mediump float;  	\n" +
					"varying vec3 vLightPos;  	\n" +
					"varying vec3 vPosition;	\n" +     // Interpolated position for this fragment.
					"varying vec3 lightVector;	\n" +
					"uniform vec4 uColor;  		\n" +     // This is the color from the vertex shader interpolated across the triangle per fragment.
					"varying vec3 vNormal;   	\n" +     // Interpolated normal for this fragment.
					"varying vec3 vMVNormal;	\n" +
					"varying vec2 vTex;			\n" +
					"uniform sampler2D uTextures[4];\n" +
					"varying vec3 vEye	;   	\n" +
					"varying vec2 shiftUV;		\n" +      // clouds shadow shift

					"uniform vec4 uLightAmbientColor;  		\n" +
					"uniform vec4 uLightDiffuseColor;  		\n" +
					"uniform vec4 uLightSpecularColor;  	\n" +
					"uniform float uLightAttenuation;  		\n" +
					"uniform vec4 uMaterialAmbientColor;  	\n" +
					"uniform vec4 uMaterialDiffuseColor; 	\n" +
					"uniform vec4 uMaterialSpecularColor;  	\n" +
					"uniform vec4 uMaterialEmissiveColor;  	\n" +
					"uniform float uMaterialShinnes;  		\n" +
					"uniform float uBumpLevel;				\n" +
					"uniform int uTexMapping;				\n" +

					// The entry point for our fragment shader.
					"void main()  				\n" +
					"{  						\n" +

					// phong shading
					//"    vec3 normal2 = normalize(vNormal);	\n" +
					// gouraud shading
					"    vec3 normal2 = vNormal;	\n" +


					// bumpmapping
					// lookup normal from normal map, move from [0,1] to  [-1, 1] range
					"	 vec3 normal = vec3(0.0, 0.0, 0.0);	\n" +
					"	 normal.x = -(2.0 * texture2D (uTextures[0], vTex).b - 1.0);	\n" +
					"	 normal.y = 2.0 * texture2D (uTextures[0], vTex).g - 1.0;	\n" +
					"	 normal.z = -(2.0 * texture2D (uTextures[0], vTex).r - 1.0);	\n" +
					"	 vec4 n = vec4(normal, 0.0); \n" +
					"	 normal = vec3(n.x, n.y, n.z); \n" +

					"	 normal = uBumpLevel*normal + (1.0 - uBumpLevel)*normal2; \n" +

					//  Will be used for attenuation
					//"    float distance = length(vLightPos - vPosition);   \n" +
					// Get a lighting direction vector from the light to the vertex.
					//"    vec3 lightVector = normalize(vLightPos - vPosition);   \n" +

					// diffuse
					"    float diffuse = max(dot(normal, lightVector), 0.0);   \n" +
					// attenuation
					//	"    diffuse = diffuse * (1.0 / (1.0 + (uLightAttenuation * distance * distance)));   \n" +
					//	"	 diffuse = clamp(diffuse, 0.0, 1.0);\n" +

					// diffuse 2
					"    float diffuse2 = max(dot(normal2, lightVector), 0.0);   \n" +
					// attenuation
					//	"    diffuse2 = diffuse2 * (1.0 / (1.0 + (uLightAttenuation * distance * distance)));   \n" +
					//	"	 diffuse2 = clamp(diffuse2, 0.0, 1.0);\n" +

					// specular
					"	 vec3 E = normalize(vEye - vPosition);\n" +  // we are in Eye Coordinates, so EyePos is (0,0,0)
					"	 vec3 R = normalize(-reflect(lightVector, normal));\n" +
					"    float sp = max(dot(R, E), 0.0);\n" +
					"	 vec4 spec = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, uMaterialShinnes);\n" +
					"	 spec = clamp(spec, 0.0, 1.0);\n" +

					// specular watter
					"	 vec4 specW = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, 15.0);\n" +
					"	 specW = clamp(specW, 0.0, 1.0);\n" +

					// specular clouds
					"	 R = normalize(-reflect(lightVector, normal2));\n" +
					"    sp = max(dot(R, E), 0.0);\n" +
					"	 vec4 specC = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, 1.5);\n" +
					"	 specC = clamp(specC, 0.0, 1.0);\n" +


					" float min = 0.0; \n" +
					" float max = 1.0; \n" +

					" float cm = clamp((texture2D(uTextures[2], vTex).g - min)/(max-min), 0.0, 1.0); \n" +
					//" cm = 0.0 + 1.0*pow(cm, 1.0); \n" +
					" cm = 0.0 + 1.25*cm; \n" +
					" vec4 cmT = vec4(1.0,1.0,1.0,1.0); \n" +
					" float cmS = clamp((texture2D(uTextures[2], vTex  + uBumpLevel*0.005*shiftUV).g - min)/(max-min), 0.0, 1.0); \n" +
					//" cmS = 0.0 + 1.0*pow(cmS, 1.0); \n" +
					" cmS = 0.0 + 1.0*cmS; \n" +


					" 		float d = (1.0 - texture2D(uTextures[1], vTex).r)*(1.0 - texture2D(uTextures[1], vTex).g)*texture2D(uTextures[1], vTex).b; \n" +

					// cloudmap
					"		gl_FragColor = 1.5*cm*cmT*( 0.15 + 1.0*uLightAmbientColor + 0.9*uLightDiffuseColor * diffuse2 + 0.2*uLightSpecularColor * specC ) "
					// clouds shadow
					+ "						+ clamp(-1.0*vec4(0.4, 0.4, 0.4, 1.0)*cmS "
					// texture
					+ "						+ clamp(1.0 - 1.0*cm, 0.0, 1.0)*texture2D(uTextures[1], vTex) * (uMaterialEmissiveColor + uLightAmbientColor * uMaterialAmbientColor + uLightDiffuseColor * uMaterialDiffuseColor * diffuse  + 0.4*uLightSpecularColor * specW )     "
					// specular light on watter
					+ "						+ 5.0*d*(1.0 - 1.0*cm)*1.0*vec4(1.0, 1.0, 1.0, 1.0)*uLightSpecularColor * specW , 0.0, 1.0)  ;   \n" +


					// night light
					//" if (diffuse < 2.0) { \n" +
					"	 diffuse = clamp(diffuse, 0.0, 1.0); \n" +
					"    gl_FragColor =  gl_FragColor + 1.0*(1.0*clamp(1.0 - 2.25*cm, 0.0, 1.0))*pow(1.0 - diffuse, 10.0)*texture2D(uTextures[3], vTex);   \n" +
					//"}   \n" +


					// antialias edges

					" float a; \n" +

					//" a = pow(abs(vMVNormal.z), 0.5); \n" +
					" a = abs(vMVNormal.z); \n" +
					// additional specular
					" gl_FragColor = clamp(gl_FragColor  + 0.2 * uLightSpecularColor * (a) * (sp*sp * 0.5), 0.0, 1.0);	\n" +

					" a = pow(abs(1.0 - vMVNormal.z), 4.0); \n" +
					" gl_FragColor = clamp(gl_FragColor  + 10.0*vec4(1.0, 1.0, 1.0, 1.0) * uLightSpecularColor  * (a) * (diffuse * 5.0), 0.0, 1.0);	\n" +

					" a = pow(abs(1.0 - vMVNormal.z), 0.5); \n" +
					" gl_FragColor = clamp(gl_FragColor  + 0.2*vec4(0.3, 0.6, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +


					" a = pow(abs(1.0 - vMVNormal.z), 30.0); \n" +
					" gl_FragColor = clamp(gl_FragColor  + 20.0*vec4(0.0, 0.4, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +

					" if (abs(vMVNormal.z) < 0.55) { \n" +
					" 	gl_FragColor = clamp(gl_FragColor * pow(abs(vMVNormal.z)/0.55 - 0.0, 5.0), 0.0, 1.0);	\n" +
					" }; \n" +


					"}   \n";


	/******************************************************/
	/******************* STRUCTURES *************************/
	/************************** LIGHT *******************************/
	int N_Lights = 0;

	static class LIGHT {
		M3DVECTOR Pos;
		float AR, AG, AB, AA; // ambient
		float DR, DG, DB, DA; // diffuse
		float SR, SG, SB, SA; // specular
		float AT; // attenuation

		LIGHT() {
			Pos = new M3DVECTOR(0.0f, 0.0f, 0.0f);
			AR = AG = AB = AA = DR = DG = DB = DA = SR = SG = SB = SA = 0.0f;
		}
	}

	;

	LIGHT Light[] = new LIGHT[8];

	/************************ M3DMATERIAL **************************/
	static class M3DMATERIAL {
		float AR, AG, AB, AA; // ambient
		float DR, DG, DB, DA; // diffuse
		float SR, SG, SB, SA; // specular
		float ER, EG, EB, EA; // emission
		float SH; // shininess
		float am, dm, sm; // ambient col. index, diffuse, specular
		char Name[] = new char[NAMELENGHT];        // TODO not used

		M3DMATERIAL() {
			AR = 1.0f;
			AG = 1.0f;
			AB = 1.0f;
			AA = 1.0f;
			DR = 1.0f;
			DG = 1.0f;
			DB = 1.0f;
			DA = 1.0f;
			SR = 1.0f;
			SG = 1.0f;
			SB = 1.0f;
			SA = 1.0f;
			ER = 0.0f;
			EG = 0.0f;
			EB = 0.0f;
			EA = 1.0f;
			SH = 45.0f;
			am = 0.0f;
			dm = 1.0f;
			sm = 1.0f;
			Name[0] = 0; // not set yet
		}

		M3DMATERIAL(float ar, float ag, float ab, float aa, float dr, float dg, float db, float da, float sr, float sg, float sb, float sa, float er, float eg,
					float eb, float ea, float sh, float _am, float _dm, float _sm) {
			AR = ar;
			AG = ag;
			AB = ab;
			AA = aa;
			DR = dr;
			DG = dg;
			DB = db;
			DA = da;
			SR = sr;
			SG = sg;
			SB = sb;
			SA = sa;
			ER = er;
			EG = eg;
			EB = eb;
			EA = ea;
			SH = sh;
			am = _am;
			dm = _dm;
			sm = _sm;
			// Name=NAME;
		}
	}

	M3DMATERIAL DEFMATERIAL = new M3DMATERIAL(1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 45.0f, 0.0f,
			1.0f, 1.0f);

	static class mD3DTexture {
		int id = -1;
		char Name[] = new char[NAMELENGHT];    // TODO Not used

		mD3DTexture() {
			id = 0;
		}

		mD3DTexture(int i) {
			id = i;
			Name[0] = 0;
		}
	}

	mD3DTexture _Texture[] = new mD3DTexture[8];

	/************* VERTEX ***************/
	static class M3DVERTEX_RGB {
		M3DVECTOR P;
		M3DVECTOR N;
		float u, v;
		float r, g, b, a;

		M3DVERTEX_RGB() {
			P = new M3DVECTOR(0.0f, 0.0f, 0.0f);
			N = new M3DVECTOR(0.0f, 0.0f, 0.0f);
			u = v = r = g = b = a = 0.0f;
		}
	}

	static class M3DVERTEX {
		M3DVECTOR P;
		M3DVECTOR N;
		float u, v;

		public M3DVERTEX() {
			P = new M3DVECTOR(0.0f, 0.0f, 0.0f);
			N = new M3DVECTOR(0.0f, 0.0f, 0.0f);
			u = v = 0.0f;
		}
	}

	/******************** CONSTRUCTOR ***********************/
	M3DM() {
		SCREEN_WIDTH = 0;
		SCREEN_HEIGHT = 0;
		for (int i = 0; i < 8; i++) {
			_Texture[i] = new mD3DTexture();
			Light[i] = new LIGHT();
		}
	}

	M3DM(int width, int height) {
		SCREEN_WIDTH = width;
		SCREEN_HEIGHT = height;

		P_fov_horiz = ((float) (SCREEN_WIDTH) / (float) (SCREEN_HEIGHT)) * P_fov_vert;
		//	Matrix.frustumM(projectionMatrix, 0, -P_NPlane*(float)Math.tan(P_fov_horiz), P_NPlane*(float)Math.tan(P_fov_horiz), -P_NPlane*(float)Math.tan(P_fov_vert), P_NPlane*(float)Math.tan(P_fov_vert), P_NPlane, P_FPlane);
		Matrix.frustumM(projectionMatrix, 0, -P_NPlane * (float) (SCREEN_WIDTH) / (float) (SCREEN_HEIGHT) * (float) Math.tan(P_fov_vert / 2.0f), P_NPlane * (float) (SCREEN_WIDTH) / (float) (SCREEN_HEIGHT) * (float) Math.tan(P_fov_vert / 2.0f), -P_NPlane * (float) Math.tan(P_fov_vert / 2.0f), P_NPlane * (float) Math.tan(P_fov_vert / 2.0f), P_NPlane, P_FPlane);


		for (int i = 0; i < 8; i++) {
			_Texture[i] = new mD3DTexture();
			Light[i] = new LIGHT();
		}
	}

	M3DM(int width, int height, float p_nplane, float p_fplane, M3DVECTOR cam_pos, M3DVECTOR cam_or, M3DVECTOR cam_up) {
		initialize(width, height, p_nplane, p_fplane, cam_pos, cam_or, cam_up);
	}

	void initializeGL() {
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glCullFace(GLES20.GL_BACK);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
	}

	private int loadGLShader(int type, String shaderCode) {
		// create a vertex shader type (GLES20.GL_VERTEX_SHADER)
		// or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
		int shader = GLES20.glCreateShader(type);

		if (shader != 0) {
			// add the source code to the shader and compile it
			GLES20.glShaderSource(shader, shaderCode);
			GLES20.glCompileShader(shader);

			int[] compiled = new int[1];
			GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
			if (compiled[0] == 0) {
				Log.e(TAG, "Could not compile shader " + type + ":");
				Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
				GLES20.glDeleteShader(shader);
				shader = 0;
			}
		}
		return shader;
	}

	public void DeleteProgram(int program) {
		GLES20.glDeleteProgram(program);

	}

	public int CompileProgram(String vsc, String fsc) {

		int vertexShader = loadGLShader(GLES20.GL_VERTEX_SHADER, vsc);
		int fragmentShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, fsc);

		int program = GLES20.glCreateProgram();             // create empty OpenGL Program
		GLES20.glAttachShader(program, vertexShader);   // add the vertex shader to program
		GLES20.glAttachShader(program, fragmentShader); // add the fragment shader to program
		GLES20.glLinkProgram(program);                  // creates OpenGL program executables

		return program;
	}


	void initialize(int width, int height, float p_nplane, float p_fplane, M3DVECTOR cam_pos, M3DVECTOR cam_or, M3DVECTOR cam_up) {
		SCREEN_WIDTH = width;
		SCREEN_HEIGHT = height;
		P_NPlane = p_nplane;
		P_FPlane = p_fplane;
		P_fov_horiz = ((float) (SCREEN_WIDTH) / (float) (SCREEN_HEIGHT)) * P_fov_vert;
		CameraPosition = cam_pos;
		CameraOrientation = cam_or;
		CameraUp = cam_up;

		Matrix.frustumM(projectionMatrix, 0, -P_NPlane * (float) (SCREEN_WIDTH) / (float) (SCREEN_HEIGHT) * (float) Math.tan(P_fov_vert / 2.0f), P_NPlane * (float) (SCREEN_WIDTH) / (float) (SCREEN_HEIGHT) * (float) Math.tan(P_fov_vert / 2.0f), -P_NPlane * (float) Math.tan(P_fov_vert / 2.0f), P_NPlane * (float) Math.tan(P_fov_vert / 2.0f), P_NPlane, P_FPlane);

	}

	/************************ mD3DMesh *****************************/
	static class mD3DMesh {
		char Name[] = new char[NAMELENGHT];
		int Flags;
		short Data[];
		int VertexStruct;
		int VertexSize; //dimension of vertex, number of floats
		int VertexCount;
		float Vertex[];
		int SrcBlend;
		int DestBlend;
		int Textures;
		int Program = -1;
		int newProgram = -1;
		mD3DTexture Texture[] = new mD3DTexture[8];
		int MatFlags; // TODO legacy
		M3DMATERIAL Material;
		float bumpLevel = 20.0f;
		HashMap<String, Float> customAttributes = new HashMap<String, Float>();
		float[] mMVPMatrixPrev = null;

		int Meshs;
		mD3DMesh Mesh[];

		// TODO legacy, used in c++
		void setFlags(int par) {
			Flags = par;
		}

		void addMesh(mD3DMesh msh) {
			Mesh[Meshs] = msh;
			Meshs++;
		}

		void removeMesh(mD3DMesh msh) {
			for (int A = Meshs - 1; A >= 0; A--) {
				if (msh == Mesh[A]) {
					for (int B = A; B < Meshs - 1; B++) {
						Mesh[B] = Mesh[B + 1];
					}
					Meshs--;
					return;
				}
			}
		}

		void setTexture(int N, mD3DTexture txt) {
			Texture[N] = txt;
		}

		void setMaterial(M3DMATERIAL mat) {
			Material = mat;
		}

		void setBumpLevel(float bump) {
			bumpLevel = bump;
		}

		void setCustomAttribute(String name, float value) {
			customAttributes.put(name, value);
		}

		void setVertex(int N, float handle[]) {
			for (int i = 0; i < VertexSize; i++) {
				Vertex[i] = handle[i];
			}
		}

		void setProgram(int p) {
			newProgram = p;
		}

		void flushProgram() {
			Program = newProgram;
		}

		int getProgram() {
			return Program;
		}

		int getNewProgram() {
			return newProgram;
		}

		float[] getVertex(int N) {
			float ret[] = new float[VertexSize];
			for (int i = 0; i < VertexSize; i++) {
				ret[i] = Vertex[i];
			}
			return ret;
		}

		// all normals are counted and than normalized
		void generateNormals() {
			int NVERTEX; // index of just modified vertex
			int i, j, k, n;
			// deletion of original normals
			for (i = 0; i < VertexCount; i++) {
				for (j = 3; j < 6; j++) {
					Vertex[i * VertexSize + j] = 0.0f;
				}
			}
			M3DVECTOR N, v0, v1, v2;
			v0 = new M3DVECTOR(0.0f, 0.0f, 0.0f); // required for compilation
			M3DVECTOR Norm[] = new M3DVECTOR[MAXNFACE];
			i = 0;
			float TT;
			while (Data[i] != 0) {
				for (j = 0; j < MAXNFACE; j++) {
					Norm[j] = new M3DVECTOR(0.0f, 0.0f, 0.0f);
				}
				k = i + 1;
				n = 0;
				for (j = 0; j < Data[i]; j++) {
					n++;
					if (n == 1) {
						NVERTEX = Data[i + 1] * VertexSize;
						v0 = new M3DVECTOR(Vertex[NVERTEX], Vertex[NVERTEX + 1], Vertex[NVERTEX + 2]);
					}
					if (n == 3) {
						n = 2;
						// get vertexs
						NVERTEX = Data[k - 1] * VertexSize;
						v1 = new M3DVECTOR(Vertex[NVERTEX], Vertex[NVERTEX + 1], Vertex[NVERTEX + 2]);
						NVERTEX = Data[k] * VertexSize;
						v2 = new M3DVECTOR(Vertex[NVERTEX], Vertex[NVERTEX + 1], Vertex[NVERTEX + 2]);
						N = M3DVECTOR.CrossProduct(M3DVECTOR.DIF(v1, v0), M3DVECTOR.DIF(v2, v0));
						TT = N.x * N.x + N.y * N.y + N.z * N.z;
						if (TT == 0.0) {
							N = new M3DVECTOR(0.0f, 0.0f, 0.0f);
						} else {
							N = M3DVECTOR.MUL(N, 1.0f / (float) Math.sqrt(TT));
						}
						Norm[0] = M3DVECTOR.ADD(Norm[0], N);
						Norm[j - 1] = M3DVECTOR.ADD(Norm[j - 1], N);
						Norm[j] = M3DVECTOR.ADD(Norm[j], N);
					}
					k++;
				}
				for (j = 1; j <= Data[i]; j++) {
					NVERTEX = Data[i + j] * VertexSize;
					Vertex[NVERTEX + 3] += Norm[j - 1].x;
					Vertex[NVERTEX + 4] += Norm[j - 1].y;
					Vertex[NVERTEX + 5] += Norm[j - 1].z;
				}
				i = k;
			}
			// normalization of normals
			i = 0;
			while (Data[i] != 0) {
				for (j = 1; j <= Data[i]; j++) {
					NVERTEX = Data[i + j] * VertexSize;
					Norm[0].x = Vertex[NVERTEX + 3];
					Norm[0].y = Vertex[NVERTEX + 4];
					Norm[0].z = Vertex[NVERTEX + 5];
					TT = Norm[0].x * Norm[0].x + Norm[0].y * Norm[0].y + Norm[0].z * Norm[0].z;
					if (TT == 0.0) {
						Norm[0] = new M3DVECTOR(0.0f, 0.0f, 0.0f);
					} else {
						Norm[0] = M3DVECTOR.MUL(Norm[0], 1.0f / (float) Math.sqrt(TT));
					}
					Vertex[NVERTEX + 3] = Norm[0].x;
					Vertex[NVERTEX + 4] = Norm[0].y;
					Vertex[NVERTEX + 5] = Norm[0].z;
				}
				i += Data[i] + 1;
			}
		}

		void releasemD3DMesh(int DeleteSubMeshes) {
			if (VertexCount > 0) {
				Data[0] = 0;
			}
			VertexCount = 0;
			if (DeleteSubMeshes == 1) {
				for (int i = 0; i < Meshs; i++) {
					Mesh[i].releasemD3DMesh(1);
				}
			}
			Meshs = 0;
		}

		mD3DMesh() {
			Flags = 0;
			Data = new short[1];
			Data[0] = 0;
			VertexStruct = 0;
			VertexSize = 0;
			VertexCount = 0;
			SrcBlend = 0;
			DestBlend = 0;
			Textures = 0;
			MatFlags = 0;
			Material = new M3DMATERIAL();
			Meshs = 0;
		}

		mD3DMesh(M3DVERTEX V[], short D[], int D_length) {
			int strsize = 8;
			VertexSize = strsize;
			Flags = 0;
			Textures = 0;
			int q;
			Meshs = 0;
			for (q = 0; q < NAMELENGHT; q++) {
				Name[q] = 0;
			}

			int j = 0;
			Data = new short[D_length];
			VertexCount = V.length;
			for (j = 0; j < D_length; j++) {
				Data[j] = D[j];
			}

			VertexStruct = M3DFVF_XYZ | M3DFVF_NORMAL | M3DFVF_TEX1;
			if (VertexCount > 0) {
				Vertex = new float[VertexCount * strsize];
				for (j = 0; j < VertexCount; j++) {
					Vertex[j * strsize] = V[j].P.x;
					Vertex[j * strsize + 1] = V[j].P.y;
					Vertex[j * strsize + 2] = V[j].P.z;
					Vertex[j * strsize + 3] = V[j].N.x;
					Vertex[j * strsize + 4] = V[j].N.y;
					Vertex[j * strsize + 5] = V[j].N.z;
					Vertex[j * strsize + 6] = V[j].u;
					Vertex[j * strsize + 7] = V[j].v;
				}
			}
			Material = new M3DMATERIAL();
		}
	}

	/***********************************************************/
	/************************** mD3DFrame ****************************/
	static class mD3DFrame {
		M3DVECTOR Position;
		M3DVECTOR Orientation;
		M3DVECTOR Up;
		M3DMATRIX world;
		int Frames;
		mD3DFrame Frame[] = new mD3DFrame[MAXFRAMES];
		int Meshs;
		mD3DMesh Mesh[] = new mD3DMesh[MAXMESHS];

		void setWorld(M3DVECTOR P, M3DVECTOR Or, M3DVECTOR U) {
			Position = P;
			Orientation = Or;
			Up = U;
			world = M3DMATRIX.CreateWorldMatrix(P, Or, U);
		}

		void setWorldM() {
			world = M3DMATRIX.CreateWorldMatrix(Position, Orientation, Up);
		}

		void addFrame(mD3DFrame Fr) {
			Frame[Frames] = Fr;
			Frames++;
		}

		void removeFrame(mD3DFrame Fr) {
			for (int A = Frames - 1; A >= 0; A--) {
				if (Fr == Frame[A]) {
					for (int B = A; B < Frames - 1; B++) {
						Frame[B] = Frame[B + 1];
					}
					Frames--;
					return;
				}
			}
		}

		void addMesh(mD3DMesh msh) {
			Mesh[Meshs] = msh;
			Meshs++;
		}

		void removeMesh(mD3DMesh msh) {
			for (int A = Meshs - 1; A >= 0; A--) {
				if (msh == Mesh[A]) {
					for (int B = A; B < Meshs - 1; B++) {
						Mesh[B] = Mesh[B + 1];
					}
					Meshs--;
					return;
				}
			}
		}

		void releasemD3DFrame(int DeleteSubMF) {
			int i;
			if (DeleteSubMF == 1) {
				for (i = 0; i < Frames; i++) {
					Frame[i].releasemD3DFrame(1);
				}
				Frames = 0;
				for (i = 0; i < Meshs; i++) {
					Mesh[i].releasemD3DMesh(1);
					Meshs = 0;
				}
			}
		}

		mD3DFrame() {
			Frames = 0;
			Meshs = 0;
			Position = new M3DVECTOR(0.0f, 0.0f, 0.0f);
			Orientation = new M3DVECTOR(0.0f, 0.0f, 1.0f);
			Up = new M3DVECTOR(0.0f, 1.0f, 0.0f);
			world = M3DMATRIX.CreateWorldMatrix(Position, Orientation, Up);
		}

		mD3DFrame(mD3DFrame Parent) {
			Frames = 0;
			Meshs = 0;
			Position = new M3DVECTOR(0.0f, 0.0f, 0.0f);
			Orientation = new M3DVECTOR(0.0f, 0.0f, 1.0f);
			Up = new M3DVECTOR(0.0f, 1.0f, 0.0f);
			world = M3DMATRIX.CreateWorldMatrix(Position, Orientation, Up);

			Parent.Frame[Parent.Frames] = this;
			Parent.Frames++;
		}

	}

	/****************************************************************/

	/*************************** RENDER ******************************/
	void setTexture(int stage, mD3DTexture TX) {
		_Texture[stage] = TX;
	}

	static class _VRX {
		int x, y;
		int z;
		int R, G, B, A;
		int u, v;

		public _VRX() {
			x = y = z = u = v = 0;
			R = G = B = A = 0;
		}
	}

	int sgn(float x) {
		if (x < 0.0) {
			return -1;
		} else if (x > 0.0) {
			return 1;
		}
		return 0;
	}

	int sgn(int x) {
		if (x < 0) {
			return -1;
		} else if (x > 0) {
			return 1;
		}
		return 0;
	}

	/*********************************************************/
	/************************ ZSORT **************************/

	private void _renderMesh_zsort(mD3DMesh mesh, M3DMATRIX world, int rendersubmesh) {
		if ((mesh.Flags & MD3DMESHF_DISABLED) == MD3DMESHF_DISABLED) {
			return;
		}

		if (mesh.Program <= 0) {
			return;
		}

		M3DVECTOR center = new M3DVECTOR(0.0f, 0.0f, 0.0f);
		M3DVECTOR transcenter; //transformed center in to world coor.
		transcenter = M3DMATRIX.VxM(center, world);
		transcenter = M3DMATRIX.VxM(transcenter, new M3DMATRIX(viewMatrix));

		ZSORTstruct zs = new ZSORTstruct();
		zs.Z = transcenter.z;
		zs.World = world;
		zs.hMesh = mesh;
		zsort.add(zs);

		/***********************************/
		// if sub-meshes shall be rendered
		if (rendersubmesh == 1) {
			for (int i = 0; i < mesh.Meshs; i++) {
				_renderMesh_zsort(mesh.Mesh[i], world, 1);
			}
		}
	}

	private void _renderFromBuff_zsort() {
		if (zsort != null) {
			Collections.sort(zsort);

			ListIterator<ZSORTstruct> iterator = zsort.listIterator();
			while (iterator.hasNext()) {
				ZSORTstruct zs = iterator.next();
				if ((zs.hMesh.Flags & MD3DMESHF_RENDEREDFIRST) == MD3DMESHF_RENDEREDFIRST) {
					_renderMesh(zs.hMesh, zs.World, 1);
				}
			}

			iterator = zsort.listIterator();
			while (iterator.hasNext()) {
				ZSORTstruct zs = iterator.next();
				if ((zs.hMesh.Flags & MD3DMESHF_RENDEREDFIRST) != MD3DMESHF_RENDEREDFIRST) {
					_renderMesh(zs.hMesh, zs.World, 1);
				}
			}
		}
	}

	/**************************************************************************/
	/**************************************************************************/

	private void _renderMesh(mD3DMesh mesh, M3DMATRIX world, int rendersubmesh) {
		if ((mesh.Flags & MD3DMESHF_DISABLED) == MD3DMESHF_DISABLED) {
			return;
		}

		if (mesh.Program == -1) {
			return;
		}

		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

		if ((mesh.Flags & MD3DMESHF_NOCULLING) == MD3DMESHF_NOCULLING) {
			GLES20.glDisable(GLES20.GL_CULL_FACE);
		} else if ((mesh.Flags & MD3DMESHF_FRONTCULLING) == MD3DMESHF_FRONTCULLING) {
			GLES20.glEnable(GLES20.GL_CULL_FACE);
			GLES20.glCullFace(GLES20.GL_FRONT);
		} else {
			GLES20.glEnable(GLES20.GL_CULL_FACE);
			GLES20.glCullFace(GLES20.GL_BACK);
		}

		// Apply a ModelView Projection transformation
		float[] mMVPMatrix = new float[16];
		float[] mMVMatrix = new float[16];
		float[] mVMatrix = new float[16];
		float[] mIMVMatrix = new float[16];        // inverse model view
		float[] mIMVPMatrix = new float[16];


		mVMatrix = viewMatrix;
		Matrix.multiplyMM(mMVMatrix, 0, viewMatrix, 0, world.values(), 0);
		Matrix.multiplyMM(mMVPMatrix, 0, projectionMatrix, 0, mMVMatrix, 0);
		Matrix.invertM(mIMVMatrix, 0, mMVMatrix, 0);
		Matrix.invertM(mIMVPMatrix, 0, mMVPMatrix, 0);

		if (mesh.mMVPMatrixPrev == null) {
			mesh.mMVPMatrixPrev = mMVPMatrix.clone();
		}
		float[] mMVPMatrixPrev = mesh.mMVPMatrixPrev.clone();
		mesh.mMVPMatrixPrev = mMVPMatrix.clone();

		// Add program to OpenGL environment
		if (GLES20.glIsProgram(mesh.Program) == false) {
			return;
		}
		GLES20.glUseProgram(mesh.Program);

		GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(mesh.Program, "uMVPMatrix"), 1, false, mMVPMatrix, 0);
		GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(mesh.Program, "uMVMatrix"), 1, false, mMVMatrix, 0);
		GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(mesh.Program, "uVMatrix"), 1, false, mVMatrix, 0);
		GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(mesh.Program, "uIMVMatrix"), 1, false, mIMVMatrix, 0);
		GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(mesh.Program, "uIMVPMatrix"), 1, false, mIMVPMatrix, 0);
		GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(mesh.Program, "uMVPMatrixPrev"), 1, false, mMVPMatrixPrev, 0);

		// light vector transformation into camera space
		float[] mTransformedLightVector = new float[4];
		Matrix.multiplyMV(mTransformedLightVector, 0, mVMatrix, 0, Light[0].Pos.values(), 0);


		int i;
		/* ALPHA */
		/* TEXTURES */
		/* MANAGMENT */
		/***********/

		if ((mesh.VertexCount > 0) && ((mesh.VertexStruct & M3DFVF_XYZ) == M3DFVF_XYZ)) {

			i = 0;

			// initialize vertex Buffer for triangle
			// TODO add possibility to have no texture U, V in vertex
			FloatBuffer vertexBuffer;
			ByteBuffer vbb = ByteBuffer.allocateDirect(mesh.Vertex.length * 4);
			vbb.order(ByteOrder.nativeOrder());// use the device hardware's native byte order
			vertexBuffer = vbb.asFloatBuffer();  // create a floating point buffer from the ByteBuffer
			vertexBuffer.put(mesh.Vertex);       // add the coordinates to the FloatBuffer

			vertexBuffer.position(0);
			GLES20.glEnableVertexAttribArray(GLES20.glGetAttribLocation(mesh.Program, "aPosition"));
			GLES20.glVertexAttribPointer(GLES20.glGetAttribLocation(mesh.Program, "aPosition"), 3, GLES20.GL_FLOAT, false, mesh.VertexSize * 4, vertexBuffer);

			vertexBuffer.position(3);
			GLES20.glEnableVertexAttribArray(GLES20.glGetAttribLocation(mesh.Program, "aNormal"));
			GLES20.glVertexAttribPointer(GLES20.glGetAttribLocation(mesh.Program, "aNormal"), 3, GLES20.GL_FLOAT, false, mesh.VertexSize * 4, vertexBuffer);

			vertexBuffer.position(6);
			GLES20.glEnableVertexAttribArray(GLES20.glGetAttribLocation(mesh.Program, "aTex"));
			GLES20.glVertexAttribPointer(GLES20.glGetAttribLocation(mesh.Program, "aTex"), 2, GLES20.GL_FLOAT, false, mesh.VertexSize * 4, vertexBuffer);

			// Light
			if (N_Lights > 0) {
				GLES20.glUniform4f(GLES20.glGetUniformLocation(mesh.Program, "uLightPos"), mTransformedLightVector[0], mTransformedLightVector[1], mTransformedLightVector[2], mTransformedLightVector[3]);

				GLES20.glUniform4f(GLES20.glGetUniformLocation(mesh.Program, "uLightAmbientColor"), Light[0].AR, Light[0].AG, Light[0].AB, Light[0].AA);
				GLES20.glUniform4f(GLES20.glGetUniformLocation(mesh.Program, "uLightDiffuseColor"), Light[0].DR, Light[0].DG, Light[0].DB, Light[0].DA);
				GLES20.glUniform4f(GLES20.glGetUniformLocation(mesh.Program, "uLightSpecularColor"), Light[0].SR, Light[0].SG, Light[0].SB, Light[0].SA);
				GLES20.glUniform1f(GLES20.glGetUniformLocation(mesh.Program, "uLightAttenuation"), Light[0].AT);
			}

			// Material
			GLES20.glUniform4f(GLES20.glGetUniformLocation(mesh.Program, "uMaterialAmbientColor"), mesh.Material.AR, mesh.Material.AG, mesh.Material.AB, mesh.Material.AA);
			GLES20.glUniform4f(GLES20.glGetUniformLocation(mesh.Program, "uMaterialDiffuseColor"), mesh.Material.DR, mesh.Material.DG, mesh.Material.DB, mesh.Material.DA);
			GLES20.glUniform4f(GLES20.glGetUniformLocation(mesh.Program, "uMaterialSpecularColor"), mesh.Material.SR, mesh.Material.SG, mesh.Material.SB, mesh.Material.SA);
			GLES20.glUniform4f(GLES20.glGetUniformLocation(mesh.Program, "uMaterialEmissiveColor"), mesh.Material.ER, mesh.Material.EG, mesh.Material.EB, mesh.Material.EA);
			GLES20.glUniform1f(GLES20.glGetUniformLocation(mesh.Program, "uMaterialShinnes"), mesh.Material.SH);

			// Bump level
			float bumpLevel = ((float) mesh.bumpLevel) / 100.0f;
			GLES20.glUniform1f(GLES20.glGetUniformLocation(mesh.Program, "uBumpLevel"), bumpLevel);

			// Custom attributes
			for (Map.Entry<String, Float> entry : mesh.customAttributes.entrySet()) {
				GLES20.glUniform1f(GLES20.glGetUniformLocation(mesh.Program, entry.getKey()), entry.getValue().floatValue());
			}

			// Texture mapping
			int texMapping = 0;
			GLES20.glUniform1i(GLES20.glGetUniformLocation(mesh.Program, "uTexMapping"), texMapping);

			// textures
			// TODO add multiple textures
			if (mesh.Textures == 1) {
				GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[0].id);
			} else if (mesh.Textures == 2) {
				GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[0].id);

				GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[1].id);

				// The {0,1} correspond to the activated textures units.
				int textureUnits[] = {0, 1};
				IntBuffer intBuffer = IntBuffer.wrap(textureUnits, 0, 2);
				GLES20.glUniform1iv(GLES20.glGetUniformLocation(mesh.Program, "uTextures"), 2, intBuffer);
			} else if (mesh.Textures == 3) {
				GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[0].id);

				GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[1].id);

				GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[2].id);

				// The {0,1} correspond to the activated textures units.
				int textureUnits[] = {0, 1, 2};
				IntBuffer intBuffer = IntBuffer.wrap(textureUnits, 0, 3);
				GLES20.glUniform1iv(GLES20.glGetUniformLocation(mesh.Program, "uTextures"), 3, intBuffer);
			} else if (mesh.Textures == 4) {
				GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[0].id);

				GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[1].id);

				GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[2].id);

				GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[3].id);

				// The {0,1} correspond to the activated textures units.
				int textureUnits[] = {0, 1, 2, 3};
				IntBuffer intBuffer = IntBuffer.wrap(textureUnits, 0, 4);
				GLES20.glUniform1iv(GLES20.glGetUniformLocation(mesh.Program, "uTextures"), 4, intBuffer);
			} else if (mesh.Textures == 5) {
				GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[0].id);

				GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[1].id);

				GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[2].id);

				GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[3].id);

				GLES20.glActiveTexture(GLES20.GL_TEXTURE4);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[4].id);

				// The {0,1} correspond to the activated textures units.
				int textureUnits[] = {0, 1, 2, 3, 4};
				IntBuffer intBuffer = IntBuffer.wrap(textureUnits, 0, 5);
				GLES20.glUniform1iv(GLES20.glGetUniformLocation(mesh.Program, "uTextures"), 5, intBuffer);
			} else if (mesh.Textures == 6) {
				GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[0].id);

				GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[1].id);

				GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[2].id);

				GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[3].id);

				GLES20.glActiveTexture(GLES20.GL_TEXTURE4);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[4].id);

				GLES20.glActiveTexture(GLES20.GL_TEXTURE5);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[5].id);

				// The {0,1} correspond to the activated textures units.
				int textureUnits[] = {0, 1, 2, 3, 4, 5};
				IntBuffer intBuffer = IntBuffer.wrap(textureUnits, 0, 6);
				GLES20.glUniform1iv(GLES20.glGetUniformLocation(mesh.Program, "uTextures"), 6, intBuffer);
			} else if (mesh.Textures == 7) {
				GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[0].id);

				GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[1].id);

				GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[2].id);

				GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[3].id);

				GLES20.glActiveTexture(GLES20.GL_TEXTURE4);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[4].id);

				GLES20.glActiveTexture(GLES20.GL_TEXTURE5);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[5].id);

				GLES20.glActiveTexture(GLES20.GL_TEXTURE6);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[6].id);

				// The {0,1} correspond to the activated textures units.
				int textureUnits[] = {0, 1, 2, 3, 4, 5, 6};
				IntBuffer intBuffer = IntBuffer.wrap(textureUnits, 0, 7);
				GLES20.glUniform1iv(GLES20.glGetUniformLocation(mesh.Program, "uTextures"), 7, intBuffer);
			}


			int mNumOfTriangleIndices = mesh.Data.length;
			ByteBuffer ibb = ByteBuffer.allocateDirect(mesh.Data.length * 2);
			ibb.order(ByteOrder.nativeOrder());
			ShortBuffer indexBuffer = ibb.asShortBuffer();
			indexBuffer.put(mesh.Data);
			indexBuffer.position(0);

			GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, mNumOfTriangleIndices, GLES20.GL_UNSIGNED_SHORT, indexBuffer);


		}

		GLES20.glUseProgram(0);


		/***********************************/
		// if sub-meshes shall be rendered
		if (rendersubmesh == 1) {
			for (i = 0; i < mesh.Meshs; i++) {
				_renderMesh(mesh.Mesh[i], world, 1);
			}
		}
	}

	/**************************************************/
	/**************************************************/
	private void _renderFrame(mD3DFrame frame, M3DMATRIX world_up) {
		M3DMATRIX actual = M3DMATRIX.MUL(frame.world, world_up);
		int i;
		for (i = 0; i < frame.Meshs; i++) {
			if (ZSORT == 1) {
				_renderMesh_zsort(frame.Mesh[i], actual, 1);
			} else {
				_renderMesh(frame.Mesh[i], actual, 1);
			}
		}
		for (i = 0; i < frame.Frames; i++) {
			_renderFrame(frame.Frame[i], actual);
		}
	}

	void renderFrame(mD3DFrame frame) {

		Matrix.setLookAtM(viewMatrix, 0, CameraPosition.x, CameraPosition.y, CameraPosition.z, CameraPosition.x + CameraOrientation.x, CameraPosition.y + CameraOrientation.y, CameraPosition.z + CameraOrientation.z, CameraUp.x, CameraUp.y, CameraUp.z);

		if (ZSORT == 1) {
			zsort.clear();
		}
		_renderFrame(frame, M3DMATRIX.IdentityMatrix());
		if (ZSORT == 1) {
			_renderFromBuff_zsort();
		}
	}

	/*************** OBJECTS ****************************/
	/****************** SPHERE ********************/
	static mD3DMesh createSphere(float R, int StepRov, int StepPol, float U1, float V1, float U2, float V2) {
		mD3DMesh mesh;
		float xU = U2 - U1;
		float xV = V2 - V1;

		int VNum = (1 + 180 / StepRov) * (1 + 360 / StepPol) + 1;
		M3DVERTEX vertex[] = new M3DVERTEX[VNum];
		for (int t = 0; t < VNum; t++) {
			vertex[t] = new M3DVERTEX();
		}
		short data[] = new short[VNum * 5];
		for (int t = 0; t < VNum * 5; t++) {
			data[t] = 0;
		}

		int NRovnobeziek = (180 / StepRov) + 1;
		int NPoludnikov = (360 / StepPol) + 1;
		float al, be;
		int i, j, count = 0;
		M3DVECTOR temp = new M3DVECTOR(0.0f, 1.0f, 0.0f), T = new M3DVECTOR(0.0f, 0.0f, 0.0f), axis = new M3DVECTOR(0.0f, 0.0f, 0.0f), t_axis = new M3DVECTOR(
				1.0f, 0.0f, 0.0f);
		for (al = 0; al <= 360; al += StepPol) {
			axis = M3DVECTOR.POINTROTATE(t_axis, new M3DVECTOR(0.0f, 0.0f, 0.0f), new M3DVECTOR(0.0f, 1.0f, 0.0f), (float) (al * (PI / 180.0f)));
			for (be = 0; be <= 180; be += StepRov) {
				T = M3DVECTOR.POINTROTATE(temp, new M3DVECTOR(0.0f, 0.0f, 0.0f), axis, (float) (be * (PI / 180.0f)));
				vertex[count].P.x = T.x * R;
				vertex[count].P.y = T.y * R;
				vertex[count].P.z = T.z * R;
				vertex[count].N.x = T.x;
				vertex[count].N.y = T.y;
				vertex[count].N.z = T.z;
				vertex[count].u = U1 + (((float) al) / 360.0f) * xU;
				vertex[count].v = V1 + (((float) be) / 180.0f) * xV;
				count++;
			}
		}
		count = 0;
		for (j = 0; j < NPoludnikov - 1; j++) {
			for (i = 0; i < NRovnobeziek - 1; i++) {
				data[count + 0] = (short) (i + (j + 1) * NRovnobeziek);
				data[count + 1] = (short) (i + (j) * NRovnobeziek);
				count += 2;

				if (i == NRovnobeziek - 2) {
					data[count + 0] = (short) (i + (NPoludnikov - 2) * NRovnobeziek);
					data[count + 1] = (short) (i + (NPoludnikov - 2) * NRovnobeziek);
					count += 2;
				}
			}
		}
		mesh = new mD3DMesh(vertex, data, count);

		return mesh;
	}

	/****************** SPHERE ********************/

	/****************** SPHERE ********************/
	static mD3DMesh createEllipsoid(float R, float V, int StepRov, int StepPol, float U1, float V1, float U2, float V2) {
		mD3DMesh mesh;
		float xU = U2 - U1;
		float xV = V2 - V1;

		int VNum = (1 + 180 / StepRov) * (1 + 360 / StepPol) + 1;
		M3DVERTEX vertex[] = new M3DVERTEX[VNum];
		for (int t = 0; t < VNum; t++) {
			vertex[t] = new M3DVERTEX();
		}
		short data[] = new short[VNum * 5];
		for (int t = 0; t < VNum * 5; t++) {
			data[t] = 0;
		}

		int NRovnobeziek = (180 / StepRov) + 1;
		int NPoludnikov = (360 / StepPol) + 1;
		float al, be;
		int i, j, count = 0;
		M3DVECTOR temp = new M3DVECTOR(0.0f, 1.0f, 0.0f), T = new M3DVECTOR(0.0f, 0.0f, 0.0f), axis = new M3DVECTOR(0.0f, 0.0f, 0.0f), t_axis = new M3DVECTOR(
				1.0f, 0.0f, 0.0f);
		for (al = 0; al <= 360; al += StepPol) {
			axis = M3DVECTOR.POINTROTATE(t_axis, new M3DVECTOR(0.0f, 0.0f, 0.0f), new M3DVECTOR(0.0f, 1.0f, 0.0f), (float) (al * (PI / 180.0f)));
			for (be = 0; be <= 180; be += StepRov) {
				T = M3DVECTOR.POINTROTATE(temp, new M3DVECTOR(0.0f, 0.0f, 0.0f), axis, (float) (be * (PI / 180.0f)));
				vertex[count].P.x = T.x * R;
				vertex[count].P.y = T.y * V;
				vertex[count].P.z = T.z * R;
				vertex[count].N.x = vertex[count].P.x;
				vertex[count].N.y = vertex[count].P.y;
				vertex[count].N.z = vertex[count].P.z;
				vertex[count].N = M3DVECTOR.Normalize(vertex[count].N);
				double u = (double) U1 + (((float) al) / 360.0f) * xU;
				double v = (double) V1 + (((float) be) / 180.0f) * xV;

				vertex[count].u = (float) u;
				vertex[count].v = (float) v;

				count++;
			}
		}
		count = 0;
		i = 0;
		j = 0;

		for (j = 0; j < NPoludnikov - 1; j++) {
			for (i = 0; i < NRovnobeziek - 1; i++) {

				data[count + 0] = (short) (i + (j + 1) * NRovnobeziek);
				data[count + 1] = (short) (i + (j) * NRovnobeziek);
				count += 2;

				if (i == NRovnobeziek - 2) {
					data[count + 0] = (short) (i + (NPoludnikov - 2) * NRovnobeziek);
					data[count + 1] = (short) (i + (NPoludnikov - 2) * NRovnobeziek);
					count += 2;
				}
			}

		}
		mesh = new mD3DMesh(vertex, data, count);

		return mesh;
	}

	/****************** ELLIPSOID ********************/

	static mD3DMesh createCube(float a) {
		mD3DMesh mesh;

		M3DVERTEX vertex[] = new M3DVERTEX[24];
		short data[] = new short[4]; //short[31];

		// front
		vertex[0] = new M3DVERTEX();
		vertex[0].P.x = -a;
		vertex[0].P.y = -a;
		vertex[0].P.z = a;
		vertex[0].u = 0.0f;
		vertex[0].v = 1.0f;

		vertex[1] = new M3DVERTEX();
		vertex[1].P.x = -a;
		vertex[1].P.y = a;
		vertex[1].P.z = a;
		vertex[1].u = 0.0f;
		vertex[1].v = 0.0f;

		vertex[2] = new M3DVERTEX();
		vertex[2].P.x = a;
		vertex[2].P.y = a;
		vertex[2].P.z = a;
		vertex[2].u = 1.0f;
		vertex[2].v = 0.0f;

		vertex[3] = new M3DVERTEX();
		vertex[3].P.x = a;
		vertex[3].P.y = -a;
		vertex[3].P.z = a;
		vertex[3].u = 1.0f;
		vertex[3].v = 1.0f;

		// right
		vertex[4] = new M3DVERTEX();
		vertex[4].P.x = a;
		vertex[4].P.y = -a;
		vertex[4].P.z = a;
		vertex[4].u = 0.0f;
		vertex[4].v = 1.0f;

		vertex[5] = new M3DVERTEX();
		vertex[5].P.x = a;
		vertex[5].P.y = a;
		vertex[5].P.z = a;
		vertex[5].u = 0.0f;
		vertex[5].v = 0.0f;

		vertex[6] = new M3DVERTEX();
		vertex[6].P.x = a;
		vertex[6].P.y = a;
		vertex[6].P.z = -a;
		vertex[6].u = 1.0f;
		vertex[6].v = 0.0f;

		vertex[7] = new M3DVERTEX();
		vertex[7].P.x = a;
		vertex[7].P.y = -a;
		vertex[7].P.z = -a;
		vertex[7].u = 1.0f;
		vertex[7].v = 1.0f;

		// back
		vertex[8] = new M3DVERTEX();
		vertex[8].P.x = -a;
		vertex[8].P.y = a;
		vertex[8].P.z = 0.0f;
		vertex[8].u = 0.0f;
		vertex[8].v = 1.0f;

		vertex[9] = new M3DVERTEX();
		vertex[9].P.x = -a;
		vertex[9].P.y = -a;
		vertex[9].P.z = 0.0f;
		vertex[9].u = 0.0f;
		vertex[9].v = 0.0f;

		vertex[10] = new M3DVERTEX();
		vertex[10].P.x = a;
		vertex[10].P.y = -a;
		vertex[10].P.z = 0.0f;
		vertex[10].u = 1.0f;
		vertex[10].v = 0.0f;

		vertex[11] = new M3DVERTEX();
		vertex[11].P.x = a;
		vertex[11].P.y = a;
		vertex[11].P.z = 0.0f;
		vertex[11].u = 1.0f;
		vertex[11].v = 1.0f;

		// left
		vertex[12] = new M3DVERTEX();
		vertex[12].P.x = -a;
		vertex[12].P.y = -a;
		vertex[12].P.z = -a;
		vertex[12].u = 0.0f;
		vertex[12].v = 1.0f;

		vertex[13] = new M3DVERTEX();
		vertex[13].P.x = -a;
		vertex[13].P.y = a;
		vertex[13].P.z = -a;
		vertex[13].u = 0.0f;
		vertex[13].v = 0.0f;

		vertex[14] = new M3DVERTEX();
		vertex[14].P.x = -a;
		vertex[14].P.y = a;
		vertex[14].P.z = a;
		vertex[14].u = 1.0f;
		vertex[14].v = 0.0f;

		vertex[15] = new M3DVERTEX();
		vertex[15].P.x = -a;
		vertex[15].P.y = -a;
		vertex[15].P.z = a;
		vertex[15].u = 1.0f;
		vertex[15].v = 1.0f;

		// top
		vertex[16] = new M3DVERTEX();
		vertex[16].P.x = -a;
		vertex[16].P.y = a;
		vertex[16].P.z = a;
		vertex[16].u = 0.0f;
		vertex[16].v = 1.0f;

		vertex[17] = new M3DVERTEX();
		vertex[17].P.x = -a;
		vertex[17].P.y = a;
		vertex[17].P.z = -a;
		vertex[17].u = 0.0f;
		vertex[17].v = 0.0f;

		vertex[18] = new M3DVERTEX();
		vertex[18].P.x = a;
		vertex[18].P.y = a;
		vertex[18].P.z = -a;
		vertex[18].u = 1.0f;
		vertex[18].v = 0.0f;

		vertex[19] = new M3DVERTEX();
		vertex[19].P.x = a;
		vertex[19].P.y = a;
		vertex[19].P.z = a;
		vertex[19].u = 1.0f;
		vertex[19].v = 1.0f;

		// bottom
		vertex[20] = new M3DVERTEX();
		vertex[20].P.x = -a;
		vertex[20].P.y = -a;
		vertex[20].P.z = -a;
		vertex[20].u = 1.0f;
		vertex[20].v = 0.0f;

		vertex[21] = new M3DVERTEX();
		vertex[21].P.x = -a;
		vertex[21].P.y = -a;
		vertex[21].P.z = a;
		vertex[21].u = 1.0f;
		vertex[21].v = 1.0f;

		vertex[22] = new M3DVERTEX();
		vertex[22].P.x = a;
		vertex[22].P.y = -a;
		vertex[22].P.z = a;
		vertex[22].u = 0.0f;
		vertex[22].v = 1.0f;

		vertex[23] = new M3DVERTEX();
		vertex[23].P.x = a;
		vertex[23].P.y = -a;
		vertex[23].P.z = -a;
		vertex[23].u = 0.0f;
		vertex[23].v = 0.0f;

		data[0] = 8;
		data[1] = 9;
		data[2] = 11;
		data[3] = 10;


		for (int i = 0; i < vertex.length; i++) {
			vertex[i].N = M3DVECTOR.Normalize(vertex[i].P);
		}

		mesh = new mD3DMesh(vertex, data, 4);

		return mesh;
	}


	static mD3DMesh createRingOneSidedTop(float R1, float R2, int Step) {
		mD3DMesh mesh;

		int VNum = 2 * (360 / Step) + 2;
		M3DVERTEX vertex[] = new M3DVERTEX[VNum];
		for (int t = 0; t < VNum; t++) {
			vertex[t] = new M3DVERTEX();
		}
		short data[] = new short[VNum * 5];
		for (int t = 0; t < VNum * 5; t++) {
			data[t] = 0;
		}

		float al, be;
		int i, j, count = 0;
		M3DVECTOR temp = new M3DVECTOR(0.0f, 1.0f, 0.0f), T = new M3DVECTOR(0.0f, 0.0f, 0.0f), axis = new M3DVECTOR(0.0f, 0.0f, 0.0f), t_axis = new M3DVECTOR(1.0f, 0.0f, 0.0f);
		for (al = 0; al <= 360; al += Step) {
			T = M3DVECTOR.POINTROTATE(t_axis, new M3DVECTOR(0.0f, 0.0f, 0.0f), new M3DVECTOR(0.0f, 1.0f, 0.0f), (float) (al * (PI / 180.0f)));

			vertex[count].P.x = T.x * R1;
			vertex[count].P.y = 0.0f;
			vertex[count].P.z = T.z * R1;
			vertex[count].N.x = 0.0f;
			vertex[count].N.y = 1.0f;
			vertex[count].N.z = 0.0f;
			vertex[count].N = M3DVECTOR.Normalize(vertex[count].N);

			vertex[count].u = 0.0f;
			vertex[count].v = 0.0f;

			count++;

			vertex[count].P.x = T.x * R2;
			vertex[count].P.y = 0.0f;
			vertex[count].P.z = T.z * R2;
			vertex[count].N.x = 0.0f;
			vertex[count].N.y = 1.0f;
			vertex[count].N.z = 0.0f;
			vertex[count].N = M3DVECTOR.Normalize(vertex[count].N);

			vertex[count].u = 1.0f;
			vertex[count].v = 0.0f;

			count++;
		}

		count = 0;
		for (al = 0; al <= 360 - Step; al += Step) {

			data[count + 3] = (short) (((int) (count / 4)) * 2 + 1);
			data[count + 1] = (short) (((int) (count / 4)) * 2 + 0);
			data[count + 2] = (short) (((int) (count / 4)) * 2 + 2);
			data[count + 0] = (short) (((int) (count / 4)) * 2 + 3);
			count += 4;

		}

		mesh = new mD3DMesh(vertex, data, count);

		return mesh;
	}

	static mD3DMesh createRingOneSidedBottom(float R1, float R2, int Step) {
		mD3DMesh mesh;

		int VNum = 2 * (360 / Step) + 2;
		M3DVERTEX vertex[] = new M3DVERTEX[VNum];
		for (int t = 0; t < VNum; t++) {
			vertex[t] = new M3DVERTEX();
		}
		short data[] = new short[VNum * 5];
		for (int t = 0; t < VNum * 5; t++) {
			data[t] = 0;
		}

		float al, be;
		int i, j, count = 0;
		M3DVECTOR temp = new M3DVECTOR(0.0f, 1.0f, 0.0f), T = new M3DVECTOR(0.0f, 0.0f, 0.0f), axis = new M3DVECTOR(0.0f, 0.0f, 0.0f), t_axis = new M3DVECTOR(1.0f, 0.0f, 0.0f);
		for (al = 0; al <= 360; al += Step) {
			T = M3DVECTOR.POINTROTATE(t_axis, new M3DVECTOR(0.0f, 0.0f, 0.0f), new M3DVECTOR(0.0f, 1.0f, 0.0f), (float) (al * (PI / 180.0f)));

			vertex[count].P.x = T.x * R1;
			vertex[count].P.y = 0.0f;
			vertex[count].P.z = T.z * R1;
			vertex[count].N.x = 0.0f;
			vertex[count].N.y = -1.0f;
			vertex[count].N.z = 0.0f;
			vertex[count].N = M3DVECTOR.Normalize(vertex[count].N);

			vertex[count].u = 0.0f;
			vertex[count].v = 0.0f;

			count++;

			vertex[count].P.x = T.x * R2;
			vertex[count].P.y = 0.0f;
			vertex[count].P.z = T.z * R2;
			vertex[count].N.x = 0.0f;
			vertex[count].N.y = -1.0f;
			vertex[count].N.z = 0.0f;
			vertex[count].N = M3DVECTOR.Normalize(vertex[count].N);

			vertex[count].u = 1.0f;
			vertex[count].v = 0.0f;

			count++;
		}

		count = 0;
		for (al = 0; al <= 360 - Step; al += Step) {

			data[count + 3] = (short) (((int) (count / 4)) * 2 + 0);
			data[count + 1] = (short) (((int) (count / 4)) * 2 + 1);
			data[count + 2] = (short) (((int) (count / 4)) * 2 + 3);
			data[count + 0] = (short) (((int) (count / 4)) * 2 + 2);
			count += 4;

		}

		mesh = new mD3DMesh(vertex, data, count);

		return mesh;
	}

	// Method calculates the x, y position on screen into line in 3D space. Set as output POINT and VECT
	void getLinefPixel(M3DVECTOR POINT, M3DVECTOR VECT, float Xx, float Yy) {
		M3DVECTOR right = M3DVECTOR.CrossProduct(CameraOrientation, CameraUp);
		right = M3DVECTOR.Normalize(right);
		M3DVECTOR up = M3DVECTOR.CrossProduct(right, CameraOrientation);
		// assumption is that camera vector is normalized
		// up=Normalize(up);
		M3DVECTOR or = CameraOrientation;
		// M3DVECTOR or=Normalize(CameraOrientation);
		float WIDTH, HEIGHT, w, h;
		WIDTH = P_NPlane * (float) (Math.tan((P_fov_horiz / 2.0f)));
		HEIGHT = P_NPlane * (float) (Math.tan((P_fov_vert / 2.0f)));
		w = WIDTH * (float) ((Xx / (float) (SCREEN_WIDTH)) * 2.0f - 1.0f);
		h = HEIGHT * (float) (((float) (SCREEN_HEIGHT - Yy) / (float) (SCREEN_HEIGHT)) * 2.0f - 1.0f);
		right = M3DVECTOR.MUL(right, w);
		up = M3DVECTOR.MUL(up, h);
		or = M3DVECTOR.MUL(or, P_NPlane);
		M3DVECTOR f, dir;
		f = M3DVECTOR.ADD(M3DVECTOR.ADD(M3DVECTOR.ADD(CameraPosition, or), up), right);
		dir = M3DVECTOR.DIF(f, CameraPosition);
		POINT.set(f);
		VECT.set(dir);
	}
}