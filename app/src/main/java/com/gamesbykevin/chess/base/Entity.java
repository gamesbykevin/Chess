package com.gamesbykevin.chess.base;

/**
 * Created by Kevin on 9/2/2017.
 */
public class Entity extends com.gamesbykevin.androidframeworkv2.base.Entity {

    private float[] uvs;

    public void setTextureCoordinates(float col, float row, float width, float height) {

        if (getTextureCoordinates() == null)
            this.uvs = new float[8];

        //assign values
        uvs[0] = col; uvs[1] = row;
        uvs[2] = col; uvs[3] = row + height;
        uvs[4] = col + width; uvs[5] = row + height;
        uvs[6] = col + width; uvs[7] = row;
    }

    public float[] getTextureCoordinates() {
        return this.uvs;
    }
}