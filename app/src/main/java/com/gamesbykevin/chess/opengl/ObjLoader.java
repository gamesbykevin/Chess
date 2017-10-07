package com.gamesbykevin.chess.opengl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.gamesbykevin.chess.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin on 10/6/2017.
 */

public class ObjLoader {

    //list of vertices
    private List<String> vertices;

    //list of triangular faces
    private List<String> faces;

    //text pattern to look in .obj file to determine our vertices
    private static final String TEXT_VERTICES = "v ";

    //text pattern to look in .obj file to determine our triangular faces
    private static final String TEXT_FACES = "f ";

    //empty space to separate the coordinates
    private static final String SPACE = " ";

    private static final String VERTEX_SHADER =
        "attribute vec4 position;" +
        "uniform mat4 matrix;" +
        "void main() {" +
        "    gl_Position = matrix * position;" +
        "}";

    private static final String FRAGMENT_SHADER =
        "precision mediump float;" +
        "void main() {" +
        "    gl_FragColor = vec4(1, 0.5, 0, 1.0);" +
        "}";

    //buffers used in open gl
    private FloatBuffer verticesBuffer;
    private ShortBuffer facesBuffer;

    private int program;

    float[] projectionMatrix = new float[16];
    float[] viewMatrix = new float[16];
    float[] productMatrix = new float[16];

    public ObjLoader(Context context) {

        InputStream is = null;
        BufferedReader br = null;

        try {

            //get the input stream for the .obj file
            is = context.getResources().openRawResource(R.raw.chess1);

            //get our buffered reader from the input stream
            br = new BufferedReader(new InputStreamReader(is));

            //current line
            String line;

            //read the entire file
            while ((line = br.readLine()) != null) {
                if (line.startsWith(TEXT_VERTICES)) {
                    getVertices().add(line);
                } else if (line.startsWith(TEXT_FACES)) {
                    getFaces().add(line);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            try {

                if (br != null) {
                    br.close();
                    br = null;
                }

                if (is != null) {
                    is.close();
                    is = null;
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

        //create our vertices buffer
        ByteBuffer buffer1 = ByteBuffer.allocateDirect(getVertices().size() * 3 * 4);
        buffer1.order(ByteOrder.nativeOrder());
        this.verticesBuffer = buffer1.asFloatBuffer();

        //populate our vertices buffer
        for (int i = 0; i < getVertices().size(); i++) {

            //separate our coordinates in this array which is split by a space
            String coordinates[] = getVertices().get(i).split(SPACE);

            //add our x coordinate
            this.verticesBuffer.put(Float.parseFloat(coordinates[1]));

            //add our y coordinate
            this.verticesBuffer.put(Float.parseFloat(coordinates[2]));

            //add our z coordinate
            this.verticesBuffer.put(Float.parseFloat(coordinates[3]));
        }

        //position at the beginning of the buffer (reset the position)
        this.verticesBuffer.position(0);

        //also create a buffer for the faces
        ByteBuffer buffer2 = ByteBuffer.allocateDirect(getFaces().size() * 3 * 2);
        buffer2.order(ByteOrder.nativeOrder());
        facesBuffer = buffer2.asShortBuffer();

        //populate our faces buffer
        for (int i = 0; i < getFaces().size(); i++) {

            //parse and assign our faces values to the buffer
            String vertexIndices[] = getFaces().get(i).split(SPACE);
            short vertex1 = Short.parseShort(vertexIndices[1]);
            short vertex2 = Short.parseShort(vertexIndices[2]);
            short vertex3 = Short.parseShort(vertexIndices[3]);
            facesBuffer.put((short)(vertex1 - 1));
            facesBuffer.put((short)(vertex2 - 1));
            facesBuffer.put((short)(vertex3 - 1));
        }

        //position at the beginning of the buffer (reset the position)
        facesBuffer.position(0);


        //create our shaders
        int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertexShader, VERTEX_SHADER);
        int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShader, FRAGMENT_SHADER);

        //now we can compile the shader's
        GLES20.glCompileShader(vertexShader);
        GLES20.glCompileShader(fragmentShader);

        //attach the shaders to the program
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);

        //finally to use this we need to link the program
        GLES20.glLinkProgram(program);
        GLES20.glUseProgram(program);
    }

    private List<String> getVertices() {
        if (this.vertices == null)
            this.vertices = new ArrayList<>();

        return this.vertices;
    }

    private List<String> getFaces() {
        if (this.faces == null)
            this.faces = new ArrayList<>();

        return this.faces;
    }

    public void draw() {

        Matrix.frustumM(projectionMatrix, 0, -1, 1, -1, 1, 2, 9);
        Matrix.setLookAtM(viewMatrix, 0, 0, 3, -4, 0, 0, 0, 0, 1, 0);
        Matrix.multiplyMM(productMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        int matrix = GLES20.glGetUniformLocation(program, "matrix");
        GLES20.glUniformMatrix4fv(matrix, 1, false, productMatrix, 0);


        //get the handle to the program code so we can send vertex position data to it
        int position = GLES20.glGetAttribLocation(program, "position");
        GLES20.glEnableVertexAttribArray(position);

        //point the position handle to our vertices buffer
        GLES20.glVertexAttribPointer(position, 3, GLES20.GL_FLOAT, false, 3 * 4, verticesBuffer);






        //
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, getFaces().size() * 3, GLES20.GL_UNSIGNED_SHORT, facesBuffer);

        //disable the attribute handler that we added earlier
        GLES20.glDisableVertexAttribArray(position);
    }
}