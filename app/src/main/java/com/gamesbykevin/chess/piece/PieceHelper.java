package com.gamesbykevin.chess.piece;

import com.gamesbykevin.androidframeworkv2.base.Cell;
import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.players.Player;

import java.util.List;

import static com.gamesbykevin.chess.piece.Piece.HEIGHT_MAX;
import static com.gamesbykevin.chess.piece.Piece.VELOCITY;
import static com.gamesbykevin.chess.players.PlayerHelper.Y;

/**
 * Created by Kevin on 10/29/2017.
 */

public class PieceHelper {

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

    public static final float[][] BONUS_QUEEN = {
        {-2,-1,-1, -.5f, -.5f,-1,-1,-2},
        {-1,  0,  0,  0,  0,  0,  0,-1},
        {-1,  0,  .5f,  .5f,  .5f,  .5f,  0,-1},
        { -.5f,  0,  .5f,  .5f,  .5f,  .5f,  0, -.5f},
        {  0,  0,  .5f,  .5f,  .5f,  .5f,  0, -.5f},
        {-1,  .5f,  .5f,  .5f,  .5f,  .5f,  0,-1},
        {-1,  0,  .5f,  0,  0,  0,  0,-1},
        {-2,-1,-1, -.5f, -.5f,-1,-1,-2}
    };

    public static final float[][] BONUS_KING = {
        {-3,-4,-4,-5,-5,-4,-4,-3},
        {-3,-4,-4,-5,-5,-4,-4,-3},
        {-3,-4,-4,-5,-5,-4,-4,-3},
        {-3,-4,-4,-5,-5,-4,-4,-3},
        {-2,-3,-3,-4,-4,-3,-3,-2},
        {-1,-2,-2,-2,-2,-2,-2,-1},
        {2, 2,  0,  0,  0,  0, 2, 2},
        {2, 3, 1,  0,  0, 1, 3, 2}
    };

    public static final float[][] BONUS_ROOK = {
        {0,  0,  0,  0,  0,  0,  0,  0},
        {  .5f, 1, 1, 1, 1, 1, 1,  .5f},
        { -.5f,  0,  0,  0,  0,  0,  0, -.5f},
        { -.5f,  0,  0,  0,  0,  0,  0, -.5f},
        { -.5f,  0,  0,  0,  0,  0,  0, -.5f},
        { -.5f,  0,  0,  0,  0,  0,  0, -.5f},
        { -.5f,  0,  0,  0,  0,  0,  0, -.5f},
        {  0,  0,  0,  .5f,  .5f,  0,  0,  0}
    };

    public static final float[][] BONUS_BISHOP = {
        {-2,-1,-1,-1,-1,-1,-1,-2},
        {-1,  0,  0,  0,  0,  0,  0,-1},
        {-1,  0,  .5f, 1, 1,  .5f,  0,-1},
        {-1,  .5f,  .5f, 1, 1,  .5f,  .5f,-1},
        {-1,  0, 1, 1, 1, 1,  0,-1},
        {-1, 1, 1, 1, 1, 1, 1,-1},
        {-1,  .5f,  0,  0,  0,  0,  .5f,-1},
        {-2,-1,-1,-1,-1,-1,-1,-2}
    };

    public static final float[][] BONUS_KNIGHT = {
        {-5,-4,-3,-3,-3,-3,-4,-5},
        {-4,-2,  0,  0,  0,  0,-2,-4},
        {-3,  0, 1, 1.5f, 1.5f, 1,  0,-3},
        {-3,  .5f, 1.5f, 2, 2, 1.5f,  .5f,-3},
        {-3,  0, 1.5f, 2, 2, 1.5f,  0,-3},
        {-3,  .5f, 1, 1.5f, 1.5f, 1,  .5f,-3},
        {-4,-2,  0,  .5f,  .5f,  0,-2,-4},
        {-5,-4,-3,-3,-3,-3,-4,-5}
    };

    public static final float[][] BONUS_PAWN = {
        {0,0,0,0,0,0,0,0},
        {5,5,5,5,5,5,5,5},
        {1,1,2,3,3,2,1,1},
        {.5f,.5f,1,2.5f,2.5f,1,.5f,.5f},
        {0,0,0,2,2,0,0,0},
        {.5f,-.5f,-1,0,0,-1,-.5f,.5f},
        {.5f,1,1,-2,-2,1,1,.5f},
        {0,0,0,0,0,0,0,0}
    };

    public static Piece copy(Piece piece) {

        Piece copy = new Piece(piece.getType(), (int)piece.getCol(), (int)piece.getRow());
        copy.setMoved(piece.hasMoved());
        copy.setCaptured(piece.isCaptured());
        copy.setPassant(piece.hasPassant());
        copy.destX = piece.destX;
        copy.destZ = piece.destZ;
        copy.startX = piece.startX;
        copy.startZ = piece.startZ;
        copy.setObject3D(piece.getObject3D().clone());

        return copy;
    }

    public static void move(Piece piece) {

        //if the piece is jumping
        if (piece.isJumping()) {

            //change the height at the start
            if (piece.getObject3D().getX() == piece.startX && piece.getObject3D().getZ() == piece.startZ) {

                //if we aren't at our desired height yet
                if (piece.getObject3D().getY() < HEIGHT_MAX) {

                    //move the 3d model up
                    piece.getObject3D().setY(piece.getObject3D().getY() + VELOCITY);

                    //don't do anything else yet
                    return;

                } else {

                    //assign desired height
                    piece.getObject3D().setY(HEIGHT_MAX);
                }
            }

            //change the height at the destination
            if (piece.getObject3D().getX() == piece.destX && piece.getObject3D().getZ() == piece.destZ) {

                if (piece.getObject3D().getY() > Y) {

                    //move the 3d model down
                    piece.getObject3D().setY(piece.getObject3D().getY() - VELOCITY);

                    //don't do anything else yet
                    return;

                } else {

                    piece.getObject3D().setY(Y);
                }
            }
        }

        //place at the destination if close enough
        if (piece.getObject3D().getX() > piece.destX && piece.getObject3D().getX() - piece.destX < VELOCITY)
            piece.getObject3D().setX(piece.destX);
        if (piece.destX > piece.getObject3D().getX() && piece.destX - piece.getObject3D().getX() < VELOCITY)
            piece.getObject3D().setX(piece.destX);
        if (piece.getObject3D().getZ() > piece.destZ && piece.getObject3D().getZ() - piece.destZ < VELOCITY)
            piece.getObject3D().setZ(piece.destZ);
        if (piece.destZ > piece.getObject3D().getZ() && piece.destZ - piece.getObject3D().getZ() < VELOCITY)
            piece.getObject3D().setZ(piece.destZ);

        //update location of 3d model
        if (piece.getObject3D().getX() < piece.destX) {
            piece.getObject3D().setX(piece.getObject3D().getX() + VELOCITY);
        } else if (piece.getObject3D().getX() > piece.destX) {
            piece.getObject3D().setX(piece.getObject3D().getX() - VELOCITY);
        }

        if (piece.getObject3D().getZ() < piece.destZ) {
            piece.getObject3D().setZ(piece.getObject3D().getZ() + VELOCITY);
        } else if (piece.getObject3D().getZ() > piece.destZ) {
            piece.getObject3D().setZ(piece.getObject3D().getZ() - VELOCITY);
        }

        //if we are at our destination, we are no longer jumping
        if (piece.hasDestination())
            piece.setJumping(false);
    }

    public static void checkForCheck(Piece piece, List<Cell> moves, Player player, Player opponent) {

        //check every move in the list
        for (int i = 0; i < moves.size(); i++) {

            //check if this move is safe
            final boolean result = checkForCheck(piece, moves.get(i), player, opponent);

            //if the move is not safe, remove
            if (!result) {
                moves.remove(i);
                i--;
            }
        }
    }

    public static boolean checkForCheck(Piece piece, Cell move, Player player, Player opponent) {

        //assume move is safe (for now...)
        boolean result = true;

        //get our king piece
        Piece king = player.getPiece(Type.King);

        //this should always happen as the king has to exist to play chess
        if (king != null) {

            //save the source
            final int sourceCol = (int)piece.getCol();
            final int sourceRow = (int)piece.getRow();

            //update to match move
            piece.setCol(move);
            piece.setRow(move);

            //is there a captured piece
            Piece captured = opponent.getPiece((int)move.getCol(), (int)move.getRow());

            //if the piece exists, mark it captured
            if (captured != null)
                captured.setCaptured(true);

            //check every opponent piece
            for (int index = 0; index < opponent.getPieceCount(); index++) {

                //get the current piece
                Piece tmp = opponent.getPiece(index, false);

                //skip pieces that don't exist
                if (tmp == null)
                    continue;

                //get the list of moves for this piece
                List<Cell> tmpMoves = tmp.getMoves(opponent, player, false);

                //check each move
                for (int x = 0; x < tmpMoves.size(); x++) {

                    if (tmpMoves.get(x).hasLocation(king)) {
                        result = false;
                        break;
                    }
                }

                tmpMoves.clear();
                tmpMoves = null;
            }

            //now that we are done, un-capture the piece
            if (captured != null)
                captured.setCaptured(false);

            //restore original position to chess piece
            piece.setCol(sourceCol);
            piece.setRow(sourceRow);
        }

        //return our result
        return result;
    }
}