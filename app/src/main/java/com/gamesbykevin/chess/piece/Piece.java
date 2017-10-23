package com.gamesbykevin.chess.piece;

import com.gamesbykevin.androidframeworkv2.base.Cell;
import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.players.Player;
import com.gamesbykevin.chess.players.PlayerHelper;

import org.rajawali3d.Object3D;

import java.util.ArrayList;
import java.util.List;

import static com.gamesbykevin.chess.players.PlayerHelper.Y;
import static com.gamesbykevin.chess.players.PlayerHelper.getRow;

/**
 * Created by Kevin on 10/15/2017.
 */
public class Piece extends Cell {

    //list of valid moves by this piece
    private List<Cell> moves;

    //the 3d model for this piece
    private Object3D object3D;

    //type of chess piece
    private Type type;

    //has this piece moved yet?
    private boolean moved = false;

    //is this piece captured
    private boolean captured = false;

    /**
     * The speed at which a chess piece can move
     */
    public static final float VELOCITY = .025f;

    /**
     * Maximum height the knight is allowed to go
     */
    private static final float HEIGHT_MAX = .4f;

    /**
     * Different types of pieces and the 3d model that shapes the piece
     */
    public enum Type {
        Pawn(R.raw.pawn_stl, 10),
        Bishop(R.raw.bishop_stl, 30),
        Knight(R.raw.knight_stl, 30),
        Rook(R.raw.rook_stl, 50),
        Queen(R.raw.queen_stl, 90),
        King(R.raw.king_stl, 99999);

        private final int resId;

        private final int score;

        Type(int resId, int score) {
            this.resId = resId;
            this.score = score;
        }

        public int getResId() {
            return this.resId;
        }

        public int getScore() {
            return this.score;
        }
    }

    /**
     * These are the bonus scores depending on the location of the chess piece
     */
    public static final int[][] BONUS_SCORE = {
        {1,1,1,1,1,1,1,1},
        {2,2,2,2,2,2,2,2},
        {2,2,3,3,3,3,2,2},
        {2,2,3,4,4,3,2,2},
        {2,2,3,4,4,3,2,2},
        {2,2,3,3,3,3,2,2},
        {2,2,2,2,2,2,2,2},
        {1,1,1,1,1,1,1,1}
    };

    //x,y coordinates where we want the chess piece to go
    private float destX, destZ;

    //the starting point before we move the chess piece
    private float startX, startZ;

    public Piece(Object3D object3D, Type type, float col, float row) {

        setObject3D(object3D);
        setType(type);

        //assign location
        super.setCol(col);
        super.setRow(row);

        //default to invalid destination
        this.destX = -1;
        this.destZ = -1;
    }

    public void setObject3D(final Object3D object3D) {
        this.object3D = object3D;
    }

    public void setType(final Type type) {
        this.type = type;
    }

    public Object3D getObject3D() {
        return this.object3D;
    }

    public Type getType() {
        return this.type;
    }

    public void setCaptured(final boolean captured) {
        this.captured = captured;
    }

    public boolean isCaptured() {
        return this.captured;
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

        //the starting point
        final int startCol = (int)getCol();
        final int startRow = (int)getRow();

        //the moves will depend on the chess piece type
        switch (getType()) {

            case Pawn:

                if (player.hasDirection(Player.Direction.North)) {

                    //pawn can move forward as long as there is no piece in front of it
                    if (PlayerHelper.hasBounds(startCol, startRow - 1) && !player.hasPiece(startCol, startRow - 1) && !opponent.hasPiece(startCol, startRow - 1))
                        moves.add(new Cell(startCol, startRow - 1));

                    //if this is the first move the pawn can move 2 spaces as long as nothing is in front of it
                    if (PlayerHelper.hasBounds(startCol, startRow - 2) && !player.hasPiece(startCol, startRow - 2) && !opponent.hasPiece(startCol, startRow - 2) && !player.hasPiece(startCol, startRow - 1) && !opponent.hasPiece(startCol, startRow - 1) && !hasMoved())
                        moves.add(new Cell(startCol, startRow - 2));

                    //if there is an opponent piece diagonally we can capture
                    if (opponent.hasPiece(startCol - 1, startRow - 1))
                        moves.add(new Cell(startCol - 1, startRow - 1));
                    if (opponent.hasPiece(startCol + 1, startRow - 1))
                        moves.add(new Cell(startCol + 1, startRow - 1));

                } else {

                    //pawn can move forward as long as there is no piece in front of it
                    if (PlayerHelper.hasBounds(startCol, startRow + 1) && !player.hasPiece(startCol, startRow + 1) && !opponent.hasPiece(startCol, startRow + 1))
                        moves.add(new Cell(startCol, startRow + 1));

                    //if this is the first move the pawn can move 2 spaces
                    if (PlayerHelper.hasBounds(startCol, startRow + 2) && !hasMoved() && !player.hasPiece(startCol, startRow + 2) && !opponent.hasPiece(startCol, startRow + 2) && !player.hasPiece(startCol, startRow + 1) && !opponent.hasPiece(startCol, startRow + 1))
                        moves.add(new Cell(startCol, startRow + 2));

                    //if there is an opponent piece diagonally we can capture
                    if (opponent.hasPiece(startCol - 1, startRow + 1))
                        moves.add(new Cell(startCol - 1, startRow + 1));
                    if (opponent.hasPiece(startCol + 1, startRow + 1))
                        moves.add(new Cell(startCol + 1, startRow + 1));
                }
                break;

            case Rook:

                //check all directions
                for (int velocityCol = -1; velocityCol <= 1; velocityCol++) {
                    for (int velocityRow = -1; velocityRow <= 1; velocityRow++) {

                        //we can only move north, south, east, west
                        if (velocityCol != 0 && velocityRow != 0 || velocityCol == 0 && velocityRow == 0)
                            continue;

                        //the location to check
                        int col = startCol + velocityCol;
                        int row = startRow + velocityRow;

                        //continue until we hit a stop
                        while (true) {

                            //skip if we are out of bounds
                            if (!PlayerHelper.hasBounds(col, row))
                                break;

                            //if the player has run into its own pieces we stop
                            if (player.hasPiece(col, row))
                                break;

                            //we also stop if we run into an opponent piece, and add the move
                            if (opponent.hasPiece(col, row)) {
                                moves.add(new Cell(col, row));
                                break;
                            }

                            //any other scenario it is a valid move
                            moves.add(new Cell(col, row));

                            //move in the current direction
                            col += velocityCol;
                            row += velocityRow;
                        }
                    }
                }
                break;

            case Queen:

                //check all directions
                for (int velocityCol = -1; velocityCol <= 1; velocityCol++) {
                    for (int velocityRow = -1; velocityRow <= 1; velocityRow++) {

                        //skip no velocity
                        if (velocityCol == 0 && velocityRow == 0)
                            continue;

                        //the location to check
                        int col = startCol + velocityCol;
                        int row = startRow + velocityRow;

                        //continue until we hit a stop
                        while (true) {

                            //skip if we are out of bounds
                            if (!PlayerHelper.hasBounds(col, row))
                                break;

                            //if the player has run into its own pieces we stop
                            if (player.hasPiece(col, row))
                                break;

                            //we also stop if we run into an opponent piece, and add the move
                            if (opponent.hasPiece(col, row)) {
                                moves.add(new Cell(col, row));
                                break;
                            }

                            //add the valid move
                            moves.add(new Cell(col, row));

                            //move in the current direction
                            col += velocityCol;
                            row += velocityRow;
                        }
                    }
                }
                break;

            case King:

                //check all diagonal directions
                for (int velocityCol = -1; velocityCol <= 1; velocityCol++) {
                    for (int velocityRow = -1; velocityRow <= 1; velocityRow++) {

                        //we only want to head diagonally
                        if (velocityCol == 0 && velocityRow == 0)
                            continue;

                        //the location to check
                        int col = startCol + velocityCol;
                        int row = startRow + velocityRow;

                        //skip if we are out of bounds
                        if (!PlayerHelper.hasBounds(col, row))
                            continue;

                        //if the player has run into its own pieces we stop
                        if (player.hasPiece(col, row))
                            continue;

                        //we also stop if we run into an opponent piece, and add the move
                        if (opponent.hasPiece(col, row)) {
                            moves.add(new Cell(col, row));
                            continue;
                        }

                        //add the valid move
                        moves.add(new Cell(col, row));
                    }
                }
                break;

            case Bishop:

                //check all diagonal directions
                for (int velocityCol = -1; velocityCol <= 1; velocityCol++) {
                    for (int velocityRow = -1; velocityRow <= 1; velocityRow++) {

                        //we only want to head diagonally
                        if (velocityCol == 0 || velocityRow == 0)
                            continue;

                        //the first location we check
                        int col = startCol + velocityCol;
                        int row = startRow + velocityRow;

                        while (true) {

                            //skip if we are out of bounds
                            if (!PlayerHelper.hasBounds(col, row))
                                break;

                            //if the player has run into its own pieces we stop
                            if (player.hasPiece(col, row))
                                break;

                            //we also stop if we run into an opponent piece, and add the move
                            if (opponent.hasPiece(col, row)) {
                                moves.add(new Cell(col, row));
                                break;
                            }

                            //add the valid move
                            moves.add(new Cell(col, row));

                            //move diagonally
                            col += velocityCol;
                            row += velocityRow;
                        }
                    }
                }
                break;

            case Knight:

                int col = startCol - 2;
                int row = startRow - 1;

                //make sure in bounds and we don't already have a chess piece there
                if (PlayerHelper.hasBounds(col, row) && !player.hasPiece(col, row))
                    moves.add(new Cell(col, row));

                col = startCol - 1;
                row = startRow - 2;

                //make sure in bounds and we don't already have a chess piece there
                if (PlayerHelper.hasBounds(col, row) && !player.hasPiece(col, row))
                    moves.add(new Cell(col, row));

                col = startCol + 1;
                row = startRow - 2;

                //make sure in bounds and we don't already have a chess piece there
                if (PlayerHelper.hasBounds(col, row) && !player.hasPiece(col, row))
                    moves.add(new Cell(col, row));

                col = startCol + 2;
                row = startRow - 1;

                //make sure in bounds and we don't already have a chess piece there
                if (PlayerHelper.hasBounds(col, row) && !player.hasPiece(col, row))
                    moves.add(new Cell(col, row));

                col = startCol + 2;
                row = startRow + 1;

                //make sure in bounds and we don't already have a chess piece there
                if (PlayerHelper.hasBounds(col, row) && !player.hasPiece(col, row))
                    moves.add(new Cell(col, row));

                col = startCol + 1;
                row = startRow + 2;

                //make sure in bounds and we don't already have a chess piece there
                if (PlayerHelper.hasBounds(col, row) && !player.hasPiece(col, row))
                    moves.add(new Cell(col, row));

                col = startCol - 1;
                row = startRow + 2;

                //make sure in bounds and we don't already have a chess piece there
                if (PlayerHelper.hasBounds(col, row) && !player.hasPiece(col, row))
                    moves.add(new Cell(col, row));

                col = startCol - 2;
                row = startRow + 1;

                //make sure in bounds and we don't already have a chess piece there
                if (PlayerHelper.hasBounds(col, row) && !player.hasPiece(col, row))
                    moves.add(new Cell(col, row));
                break;
        }

        return moves;
    }

    public void setDestinationCoordinates(float destX, float destZ) {

        //assign the destination
        this.destX = destX;
        this.destZ = destZ;

        //assign the starting point
        this.startX = (float)getObject3D().getX();
        this.startZ = (float)getObject3D().getZ();
    }

    public boolean hasDestination() {
        return (getObject3D().getX() == destX && getObject3D().getZ() == destZ && getObject3D().getY() == Y);
    }

    public void move() {

        //only change height for the knight
        if (getType() == Type.Knight) {

            //change the height at the start
            if (getObject3D().getX() == startX && getObject3D().getZ() == startZ) {

                //if we aren't at our desired height yet
                if (getObject3D().getY() < HEIGHT_MAX) {

                    //move the 3d model up
                    getObject3D().setY(getObject3D().getY() + VELOCITY);

                    //don't do anything else yet
                    return;

                } else {

                    //assign desired height
                    getObject3D().setY(HEIGHT_MAX);
                }
            }

            //change the height at the destination
            if (getObject3D().getX() == destX && getObject3D().getZ() == destZ) {

                if (getObject3D().getY() > Y) {

                    //move the 3d model down
                    getObject3D().setY(getObject3D().getY() - VELOCITY);

                    //don't do anything else yet
                    return;

                } else {

                    getObject3D().setY(Y);
                }
            }
        }

        //place at the destination if close enough
        if (getObject3D().getX() > this.destX && getObject3D().getX() - this.destX < VELOCITY)
            getObject3D().setX(this.destX);
        if (this.destX > getObject3D().getX() && this.destX - getObject3D().getX() < VELOCITY)
            getObject3D().setX(this.destX);
        if (getObject3D().getZ() > this.destZ && getObject3D().getZ() - this.destZ < VELOCITY)
            getObject3D().setZ(this.destZ);
        if (this.destZ > getObject3D().getZ() && this.destZ - getObject3D().getZ() < VELOCITY)
            getObject3D().setZ(this.destZ);

        //update location of 3d model
        if (getObject3D().getX() < this.destX) {
            getObject3D().setX(getObject3D().getX() + VELOCITY);
        } else if (getObject3D().getX() > this.destX) {
            getObject3D().setX(getObject3D().getX() - VELOCITY);
        }

        if (getObject3D().getZ() < this.destZ) {
            getObject3D().setZ(getObject3D().getZ() + VELOCITY);
        } else if (getObject3D().getZ() > this.destZ) {
            getObject3D().setZ(getObject3D().getZ() - VELOCITY);
        }
    }
}