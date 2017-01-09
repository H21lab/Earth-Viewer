/*
 * Shaders class
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

public class Shaders {


	public static int p_meteosat_0;
	
	/* OpenGL */
	// TODO only for 1 light and texture required
	public static final String vsc_meteosat_0 = 
			"uniform mat4 uMVPMatrix;   \n" +
			"uniform mat4 uMVMatrix;   	\n" +
			"uniform mat4 uVMatrix;   	\n" +
			"varying mat4 vMVMatrix;   	\n" +
			"uniform mat4 uIMVMatrix;	\n" +
			
			"attribute vec4 aPosition; 	\n" +
			"attribute vec3 aNormal;  	\n" +
			"attribute vec2 aTex;		\n" +
			
			"varying vec3 vPosition;	\n" +
			"varying vec3 vNormal;		\n" + 
			"varying vec3 vMVNormal;		\n" + 
			"varying vec2 vTex;			\n" +
			"varying vec2 vTexM;		\n" +
			
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
			
			
			" 	vTexM[0] = 1.0 - (vPosition.x+1.0)/2.0; \n" +
			" 	vTexM[1] = 1.0 - (vPosition.y+1.0)/2.0; \n" +

			"   float s = sqrt(vPosition.x*vPosition.x + vPosition.y*vPosition.y); \n" +
			"   float sx = sqrt(vPosition.x*vPosition.x); \n" +
			"   float sy = sqrt(vPosition.y*vPosition.y); \n" +
			" 	vTexM[0] = 0.5 + (vTexM[0] - 0.5)*(1.0 - 0.16*pow(s,1.8)); \n" +
			" 	vTexM[1] = 0.5 + (vTexM[1] - 0.5)*(1.0 - 0.16*pow(s,1.8)); \n" +
			" 	vTexM[0] = -0.1 + (1.0 + 0.2 )*vTexM[0]; \n" +
			" 	vTexM[1] = -0.09 + (1.0 + 0.18 )*vTexM[1]; \n" +
			
			"}                         \n";
	    
	public static final String fsc_meteosat_0 = 
		 	"precision mediump float;  	\n" +
			"varying vec3 vLightPos;  	\n" +
			"varying vec3 vPosition;	\n" +     // Interpolated position for this fragment.
			"varying vec3 lightVector;	\n" +
			"uniform vec4 uColor;  		\n" +     // This is the color from the vertex shader interpolated across the triangle per fragment.
			"varying vec3 vNormal;   	\n" +     // Interpolated normal for this fragment.
			"varying vec3 vMVNormal;	\n" + 
			"varying vec2 vTex;			\n" +
			"varying vec2 vTexM;		\n" +
	        "uniform sampler2D uTextures[7];\n" +
	        "varying vec3 vEye	;   	\n" +
	        "varying vec2 shiftUV;		\n" +	  // clouds shadow shift
	        
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
			
			"uniform float uTW1;					\n" +		// cloud map texture weight
			"uniform float uTW2;					\n" +		// cloud map texture weight
			"uniform float uTW3;					\n" +		// cloud map texture weight
			
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

				// diffuse
			"    float diffuse = max(dot(normal, lightVector), 0.0);   \n" +

				// diffuse 2
			"    float diffuse2 = max(dot(normal2, lightVector), 0.0);   \n" +
		
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

			
			// text in the bottom
			" 	vec2 _vTexM =  vTexM; \n" +
			" if (vPosition.y < -0.92) { \n" +
			" 	_vTexM[1] =  0.05 + (0.92 - (vPosition.y+0.92)) ; \n" +
			" 	_vTexM[0] =  1.5*(-0.37 + (1.0 - (vPosition.x+1.0)/2.0)) ; \n" +
			" } \n" +
			
			" vec4 cmT = 0.2*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM)) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM)) ) "
			+ "			+ 0.4*( uTW1*(texture2D(uTextures[2], _vTexM)) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM)) ) "
			+ "			+ 0.4*( uTW2*(texture2D(uTextures[4], _vTexM))						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM)) ) "
			+ "; \n" +
			
			" vec4 cmTS = 0.2*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV)) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV)) ) "
			+ "			+ 0.4*( uTW1*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV)) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM + 0.001*shiftUV)) ) "
			+ "			+ 0.4*( uTW2*(texture2D(uTextures[4], _vTexM + 0.001*shiftUV))						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV)) ) "
			+ "; \n" +
			
			" float cm = (cmT.r + cmT.g + cmT.b)/3.0; \n" +
			" cm = 0.3 + 0.7*cm; \n" +

			
			" float cmS = (cmTS.r + cmTS.g + cmTS.b)/3.0; \n" +
			" cmS = 0.0 + 1.0*cmS; \n" +

			
			" if (vPosition.z > 0.0) { \n" +
			" 	cmT = vec4(0.0,0.0,0.0,1.0); \n" +
			" 	cm = 0.0; \n" +
			" 	cmS = 0.0; \n" +
			" } \n" +

			
	
			
			" 		float d = (1.0 - texture2D(uTextures[1], vTex).r)*(1.0 - texture2D(uTextures[1], vTex).g)*texture2D(uTextures[1], vTex).b; \n" +
			// cloudmap
			"		gl_FragColor = 1.0*cm*cmT*( 0.4 + 1.0*uLightAmbientColor + 0.9*uLightDiffuseColor * diffuse2 + 0.1*uLightSpecularColor * specC ) "
			// clouds shadow
			+ "						+ clamp(-1.0*vec4(0.4, 0.4, 0.4, 1.0)*cmS "
			// texture
			+ "						+ clamp(1.0 - 0.5*cm, 0.0, 1.0)*texture2D(uTextures[1], vTex) * (uMaterialEmissiveColor + uLightAmbientColor * uMaterialAmbientColor + uLightDiffuseColor * uMaterialDiffuseColor * diffuse  + 0.3*uLightSpecularColor * specW )     "
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
			" gl_FragColor = clamp(gl_FragColor  + 0.1 * uLightSpecularColor * (a) * (sp*sp * 0.5), 0.0, 1.0);	\n" + 
	
			" a = pow(abs(1.0 - vMVNormal.z), 4.0); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 7.0*vec4(1.0, 1.0, 1.0, 1.0) * uLightSpecularColor  * (a) * (diffuse * 5.0), 0.0, 1.0);	\n" + 
			
			" a = pow(abs(1.0 - vMVNormal.z), 0.5); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 0.2*vec4(0.3, 0.6, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" + 

			
			" a = pow(abs(1.0 - vMVNormal.z), 30.0); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 20.0*vec4(0.0, 0.4, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +

			" gl_FragColor.a = 1.0;	\n" +
			" if (abs(vMVNormal.z) < 0.55) { \n" +
			" 	gl_FragColor = clamp(gl_FragColor * pow(abs(vMVNormal.z)/0.55 - 0.0, 5.0), 0.0, 1.0);	\n" + 
			" }; \n" +
	

			"}   \n";
	

	public static int p_meteosat_0_hd;
	
	/* OpenGL */
	// TODO only for 1 light and texture required
	public static final String vsc_meteosat_0_hd = 
			"uniform mat4 uMVPMatrix;   \n" +
			"uniform mat4 uMVMatrix;   	\n" +
			"uniform mat4 uVMatrix;   	\n" +
			"varying mat4 vMVMatrix;   	\n" +
			"uniform mat4 uIMVMatrix;	\n" +
			
			"attribute vec4 aPosition; 	\n" +
			"attribute vec3 aNormal;  	\n" +
			"attribute vec2 aTex;		\n" +
			
			"varying vec3 vPosition;	\n" +
			"varying vec3 vNormal;		\n" + 
			"varying vec3 vMVNormal;		\n" + 
			"varying vec2 vTex;			\n" +
			"varying vec2 vTexM;		\n" +
			
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
			
			
			" 	vTexM[0] = 1.0 - (vPosition.x+1.0)/2.0; \n" +
			" 	vTexM[1] = 1.0 - (vPosition.y+1.0)/2.0; \n" +
			"   float s = sqrt(vPosition.x*vPosition.x + vPosition.y*vPosition.y); \n" +
			"   float sx = sqrt(vPosition.x*vPosition.x); \n" +
			"   float sy = sqrt(vPosition.y*vPosition.y); \n" +
			" 	vTexM[0] = 0.5 + (vTexM[0] - 0.5)*(1.0 - 0.16*pow(s,1.8)); \n" +
			" 	vTexM[1] = 0.5 + (vTexM[1] - 0.5)*(1.0 - 0.16*pow(s,1.8)); \n" +
			" 	vTexM[0] = -0.1 + (1.0 + 0.2 )*vTexM[0]; \n" +
			" 	vTexM[1] = -0.09 + (1.0 + 0.18 )*vTexM[1]; \n" +
			

			"}                         \n";
	    
	public static final String fsc_meteosat_0_hd = 
		 	"precision mediump float;  	\n" +
			"varying vec3 vLightPos;  	\n" +
			"varying vec3 vPosition;	\n" +     // Interpolated position for this fragment.
			"varying vec3 lightVector;	\n" +
			"uniform vec4 uColor;  		\n" +     // This is the color from the vertex shader interpolated across the triangle per fragment.
			"varying vec3 vNormal;   	\n" +     // Interpolated normal for this fragment.
			"varying vec3 vMVNormal;	\n" + 
			"varying vec2 vTex;			\n" +
			"varying vec2 vTexM;		\n" +
	        "uniform sampler2D uTextures[7];\n" +
	        "varying vec3 vEye	;   	\n" +
	        "varying vec2 shiftUV;		\n" +	  // clouds shadow shift
	        
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
			
			"uniform float uTW1;					\n" +		// cloud map texture weight
			"uniform float uTW2;					\n" +		// cloud map texture weight
			"uniform float uTW3;					\n" +		// cloud map texture weight
			
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

				// diffuse
			"    float diffuse = max(dot(normal, lightVector), 0.0);   \n" +
			
				// diffuse 2
			"    float diffuse2 = max(dot(normal2, lightVector), 0.0);   \n" +
		
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

			
			// text in the bottom
			" 	vec2 _vTexM =  vTexM; \n" +
			" if (vPosition.y < -0.92) { \n" +
			" 	_vTexM[1] =  0.05 + (0.92 - (vPosition.y+0.92)) ; \n" +
			" 	_vTexM[0] =  0.3*(-0.37 + (1.0 - (vPosition.x+1.0)/2.0)) ; \n" +
			" } \n" +

			" vec4 cmT = 1.0*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM)) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM)) ) "
			+ "			+ 0.0*( uTW1*(texture2D(uTextures[2], _vTexM)) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM)) ) "
			+ "			+ 0.0*( uTW2*(texture2D(uTextures[4], _vTexM))						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM)) ) "
			+ "; \n" +
			
			" vec4 cmTS = 1.0*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV)) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV)) ) "
			+ "			+ 0.0*( uTW1*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV)) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM + 0.001*shiftUV)) ) "
			+ "			+ 0.0*( uTW2*(texture2D(uTextures[4], _vTexM + 0.001*shiftUV))						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV)) ) "
			+ "; \n" +
			
			" float cm = (cmT.r + cmT.g + cmT.b)/3.0; \n" +
			" cm = 0.3 + 0.7*cm; \n" +
			
			
			" float cmS = (cmTS.r + cmTS.g + cmTS.b)/3.0; \n" +
			" cmS = 0.0 + 1.0*cmS; \n" +

			
			" if (vPosition.z > 0.0) { \n" +
			" 	cmT = vec4(0.0,0.0,0.0,1.0); \n" +
			" 	cm = 0.0; \n" +
			" 	cmS = 0.0; \n" +
			" } \n" +

			
	
			
			" 		float d = (1.0 - texture2D(uTextures[1], vTex).r)*(1.0 - texture2D(uTextures[1], vTex).g)*texture2D(uTextures[1], vTex).b; \n" +
			// cloudmap
			"		gl_FragColor = 1.0*cm*cmT*( 0.4 + 1.0*uLightAmbientColor + 0.9*uLightDiffuseColor * diffuse2 + 0.1*uLightSpecularColor * specC ) "
			// clouds shadow
			+ "						+ clamp(-1.0*vec4(0.4, 0.4, 0.4, 1.0)*cmS "
			// texture
			+ "						+ clamp(1.0 - 0.5*cm, 0.0, 1.0)*texture2D(uTextures[1], vTex) * (uMaterialEmissiveColor + uLightAmbientColor * uMaterialAmbientColor + uLightDiffuseColor * uMaterialDiffuseColor * diffuse  + 0.3*uLightSpecularColor * specW )     "
			// specular light on watter
			+ "						+ 5.0*d*(1.0 - 1.0*cm)*1.0*vec4(1.0, 1.0, 1.0, 1.0)*uLightSpecularColor * specW , 0.0, 1.0)  ;   \n" +


			
			// night light
			//" if (diffuse < 2.0) { \n" +
			"	 diffuse = clamp(diffuse, 0.0, 1.0); \n" +
			"    gl_FragColor =  gl_FragColor + 1.0*(1.0*clamp(1.0 - 2.25*cm, 0.0, 1.0))*pow(1.0 - diffuse, 10.0)*texture2D(uTextures[3], vTex);   \n" +
			//"}   \n" +

			
			
			// antialias edges
			
			" float a; \n" +

			" a = abs(vMVNormal.z); \n" +
			// additional specular
			" gl_FragColor = clamp(gl_FragColor  + 0.1 * uLightSpecularColor * (a) * (sp*sp * 0.5), 0.0, 1.0);	\n" + 
	
			" a = pow(abs(1.0 - vMVNormal.z), 4.0); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 7.0*vec4(1.0, 1.0, 1.0, 1.0) * uLightSpecularColor  * (a) * (diffuse * 5.0), 0.0, 1.0);	\n" + 
			
			" a = pow(abs(1.0 - vMVNormal.z), 0.5); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 0.2*vec4(0.3, 0.6, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" + 

			
			" a = pow(abs(1.0 - vMVNormal.z), 30.0); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 20.0*vec4(0.0, 0.4, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +

			" gl_FragColor.a = 1.0;	\n" +
			" if (abs(vMVNormal.z) < 0.55) { \n" +
			" 	gl_FragColor = clamp(gl_FragColor * pow(abs(vMVNormal.z)/0.55 - 0.0, 5.0), 0.0, 1.0);	\n" + 
			" }; \n" +
	

			"}   \n";

	
	
	public static int p_meteosat_iodc;
	
	/* OpenGL */
	// TODO only for 1 light and texture required
	public static final String vsc_meteosat_iodc = 
			"uniform mat4 uMVPMatrix;   \n" +
			"uniform mat4 uMVMatrix;   	\n" +
			"uniform mat4 uVMatrix;   	\n" +
			"varying mat4 vMVMatrix;   	\n" +
			"uniform mat4 uIMVMatrix;	\n" +
			
			"attribute vec4 aPosition; 	\n" +
			"attribute vec3 aNormal;  	\n" +
			"attribute vec2 aTex;		\n" +
			
			"varying vec3 vPosition;	\n" +
			"varying vec4 _vPosition;	\n" +
			
			"varying vec3 vNormal;		\n" + 
			"varying vec3 vMVNormal;		\n" + 
			"varying vec2 vTex;			\n" +
			"varying vec2 vTexM;		\n" +
			
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
			
			
			" mat4 m = mat4( \n" + 
		    " cos(3.1415*(58.18/180.0)), 0.0, sin(3.1415*(57.5/180.0)), 0.0, \n" +  	// first column 
		    " 0.0, 1.0, 0.0, 0.0, \n" +  												// second column
		    " -sin(3.1415*(58.18/180.0)), 0.0, cos(3.1415*(57.5/180.0)), 0.0, \n" +  	// third column
		    " 0.0, 0.0, 0.0, 1.0  \n" +  												// forth column
		    " ); \n" + 
		    "_vPosition = m*aPosition;	\n" +
						
			" 	vTexM[0] = 1.0 - (_vPosition.x+1.0)/2.0; \n" +
			" 	vTexM[1] = 1.0 - (_vPosition.y+1.0)/2.0; \n" +
			"   float s = sqrt(_vPosition.x*_vPosition.x + _vPosition.y*_vPosition.y); \n" +
			"   float sx = sqrt(_vPosition.x*_vPosition.x); \n" +
			"   float sy = sqrt(_vPosition.y*_vPosition.y); \n" +
			" 	vTexM[0] = 0.0025 + vTexM[0]; \n" +
			" 	vTexM[1] = 0.0025 + vTexM[1]; \n" +
			" 	vTexM[0] = 0.5 + (vTexM[0] - 0.5)*(1.0 + 0.13); \n" +
			" 	vTexM[1] = 0.5 + (vTexM[1] - 0.5)*(1.0 + 0.18); \n" +
			" 	vTexM[0] = 0.5 + (vTexM[0] - 0.5)*(1.0 - 0.125*pow(s,2.1)); \n" +
			" 	vTexM[1] = 0.5 + (vTexM[1] - 0.5)*(1.0 - 0.165*pow(s,1.85)); \n" +

			
			"}                         \n";
	
	
	public static final String fsc_meteosat_iodc = 
		 	"precision mediump float;  	\n" +
			"varying vec3 vLightPos;  	\n" +
			"varying vec3 vPosition;	\n" +     // Interpolated position for this fragment.
			"varying vec4 _vPosition;	\n" +
			"varying vec3 lightVector;	\n" +
			"uniform vec4 uColor;  		\n" +     // This is the color from the vertex shader interpolated across the triangle per fragment.
			"varying vec3 vNormal;   	\n" +     // Interpolated normal for this fragment.
			"varying vec3 vMVNormal;	\n" + 
			"varying vec2 vTex;			\n" +
			"varying vec2 vTexM;		\n" +
	        "uniform sampler2D uTextures[7];\n" +
	        "varying vec3 vEye	;   	\n" +
	        "varying vec2 shiftUV;		\n" +	  // clouds shadow shift
	        
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
			
			"uniform float uTW1;					\n" +		// cloud map texture weight
			"uniform float uTW2;					\n" +		// cloud map texture weight
			"uniform float uTW3;					\n" +		// cloud map texture weight
			
			// The entry point for our fragment shader.
			"void main()  				\n" +
			"{  						\n" +

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

				// diffuse
			"    float diffuse = max(dot(normal, lightVector), 0.0);   \n" +

				// diffuse 2
			"    float diffuse2 = max(dot(normal2, lightVector), 0.0);   \n" +

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

			
			// text in the bottom
			" 	vec2 _vTexM =  vTexM; \n" +
			" if (_vPosition.y < -0.92) { \n" +
			" 	_vTexM[1] =  0.05 + (0.92 - (_vPosition.y+0.92)) ; \n" +
			" 	_vTexM[0] =  1.5*(-0.37 + (1.0 - (_vPosition.x+1.0)/2.0)) ; \n" +
			" } \n" +

			" vec4 cmT = 0.2*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM)) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM)) ) "
			+ "			+ 0.4*( uTW1*(texture2D(uTextures[2], _vTexM)) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM)) ) "
			+ "			+ 0.4*( uTW2*(texture2D(uTextures[4], _vTexM))						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM)) ) "
			+ "; \n" +
			
			" vec4 cmTS = 0.2*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV)) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV)) ) "
			+ "			+ 0.4*( uTW1*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV)) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM + 0.001*shiftUV)) ) "
			+ "			+ 0.4*( uTW2*(texture2D(uTextures[4], _vTexM + 0.001*shiftUV))						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV)) ) "
			+ "; \n" +
			
			" float cm = (cmT.r + cmT.g + cmT.b)/3.0; \n" +
			" cm = 0.3 + 0.7*cm; \n" +
			
			" float cmS = (cmTS.r + cmTS.g + cmTS.b)/3.0; \n" +
			" cmS = 0.0 + 1.0*cmS; \n" +

			" if (_vPosition.z > 0.0) { \n" +
			" 	cmT = vec4(0.0,0.0,0.0,1.0); \n" +
			" 	cm = 0.0; \n" +
			" 	cmS = 0.0; \n" +
			" } \n" +
		
			
						
			
			" 		float d = (1.0 - texture2D(uTextures[1], vTex).r)*(1.0 - texture2D(uTextures[1], vTex).g)*texture2D(uTextures[1], vTex).b; \n" +
			// cloudmap
			"		gl_FragColor = 1.0*cm*cmT*( 0.4 + 1.0*uLightAmbientColor + 0.9*uLightDiffuseColor * diffuse2 + 0.1*uLightSpecularColor * specC ) "
			// clouds shadow
			+ "						+ clamp(-1.0*vec4(0.4, 0.4, 0.4, 1.0)*cmS "
			// texture
			+ "						+ clamp(1.0 - 0.5*cm, 0.0, 1.0)*texture2D(uTextures[1], vTex) * (uMaterialEmissiveColor + uLightAmbientColor * uMaterialAmbientColor + uLightDiffuseColor * uMaterialDiffuseColor * diffuse  + 0.3*uLightSpecularColor * specW )     "
			// specular light on watter
			+ "						+ 5.0*d*(1.0 - 1.0*cm)*1.0*vec4(1.0, 1.0, 1.0, 1.0)*uLightSpecularColor * specW , 0.0, 1.0)  ;   \n" +

			
			// night light
			"	 diffuse = clamp(diffuse, 0.0, 1.0); \n" +
			"    gl_FragColor =  gl_FragColor + 1.0*(1.0*clamp(1.0 - 2.25*cm, 0.0, 1.0))*pow(1.0 - diffuse, 10.0)*texture2D(uTextures[3], vTex);   \n" +

			
			// antialias edges
			" float a; \n" +

			" a = abs(vMVNormal.z); \n" +
			// additional specular
			" gl_FragColor = clamp(gl_FragColor  + 0.1 * uLightSpecularColor * (a) * (sp*sp * 0.5), 0.0, 1.0);	\n" + 
	
			" a = pow(abs(1.0 - vMVNormal.z), 4.0); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 7.0*vec4(1.0, 1.0, 1.0, 1.0) * uLightSpecularColor  * (a) * (diffuse * 5.0), 0.0, 1.0);	\n" + 
			
			" a = pow(abs(1.0 - vMVNormal.z), 0.5); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 0.2*vec4(0.3, 0.6, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" + 

			
			" a = pow(abs(1.0 - vMVNormal.z), 30.0); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 20.0*vec4(0.0, 0.4, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +

			" gl_FragColor.a = 1.0;	\n" +
			" if (abs(vMVNormal.z) < 0.55) { \n" +
			" 	gl_FragColor = clamp(gl_FragColor * pow(abs(vMVNormal.z)/0.55 - 0.0, 5.0), 0.0, 1.0);	\n" + 
			" }; \n" +

			"}   \n";


	public static int p_xplanet;
	
	public static String vsc_xplanet = 
			"uniform mat4 uMVPMatrix;   \n" +
			"uniform mat4 uMVMatrix;   	\n" +
			"uniform mat4 uVMatrix;   	\n" +
			"varying mat4 vMVMatrix;   	\n" +
			"uniform mat4 uIMVMatrix;	\n" +
			
			"attribute vec4 aPosition; 	\n" +
			"attribute vec3 aNormal;  	\n" +
			"attribute vec2 aTex;		\n" +
			
			"varying vec3 vPosition;	\n" +
			"varying vec3 vNormal;		\n" + 
			"varying vec3 vMVNormal;		\n" + 
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
	    
	public static String fsc_xplanet = 
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
	        "varying vec2 shiftUV;		\n" +	  // clouds shadow shift
	        
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

				// diffuse
			"    float diffuse = max(dot(normal, lightVector), 0.0);   \n" +

				// diffuse 2
			"    float diffuse2 = max(dot(normal2, lightVector), 0.0);   \n" +

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

			" cm = 0.0 + 1.0*cm; \n" +
			" vec4 cmT = vec4(1.0,1.0,1.0,1.0); \n" +
			" float cmS = clamp((texture2D(uTextures[2], vTex  + 0.0005*shiftUV).g - min)/(max-min), 0.0, 1.0); \n" +

			" cmS = 0.0 + 0.9*cmS; \n" +

			" 		float d = (1.0 - texture2D(uTextures[1], vTex).r)*(1.0 - texture2D(uTextures[1], vTex).g)*texture2D(uTextures[1], vTex).b; \n" +
			
									// cloudmap
			"		gl_FragColor = 1.0*cm*cmT*( 0.15 + 1.0*uLightAmbientColor + 0.9*uLightDiffuseColor * diffuse2 + 0.1*uLightSpecularColor * specC ) "
									// clouds shadow
			+ "						+ clamp(-1.0*vec4(0.4, 0.4, 0.4, 1.0)*cmS "
									// texture
			+ "						+ clamp(1.0 - 1.0*cm, 0.0, 1.0)*texture2D(uTextures[1], vTex) * (uMaterialEmissiveColor + uLightAmbientColor * uMaterialAmbientColor + uLightDiffuseColor * uMaterialDiffuseColor * diffuse  + 0.3*uLightSpecularColor * specW )     "
									// specular light on watter
			+ "						+ 5.0*d*(1.0 - 1.0*cm)*1.0*vec4(1.0, 1.0, 1.0, 1.0)*uLightSpecularColor * specW , 0.0, 1.0)  ;   \n" +


			// night light
			"	 diffuse = clamp(diffuse, 0.0, 1.0); \n" +
			"    gl_FragColor =  gl_FragColor + 1.0*(1.0*clamp(1.0 - 2.25*cm, 0.0, 1.0))*pow(1.0 - diffuse, 10.0)*texture2D(uTextures[3], vTex);   \n" +

			
			
			// antialias edges
			" float a; \n" +

			" a = abs(vMVNormal.z); \n" +
			// additional specular
			" gl_FragColor = clamp(gl_FragColor  + 0.1 * uLightSpecularColor * (a) * (sp*sp * 0.5), 0.0, 1.0);	\n" + 
			
			" a = pow(abs(1.0 - vMVNormal.z), 4.0); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 7.0*vec4(1.0, 1.0, 1.0, 1.0) * uLightSpecularColor  * (a) * (diffuse * 5.0), 0.0, 1.0);	\n" + 
			
			" a = pow(abs(1.0 - vMVNormal.z), 0.5); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 0.2*vec4(0.3, 0.6, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" + 
			
			
			" a = pow(abs(1.0 - vMVNormal.z), 30.0); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 20.0*vec4(0.0, 0.4, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +

			" gl_FragColor.a = 1.0;	\n" +
			" if (abs(vMVNormal.z) < 0.55) { \n" +
			" 	gl_FragColor = clamp(gl_FragColor * pow(abs(vMVNormal.z)/0.55 - 0.0, 5.0), 0.0, 1.0);	\n" + 
			" }; \n" +

			"}   \n";
	
	
	public static int p_ssec_water;
	
	public static String vsc_ssec_water = 
			"uniform mat4 uMVPMatrix;   \n" +
			"uniform mat4 uMVMatrix;   	\n" +
			"uniform mat4 uVMatrix;   	\n" +
			"varying mat4 vMVMatrix;   	\n" +
			"uniform mat4 uIMVMatrix;	\n" +
			
			"attribute vec4 aPosition; 	\n" +
			"attribute vec3 aNormal;  	\n" +
			"attribute vec2 aTex;		\n" +
			
			"varying vec3 vPosition;	\n" +
			"varying vec3 vNormal;		\n" + 
			"varying vec3 vMVNormal;		\n" + 
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
	    
	public static String fsc_ssec_water = 
		 	"precision mediump float;  	\n" +
			"varying vec3 vLightPos;  	\n" +
			"varying vec3 vPosition;	\n" +     // Interpolated position for this fragment.
			"varying vec3 lightVector;	\n" +
			"uniform vec4 uColor;  		\n" +     // This is the color from the vertex shader interpolated across the triangle per fragment.
			"varying vec3 vNormal;   	\n" +     // Interpolated normal for this fragment.
			"varying vec3 vMVNormal;	\n" + 
			"varying vec2 vTex;			\n" +
	        "uniform sampler2D uTextures[7];\n" +
	        "varying vec3 vEye	;   	\n" +
	        "varying vec2 shiftUV;		\n" +	  // clouds shadow shift
	        
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
			
			"uniform float uTW1;					\n" +		// cloud map texture weight
			"uniform float uTW2;					\n" +		// cloud map texture weight
			"uniform float uTW3;					\n" +		// cloud map texture weight
			
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

				// diffuse
			"    float diffuse = max(dot(normal, lightVector), 0.0);   \n" +

				// diffuse 2
			"    float diffuse2 = max(dot(normal2, lightVector), 0.0);   \n" +

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

    	
    		"	 vec2 vTexM; \n" +
			
			// Mollweide
			" 	float psi = 3.14159265*(vTex[1] - 0.5); \n" +
			" 	for (int i = 0; i < 4; i++) { \n" +
			" 		psi = psi - (2.0*psi + sin(2.0*psi) - 3.14159265*sin(vTex[1]*3.14159265 - 3.14159265*0.5))/(2.0 + 2.0*cos(2.0*psi)); \n" +
			" 	} \n" +
			" 	vTexM[0] = (2.0*vTex[0] - 1.0)*cos(psi); \n" +
			" 	vTexM[1] = sin(psi); \n" +
			" 	vTexM[0] = 0.5 + 0.5*vTexM[0]; \n" +
			" 	vTexM[1] = 0.5 + 0.5*vTexM[1]; \n" +
			
			" 	vTexM[0] = 0.015 + (1.0 - 0.015 - 0.015)*vTexM[0]; \n" +
			" 	vTexM[1] = 0.0525 + (1.0 - 0.055 - 0.05)*vTexM[1]; \n" +
		
			

			
			" float min = 0.0; \n" +
			" float max = 1.0; \n" +
			
			" float cm = 0.05*(uTW3*clamp((texture2D(uTextures[2], vTexM).g - min)/(max-min), 0.0, 1.0) 						+ (1.0 - uTW3)*(clamp((texture2D(uTextures[5], vTexM).g - min)/(max-min), 0.0, 1.0))) + "
			+ "			 0.475*(uTW1*clamp((texture2D(uTextures[2], vTexM).g - min)/(max-min), 0.0, 1.0) 						+ (1.0 - uTW1)*(clamp((texture2D(uTextures[6], vTexM).g - min)/(max-min), 0.0, 1.0))) + "
			+ "			 0.475*(uTW2*clamp((texture2D(uTextures[4], vTexM).g - min)/(max-min), 0.0, 1.0) 						+ (1.0 - uTW2)*(clamp((texture2D(uTextures[5], vTexM).g - min)/(max-min), 0.0, 1.0))); \n" +

			" cm = 0.0 + 1.2*cm; \n" +
			" vec4 cmT = vec4(0.8,0.95,1.0,1.0); \n" +
			" float cmS = 0.05*(uTW3*clamp((texture2D(uTextures[2], vTexM  + 0.0005*shiftUV).g - min)/(max-min), 0.0, 1.0) 		+ (1.0 - uTW3)*(clamp((texture2D(uTextures[5], vTexM  + 0.0005*shiftUV).g - min)/(max-min), 0.0, 1.0))) +"
			+ "			  0.475*(uTW1*clamp((texture2D(uTextures[2], vTexM  + 0.0005*shiftUV).g - min)/(max-min), 0.0, 1.0) 		+ (1.0 - uTW1)*(clamp((texture2D(uTextures[6], vTexM  + 0.0005*shiftUV).g - min)/(max-min), 0.0, 1.0))) +"
			+ "           0.475*(uTW2*clamp((texture2D(uTextures[4], vTexM  + 0.0005*shiftUV).g - min)/(max-min), 0.0, 1.0) 		+ (1.0 - uTW2)*(clamp((texture2D(uTextures[5], vTexM  + 0.0005*shiftUV).g - min)/(max-min), 0.0, 1.0))); \n" +

			" cmS = 0.0 + 1.0*cmS; \n" +
						
						

			
			" 		float d = (1.0 - texture2D(uTextures[1], vTex).r)*(1.0 - texture2D(uTextures[1], vTex).g)*texture2D(uTextures[1], vTex).b; \n" +

									// cloudmap
			"		gl_FragColor = 0.95*cm*cmT*( 0.15 + 1.0*uLightAmbientColor + 0.9*uLightDiffuseColor * diffuse2 + 0.1*uLightSpecularColor * specC ) "
									// clouds shadow
			+ "						+ clamp(-1.0*vec4(0.4, 0.4, 0.4, 1.0)*cmS "
									// texture
			+ "						+ 0.5*clamp(1.0 - 0.0*cm, 0.0, 1.0)*texture2D(uTextures[1], vTex) * (uMaterialEmissiveColor + uLightAmbientColor * uMaterialAmbientColor + uLightDiffuseColor * uMaterialDiffuseColor * diffuse  + 0.3*uLightSpecularColor * specW )     "
									// specular light on watter
			+ "						+ 5.0*d*(1.0 - 1.0*cm)*1.0*vec4(1.0, 1.0, 1.0, 1.0)*uLightSpecularColor * specW , 0.0, 1.0)  ;   \n" +
			
			
			// night light
			"	 diffuse = clamp(diffuse, 0.0, 1.0); \n" +
			"    gl_FragColor =  gl_FragColor + 1.0*(1.0*clamp(1.0 - 2.25*cm, 0.0, 1.0))*pow(1.0 - diffuse, 10.0)*texture2D(uTextures[3], vTex);   \n" +


			// antialias edges
			" float a; \n" +

			" a = abs(vMVNormal.z); \n" +
			// additional specular
			" gl_FragColor = clamp(gl_FragColor  + 0.1 * uLightSpecularColor * (a) * (sp*sp * 0.5)*(1.0 - 1.0*cm), 0.0, 1.0);	\n" + 
	
			" a = pow(abs(1.0 - vMVNormal.z), 4.0); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 7.0*vec4(1.0, 1.0, 1.0, 1.0) * uLightSpecularColor  * (a) * (diffuse * 5.0), 0.0, 1.0);	\n" + 
			
			" a = pow(abs(1.0 - vMVNormal.z), 0.5); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 0.2*vec4(0.3, 0.6, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" + 

			
			" a = pow(abs(1.0 - vMVNormal.z), 30.0); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 20.0*vec4(0.0, 0.4, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +

			" gl_FragColor.a = 1.0;	\n" +
			" if (abs(vMVNormal.z) < 0.55) { \n" +
			" 	gl_FragColor = clamp(gl_FragColor * pow(abs(vMVNormal.z)/0.55 - 0.0, 5.0), 0.0, 1.0);	\n" + 
			" }; \n" +

			"}   \n";
	
	
	public static int p_ssec_ir;
	
	public static String vsc_ssec_ir = 
			"uniform mat4 uMVPMatrix;   \n" +
			"uniform mat4 uMVMatrix;   	\n" +
			"uniform mat4 uVMatrix;   	\n" +
			"varying mat4 vMVMatrix;   	\n" +
			"uniform mat4 uIMVMatrix;	\n" +
			
			"attribute vec4 aPosition; 	\n" +
			"attribute vec3 aNormal;  	\n" +
			"attribute vec2 aTex;		\n" +
			
			"varying vec3 vPosition;	\n" +
			"varying vec3 vNormal;		\n" + 
			"varying vec3 vMVNormal;		\n" + 
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
	    
	public static String fsc_ssec_ir = 
		 	"precision mediump float;  	\n" +
			"varying vec3 vLightPos;  	\n" +
			"varying vec3 vPosition;	\n" +     // Interpolated position for this fragment.
			"varying vec3 lightVector;	\n" +
			"uniform vec4 uColor;  		\n" +     // This is the color from the vertex shader interpolated across the triangle per fragment.
			"varying vec3 vNormal;   	\n" +     // Interpolated normal for this fragment.
			"varying vec3 vMVNormal;	\n" + 
			"varying vec2 vTex;			\n" +
	        "uniform sampler2D uTextures[7];\n" +
	        "varying vec3 vEye	;   	\n" +
	        "varying vec2 shiftUV;		\n" +	  // clouds shadow shift
	        
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
			
			"uniform float uTW1;					\n" +		// cloud map texture weight
			"uniform float uTW2;					\n" +		// cloud map texture weight
			"uniform float uTW3;					\n" +		// cloud map texture weight
			
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


				// diffuse
			"    float diffuse = max(dot(normal, lightVector), 0.0);   \n" +

				// diffuse 2
			"    float diffuse2 = max(dot(normal2, lightVector), 0.0);   \n" +
		
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

    	
    		"	 vec2 vTexM; \n" +

			// Mollweide
			" 	float psi = 3.14159265*(vTex[1] - 0.5); \n" +
			" 	for (int i = 0; i < 4; i++) { \n" +
			" 		psi = psi - (2.0*psi + sin(2.0*psi) - 3.14159265*sin(vTex[1]*3.14159265 - 3.14159265*0.5))/(2.0 + 2.0*cos(2.0*psi)); \n" +
			" 	} \n" +
			" 	vTexM[0] = (2.0*vTex[0] - 1.0)*cos(psi); \n" +
			" 	vTexM[1] = sin(psi); \n" +
			" 	vTexM[0] = 0.5 + 0.5*vTexM[0]; \n" +
			" 	vTexM[1] = 0.5 + 0.5*vTexM[1]; \n" +
			
			" 	vTexM[0] = 0.015 + (1.0 - 0.015 - 0.015)*vTexM[0]; \n" +
			" 	vTexM[1] = 0.0525 + (1.0 - 0.055 - 0.05)*vTexM[1]; \n" +
		
			
			
			" float min = 0.25; \n" +
			" float max = 0.9; \n" +
			
			" float cm = 0.05*(uTW3*clamp((texture2D(uTextures[2], vTexM).g - min)/(max-min), 0.0, 1.0) 						+ (1.0 - uTW3)*(clamp((texture2D(uTextures[5], vTexM).g - min)/(max-min), 0.0, 1.0))) "
			+ "			+ 0.475*(uTW1*clamp((texture2D(uTextures[2], vTexM).g - min)/(max-min), 0.0, 1.0) 						+ (1.0 - uTW1)*(clamp((texture2D(uTextures[6], vTexM).g - min)/(max-min), 0.0, 1.0))) "
			+ "			+ 0.475*(uTW2*clamp((texture2D(uTextures[4], vTexM).g - min)/(max-min), 0.0, 1.0) 						+ (1.0 - uTW2)*(clamp((texture2D(uTextures[5], vTexM).g - min)/(max-min), 0.0, 1.0))) "
			+ "; \n" +

			" cm = 0.0 + 1.25*cm; \n" +
			" vec4 cmT = vec4(1.0,1.0,1.0,1.0); \n" +
			" float cmS = 0.05*(uTW3*clamp((texture2D(uTextures[2], vTexM  + 0.0005*shiftUV).g - min)/(max-min), 0.0, 1.0) 		+ (1.0 - uTW3)*(clamp((texture2D(uTextures[5], vTexM  + 0.0005*shiftUV).g - min)/(max-min), 0.0, 1.0)))"
			+ "			 + 0.475*(uTW1*clamp((texture2D(uTextures[2], vTexM  + 0.0005*shiftUV).g - min)/(max-min), 0.0, 1.0) 		+ (1.0 - uTW1)*(clamp((texture2D(uTextures[6], vTexM  + 0.0005*shiftUV).g - min)/(max-min), 0.0, 1.0))) "
			+ "          + 0.475*(uTW2*clamp((texture2D(uTextures[4], vTexM  + 0.0005*shiftUV).g - min)/(max-min), 0.0, 1.0) 		+ (1.0 - uTW2)*(clamp((texture2D(uTextures[5], vTexM  + 0.0005*shiftUV).g - min)/(max-min), 0.0, 1.0))) "
			+ "; \n" +

			" cmS = 0.0 + 1.0*cmS; \n" +
			
						
				
			" 		float d = (1.0 - texture2D(uTextures[1], vTex).r)*(1.0 - texture2D(uTextures[1], vTex).g)*texture2D(uTextures[1], vTex).b; \n" +

									// cloudmap
			"		gl_FragColor = 1.0*cm*cmT*( 0.15 + 1.0*uLightAmbientColor + 0.9*uLightDiffuseColor * diffuse2 + 0.1*uLightSpecularColor * specC ) "
									// clouds shadow
			+ "						+ clamp(-1.0*vec4(0.4, 0.4, 0.4, 1.0)*cmS "
									// texture
			+ "						+ clamp(1.0 - 1.0*cm, 0.0, 1.0)*texture2D(uTextures[1], vTex) * (uMaterialEmissiveColor + uLightAmbientColor * uMaterialAmbientColor + uLightDiffuseColor * uMaterialDiffuseColor * diffuse  + 0.3*uLightSpecularColor * specW )     "
									// specular light on watter
			+ "						+ 5.0*d*(1.0 - 1.0*cm)*1.0*vec4(1.0, 1.0, 1.0, 1.0)*uLightSpecularColor * specW , 0.0, 1.0)  ;   \n" +
		
			
			// night light
			"	 diffuse = clamp(diffuse, 0.0, 1.0); \n" +
			"    gl_FragColor =  gl_FragColor + 1.0*(1.0*clamp(1.0 - 2.25*cm, 0.0, 1.0))*pow(1.0 - diffuse, 10.0)*texture2D(uTextures[3], vTex);   \n" +
			
			// antialias edges
			
			" float a; \n" +

			" a = abs(vMVNormal.z); \n" +
			// additional specular
			" gl_FragColor = clamp(gl_FragColor  + 0.1 * uLightSpecularColor * (a) * (sp*sp * 0.5), 0.0, 1.0);	\n" + 
	
			" a = pow(abs(1.0 - vMVNormal.z), 4.0); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 7.0*vec4(1.0, 1.0, 1.0, 1.0) * uLightSpecularColor  * (a) * (diffuse * 5.0), 0.0, 1.0);	\n" + 
			
			" a = pow(abs(1.0 - vMVNormal.z), 0.5); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 0.2*vec4(0.3, 0.6, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" + 

			
			" a = pow(abs(1.0 - vMVNormal.z), 30.0); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 20.0*vec4(0.0, 0.4, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +

			" gl_FragColor.a = 1.0;	\n" +
			" if (abs(vMVNormal.z) < 0.55) { \n" +
			" 	gl_FragColor = clamp(gl_FragColor * pow(abs(vMVNormal.z)/0.55 - 0.0, 5.0), 0.0, 1.0);	\n" + 
			" }; \n" +

			"}   \n";
	
	
	
	public static int p_goes_east;
	
	/* OpenGL */
	// TODO only for 1 light and texture required
	public static final String vsc_goes_east = 
			"uniform mat4 uMVPMatrix;   \n" +
			"uniform mat4 uMVMatrix;   	\n" +
			"uniform mat4 uVMatrix;   	\n" +
			"varying mat4 vMVMatrix;   	\n" +
			"uniform mat4 uIMVMatrix;	\n" +
			
			"attribute vec4 aPosition; 	\n" +
			"attribute vec3 aNormal;  	\n" +
			"attribute vec2 aTex;		\n" +
			
			"varying vec3 vPosition;	\n" +
			"varying vec4 _vPosition;	\n" +
			
			"varying vec3 vNormal;		\n" + 
			"varying vec3 vMVNormal;		\n" + 
			"varying vec2 vTex;			\n" +
			"varying vec2 vTexM;		\n" +
			
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
			
			
			" mat4 m = mat4( \n" + 
		    " cos(3.1415*(-75.324/180.0)), 0.0, sin(3.1415*(-75.324/180.0)), 0.0, \n" +  	// first column 
		    " 0.0, 1.0, 0.0, 0.0, \n" +  												// second column
		    " -sin(3.1415*(-75.324/180.0)), 0.0, cos(3.1415*(-75.324/180.0)), 0.0, \n" +  	// third column
		    " 0.0, 0.0, 0.0, 1.0  \n" +  												// forth column
		    " ); \n" + 
		    "_vPosition = m*aPosition;	\n" +
						
			" 	vTexM[0] = 1.0 - (_vPosition.x+1.0)/2.0; \n" +
			" 	vTexM[1] = 1.0 - (_vPosition.y+1.0)/2.0; \n" +
			"   float s = sqrt(_vPosition.x*_vPosition.x + _vPosition.y*_vPosition.y); \n" +
			"   float sx = sqrt(_vPosition.x*_vPosition.x); \n" +
			"   float sy = sqrt(_vPosition.y*_vPosition.y); \n" +
			" 	vTexM[0] = -0.048 + vTexM[0]; \n" +
			" 	vTexM[1] = -0.015 + vTexM[1]; \n" +
			" 	vTexM[0] = 0.5 + (vTexM[0] - 0.5)*(1.0 - 0.15); \n" +
			" 	vTexM[1] = 0.5 + (vTexM[1] - 0.5)*(1.0 + 0.05); \n" +
			" 	vTexM[0] = 0.5 + (vTexM[0] - 0.5)*(1.0 - 0.1*pow(s,5.0)); \n" +
			" 	vTexM[1] = 0.5 + (vTexM[1] - 0.5)*(1.0 - 0.20*pow(s,5.0)); \n" +

			
			"}                         \n";
	
	
	public static final String fsc_goes_east = 
		 	"precision mediump float;  	\n" +
			"varying vec3 vLightPos;  	\n" +
			"varying vec3 vPosition;	\n" +     // Interpolated position for this fragment.
			"varying vec4 _vPosition;	\n" +
			"varying vec3 lightVector;	\n" +
			"uniform vec4 uColor;  		\n" +     // This is the color from the vertex shader interpolated across the triangle per fragment.
			"varying vec3 vNormal;   	\n" +     // Interpolated normal for this fragment.
			"varying vec3 vMVNormal;	\n" + 
			"varying vec2 vTex;			\n" +
			"varying vec2 vTexM;		\n" +
	        "uniform sampler2D uTextures[7];\n" +
	        "varying vec3 vEye	;   	\n" +
	        "varying vec2 shiftUV;		\n" +	  // clouds shadow shift
	        
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
			
			"uniform float uTW1;					\n" +		// cloud map texture weight
			"uniform float uTW2;					\n" +		// cloud map texture weight
			"uniform float uTW3;					\n" +		// cloud map texture weight
			
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

				// diffuse
			"    float diffuse = max(dot(normal, lightVector), 0.0);   \n" +

				// diffuse 2
			"    float diffuse2 = max(dot(normal2, lightVector), 0.0);   \n" +

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
			
		
			
			// text in the bottom
			" 	vec2 _vTexM =  vTexM; \n" +

			" vec4 cmT = vec4(0.75,0.75,0.75,1.0); \n" +

			" vec4 cmTS = vec4(0.75,0.75,0.75,1.0); \n" +
			

			" float cm = 0.2*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM).g) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM).g) ) "
			+ "			+ 0.4*( uTW1*(texture2D(uTextures[2], _vTexM).g) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM).g) ) "
			+ "			+ 0.4*( uTW2*(texture2D(uTextures[4], _vTexM).g)						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM).g) ) "
			+ "; \n" +

			" cm = clamp(-0.3 + cm/0.7, 0.0, 1.0); \n" +
			

			" float cmS = 0.2*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV).g) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV).g) ) "
			+ "			+ 0.4*( uTW1*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV).g) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM + 0.001*shiftUV).g) ) "
			+ "			+ 0.4*( uTW2*(texture2D(uTextures[4], _vTexM + 0.001*shiftUV).g)						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV).g) ) "
			+ "; \n" +

			" cmS = clamp(-0.3 + cmS/0.7, 0.0, 1.0); \n" +

			
			" if (_vPosition.z > 0.0) { \n" +
			" 	cmT = vec4(0.0,0.0,0.0,1.0); \n" +
			" 	cm = 0.0; \n" +
			" 	cmS = 0.0; \n" +
			" } \n" +
		
			
	
			" 		float d = (1.0 - texture2D(uTextures[1], vTex).r)*(1.0 - texture2D(uTextures[1], vTex).g)*texture2D(uTextures[1], vTex).b; \n" +

									// cloudmap
			"		gl_FragColor = 1.0*cm*cmT*( 0.4 + 1.0*uLightAmbientColor + 0.9*uLightDiffuseColor * diffuse2 + 0.1*uLightSpecularColor * specC ) "
									// clouds shadow
			+ "						+ clamp(-1.0*vec4(0.4, 0.4, 0.4, 1.0)*cmS "
									// texture
			+ "						+ clamp(1.0 - 0.5*cm, 0.0, 1.0)*texture2D(uTextures[1], vTex) * (uMaterialEmissiveColor + uLightAmbientColor * uMaterialAmbientColor + uLightDiffuseColor * uMaterialDiffuseColor * diffuse  + 0.3*uLightSpecularColor * specW )     "
									// specular light on watter
			+ "						+ 2.0*d*(1.0 - 1.0*cm)*1.0*vec4(1.0, 1.0, 1.0, 1.0)*uLightSpecularColor * specW , 0.0, 1.0)  ;   \n" +
			
			
			// night light
			"	 diffuse = clamp(diffuse, 0.0, 1.0); \n" +
			"    gl_FragColor =  gl_FragColor + 1.0*(1.0*clamp(1.0 - 2.25*cm, 0.0, 1.0))*pow(1.0 - diffuse, 10.0)*texture2D(uTextures[3], vTex);   \n" +

			
			// antialias edges
			
			" float a; \n" +

			" a = abs(vMVNormal.z); \n" +
			// additional specular
			" gl_FragColor = clamp(gl_FragColor  + 0.1 * uLightSpecularColor * (a) * (sp*sp * 0.5), 0.0, 1.0);	\n" + 
	
			" a = pow(abs(1.0 - vMVNormal.z), 4.0); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 7.0*vec4(1.0, 1.0, 1.0, 1.0) * uLightSpecularColor  * (a) * (diffuse * 5.0), 0.0, 1.0);	\n" + 
			
			" a = pow(abs(1.0 - vMVNormal.z), 0.5); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 0.2*vec4(0.3, 0.6, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" + 

			
			" a = pow(abs(1.0 - vMVNormal.z), 30.0); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 20.0*vec4(0.0, 0.4, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +

			" gl_FragColor.a = 1.0;	\n" +
			" if (abs(vMVNormal.z) < 0.55) { \n" +
			" 	gl_FragColor = clamp(gl_FragColor * pow(abs(vMVNormal.z)/0.55 - 0.0, 5.0), 0.0, 1.0);	\n" + 
			" }; \n" +

			"}   \n";
	
	
	public static int p_goes_west;
	
	/* OpenGL */
	// TODO only for 1 light and texture required
	public static final String vsc_goes_west = 
			"uniform mat4 uMVPMatrix;   \n" +
			"uniform mat4 uMVMatrix;   	\n" +
			"uniform mat4 uVMatrix;   	\n" +
			"varying mat4 vMVMatrix;   	\n" +
			"uniform mat4 uIMVMatrix;	\n" +
			
			"attribute vec4 aPosition; 	\n" +
			"attribute vec3 aNormal;  	\n" +
			"attribute vec2 aTex;		\n" +
			
			"varying vec3 vPosition;	\n" +
			"varying vec4 _vPosition;	\n" +
			
			"varying vec3 vNormal;		\n" + 
			"varying vec3 vMVNormal;		\n" + 
			"varying vec2 vTex;			\n" +
			"varying vec2 vTexM;		\n" +
			
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
			
			
			" mat4 m = mat4( \n" + 
		    " cos(3.1415*(-135.0/180.0)), 0.0, sin(3.1415*(-135.0/180.0)), 0.0, \n" +  	// first column 
		    " 0.0, 1.0, 0.0, 0.0, \n" +  												// second column
		    " -sin(3.1415*(-135.0/180.0)), 0.0, cos(3.1415*(-135.0/180.0)), 0.0, \n" +  	// third column
		    " 0.0, 0.0, 0.0, 1.0  \n" +  												// forth column
		    " ); \n" + 
		    "_vPosition = m*aPosition;	\n" +
						
			" 	vTexM[0] = 1.0 - (_vPosition.x+1.0)/2.0; \n" +
			" 	vTexM[1] = 1.0 - (_vPosition.y+1.0)/2.0; \n" +

			"   float s = sqrt(_vPosition.x*_vPosition.x + _vPosition.y*_vPosition.y); \n" +
			"   float sx = sqrt(_vPosition.x*_vPosition.x); \n" +
			"   float sy = sqrt(_vPosition.y*_vPosition.y); \n" +

			" 	vTexM[0] = -0.048 + vTexM[0]; \n" +
			" 	vTexM[1] = -0.035 + vTexM[1]; \n" +

			" 	vTexM[0] = 0.5 + (vTexM[0] - 0.5)*(1.0 - 0.15); \n" +
			" 	vTexM[1] = 0.5 + (vTexM[1] - 0.5)*(1.0 + 0.05); \n" +
			" 	vTexM[0] = 0.5 + (vTexM[0] - 0.5)*(1.0 - 0.1*pow(s,9.0)); \n" +
			" 	vTexM[1] = 0.5 + (vTexM[1] - 0.5)*(1.0 - 0.20*pow(s,5.0)); \n" +
			
			"}                         \n";
	
	
	
	public static int p_mtsat;
	
	/* OpenGL */
	// TODO only for 1 light and texture required
	public static final String vsc_mtsat = 
			"uniform mat4 uMVPMatrix;   \n" +
			"uniform mat4 uMVMatrix;   	\n" +
			"uniform mat4 uVMatrix;   	\n" +
			"varying mat4 vMVMatrix;   	\n" +
			"uniform mat4 uIMVMatrix;	\n" +
			
			"attribute vec4 aPosition; 	\n" +
			"attribute vec3 aNormal;  	\n" +
			"attribute vec2 aTex;		\n" +
			
			"varying vec3 vPosition;	\n" +
			"varying vec4 _vPosition;	\n" +
			
			"varying vec3 vNormal;		\n" + 
			"varying vec3 vMVNormal;		\n" + 
			"varying vec2 vTex;			\n" +
			"varying vec2 vTexM;		\n" +
			
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
			
			
			" mat4 m = mat4( \n" + 
		    " cos(3.1415*(140.0/180.0)), 0.0, sin(3.1415*(140.0/180.0)), 0.0, \n" +  	// first column 
		    " 0.0, 1.0, 0.0, 0.0, \n" +  												// second column
		    " -sin(3.1415*(140.0/180.0)), 0.0, cos(3.1415*(140.0/180.0)), 0.0, \n" +  	// third column
		    " 0.0, 0.0, 0.0, 1.0  \n" +  												// forth column
		    " ); \n" + 
		    "_vPosition = m*aPosition;	\n" +
						
			" 	vTexM[0] = 1.0 - (_vPosition.x+1.0)/2.0; \n" +
			" 	vTexM[1] = 1.0 - (_vPosition.y+1.0)/2.0; \n" +

			"   float s = sqrt(_vPosition.x*_vPosition.x + _vPosition.y*_vPosition.y); \n" +
			"   float sx = sqrt(_vPosition.x*_vPosition.x); \n" +
			"   float sy = sqrt(_vPosition.y*_vPosition.y); \n" +

			" 	vTexM[0] = 0.5 + (vTexM[0] - 0.5)*(1.0 - 0.117*pow(s,4.5)); \n" +
			" 	vTexM[1] = 0.5 + (vTexM[1] - 0.5)*(1.0 - 0.065*pow(s,4.8)); \n" +
			
			" 	vTexM[0] = 0.5 + (vTexM[0] - 0.5)*(1.0 + 0.12); \n" +
			" 	vTexM[1] = 0.5 + (vTexM[1] - 0.5)*(1.0 + 0.09); \n" +

			" 	vTexM[1] = -0.008 + vTexM[1]; \n" +
			
			"}                         \n";
	
	
	public static final String fsc_mtsat = 
		 	"precision mediump float;  	\n" +
			"varying vec3 vLightPos;  	\n" +
			"varying vec3 vPosition;	\n" +     // Interpolated position for this fragment.
			"varying vec4 _vPosition;	\n" +
			"varying vec3 lightVector;	\n" +
			"uniform vec4 uColor;  		\n" +     // This is the color from the vertex shader interpolated across the triangle per fragment.
			"varying vec3 vNormal;   	\n" +     // Interpolated normal for this fragment.
			"varying vec3 vMVNormal;	\n" + 
			"varying vec2 vTex;			\n" +
			"varying vec2 vTexM;		\n" +
	        "uniform sampler2D uTextures[7];\n" +
	        "varying vec3 vEye	;   	\n" +
	        "varying vec2 shiftUV;		\n" +	  // clouds shadow shift
	        
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
			
			"uniform float uTW1;					\n" +		// cloud map texture weight
			"uniform float uTW2;					\n" +		// cloud map texture weight
			"uniform float uTW3;					\n" +		// cloud map texture weight
			
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

				// diffuse
			"    float diffuse = max(dot(normal, lightVector), 0.0);   \n" +

				// diffuse 2
			"    float diffuse2 = max(dot(normal2, lightVector), 0.0);   \n" +

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
			
		
			
			// text in the bottom
			" 	vec2 _vTexM =  vTexM; \n" +

			" vec4 cmT = vec4(0.75,0.75,0.75,1.0); \n" +

			" vec4 cmTS = vec4(0.75,0.75,0.75,1.0); \n" +
			

			" float cm = 0.2*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM).g) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM).g) ) "
			+ "			+ 0.4*( uTW1*(texture2D(uTextures[2], _vTexM).g) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM).g) ) "
			+ "			+ 0.4*( uTW2*(texture2D(uTextures[4], _vTexM).g)						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM).g) ) "
			+ "; \n" +

			" float cmS = 0.2*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV).g) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV).g) ) "
			+ "			+ 0.4*( uTW1*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV).g) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM + 0.001*shiftUV).g) ) "
			+ "			+ 0.4*( uTW2*(texture2D(uTextures[4], _vTexM + 0.001*shiftUV).g)						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV).g) ) "
			+ "; \n" +

			
			" if (_vPosition.z > 0.0) { \n" +
			" 	cmT = vec4(0.0,0.0,0.0,1.0); \n" +
			" 	cm = 0.0; \n" +
			" 	cmS = 0.0; \n" +
			" } \n" +
		
			
	
			" 		float d = (1.0 - texture2D(uTextures[1], vTex).r)*(1.0 - texture2D(uTextures[1], vTex).g)*texture2D(uTextures[1], vTex).b; \n" +
									// cloudmap
			"		gl_FragColor = 1.0*cm*cmT*( 0.4 + 1.0*uLightAmbientColor + 0.9*uLightDiffuseColor * diffuse2 + 0.1*uLightSpecularColor * specC ) "
									// clouds shadow
			+ "						+ clamp(-1.0*vec4(0.4, 0.4, 0.4, 1.0)*cmS "
									// texture
			+ "						+ clamp(1.0 - 0.5*cm, 0.0, 1.0)*texture2D(uTextures[1], vTex) * (uMaterialEmissiveColor + uLightAmbientColor * uMaterialAmbientColor + uLightDiffuseColor * uMaterialDiffuseColor * diffuse  + 0.3*uLightSpecularColor * specW )     "
									// specular light on watter
			+ "						+ 2.0*d*(1.0 - 1.0*cm)*1.0*vec4(1.0, 1.0, 1.0, 1.0)*uLightSpecularColor * specW , 0.0, 1.0)  ;   \n" +
			
			
			
			// night light
			"	 diffuse = clamp(diffuse, 0.0, 1.0); \n" +
			"    gl_FragColor =  gl_FragColor + 1.0*(1.0*clamp(1.0 - 2.25*cm, 0.0, 1.0))*pow(1.0 - diffuse, 10.0)*texture2D(uTextures[3], vTex);   \n" +

			// antialias edges
			" float a; \n" +

			" a = abs(vMVNormal.z); \n" +
			// additional specular
			" gl_FragColor = clamp(gl_FragColor  + 0.1 * uLightSpecularColor * (a) * (sp*sp * 0.5), 0.0, 1.0);	\n" + 
	
			" a = pow(abs(1.0 - vMVNormal.z), 4.0); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 7.0*vec4(1.0, 1.0, 1.0, 1.0) * uLightSpecularColor  * (a) * (diffuse * 5.0), 0.0, 1.0);	\n" + 
			
			" a = pow(abs(1.0 - vMVNormal.z), 0.5); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 0.2*vec4(0.3, 0.6, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" + 

			
			" a = pow(abs(1.0 - vMVNormal.z), 30.0); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 20.0*vec4(0.0, 0.4, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +

			" gl_FragColor.a = 1.0;	\n" +
			" if (abs(vMVNormal.z) < 0.55) { \n" +
			" 	gl_FragColor = clamp(gl_FragColor * pow(abs(vMVNormal.z)/0.55 - 0.0, 5.0), 0.0, 1.0);	\n" + 
			" }; \n" +

			"}   \n";
	
	
	public static int p_cci;
	
	public static final String vsc_cci = 
			"uniform mat4 uMVPMatrix;   \n" +
			"uniform mat4 uMVMatrix;   	\n" +
			"uniform mat4 uVMatrix;   	\n" +
			"varying mat4 vMVMatrix;   	\n" +
			"uniform mat4 uIMVMatrix;	\n" +
			
			"attribute vec4 aPosition; 	\n" +
			"attribute vec3 aNormal;  	\n" +
			"attribute vec2 aTex;		\n" +
			
			"varying vec3 vPosition;	\n" +
			"varying vec3 vNormal;		\n" + 
			"varying vec3 vMVNormal;		\n" + 
			"varying vec2 vTex;			\n" +
			"varying vec2 vTexM;		\n" +
			
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
			
			// 1013x643
			// [6,55]
			// [1006,554]
			" 	vTexM[0] = vTex[0]; \n" +
			" 	vTexM[1] = vTex[1]; \n" +
			//" 	vTexM[0] = (vTex[0] + (5.0/1013.0))*((1008.0-5.0)/1013.0); \n" +
			//" 	vTexM[1] = - (14.0/643.0) + 0.5 + (vTex[1] - 0.5)*((554.0-40.0)/643.0); \n" +
			
			"}                         \n";
	    
	public static final String fsc_cci = 
		 	"precision mediump float;  	\n" +
			"varying vec3 vLightPos;  	\n" +
			"varying vec3 vPosition;	\n" +     // Interpolated position for this fragment.
			"varying vec3 lightVector;	\n" +
			"uniform vec4 uColor;  		\n" +     // This is the color from the vertex shader interpolated across the triangle per fragment.
			"varying vec3 vNormal;   	\n" +     // Interpolated normal for this fragment.
			"varying vec3 vMVNormal;	\n" + 
			"varying vec2 vTex;			\n" +
			"varying vec2 vTexM;		\n" +
	        "uniform sampler2D uTextures[7];\n" +
	        "varying vec3 vEye	;   	\n" +
	        "varying vec2 shiftUV;		\n" +	  // clouds shadow shift
	        
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
			
			"uniform float uTW1;					\n" +		// cloud map texture weight
			"uniform float uTW2;					\n" +		// cloud map texture weight
			"uniform float uTW3;					\n" +		// cloud map texture weight
			
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

				// diffuse
			"    float diffuse = max(dot(normal, lightVector), 0.0);   \n" +

				// diffuse 2
			"    float diffuse2 = max(dot(normal2, lightVector), 0.0);   \n" +
		
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
			
			
			// text in the bottom
			" 	vec2 _vTexM =  vTexM; \n" +
		/*	" if (vPosition.y > 0.95) { \n" +
			" 	_vTexM[1] =  -0.025 + 0.1 - 2.0*(vPosition.y-0.95) ; \n" +
			" 	_vTexM[0] =  1.6*_vTexM[0] ; \n" +
			" } \n" +
			
			" if (vPosition.y < -0.9) { \n" +
			" 	_vTexM[1] =  -0.05 + 0.9 - 1.5*(0.9 + vPosition.y - 0.01) ; \n" +
			" } \n" +
		*/

			" vec4 cmT = 1.0*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM)) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM)) ) "
			+ "			+ 0.0*( uTW1*(texture2D(uTextures[2], _vTexM)) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM)) ) "
			+ "			+ 0.0*( uTW2*(texture2D(uTextures[4], _vTexM))						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM)) ) "
			+ "; \n" +
			
			" vec4 cmTS = 1.0*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV)) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV)) ) "
			+ "			+ 0.0*( uTW1*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV)) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM + 0.001*shiftUV)) ) "
			+ "			+ 0.0*( uTW2*(texture2D(uTextures[4], _vTexM + 0.001*shiftUV))						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV)) ) "
			+ "; \n" +
			
			" float cm = (cmT.r + cmT.g + cmT.b)/3.0; \n" +
			" cm = 0.5; \n" +
			
			
			" float cmS = (cmTS.r + cmTS.g + cmTS.b)/3.0; \n" +
			" cmS = 0.5; \n" +

			
			" 		float d = (1.0 - texture2D(uTextures[1], vTex).r)*(1.0 - texture2D(uTextures[1], vTex).g)*texture2D(uTextures[1], vTex).b; \n" +
			// cloudmap
			"		gl_FragColor = 1.0*cm*cmT*( 0.4 + 1.0*uLightAmbientColor + 0.9*uLightDiffuseColor * diffuse2 + 0.1*uLightSpecularColor * specC ) "
			// clouds shadow
			+ "						+ clamp(-1.0*vec4(0.4, 0.4, 0.4, 1.0)*cmS "
			// texture
			+ "						+ clamp(1.0 - 0.5*cm, 0.0, 1.0)*texture2D(uTextures[1], vTex) * (uMaterialEmissiveColor + uLightAmbientColor * uMaterialAmbientColor + uLightDiffuseColor * uMaterialDiffuseColor * diffuse  + 0.3*uLightSpecularColor * specW )     "
			// specular light on watter
			+ "						+ 5.0*d*(1.0 - 1.0*cm)*1.0*vec4(1.0, 1.0, 1.0, 1.0)*uLightSpecularColor * specW , 0.0, 1.0)  ;   \n" +


			// night light
			"	 diffuse = clamp(diffuse, 0.0, 1.0); \n" +
			"    gl_FragColor =  gl_FragColor + 1.0*(1.0*clamp(1.0 - 2.25*cm, 0.0, 1.0))*pow(1.0 - diffuse, 10.0)*texture2D(uTextures[3], vTex);   \n" +

			
			// antialias edges
			" float a; \n" +

			" a = abs(vMVNormal.z); \n" +
			// additional specular
			" gl_FragColor = clamp(gl_FragColor  + 0.1 * uLightSpecularColor * (a) * (sp*sp * 0.5), 0.0, 1.0);	\n" + 
	
			" a = pow(abs(1.0 - vMVNormal.z), 4.0); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 7.0*vec4(1.0, 1.0, 1.0, 1.0) * uLightSpecularColor  * (a) * (diffuse * 5.0), 0.0, 1.0);	\n" + 
			
			" a = pow(abs(1.0 - vMVNormal.z), 0.5); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 0.2*vec4(0.3, 0.6, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" + 

			
			" a = pow(abs(1.0 - vMVNormal.z), 30.0); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 20.0*vec4(0.0, 0.4, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +

			" gl_FragColor.a = 1.0;	\n" +
			" if (abs(vMVNormal.z) < 0.55) { \n" +
			" 	gl_FragColor = clamp(gl_FragColor * pow(abs(vMVNormal.z)/0.55 - 0.0, 5.0), 0.0, 1.0);	\n" + 
			" }; \n" +

			"}   \n";
	
	
	public static int p_cci_temp;
	
	public static final String vsc_cci_temp = 
			"uniform mat4 uMVPMatrix;   \n" +
			"uniform mat4 uMVMatrix;   	\n" +
			"uniform mat4 uVMatrix;   	\n" +
			"varying mat4 vMVMatrix;   	\n" +
			"uniform mat4 uIMVMatrix;	\n" +
			
			"attribute vec4 aPosition; 	\n" +
			"attribute vec3 aNormal;  	\n" +
			"attribute vec2 aTex;		\n" +
			
			"varying vec3 vPosition;	\n" +
			"varying vec3 vNormal;		\n" + 
			"varying vec3 vMVNormal;		\n" + 
			"varying vec2 vTex;			\n" +
			"varying vec2 vTexM;		\n" +
			
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

			// 1013x643
			// [6,55]
			// [1006,554]
			" 	vTexM[0] = vTex[0]; \n" +
			" 	vTexM[1] = vTex[1]; \n" +
			//" 	vTexM[0] = (vTex[0] + (5.0/1013.0))*((1008.0-5.0)/1013.0); \n" +
			//" 	vTexM[1] = - (20.0/643.0) + 0.5 + (vTex[1] - 0.5)*((554.0-43.0)/643.0); \n" +
			
			
			"}                         \n";
	 
	
	public static final String fsc_cci_temp = 
		 	"precision mediump float;  	\n" +
			"varying vec3 vLightPos;  	\n" +
			"varying vec3 vPosition;	\n" +     // Interpolated position for this fragment.
			"varying vec3 lightVector;	\n" +
			"uniform vec4 uColor;  		\n" +     // This is the color from the vertex shader interpolated across the triangle per fragment.
			"varying vec3 vNormal;   	\n" +     // Interpolated normal for this fragment.
			"varying vec3 vMVNormal;	\n" + 
			"varying vec2 vTex;			\n" +
			"varying vec2 vTexM;		\n" +
	        "uniform sampler2D uTextures[7];\n" +
	        "varying vec3 vEye	;   	\n" +
	        "varying vec2 shiftUV;		\n" +	  // clouds shadow shift
	        
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
			
			"uniform float uTW1;					\n" +		// cloud map texture weight
			"uniform float uTW2;					\n" +		// cloud map texture weight
			"uniform float uTW3;					\n" +		// cloud map texture weight
			
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

				// diffuse
			"    float diffuse = max(dot(normal, lightVector), 0.0);   \n" +

				// diffuse 2
			"    float diffuse2 = max(dot(normal2, lightVector), 0.0);   \n" +

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

			
			// text in the bottom
			" 	vec2 _vTexM =  vTexM; \n" +
		/*	" if (vPosition.y > 0.95) { \n" +
			" 	_vTexM[1] =  -0.025 + 0.1 - 2.0*(vPosition.y-0.95) ; \n" +
			" 	_vTexM[0] =  1.6*_vTexM[0] ; \n" +
			" } \n" +

			" if (vPosition.y < -0.9) { \n" +
			" 	_vTexM[1] =  -0.05 + 0.9 - 1.5*(0.9 + vPosition.y - 0.01) ; \n" +
			" } \n" +
		*/
			" vec4 cmT = 0.2*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM)) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM)) ) "
			+ "			+ 0.4*( uTW1*(texture2D(uTextures[2], _vTexM)) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM)) ) "
			+ "			+ 0.4*( uTW2*(texture2D(uTextures[4], _vTexM))						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM)) ) "
			+ "; \n" +
			
			" vec4 cmTS = 0.2*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV)) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV)) ) "
			+ "			+ 0.4*( uTW1*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV)) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM + 0.001*shiftUV)) ) "
			+ "			+ 0.4*( uTW2*(texture2D(uTextures[4], _vTexM + 0.001*shiftUV))						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV)) ) "
			+ "; \n" +
			
			" float cm = (cmT.r + cmT.g + cmT.b)/3.0; \n" +
			" cm = 0.5; \n" +
			
			" float cmS = (cmTS.r + cmTS.g + cmTS.b)/3.0; \n" +
			" cmS = 0.5; \n" +

			
			" 		float d = (1.0 - texture2D(uTextures[1], vTex).r)*(1.0 - texture2D(uTextures[1], vTex).g)*texture2D(uTextures[1], vTex).b; \n" +
			// cloudmap
			"		gl_FragColor = 1.0*cm*cmT*( 0.4 + 1.0*uLightAmbientColor + 0.9*uLightDiffuseColor * diffuse2 + 0.1*uLightSpecularColor * specC ) "
			// clouds shadow
			+ "						+ clamp(-1.0*vec4(0.4, 0.4, 0.4, 1.0)*cmS "
			// texture
			+ "						+ clamp(1.0 - 0.5*cm, 0.0, 1.0)*texture2D(uTextures[1], vTex) * (uMaterialEmissiveColor + uLightAmbientColor * uMaterialAmbientColor + uLightDiffuseColor * uMaterialDiffuseColor * diffuse  + 0.3*uLightSpecularColor * specW )     "
			// specular light on watter
			+ "						+ 5.0*d*(1.0 - 1.0*cm)*1.0*vec4(1.0, 1.0, 1.0, 1.0)*uLightSpecularColor * specW , 0.0, 1.0)  ;   \n" +

			
			// night light
			//" if (diffuse < 2.0) { \n" +
			"	 diffuse = clamp(diffuse, 0.0, 1.0); \n" +
			"    gl_FragColor =  gl_FragColor + 1.0*(1.0*clamp(1.0 - 2.25*cm, 0.0, 1.0))*pow(1.0 - diffuse, 10.0)*texture2D(uTextures[3], vTex);   \n" +
			//"}   \n" +

			
			// antialias edges
			" float a; \n" +

			" a = abs(vMVNormal.z); \n" +
			// additional specular
			" gl_FragColor = clamp(gl_FragColor  + 0.1 * uLightSpecularColor * (a) * (sp*sp * 0.5), 0.0, 1.0);	\n" + 
	
			" a = pow(abs(1.0 - vMVNormal.z), 4.0); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 7.0*vec4(1.0, 1.0, 1.0, 1.0) * uLightSpecularColor  * (a) * (diffuse * 5.0), 0.0, 1.0);	\n" + 
			
			" a = pow(abs(1.0 - vMVNormal.z), 0.5); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 0.2*vec4(0.3, 0.6, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" + 

			
			" a = pow(abs(1.0 - vMVNormal.z), 30.0); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 20.0*vec4(0.0, 0.4, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +

			" gl_FragColor.a = 1.0;	\n" +
			" if (abs(vMVNormal.z) < 0.55) { \n" +
			" 	gl_FragColor = clamp(gl_FragColor * pow(abs(vMVNormal.z)/0.55 - 0.0, 5.0), 0.0, 1.0);	\n" + 
			" }; \n" +

			"}   \n";
	
	
	public static int p_cci_temp_an;
	
	public static final String vsc_cci_temp_an = 
			"uniform mat4 uMVPMatrix;   \n" +
			"uniform mat4 uMVMatrix;   	\n" +
			"uniform mat4 uVMatrix;   	\n" +
			"varying mat4 vMVMatrix;   	\n" +
			"uniform mat4 uIMVMatrix;	\n" +
			
			"attribute vec4 aPosition; 	\n" +
			"attribute vec3 aNormal;  	\n" +
			"attribute vec2 aTex;		\n" +
			
			"varying vec3 vPosition;	\n" +
			"varying vec3 vNormal;		\n" + 
			"varying vec3 vMVNormal;		\n" + 
			"varying vec2 vTex;			\n" +
			"varying vec2 vTexM;		\n" +
			
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
			
			// 1013x643
			// [6,55]
			// [1006,554]

			" 	vTexM[0] = vTex[0]; \n" +
			" 	vTexM[1] = vTex[1]; \n" +
			//" 	vTexM[0] = - (0.0/1013.0) + 0.5 + (vTex[0] - 0.5)*((1008.0-5.0)/1013.0); \n" +
			//" 	vTexM[1] = - (62.0/643.0) + 0.5 + (vTex[1] - 0.5)*((554.0-120.0)/643.0); \n" +
			
			"}                         \n";
	 
	
	public static final String fsc_cci_temp_an = 
		 	"precision mediump float;  	\n" +
			"varying vec3 vLightPos;  	\n" +
			"varying vec3 vPosition;	\n" +     // Interpolated position for this fragment.
			"varying vec3 lightVector;	\n" +
			"uniform vec4 uColor;  		\n" +     // This is the color from the vertex shader interpolated across the triangle per fragment.
			"varying vec3 vNormal;   	\n" +     // Interpolated normal for this fragment.
			"varying vec3 vMVNormal;	\n" + 
			"varying vec2 vTex;			\n" +
			"varying vec2 vTexM;		\n" +
	        "uniform sampler2D uTextures[7];\n" +
	        "varying vec3 vEye	;   	\n" +
	        "varying vec2 shiftUV;		\n" +	  // clouds shadow shift
	        
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
			
			"uniform float uTW1;					\n" +		// cloud map texture weight
			"uniform float uTW2;					\n" +		// cloud map texture weight
			"uniform float uTW3;					\n" +		// cloud map texture weight
			
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

				// diffuse
			"    float diffuse = max(dot(normal, lightVector), 0.0);   \n" +

				// diffuse 2
			"    float diffuse2 = max(dot(normal2, lightVector), 0.0);   \n" +

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
			

			// text in the bottom
			" 	vec2 _vTexM =  vTexM; \n" +
		/*	" if (vPosition.y > 0.95) { \n" +
			" 	_vTexM[1] =  -0.03 + 0.1 - 2.0*(vPosition.y-0.95) ; \n" +
			" 	_vTexM[0] =  1.6*_vTexM[0] ; \n" +
			" } \n" +
			
			" if (vPosition.y < -0.9) { \n" +
			" 	_vTexM[1] =  -0.19 + 0.9 - 1.5*(0.9 + vPosition.y - 0.01) ; \n" +
			" } \n" +
		*/

			" vec4 cmT = 0.2*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM)) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM)) ) "
			+ "			+ 0.4*( uTW1*(texture2D(uTextures[2], _vTexM)) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM)) ) "
			+ "			+ 0.4*( uTW2*(texture2D(uTextures[4], _vTexM))						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM)) ) "
			+ "; \n" +
			
			" vec4 cmTS = 0.2*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV)) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV)) ) "
			+ "			+ 0.4*( uTW1*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV)) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM + 0.001*shiftUV)) ) "
			+ "			+ 0.4*( uTW2*(texture2D(uTextures[4], _vTexM + 0.001*shiftUV))						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV)) ) "
			+ "; \n" +
			
			" float cm = (cmT.r + cmT.g + cmT.b)/3.0; \n" +
			" cm = 0.5; \n" +
			
			
			" float cmS = (cmTS.r + cmTS.g + cmTS.b)/3.0; \n" +
			" cmS = 0.5; \n" +

			
			" 		float d = (1.0 - texture2D(uTextures[1], vTex).r)*(1.0 - texture2D(uTextures[1], vTex).g)*texture2D(uTextures[1], vTex).b; \n" +
			// cloudmap
			"		gl_FragColor = 1.0*cm*cmT*( 0.4 + 1.0*uLightAmbientColor + 0.9*uLightDiffuseColor * diffuse2 + 0.1*uLightSpecularColor * specC ) "
			// clouds shadow
			+ "						+ clamp(-1.0*vec4(0.4, 0.4, 0.4, 1.0)*cmS "
			// texture
			+ "						+ clamp(1.0 - 0.5*cm, 0.0, 1.0)*texture2D(uTextures[1], vTex) * (uMaterialEmissiveColor + uLightAmbientColor * uMaterialAmbientColor + uLightDiffuseColor * uMaterialDiffuseColor * diffuse  + 0.3*uLightSpecularColor * specW )     "
			// specular light on watter
			+ "						+ 5.0*d*(1.0 - 1.0*cm)*1.0*vec4(1.0, 1.0, 1.0, 1.0)*uLightSpecularColor * specW , 0.0, 1.0)  ;   \n" +


			
			// night light
			//" if (diffuse < 2.0) { \n" +
			"	 diffuse = clamp(diffuse, 0.0, 1.0); \n" +
			"    gl_FragColor =  gl_FragColor + 1.0*(1.0*clamp(1.0 - 2.25*cm, 0.0, 1.0))*pow(1.0 - diffuse, 10.0)*texture2D(uTextures[3], vTex);   \n" +
			//"}   \n" +

			
			// antialias edges
			" float a; \n" +

			" a = abs(vMVNormal.z); \n" +
			// additional specular
			" gl_FragColor = clamp(gl_FragColor  + 0.1 * uLightSpecularColor * (a) * (sp*sp * 0.5), 0.0, 1.0);	\n" + 
	
			" a = pow(abs(1.0 - vMVNormal.z), 4.0); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 7.0*vec4(1.0, 1.0, 1.0, 1.0) * uLightSpecularColor  * (a) * (diffuse * 5.0), 0.0, 1.0);	\n" + 
			
			" a = pow(abs(1.0 - vMVNormal.z), 0.5); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 0.2*vec4(0.3, 0.6, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" + 

			
			" a = pow(abs(1.0 - vMVNormal.z), 30.0); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 20.0*vec4(0.0, 0.4, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +

			" gl_FragColor.a = 1.0;	\n" +
			" if (abs(vMVNormal.z) < 0.55) { \n" +
			" 	gl_FragColor = clamp(gl_FragColor * pow(abs(vMVNormal.z)/0.55 - 0.0, 5.0), 0.0, 1.0);	\n" + 
			" }; \n" +
			
			"}   \n";
	
	public static int p_cci_water;
	
	public static final String vsc_cci_water = 
			"uniform mat4 uMVPMatrix;   \n" +
			"uniform mat4 uMVMatrix;   	\n" +
			"uniform mat4 uVMatrix;   	\n" +
			"varying mat4 vMVMatrix;   	\n" +
			"uniform mat4 uIMVMatrix;	\n" +
			
			"attribute vec4 aPosition; 	\n" +
			"attribute vec3 aNormal;  	\n" +
			"attribute vec2 aTex;		\n" +
			
			"varying vec3 vPosition;	\n" +
			"varying vec3 vNormal;		\n" + 
			"varying vec3 vMVNormal;		\n" + 
			"varying vec2 vTex;			\n" +
			"varying vec2 vTexM;		\n" +
			
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

			
			// 1013x643
			// [6,55]
			// [1006,554]

			" 	vTexM[0] = vTex[0]; \n" +
			" 	vTexM[1] = vTex[1]; \n" +
			//" 	vTexM[0] = - (0.0/1013.0) + 0.5 + (vTex[0] - 0.5)*((1008.0-5.0)/1013.0); \n" +
			//" 	vTexM[1] = - (16.0/643.0) + 0.5 + (vTex[1] - 0.5)*((554.0-42.0)/643.0); \n" +
			
			"}                         \n";
	 
	
	public static int p_cci_wind;
	
	public static final String vsc_cci_wind = 
			"uniform mat4 uMVPMatrix;   \n" +
			"uniform mat4 uMVMatrix;   	\n" +
			"uniform mat4 uVMatrix;   	\n" +
			"varying mat4 vMVMatrix;   	\n" +
			"uniform mat4 uIMVMatrix;	\n" +
			
			"attribute vec4 aPosition; 	\n" +
			"attribute vec3 aNormal;  	\n" +
			"attribute vec2 aTex;		\n" +
			
			"varying vec3 vPosition;	\n" +
			"varying vec3 vNormal;		\n" + 
			"varying vec3 vMVNormal;		\n" + 
			"varying vec2 vTex;			\n" +
			"varying vec2 vTexM;		\n" +
			
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
			
			// 1013x643
			// [6,55]
			// [1006,554]

			" 	vTexM[0] = vTex[0]; \n" +
			" 	vTexM[1] = vTex[1]; \n" +
			//" 	vTexM[0] = - (0.0/1013.0) + 0.5 + (vTex[0] - 0.5)*((1008.0-5.0)/1013.0); \n" +
			//" 	vTexM[1] = - (20.0/643.0) + 0.5 + (vTex[1] - 0.5)*((554.0-48.0)/643.0); \n" +
			
			"}                         \n";
	 
	public static int p_cci_jet;
	
	public static final String vsc_cci_jet = 
			"uniform mat4 uMVPMatrix;   \n" +
			"uniform mat4 uMVMatrix;   	\n" +
			"uniform mat4 uVMatrix;   	\n" +
			"varying mat4 vMVMatrix;   	\n" +
			"uniform mat4 uIMVMatrix;	\n" +
			
			"attribute vec4 aPosition; 	\n" +
			"attribute vec3 aNormal;  	\n" +
			"attribute vec2 aTex;		\n" +
			
			"varying vec3 vPosition;	\n" +
			"varying vec3 vNormal;		\n" + 
			"varying vec3 vMVNormal;		\n" + 
			"varying vec2 vTex;			\n" +
			"varying vec2 vTexM;		\n" +
			
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

			
			// 1013x643
			// [6,55]
			// [1006,554]

			" 	vTexM[0] = vTex[0]; \n" +
			" 	vTexM[1] = vTex[1]; \n" +
			//" 	vTexM[0] = - (0.0/1013.0) + 0.5 + (vTex[0] - 0.5)*((1008.0-5.0)/1013.0); \n" +
			//" 	vTexM[1] = - (14.0/643.0) + 0.5 + (vTex[1] - 0.5)*((554.0-38.0)/643.0); \n" +
			
			"}                         \n";
	
	public static int p_cci_snow;
	
	
	
	
	
	public static int p_nrl_rainrate;
	
	public static final String vsc_nrl_rainrate = 
			"uniform mat4 uMVPMatrix;   \n" +
			"uniform mat4 uMVMatrix;   	\n" +
			"uniform mat4 uVMatrix;   	\n" +
			"varying mat4 vMVMatrix;   	\n" +
			"uniform mat4 uIMVMatrix;	\n" +
			
			"attribute vec4 aPosition; 	\n" +
			"attribute vec3 aNormal;  	\n" +
			"attribute vec2 aTex;		\n" +
			
			"varying vec3 vPosition;	\n" +
			"varying vec3 vNormal;		\n" + 
			"varying vec3 vMVNormal;		\n" + 
			"varying vec2 vTex;			\n" +
			"varying vec2 vTexM;		\n" +
			
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
			
			
			" vTexM = vTex; \n" +
			
			" 	vTexM[0] = + 0.5 + (vTex[0] - 0.5)*(1.0); \n" +
			" 	vTexM[1] = + 0.5 + (vTex[1] - 0.5)*(1.5); \n" +
			
			
			"}                         \n";
	    
	public static final String fsc_nrl_rainrate = 
		 	"precision mediump float;  	\n" +
			"varying vec3 vLightPos;  	\n" +
			"varying vec3 vPosition;	\n" +     // Interpolated position for this fragment.
			"varying vec3 lightVector;	\n" +
			"uniform vec4 uColor;  		\n" +     // This is the color from the vertex shader interpolated across the triangle per fragment.
			"varying vec3 vNormal;   	\n" +     // Interpolated normal for this fragment.
			"varying vec3 vMVNormal;	\n" + 
			"varying vec2 vTex;			\n" +
			"varying vec2 vTexM;		\n" +
	        "uniform sampler2D uTextures[7];\n" +
	        "varying vec3 vEye	;   	\n" +
	        "varying vec2 shiftUV;		\n" +	  // clouds shadow shift
	        
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
			
			"uniform float uTW1;					\n" +		// cloud map texture weight
			"uniform float uTW2;					\n" +		// cloud map texture weight
			"uniform float uTW3;					\n" +		// cloud map texture weight
			
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

				// diffuse
			"    float diffuse = max(dot(normal, lightVector), 0.0);   \n" +

				// diffuse 2
			"    float diffuse2 = max(dot(normal2, lightVector), 0.0);   \n" +

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
			
		
			
			// text in the bottom
			" 	vec2 _vTexM =  vTexM; \n" +

			" vec4 cmT = 1.0*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM)) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM)) ) "
			+ "			+ 0.0*( uTW1*(texture2D(uTextures[2], _vTexM)) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM)) ) "
			+ "			+ 0.0*( uTW2*(texture2D(uTextures[4], _vTexM))						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM)) ) "
			+ "; \n" +
			
			" vec4 cmTS = 1.0*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV)) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV)) ) "
			+ "			+ 0.0*( uTW1*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV)) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM + 0.001*shiftUV)) ) "
			+ "			+ 0.0*( uTW2*(texture2D(uTextures[4], _vTexM + 0.001*shiftUV))						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV)) ) "
			+ "; \n" +
			
			
			" float min = 0.3; \n" +
			" float max = 1.0; \n" +
			" cmT = vec4((cmT.r - min)/(max-min),(cmT.g - min)/(max-min),(cmT.b - min)/(max-min),1.0); \n" +
			" cmTS = vec4((cmTS.r - min)/(max-min),(cmTS.g - min)/(max-min),(cmTS.b - min)/(max-min),1.0); \n" +
					
			" float cm = (cmT.r + cmT.g + cmT.b)/3.0; \n" +
			" cm = 0.7 + 0.3*cm; \n" +
			//" cm = 1.0; \n" +
			
			
			" float cmS = (cmTS.r + cmTS.g + cmTS.b)/3.0; \n" +
			" cmS = 0.0 + 0.3*cmS; \n" +

			" if (vPosition.y > 0.87) { \n" +
			" 	cmT = vec4(0.0,0.0,0.0,1.0); \n" +
			" 	cm = 0.0; \n" +
			" 	cmS = 0.0; \n" +
			" } \n" +
			
			" if (vPosition.y < -0.87) { \n" +
			" 	cmT = vec4(0.0,0.0,0.0,1.0); \n" +
			" 	cm = 0.0; \n" +
			" 	cmS = 0.0; \n" +
			" } \n" +
			
	
			
			" 		float d = (1.0 - texture2D(uTextures[1], vTex).r)*(1.0 - texture2D(uTextures[1], vTex).g)*texture2D(uTextures[1], vTex).b; \n" +
			// cloudmap
			"		gl_FragColor = 1.0*cm*cmT*( 0.4 + 1.0*uLightAmbientColor + 0.9*uLightDiffuseColor * diffuse2 + 0.1*uLightSpecularColor * specC ) "
			// clouds shadow
			+ "						+ clamp(-1.0*vec4(0.4, 0.4, 0.4, 1.0)*cmS "
			// texture
			+ "						+ clamp(1.0 - 0.5*cm, 0.0, 1.0)*texture2D(uTextures[1], vTex) * (uMaterialEmissiveColor + uLightAmbientColor * uMaterialAmbientColor + uLightDiffuseColor * uMaterialDiffuseColor * diffuse  + 0.3*uLightSpecularColor * specW )     "
			// specular light on watter
			+ "						+ 5.0*d*(1.0 - 1.0*cm)*1.0*vec4(1.0, 1.0, 1.0, 1.0)*uLightSpecularColor * specW , 0.0, 1.0)  ;   \n" +


			// night light
			//" if (diffuse < 2.0) { \n" +
			"	 diffuse = clamp(diffuse, 0.0, 1.0); \n" +
			"    gl_FragColor =  gl_FragColor + 1.0*(1.0*clamp(1.0 - 2.25*cm, 0.0, 1.0))*pow(1.0 - diffuse, 10.0)*texture2D(uTextures[3], vTex);   \n" +
			//"}   \n" +

			// antialias edges
			" float a; \n" +

			" a = abs(vMVNormal.z); \n" +
			// additional specular
			" gl_FragColor = clamp(gl_FragColor  + 0.1 * uLightSpecularColor * (a) * (sp*sp * 0.5), 0.0, 1.0);	\n" + 
	
			" a = pow(abs(1.0 - vMVNormal.z), 4.0); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 7.0*vec4(1.0, 1.0, 1.0, 1.0) * uLightSpecularColor  * (a) * (diffuse * 5.0), 0.0, 1.0);	\n" + 
			
			" a = pow(abs(1.0 - vMVNormal.z), 0.5); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 0.2*vec4(0.3, 0.6, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" + 

			
			" a = pow(abs(1.0 - vMVNormal.z), 30.0); \n" +
			" gl_FragColor = clamp(gl_FragColor  + 20.0*vec4(0.0, 0.4, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +

			" gl_FragColor.a = 1.0;	\n" +
			" if (abs(vMVNormal.z) < 0.55) { \n" +
			" 	gl_FragColor = clamp(gl_FragColor * pow(abs(vMVNormal.z)/0.55 - 0.0, 5.0), 0.0, 1.0);	\n" + 
			" }; \n" +
	

			"}   \n";
	
}
