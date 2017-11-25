package com.gamesbykevin.chess.piece;

import com.gamesbykevin.androidframeworkv2.base.Cell;
import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.players.Player;
import com.gamesbykevin.chess.players.PlayerHelper;

import java.util.List;

import static com.gamesbykevin.chess.piece.Piece.HEIGHT_MAX;
import static com.gamesbykevin.chess.piece.Piece.VELOCITY;
import static com.gamesbykevin.chess.players.Player.Direction.North;
import static com.gamesbykevin.chess.players.PlayerHelper.ROWS;
import static com.gamesbykevin.chess.players.PlayerHelper.Y;
import static com.gamesbykevin.chess.players.PlayerHelper.reset;

/**
 * Created by Kevin on 10/29/2017.
 */

public class PieceHelper {

    /**
     * Different types of pieces and the 3d model that shapes the piece
     */
    public enum Type {
        Pawn(R.raw.pawn_stl, 100),
        Bishop(R.raw.bishop_stl, 330),
        Knight(R.raw.knight_stl, 320),
        Rook(R.raw.rook_stl, 500),
        Queen(R.raw.queen_stl, 900),
        King(R.raw.king_stl, 20000);

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

    public static final int[][] BONUS_QUEEN = {
        {-20,-10,-10, -5, -5,-10,-10,-20},
        {-10,  0,  0,  0,  0,  0,  0,-10},
        {-10,  0,  5,  5,  5,  5,  0,-10},
        { -5,  0,  5,  5,  5,  5,  0, -5},
        {  0,  0,  5,  5,  5,  5,  0, -5},
        {-10,  5,  5,  5,  5,  5,  0,-10},
        {-10,  0,  5,  0,  0,  0,  0,-10},
        {-20,-10,-10, -5, -5,-10,-10,-20}
    };

    public static final int[][] BONUS_KING = {
            {-30,-40,-40,-50,-50,-40,-40,-30},
            {-30,-40,-40,-50,-50,-40,-40,-30},
            {-30,-40,-40,-50,-50,-40,-40,-30},
            {-30,-40,-40,-50,-50,-40,-40,-30},
            {-20,-30,-30,-40,-40,-30,-30,-20},
            {-10,-20,-20,-20,-20,-20,-20,-10},
            { 20, 20,  0,  0,  0,  0, 20, 20},
            { 20, 30, 10,  0,  0, 10, 30, 20}    };

    public static final int[][] BONUS_ROOK = {
        {  0,  0,  0,  0,  0,  0,  0,  0},
        {  5, 10, 10, 10, 10, 10, 10,  5},
        { -5,  0,  0,  0,  0,  0,  0, -5},
        { -5,  0,  0,  0,  0,  0,  0, -5},
        { -5,  0,  0,  0,  0,  0,  0, -5},
        { -5,  0,  0,  0,  0,  0,  0, -5},
        { -5,  0,  0,  0,  0,  0,  0, -5},
        {  0,  0,  0,  5,  5,  0,  0,  0}
    };

    public static final int[][] BONUS_BISHOP = {
        {-20,-10,-10,-10,-10,-10,-10,-20},
        {-10,  0,  0,  0,  0,  0,  0,-10},
        {-10,  0,  5, 10, 10,  5,  0,-10},
        {-10,  5,  5, 10, 10,  5,  5,-10},
        {-10,  0, 10, 10, 10, 10,  0,-10},
        {-10, 10, 10, 10, 10, 10, 10,-10},
        {-10,  5,  0,  0,  0,  0,  5,-10},
        {-20,-10,-10,-10,-10,-10,-10,-20}
    };

    public static final int[][] BONUS_KNIGHT = {
        {-50,-40,-30,-30,-30,-30,-40,-50},
        {-40,-20,  0,  0,  0,  0,-20,-40},
        {-30,  0, 10, 15, 15, 10,  0,-30},
        {-30,  5, 15, 20, 20, 15,  5,-30},
        {-30,  0, 15, 20, 20, 15,  0,-30},
        {-30,  5, 10, 15, 15, 10,  5,-30},
        {-40,-20,  0,  5,  5,  0,-20,-40},
        {-50,-40,-30,-30,-30,-30,-40,-50}
    };

    public static final int[][] BONUS_PAWN = {
        {100,  100,  100,  100,  100,  100,  100,  100},
        {50, 50, 50, 50, 50, 50, 50, 50},
        {10, 10, 20, 30, 30, 20, 10, 10},
        { 5,  5, 10, 25, 25, 10,  5,  5},
        { 0,  0,  0, 20, 20,  0,  0,  0},
        { 5, -5,-10,  0,  0,-10, -5,  5},
        { 5, 10, 10,-20,-20, 10, 10,  5},
        { 0,  0,  0,  0,  0,  0,  0,  0}
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

    public static int getOrderScore(Player player, Player opponent, PlayerHelper.Move move) {

        int score = 0;

        //get the piece at the source
        Piece piece = player.getPiece(move.sourceCol, move.sourceRow);

        //add score based on the chess piece type
        score += piece.getType().getScore();

        //get the scores for both moves so we can compare
        final int score1 = getPositionScore(move.sourceCol, move.sourceRow, piece.getType(), player.getDirection());
        final int score2 = getPositionScore(move.destCol, move.destRow, piece.getType(), player.getDirection());

        //add or subtract bonus depending if we are moving to a better location
        score += (score2 - score1);

        //if a piece is captured, add that as a bonus as well
        if (move.pieceCaptured != null)
            score += move.pieceCaptured.getScore(opponent.getDirection());

        //return the score
        return score;
    }

    public static int getPositionScore(Piece piece, Player.Direction direction) {
        return getPositionScore((int)piece.getCol(), (int)piece.getRow(), piece.getType(), direction);
    }

    public static int getPositionScore(int col, int row, Type type, Player.Direction direction) {

        //our bonus array based on our position
        int[][] bonus;

        //add bonus score depending where the piece is located
        switch (type) {

            case Pawn:
                bonus = PieceHelper.BONUS_PAWN;
                break;

            case Knight:
                bonus = PieceHelper.BONUS_KNIGHT;
                break;

            case Rook:
                bonus = PieceHelper.BONUS_ROOK;
                break;

            case Bishop:
                bonus = PieceHelper.BONUS_BISHOP;
                break;

            case Queen:
                bonus = PieceHelper.BONUS_QUEEN;
                break;

            case King:
                bonus = PieceHelper.BONUS_KING;
                break;

            default:
                throw new RuntimeException("Type not handled: " + type);
        }

        //flip the row index if the player is heading south
        if (direction == North) {
            return bonus[(int)row][col];
        } else {
            return bonus[(ROWS-1) - row][col];
        }
    }
}