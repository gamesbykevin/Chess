package com.gamesbykevin.chess.piece;

import com.gamesbykevin.androidframeworkv2.base.Cell;
import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.players.Player;
import com.gamesbykevin.chess.players.PlayerHelper;

import org.rajawali3d.Object3D;
import org.rajawali3d.materials.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin on 10/15/2017.
 */
public class Piece extends Cell {

    //list of valid moves by this piece
    private List<Cell> moves;

    private final Object3D obj;

    private final Type type;

    //has this piece moved yet?
    private boolean moved = false;

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

    public Piece(Object3D obj, Type type, float col, float row) {

        this.obj = obj;
        this.type = type;

        //assign location
        super.setCol(col);
        super.setRow(row);
    }

    public Object3D getObj() {
        return this.obj;
    }

    public Type getType() {
        return this.type;
    }

    public void setMoved(final boolean moved) {
        this.moved = moved;
    }

    public boolean hasMoved() {
        return this.moved;
    }

    public List<Cell> getMoves(Player player, Player opponent) {

        if (this.moves == null)
            this.moves = new ArrayList<>();

        //clear any existing moves
        this.moves.clear();

        //the moves will depend on the chess piece type
        switch (getType()) {

            case Pawn:

                if (player.hasDirection(Player.Direction.North)) {

                    //pawn can move forward as long as there is no piece in front of it
                    if (PlayerHelper.hasBounds(getCol(), getRow() - 1) && !player.hasPiece(getCol(), getRow() - 1) && !opponent.hasPiece(getCol(), getRow() - 1))
                        moves.add(new Cell(getCol(), getRow() - 1));

                    //if this is the first move the pawn can move 2 spaces
                    if (PlayerHelper.hasBounds(getCol(), getRow() - 2) && !hasMoved() && !player.hasPiece(getCol(), getRow() - 2) && !opponent.hasPiece(getCol(), getRow() - 2))
                        moves.add(new Cell(getCol(), getRow() - 2));

                    //if there is an opponent piece diagonally we can capture
                    if (opponent.hasPiece(getCol() - 1, getRow() - 1))
                        moves.add(new Cell(getCol() - 1, getRow() - 1));
                    if (opponent.hasPiece(getCol() + 1, getRow() - 1))
                        moves.add(new Cell(getCol() + 1, getRow() - 1));

                } else {

                    //pawn can move forward as long as there is no piece in front of it
                    if (PlayerHelper.hasBounds(getCol(), getRow() + 1) && !player.hasPiece(getCol(), getRow() + 1) && !opponent.hasPiece(getCol(), getRow() + 1))
                        moves.add(new Cell(getCol(), getRow() + 1));

                    //if this is the first move the pawn can move 2 spaces
                    if (PlayerHelper.hasBounds(getCol(), getRow() + 2) && !hasMoved() && !player.hasPiece(getCol(), getRow() + 2) && !opponent.hasPiece(getCol(), getRow() + 2))
                        moves.add(new Cell(getCol(), getRow() + 2));

                    //if there is an opponent piece diagonally we can capture
                    if (opponent.hasPiece(getCol() - 1, getRow() + 1))
                        moves.add(new Cell(getCol() - 1, getRow() + 1));
                    if (opponent.hasPiece(getCol() + 1, getRow() + 1))
                        moves.add(new Cell(getCol() + 1, getRow() + 1));

                }
                break;

            case Rook:
                break;

            case Queen:
                break;

            case King:
                break;

            case Bishop:
                break;

            case Knight:
                break;
        }

        return moves;
    }

    public boolean hasTarget(List<Object3D> targets) {

        for (int i = 0; i < targets.size(); i++) {

            final int col = PlayerHelper.getCol(targets.get(i).getX());
            final int row = PlayerHelper.getRow(targets.get(i).getZ());

            if (super.hasLocation(col, row))
                return true;
        }

        return false;
    }
}