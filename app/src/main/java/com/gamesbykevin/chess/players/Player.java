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

    //direction we are heading towards
    private final Direction direction;

    //is the player in check
    private boolean check = false;

    //is the game over
    private boolean checkMate = false;

    //does the player have any valid moves remaining?
    private boolean stalemate = false;

    protected Player(final boolean human, final Direction direction) {
        this.human = human;
        this.direction = direction;
        this.pieces = new ArrayList<>();

        setCheck(false);
        setCheckMate(false);
        setStalemate(false);
    }

    public void setStalemate(final boolean stalemate) {
        this.stalemate = stalemate;
    }

    public boolean hasStalemate() {
        return this.stalemate;
    }

    public void setCheck(final boolean check) {
        this.check = check;
    }

    public boolean hasCheck() {
        return this.check;
    }

    public void setCheckMate(final boolean checkMate) {
        this.checkMate = checkMate;
    }

    public boolean hasCheckMate() {
        return this.checkMate;
    }

    /**
     * Players need to implement different logic upon update
     */
    public abstract void update(Players players);

    /**
     * In case we need to reset anything
     */
    public abstract void reset();

    public boolean hasDirection(final Direction direction) {
        return (this.direction == direction);
    }

    public boolean isHuman() {
        return this.human;
    }

    public Piece getPiece(Piece.Type type) {

        for (int i = 0; i < getPieceCount(); i++) {

            Piece piece = getPiece(i, true);

            if (piece != null && piece.getType() == type)
                return piece;
        }

        return null;
    }

    public Piece getPiece(int col, int row) {

        for (int i = 0; i < getPieceCount(); i++) {

            Piece piece = getPiece(i, true);

            if (!piece.isCaptured() && piece.hasLocation(col, row))
                return piece;
        }

        return null;
    }

    public Piece getPiece(int index, boolean ignore) {

        Piece piece = getPieces().get(index);

        if (ignore) {
            return piece;
        } else {
            if (piece.isCaptured()) {
                return null;
            } else {
                return piece;
            }
        }
    }

    public void removeAllPieces() {
        getPieces().clear();
    }

    public void addPiece(Piece piece) {
        getPieces().add(piece);
    }

    public boolean hasPiece(int col, int row) {

        return (getPiece(col, row) != null);
    }

    public int getPieceCount() {
        return this.pieces.size();
    }

    private List<Piece> getPieces() {
        return this.pieces;
    }

    public int calculateScore() {

        //keep track of our score
        int score = 0;

        //check
        for (int index = 0; index < getPieceCount(); index++) {

            Piece piece = getPiece(index, false);

            //add each chess piece score to the total
            if (piece != null) {

                //add to the score based on the piece type
                score += piece.getType().getScore();

                //also add bonus score depending where the piece is located
                score += Piece.BONUS_SCORE[(int)piece.getRow()][(int)piece.getCol()];
            }
        }

        //return our calculated score
        return score;
    }
}