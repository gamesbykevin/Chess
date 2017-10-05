package com.gamesbykevin.chess.opengl;

import android.opengl.GLES20;

import com.gamesbykevin.chess.util.UtilityHelper;

/**
 * Created by Kevin on 8/13/2017.
 */

public class riGraphicTools {

    //Program variables
    public static int sp_SolidColor;
    public static int sp_Image;

    /* SHADER Solid
     *
     * This shader is for rendering a colored primitive.
     *
     */
    public static final String vs_SolidColor =
            "uniform 	mat4 		uMVPMatrix;" +
                    "attribute 	vec4 		vPosition;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    public static final String fs_SolidColor =
            "precision mediump float;" +
                    "void main() {" +
                    "  gl_FragColor = vec4(0.5,0,0,1);" +
                    "}";

    /* SHADER Image
     *
     * This shader is for rendering 2D images straight from a texture
     * No additional effects.
     *
     */
    public static final String vs_Image =
        "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "attribute vec2 a_texCoord;" +
            "varying vec2 v_texCoord;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "  v_texCoord = a_texCoord;" +
            "}";

    public static final String fs_Image =
        "precision mediump float;" +
            "varying vec2 v_texCoord;" +
            "uniform sampler2D s_texture;" +
            "void main() {" +
            "  gl_FragColor = texture2D( s_texture, v_texCoord );" +
            "}";

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        //if there is an error, delete shader and throw exception
        if (compileStatus[0] == 0)
        {
            //print out error details
            UtilityHelper.logEvent(GLES20.glGetShaderInfoLog(shader));

            //delete shader
            GLES20.glDeleteShader(shader);

            //throw exception
            throw new RuntimeException("Error creating shader");
        }


        // return the shader
        return shader;
    }
}