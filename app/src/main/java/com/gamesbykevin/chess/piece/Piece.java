package com.gamesbykevin.chess.piece;

import com.gamesbykevin.androidframeworkv2.base.Cell;
import com.gamesbykevin.androidframeworkv2.base.Disposable;
import com.gamesbykevin.chess.players.Player;
import com.gamesbykevin.chess.players.PlayerHelper;
import com.gamesbykevin.chess.piece.PieceHelper.Type;

import org.rajawali3d.Object3D;

import java.util.ArrayList;
import java.util.List;

import static com.gamesbykevin.chess.players.PlayerHelper.COLS;
import static com.gamesbykevin.chess.players.PlayerHelper.ROWS;
import static com.gamesbykevin.chess.players.PlayerHelper.Y;

/**
 * Created by Kevin on 10/15/2017.
 */
public class Piece extends Cell implements Disposable {

    //list of valid moves by this piece
    private List<Cell> options;

    //the 3d model for this piece
    private Object3D object3D;

    //type of chess piece
    private Type type;

    //has this piece moved yet?
    private boolean moved = false;

    //is this piece captured
    private boolean captured = false;

    //is this piece jumping?
    private boolean jumping = false;

    //can the pawn be captured via "en passant"?
    private boolean passant = false;

    /**
     * The speed at which a chess piece can move
     */
    public static final float VELOCITY = .025f;

    /**
     * Maximum height the knight is allowed to go
     */
    protected static final float HEIGHT_MAX = .4f;

    //x,y coordinates where we want the chess piece to go
    protected float destX, destZ;

    //the starting point before we move the chess piece
    protected float startX, startZ;

    //where is this piece coming from
    protected int sourceCol, sourceRow;

    public Piece(Type type, float col, float row) {

        setType(type);

        //assign location
        super.setCol(col);
        super.setRow(row);

        //default to invalid destination
        this.destX = -1;
        this.destZ = -1;
    }

    @Override
    public void dispose() {

        if (options != null)
            options.clear();
        if (object3D != null)
            object3D.destroy();

        this.options = null;
        this.object3D = null;
        this.type = null;
    }

    public void setSourceCol(final float sourceCol) {
        setSourceCol((int)sourceCol);
    }

    public void setSourceCol(final int sourceCol) {
        this.sourceCol = sourceCol;
    }

    public void setSourceRow(final float sourceRow) {
        setSourceRow((int)sourceRow);
    }

    public void setSourceRow(final int sourceRow) {
        this.sourceRow = sourceRow;
    }

    public int getSourceCol() {
        return this.sourceCol;
    }

    public int getSourceRow() {
        return this.sourceRow;
    }

    public void setJumping(final boolean jumping) {
        this.jumping = jumping;
    }

    public boolean isJumping() {
        return this.jumping;
    }

    public void setPassant(final boolean passant) {
        this.passant = passant;
    }

    public boolean hasPassant() {
        return this.passant;
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

    public List<Cell> getMoves(Player player, Player opponent, final boolean performCheck) {

        if (this.options == null)
            this.options = new ArrayList<>();

        //clear any existing moves
        this.options.clear();

        //the starting point
        final int startCol = (int)getCol();
        final int startRow = (int)getRow();

        //the moves will depend on the chess piece type
        switch (getType()) {

            case Pawn:

                //the rows will be the direction we are heading
                int singleRow = player.hasDirection(Player.Direction.North) ? -1 : 1;
                int doubleRow = player.hasDirection(Player.Direction.North) ? -2 : 2;

                //pawn can move forward as long as there is no piece in front of it
                if (PlayerHelper.hasBounds(startCol, startRow + singleRow) && !player.hasPiece(startCol, startRow + singleRow) && !opponent.hasPiece(startCol, startRow + singleRow))
                    options.add(new Cell(startCol, startRow + singleRow));

                //if this is the first move the pawn can move 2 spaces as long as nothing is in front of it
                if (PlayerHelper.hasBounds(startCol, startRow + doubleRow) && !player.hasPiece(startCol, startRow + doubleRow) && !opponent.hasPiece(startCol, startRow + doubleRow) && !player.hasPiece(startCol, startRow + singleRow) && !opponent.hasPiece(startCol, startRow + singleRow) && !hasMoved())
                    options.add(new Cell(startCol, startRow + doubleRow));

                //if there is an opponent piece diagonally we can capture
                if (opponent.hasPiece(startCol - 1, startRow + singleRow))
                    options.add(new Cell(startCol - 1, startRow + singleRow));
                if (opponent.hasPiece(startCol + 1, startRow + singleRow))
                    options.add(new Cell(startCol + 1, startRow + singleRow));


                //check for NW/SW capture via "en passant"
                if (PlayerHelper.hasBounds(startCol - 1, startRow + singleRow) && !opponent.hasPiece(startCol - 1, startRow + singleRow)) {

                    //get neighbor piece
                    Piece piece = opponent.getPiece(startCol - 1, startRow);

                    //we can only capture pawn's via "en passant" after their first move
                    if (piece != null && piece.getType() == Type.Pawn && piece.hasPassant())
                        options.add(new Cell(startCol - 1, startRow + singleRow));
                }

                //check for NE/SE capture via "en passant"
                if (PlayerHelper.hasBounds(startCol + 1, startRow + + singleRow) && !opponent.hasPiece(startCol + 1, startRow + singleRow)) {

                    //get neighbor piece
                    Piece piece = opponent.getPiece(startCol + 1, startRow);

                    //we can only capture pawn's via "en passant" after their first move
                    if (piece != null && piece.getType() == Type.Pawn && piece.hasPassant())
                        options.add(new Cell(startCol + 1, startRow + singleRow));
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
                                options.add(new Cell(col, row));
                                break;
                            }

                            //any other scenario it is a valid move
                            options.add(new Cell(col, row));

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
                                options.add(new Cell(col, row));
                                break;
                            }

                            //add the valid move
                            options.add(new Cell(col, row));

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
                            options.add(new Cell(col, row));
                            continue;
                        }

                        //add the valid move
                        options.add(new Cell(col, row));
                    }
                }

                //if the King hasn't moved, check if we can castle
                if (!hasMoved()) {

                    //we can't be in check if castling and the king has to be in the original position
                    if (!player.hasCheck() && getCol() == 4) {

                        //which row has the rooks
                        final int row = player.hasDirection(Player.Direction.North) ? ROWS - 1 : 0;

                        //get the west piece
                        Piece west = player.getPiece(0, row);

                        //the rook also can't move previously
                        if (west != null && !west.hasMoved()) {

                            //is the move valid
                            boolean valid = true;

                            //make sure no pieces in between
                            for (int col = 1; col < getCol(); col++) {

                                if (player.hasPiece(col, row) || opponent.hasPiece(col, row)) {
                                    valid = false;
                                    break;
                                }
                            }

                            //if valid add move
                            if (valid)
                                options.add(new Cell(getCol() - 2, getRow()));
                        }

                        //get the east piece
                        Piece east = player.getPiece(COLS - 1, row);

                        //the rook also can't move previous
                        if (east != null && !east.hasMoved()) {

                            //is the move valid
                            boolean valid = true;

                            //make sure no pieces in between
                            for (int col = (int)getCol() + 1; col < COLS - 1; col++) {

                                if (player.hasPiece(col, row) || opponent.hasPiece(col, row)) {
                                    valid = false;
                                    break;
                                }
                            }

                            //if valid add move
                            if (valid)
                                options.add(new Cell(getCol() + 2, getRow()));
                        }
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
                                options.add(new Cell(col, row));
                                break;
                            }

                            //add the valid move
                            options.add(new Cell(col, row));

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
                    options.add(new Cell(col, row));

                col = startCol - 1;
                row = startRow - 2;

                //make sure in bounds and we don't already have a chess piece there
                if (PlayerHelper.hasBounds(col, row) && !player.hasPiece(col, row))
                    options.add(new Cell(col, row));

                col = startCol + 1;
                row = startRow - 2;

                //make sure in bounds and we don't already have a chess piece there
                if (PlayerHelper.hasBounds(col, row) && !player.hasPiece(col, row))
                    options.add(new Cell(col, row));

                col = startCol + 2;
                row = startRow - 1;

                //make sure in bounds and we don't already have a chess piece there
                if (PlayerHelper.hasBounds(col, row) && !player.hasPiece(col, row))
                    options.add(new Cell(col, row));

                col = startCol + 2;
                row = startRow + 1;

                //make sure in bounds and we don't already have a chess piece there
                if (PlayerHelper.hasBounds(col, row) && !player.hasPiece(col, row))
                    options.add(new Cell(col, row));

                col = startCol + 1;
                row = startRow + 2;

                //make sure in bounds and we don't already have a chess piece there
                if (PlayerHelper.hasBounds(col, row) && !player.hasPiece(col, row))
                    options.add(new Cell(col, row));

                col = startCol - 1;
                row = startRow + 2;

                //make sure in bounds and we don't already have a chess piece there
                if (PlayerHelper.hasBounds(col, row) && !player.hasPiece(col, row))
                    options.add(new Cell(col, row));

                col = startCol - 2;
                row = startRow + 1;

                //make sure in bounds and we don't already have a chess piece there
                if (PlayerHelper.hasBounds(col, row) && !player.hasPiece(col, row))
                    options.add(new Cell(col, row));
                break;
        }

        //do we check and make sure we aren't putting ourselves in check?
        if (performCheck)
            PieceHelper.checkForCheck(this, options, player, opponent);

        //return our list of moves
        return options;
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
}