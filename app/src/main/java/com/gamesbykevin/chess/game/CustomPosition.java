package com.gamesbykevin.chess.game;

import com.gamesbykevin.androidframeworkv2.base.Cell;
import com.gamesbykevin.androidframeworkv2.base.Disposable;

import org.rajawali3d.Object3D;

import static com.gamesbykevin.chess.players.PlayerHelper.ROWS;

/**
 * Created by Kevin on 11/20/2017.
 */

public class CustomPosition extends Cell implements Disposable {

    //3d object model
    private Object3D object3D;

    //are these laid out horizontal
    private boolean horizontal = false;

    public CustomPosition(final boolean horizontal) {
        this.horizontal = horizontal;
    }

    public boolean isHorizontal() {
        return this.horizontal;
    }

    public void changePositions() {

        if (isHorizontal()) {

            if (getRow() < 0) {
                setRow(ROWS);
            } else {
                setRow(-1);
            }

        } else {

            if (getCol() < 0) {
                setCol(ROWS + 1);
            } else {
                setCol(-1);
            }
        }
    }

    @Override
    public void dispose() {
        if (object3D != null) {
            object3D.destroy();
            object3D = null;
        }
    }

    public void setObject3D(final Object3D object3D) {
        this.object3D = object3D;
    }

    public Object3D getObject3D() {
        return this.object3D;
    }
}
