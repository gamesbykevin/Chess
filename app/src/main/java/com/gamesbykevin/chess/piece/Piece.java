package com.gamesbykevin.chess.piece;

import com.gamesbykevin.androidframeworkv2.base.Cell;
import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.players.Player;
import com.gamesbykevin.chess.players.PlayerHelper;

import org.rajawali3d.Object3D;
import org.rajawali3d.materials.Material;

/**
 * Created by Kevin on 10/15/2017.
 */
public class Piece extends Cell {

    private final Object3D obj;

    private final Material material;

    private final Type type;

    //is the piece in a valid place
    private boolean valid = false;

    public enum Type {
        Pawn(R.raw.pawn_stl),
        Bishop(R.raw.bishop_stl),
        Knight(R.raw.knight_stl),
        Rook(R.raw.rook_stl),
        Queen(R.raw.queen_stl),
        King(R.raw.king_stl);

        private final int resId;

        Type(int resId) {
            this.resId = resId;
        }

        public int getResId() {
            return this.resId;
        }
    }

    //if moving this piece what is the current coordinates
    private int moveCol, moveRow;

    public Piece(Object3D obj, Type type, float col, float row) {

        this.obj = obj;
        this.type = type;
        this.material = obj.getMaterial();

        //assign location
        super.setCol(col);
        super.setRow(row);
    }

    public void setMoveCol(final int moveCol) {
        this.moveCol = moveCol;
    }

    public void setMoveRow(final int moveRow) {
        this.moveRow = moveRow;
    }

    public int getMoveCol() {
        return this.moveCol;
    }

    public int getMoveRow() {
        return this.moveRow;
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

    public boolean checkValid(Player player, Player opponent) {

        //figure out where we are
        final int col = PlayerHelper.getCol(getObj().getX());
        final int row = PlayerHelper.getRow(getObj().getZ());

        boolean tmp = true;

        for (int i = 0; i < player.getPieces().size(); i++) {

            Piece piece = player.getPieces().get(i);

            //don't check the current piece
            if (piece.getCol() == getCol() && piece.getRow() == getRow())
                continue;

            if (piece.getCol() == col && piece.getRow() == row) {
                tmp = false;
                break;
            }
        }

        this.valid = tmp;

        return isValid();
    }

    public boolean isValid() {
        return this.valid;
    }
}