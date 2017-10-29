package com.gamesbykevin.chess.players;

import com.gamesbykevin.chess.piece.Piece;
import com.gamesbykevin.chess.util.UtilityHelper;

import java.util.ArrayList;
import java.util.List;

import static com.gamesbykevin.chess.players.PlayerHelper.COLS;
import static com.gamesbykevin.chess.players.PlayerHelper.ROWS;

/**
 * Created by Kevin on 10/15/2017.
 */
public abstract class Player {

    //list of pieces and its clone
    private List<Piece> pieces, clone;

    private final boolean human;

    public enum Direction {
        North, South
    }

    //direction we are heading towards
    private final Direction direction;

    //is the player in check
    private boolean check = false;

    protected Player(final boolean human, final Direction direction) {
        this.human = human;
        this.direction = direction;
        this.pieces = new ArrayList<>();
        this.clone = new ArrayList<>();

        setCheck(false);
    }

    public void restore() {

        //clear the list
        this.pieces.clear();

        //copy every piece to the existing list
        for (int i = 0; i < this.clone.size(); i++) {
            this.pieces.add(this.clone.get(i));
        }
    }

    /**
     * Clone all the chess pieces
     */
    public void copy() {

        //remove any existing pieces
        clone.clear();

        //copy every piece to the new list
        for (int i = 0; i < getPieceCount(); i++) {
            clone.add(getPiece(i, true).copy());
        }
    }

    public void setCheck(final boolean check) {
        this.check = check;
    }

    public boolean hasCheck() {
        return this.check;
    }

    /**
     * Players need to implement different logic upon update
     */
    public abstract void update(Players players);

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

                //add bonus score depending where the piece is located
                score += Piece.BONUS_SCORE[(int)piece.getRow()][(int)piece.getCol()];

                //this bonus only applies to pawns
                if (piece.getType() == Piece.Type.Pawn) {

                    //add extra bonus if the pawn can promote itself
                    if (this.direction == Direction.North) {

                        if (piece.getRow() == 0)
                            score += 2;
                        if (piece.getRow() == 1)
                            score += 1;

                    } else {

                        if (piece.getRow() == ROWS - 1)
                            score += 2;
                        if (piece.getRow() == ROWS - 2)
                            score += 1;

                    }
                }
            }
        }

        //return our calculated score
        return score;
    }
}