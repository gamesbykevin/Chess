package com.gamesbykevin.chess.opengl;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by Kevin on 8/13/2017.
 */
public class Square {

    //buffer arrays
    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;
    private FloatBuffer uvBuffer;

    private int indicesLength;

    public Square() {
        //default constructor
    }

    public void setupImage() {

        //create our UV coordinates meaning we are going to render the entire texture
        setupImage(new float[] { 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f });
    }

    public void setupImage(float[] uvs) {

        //the texture buffer
        ByteBuffer bb = ByteBuffer.allocateDirect(uvs.length * 4);
        bb.order(ByteOrder.nativeOrder());
        uvBuffer = bb.asFloatBuffer();
        uvBuffer.put(uvs);
        uvBuffer.position(0);
    }

    public void setupTriangle() {

        //create indices meaning we are only rendering one quad texture (2 triangles)
        setupTriangle(new short[] { 0, 1, 2, 0, 2, 3 });
    }

    public void setupTriangle(short[] indices) {

        this.indicesLength = indices.length;

        //initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(indices.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(indices);
        drawListBuffer.position(0);
    }

    public void setupVertices(float[] vertices) {

        //the vertex buffer.
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);
    }

    public void render(final float[] m) {

        // get handle to vertex shader's vPosition member
        int matrixPositionHandle = GLES20.glGetAttribLocation(riGraphicTools.sp_Image, "vPosition");

        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray(matrixPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(matrixPositionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        // Get handle to texture coordinates location
        int matrixTextureCoordinateLocation = GLES20.glGetAttribLocation(riGraphicTools.sp_Image, "a_texCoord" );

        //enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray(matrixTextureCoordinateLocation);

        //prepare the texture coordinates
        GLES20.glVertexAttribPointer(matrixTextureCoordinateLocation, 2, GLES20.GL_FLOAT, false, 0, uvBuffer);

        // Get handle to shape's transformation matrix
        int matrixHandle = GLES20.glGetUniformLocation(riGraphicTools.sp_Image, "uMVPMatrix");

        //apply the projection and view transformation
        GLES20.glUniformMatrix4fv(matrixHandle, 1, false, m, 0);

        //Get handle to textures locations
        int mSamplerLoc = GLES20.glGetUniformLocation(riGraphicTools.sp_Image, "s_texture");

        //Set the sampler texture unit to 0, where we have saved the texture.
        GLES20.glUniform1i(mSamplerLoc, 0);

        //draw the triangle
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indicesLength, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        //disable vertex array
        GLES20.glDisableVertexAttribArray(matrixPositionHandle);
        GLES20.glDisableVertexAttribArray(matrixTextureCoordinateLocation);
    }
}