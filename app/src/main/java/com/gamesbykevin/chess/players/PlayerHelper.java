package com.gamesbykevin.chess.players;

import com.gamesbykevin.androidframeworkv2.base.Cell;
import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.piece.Piece;
import com.gamesbykevin.chess.util.UtilityHelper;

import org.rajawali3d.Object3D;
import org.rajawali3d.loader.LoaderSTL;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.renderer.Renderer;

import java.util.ArrayList;
import java.util.List;

import static com.gamesbykevin.chess.players.Cpu.DEFAULT_DEPTH;

/**
 * Created by Kevin on 10/15/2017.
 */
public class PlayerHelper {

    //default height for all pieces so they look like they are placed on the board
    public static final float Y = .075f;

    //size of a chess board
    public static final int COLS = 8;
    public static final int ROWS = 8;

    //the west/east/north/south coordinates on the board
    public static final float COORDINATE_MIN = -.7f;
    public static final float COORDINATE_MAX = .7f;

    //distance between each col/row
    private static final float COORDINATE_INCREMENT = .2f;

    public static void correctPiece(Piece piece) {

        //keep the column in boundary
        if (piece.getObject3D().getX() < COORDINATE_MIN)
            piece.getObject3D().setX(getCoordinate(0));
        if (piece.getObject3D().getX() > COORDINATE_MAX)
            piece.getObject3D().setX(getCoordinate(COLS - 1));

        //keep the row in boundary
        if (piece.getObject3D().getZ() < COORDINATE_MIN)
            piece.getObject3D().setZ(getCoordinate(0));
        if (piece.getObject3D().getZ() > COORDINATE_MAX)
            piece.getObject3D().setZ(getCoordinate(ROWS - 1));
    }

    public static boolean hasBounds(double col, double row) {
        return (col >= 0 && col < COLS && row >= 0 && row < ROWS);
    }

    public static int getRow(final double coordinate) {
        return getRow((float)coordinate);
    }

    public static int getRow(final float coordinate) {

        for (int i = 0; i < ROWS; i++) {
            final float min = COORDINATE_MIN + (COORDINATE_INCREMENT * i) - .1f;
            final float max = COORDINATE_MIN + (COORDINATE_INCREMENT * i) + .1f;

            if (coordinate >= min && coordinate <= max)
                return i;
        }

        return -1;
    }

    public static int getCol(final double coordinate) {
        return getCol((float)coordinate);
    }

    public static int getCol(final float coordinate) {

        for (int i = 0; i < COLS; i++) {
            final float min = COORDINATE_MIN + (COORDINATE_INCREMENT * i) - .1f;
            final float max = COORDINATE_MIN + (COORDINATE_INCREMENT * i) + .1f;

            if (coordinate >= min && coordinate <= max)
                return i;
        }

        return -1;
    }

    public static float getCoordinate(final int row_col) {
        return COORDINATE_MIN + (COORDINATE_INCREMENT * row_col);
    }

    protected static void reset(Player player, Renderer renderer, Material material) {

        //remove all existing pieces
        player.removeAllPieces();

        final float row1, row2;

        //the rows will depend on the direction the player is targeting
        if (player.hasDirection(Player.Direction.North)) {
            row1 = COORDINATE_MAX - COORDINATE_INCREMENT;
            row2 = COORDINATE_MAX;
        } else {
            row1 = COORDINATE_MIN + COORDINATE_INCREMENT;
            row2 = COORDINATE_MIN;
        }

        //populate all the pieces for row 1
        addPiece(player, renderer, material, Piece.Type.Pawn, getCoordinate(0), Y, row1);
        addPiece(player, renderer, material, Piece.Type.Pawn, getCoordinate(1), Y, row1);
        addPiece(player, renderer, material, Piece.Type.Pawn, getCoordinate(2), Y, row1);
        addPiece(player, renderer, material, Piece.Type.Pawn, getCoordinate(3), Y, row1);
        addPiece(player, renderer, material, Piece.Type.Pawn, getCoordinate(4), Y, row1);
        addPiece(player, renderer, material, Piece.Type.Pawn, getCoordinate(5), Y, row1);
        addPiece(player, renderer, material, Piece.Type.Pawn, getCoordinate(6), Y, row1);
        addPiece(player, renderer, material, Piece.Type.Pawn, getCoordinate(7), Y, row1);

        //populate all the pieces for row 2
        addPiece(player, renderer, material, Piece.Type.Rook,   getCoordinate(0), Y, row2);
        addPiece(player, renderer, material, Piece.Type.Knight, getCoordinate(1), Y, row2);
        addPiece(player, renderer, material, Piece.Type.Bishop, getCoordinate(2), Y, row2);
        addPiece(player, renderer, material, Piece.Type.Queen,  getCoordinate(3), Y, row2);
        addPiece(player, renderer, material, Piece.Type.King,   getCoordinate(4), Y, row2);
        addPiece(player, renderer, material, Piece.Type.Bishop, getCoordinate(5), Y, row2);
        addPiece(player, renderer, material, Piece.Type.Knight, getCoordinate(6), Y, row2);
        addPiece(player, renderer, material, Piece.Type.Rook,   getCoordinate(7), Y, row2);
    }

    private static void addPiece(Player player, Renderer renderer, Material material, Piece.Type type, float x, float y, float z) {

        try {

            //parse our 3d model
            LoaderSTL stlParser = new LoaderSTL(renderer.getContext().getResources(), renderer.getTextureManager(), type.getResId());
            stlParser.parse();

            //get our 3d object
            Object3D obj = stlParser.getParsedObject();

            //rotate piece 90 degrees so it is standing up
            obj.rotate(Vector3.Axis.X, 90);

            //assign the location
            obj.setPosition(x, y, z);

            //our model is too large so we need to shrink it
            obj.setScale(.05);

            //assign the texture to the model
            obj.setMaterial(material);

            //add to the scene
            renderer.getCurrentScene().addChild(obj);

            //add piece to the player
            player.addPiece(new Piece(obj, type, getCol(x), getRow(z)));

        } catch (Exception e) {
            UtilityHelper.handleException(e);
        }
    }

    protected static void loadSelected(Players players, Renderer renderer, Material material) {

        try {
            //parse our 3d model
            LoaderSTL stlParser = new LoaderSTL(renderer.getContext().getResources(), renderer.getTextureManager(), R.raw.valid_stl);
            stlParser.parse();

            //get our 3d object
            Object3D obj = stlParser.getParsedObject();

            //rotate piece 90 degrees so it is laying down
            obj.rotate(Vector3.Axis.X, 90);

            //our model is too large so we need to shrink it
            obj.setScale(.1);

            //set the texture material
            obj.setMaterial(material);

            //assign accordingly
            players.setBoardSelection(obj);

        } catch (Exception e) {
            UtilityHelper.handleException(e);
        }
    }

    protected static Move getBestMove(Players players) {

        //keep track of the score for the best move
        int score = Integer.MIN_VALUE;

        //keep track of our best move
        Move bestMove = null;

        Player player = players.isPlayer1Turn() ? players.getPlayer1() : players.getPlayer2();
        Player opponent = players.isPlayer1Turn() ? players.getPlayer2() : players.getPlayer1();

        //create a new list for all the moves
        List<Move> moves = getMoves(player, opponent);

        //go through all of the moves
        for (Move move : moves) {

            //execute the current move
            executeMove(move, players);

            //calculate the score
            final int tmpScore = -1 * negaMax(DEFAULT_DEPTH, players);

            //undo the previous move
            undoMove(move, players);

            //if we have a better score
            if (tmpScore > score) {

                //this is the new score to beat
                score = tmpScore;

                //this is now the best move
                bestMove = move;
            }
        }

        moves.clear();
        moves = null;

        //return the best move
        return bestMove;
    }

    private static int negaMax(int depth, Players players) {

        Player player = players.isPlayer1Turn() ? players.getPlayer1() : players.getPlayer2();
        Player opponent = players.isPlayer1Turn() ? players.getPlayer2() : players.getPlayer1();

        if (depth <= 0)
            return (player.calculateScore() - opponent.calculateScore());

        //get list of all valid moves based on the current state of the board
        List<Move> currentMoves = getMoves(player, opponent);

        //keep track of the best score
        int bestScore = Integer.MIN_VALUE;

        //check every valid move
        for (Move currentMove : currentMoves) {

            //execute the move
            executeMove(currentMove, players);

            //calculate the score
            final int tmpScore = -1 * negaMax(depth - 1, players);

            //undo the move
            undoMove(currentMove, players);

            //if there is a new best score
            if (tmpScore > bestScore)
                bestScore = tmpScore;
        }

        currentMoves.clear();
        currentMoves = null;

        //return the best score we found
        return bestScore;
    }

    private static List<Move> getMoves(Player player, Player opponent) {

        //create a new list for all the moves
        List<Move> options = new ArrayList<>();

        for (int i = 0; i < player.getPieceCount(); i++) {

            //get the current (non-captured) piece
            Piece piece = player.getPiece(i, false);

            //if there is no piece skip
            if (piece == null)
                continue;

            //get a list of moves for the current piece
            List<Cell> moves = piece.getMoves(player, opponent);

            //add all possible moves to the list of options
            for (Cell move : moves) {

                Move tmp = new Move();
                tmp.sourceCol = (int)piece.getCol();
                tmp.sourceRow = (int)piece.getRow();
                tmp.destCol = (int)move.getCol();
                tmp.destRow = (int)move.getRow();

                //add the move to the list
                options.add(tmp);
            }
        }

        return options;
    }

    private static void executeMove(Move move, Players players) {

        Player player = players.isPlayer1Turn() ? players.getPlayer1() : players.getPlayer2();
        Player opponent = players.isPlayer1Turn() ? players.getPlayer2() : players.getPlayer1();

        //get the player piece at the source location
        Piece piece = player.getPiece(move.sourceCol, move.sourceRow);

        //get the captured piece at the destination
        Piece captured = opponent.getPiece(move.destCol, move.destRow);

        //place at the destination (if it exists)
        if (piece != null) {
            piece.setCol(move.destCol);
            piece.setRow(move.destRow);
        }

        //flag the piece captured (if it exists)
        if (captured != null) {
            move.pieceCaptured = captured;
            move.pieceCaptured.setCaptured(true);
        }

        //since we made a move, switch turns
        players.setPlayer1Turn(!players.isPlayer1Turn());
    }

    private static void undoMove(Move move, Players players) {

        //let's try to get the piece at the destination location for player 1
        Piece piece = players.getPlayer1().getPiece(move.destCol, move.destRow);
        boolean player1Turn = true;

        //if the piece doesn't exist it must be player 2
        if (piece == null) {
            piece = players.getPlayer2().getPiece(move.destCol, move.destRow);
            player1Turn = false;
        }

        //move back to the source
        piece.setCol(move.sourceCol);
        piece.setRow(move.sourceRow);

        //flag the piece captured false (if it exists)
        if (move.pieceCaptured != null) {
            move.pieceCaptured.setCaptured(false);
            move.pieceCaptured.setCol(move.destCol);
            move.pieceCaptured.setRow(move.destRow);
        }

        //assign the correct turn
        players.setPlayer1Turn(player1Turn);
    }

    protected static class Move {

        //where the piece is heading
        protected int destCol, destRow;
        protected int sourceCol, sourceRow;

        //the piece captured during the move (if exists)
        protected Piece pieceCaptured;

        protected Move() {
            //default constructor
        }
    }
}