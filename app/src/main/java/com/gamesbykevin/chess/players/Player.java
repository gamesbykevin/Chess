package com.gamesbykevin.chess.players;

import com.gamesbykevin.chess.piece.Piece;

import org.rajawali3d.Object3D;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin on 10/15/2017.
 */

public abstract class Player {

    private List<Piece> pieces;

    private final boolean human;

    public enum Direction {
        North, South
    }

    private final Direction direction;

    protected Player(final boolean human, final Direction direction) {
        this.human = human;
        this.direction = direction;
        this.pieces = new ArrayList<>();
    }

    public boolean hasDirection(final Direction direction) {
        return (this.direction == direction);
    }

    public boolean isHuman() {
        return this.human;
    }

    public Object3D getPiece(double col, double row) {

        for (int i = 0; i < getPieces().size(); i++) {

            Piece piece = getPieces().get(i);

            if (piece.hasLocation(col, row))
                return piece.getObj();
        }

        return null;
    }

    public boolean hasPiece(double col, double row) {

        return (getPiece(col, row) != null);
    }

    public List<Piece> getPieces() {
        return this.pieces;
    }
}
