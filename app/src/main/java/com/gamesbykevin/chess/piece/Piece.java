package com.gamesbykevin.chess.piece;

import android.opengl.Matrix;

import com.gamesbykevin.androidframeworkv2.base.Cell;
import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.util.UtilityHelper;

import org.rajawali3d.Object3D;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.renderer.Renderer;

/**
 * Created by Kevin on 10/15/2017.
 */

public class Piece extends Cell {

    private final Object3D obj;

    private final Material material;

    private final Type type;

    public enum Type {
        Pawn(R.raw.pawn_stl),
        Bishop(R.raw.bishop_stl),
        Knight(R.raw.knight_stl),
        Rook(R.raw.rook_stl),
        Queen(R.raw.queen_stl),
        King(R.raw.king_stl);

        private final int resId;

        private Type(int resId) {
            this.resId = resId;
        }

        public int getResId() {
            return this.resId;
        }
    }

    public Piece(Object3D obj, Type type, float col, float row) {

        this.obj = obj;
        this.type = type;
        this.material = obj.getMaterial();

        //assign location
        super.setCol(col);
        super.setRow(row);
    }

    public Material getMaterial() {
        return this.material;
    }

    public Object3D getObj() {
        return this.obj;
    }

    public Type getType() {
        return this.type;
    }
}