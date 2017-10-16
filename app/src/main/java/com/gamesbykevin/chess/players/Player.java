package com.gamesbykevin.chess.players;

import com.gamesbykevin.chess.piece.Piece;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin on 10/15/2017.
 */

public class Player {

    private List<Piece> pieces;

    public Player() {
        this.pieces = new ArrayList<>();
    }

    public List<Piece> getPieces() {
        return this.pieces;
    }
}
