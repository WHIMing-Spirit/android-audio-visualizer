uniform mat4   u_MVPMatrix;	        // A constant representing the combined model/view/projection matrix.
attribute vec4 a_Position;	        // Per-vertex position information we will pass in.
attribute vec4 a_Color;	            // Per-vertex color information we will pass in.
varying vec4   v_Color;             // This will be passed into the fragment shader.
uniform float  an_old_DB_Level;      // The current decibel level to be used by the shader that is being passed in by each indivisual visualizer
uniform float  a_current_DB_Level;      // The current decibel level to be used by the shader that is being passed in by each indivisual visualizer

void main() {           		    // The entry point for our vertex shader.
    v_Color = a_Color;    	        // Pass the color through to the fragment shader.
    gl_Position = a_Position; 	    // gl_Position is a special variable used to store the final position.
}
