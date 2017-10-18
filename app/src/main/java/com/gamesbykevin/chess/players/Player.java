package com.gamesbykevin.chess.players;

import com.gamesbykevin.chess.piece.Piece;

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

    public List<Piece> getPieces() {
        return this.pieces;
    }
}
