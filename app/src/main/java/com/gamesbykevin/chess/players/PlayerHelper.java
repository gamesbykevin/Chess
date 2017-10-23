package com.gamesbykevin.chess.players;

import com.gamesbykevin.androidframeworkv2.base.Cell;
import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.activity.BasicRenderer;
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
import static com.gamesbykevin.chess.util.UtilityHelper.DEBUG;

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

    protected static void reset(Player player, BasicRenderer renderer, Material material) {

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

    private static void addPiece(Player player, BasicRenderer renderer, Material material, Piece.Type type, float x, float y, float z) {

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

    protected static void loadBoardSelection(Players players, Renderer renderer, Material material) {

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

        //count the number of moves
        int count = 0;

        if (DEBUG)
            UtilityHelper.logEvent("Thinking... size:" + moves.size());

        //go through all of the moves
        for (Move move : moves) {

            if (DEBUG) {
                count++;
                UtilityHelper.logEvent("Checking move " + count);
            }

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

    protected static void updateMoves(List<Cell> moves, Players players) {

        //no need to do anything if the list is empty
        if (moves.isEmpty())
            return;

        Player player = players.isPlayer1Turn() ? players.getPlayer1() : players.getPlayer2();
        Player opponent = players.isPlayer1Turn() ? players.getPlayer2() : players.getPlayer1();

        //store the current turn
        boolean tmpPlayer1Turn = new Boolean(players.isPlayer1Turn());

        //create our move for reference
        Move move = new Move();

        //remove any moves that don't get us out of check
        for (int i = 0; i < moves.size(); i++) {

            if (moves.isEmpty())
                break;

            //populate our next move
            move.sourceCol = (int)players.getSelected().getCol();
            move.sourceRow = (int)players.getSelected().getRow();
            move.destCol = (int)moves.get(i).getCol();
            move.destRow = (int)moves.get(i).getRow();
            move.pieceCaptured = opponent.getPiece(move.destCol, move.destRow);

            //execute the move
            executeMove(move, players);

            //keep the turn the same
            players.setPlayer1Turn(tmpPlayer1Turn);

            //if we are still in check, this isn't a valid move
            if (hasCheck(opponent, player)) {

                if (i < moves.size())
                    moves.remove(i);
                i--;
            }

            //undo the move
            undoMove(move, players);

            //keep the turn the same
            players.setPlayer1Turn(tmpPlayer1Turn);
        }

        //restore the correct player turn
        players.setPlayer1Turn(tmpPlayer1Turn);
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

    /**
     * Is the opponent in check?
     * @param player offense
     * @param opponent defense
     * @return true if the player has the opponent in check
     */
    protected static boolean hasCheck(Player player, Player opponent) {

        Piece king = null;

        //check the opponent for the king chess piece
        for (int i = 0; i < opponent.getPieceCount(); i++) {
            Piece piece = opponent.getPiece(i, false);

            //if the piece does not exist continue
            if (piece == null)
                continue;

            //if the piece is king, we found it
            if (piece.getType() == Piece.Type.King) {
                king = piece;
                break;
            }
        }

        for (int i = 0; i < player.getPieceCount(); i++) {
            Piece piece = player.getPiece(i, false);

            if (piece == null)
                continue;

            //get the list of moves for the current piece
            List<Cell> moves = piece.getMoves(player, opponent);

            //check each move
            for (int index = 0; index < moves.size(); index++) {

                //if the move is at the location of the king, the king is in check
                if (king.hasLocation(moves.get(index)))
                    return true;
            }
        }

        //we haven't found the king to be in danger
        return false;
    }

    protected static boolean hasStalemate(Players players) {

        //player who needs to escape check
        Player player = players.isPlayer1Turn() ? players.getPlayer1() : players.getPlayer2();

        //opponent attacking the vulnerable player
        Player opponent = players.isPlayer1Turn() ? players.getPlayer2() : players.getPlayer1();

        //for right now assume stalemate
        boolean result = true;

        //check all our pieces
        for (int i = 0; i < opponent.getPieceCount(); i++) {

            Piece piece = opponent.getPiece(i, false);

            if (piece == null)
                continue;

            //get the list of moves for the current piece
            List<Cell> tmpMoves = piece.getMoves(opponent, player);

            //remove any invalid moves (if exist)
            if (!tmpMoves.isEmpty()) {

                final int col = (int)piece.getCol();
                final int row = (int)piece.getRow();

                //see if any moves can be removed
                for (int index = 0; index < tmpMoves.size(); index++) {

                    //update the location
                    piece.setCol(tmpMoves.get(index));
                    piece.setRow(tmpMoves.get(index));

                    //if we are still in check, remove move
                    if (hasCheck(player, opponent)) {
                        tmpMoves.remove(index);
                        index--;
                    }
                }

                //restore location
                piece.setCol(col);
                piece.setRow(row);
            }

            //if we have at least 1 move, this isn't a stalemate
            if (!tmpMoves.isEmpty()) {
                result = false;
                break;
            }
        }

        //return our result
        return result;
    }

    /**
     * Do we have check mate?
     * @return true if the game is over, false otherwise
     */
    protected static boolean hasCheckMate(Players players) {

        boolean tmpPlayer1Turn = new Boolean(players.isPlayer1Turn());

        //switch turns temporary to see if the player can escape check
        players.setPlayer1Turn(!tmpPlayer1Turn);

        //player who needs to escape check
        Player player = players.isPlayer1Turn() ? players.getPlayer1() : players.getPlayer2();

        //opponent attacking the vulnerable player
        Player opponent = players.isPlayer1Turn() ? players.getPlayer2() : players.getPlayer1();

        //create temp move
        Move move = new Move();

        //for right now assume check mate
        boolean result = true;

        for (int i = 0; i < player.getPieceCount(); i++) {
            Piece piece = player.getPiece(i, false);

            if (piece == null)
                continue;

            //get the list of moves for the current piece
            List<Cell> moves = piece.getMoves(player, opponent);

            for (int x = 0; x < moves.size(); x++) {

                //if not in check, no need to continue
                if (!result)
                    break;

                //create our move
                move.sourceCol = (int)piece.getCol();
                move.sourceRow = (int)piece.getRow();
                move.destCol = (int)moves.get(x).getCol();
                move.destRow = (int)moves.get(x).getRow();
                move.pieceCaptured = opponent.getPiece(move.destCol, move.destRow);

                //execute the move
                executeMove(move, players);

                //keep the same turn
                players.setPlayer1Turn(!tmpPlayer1Turn);

                //see if we are still in check
                if (!hasCheck(opponent, player))
                    result = false;

                //undo the previous move
                undoMove(move, players);

                //keep the same turn
                players.setPlayer1Turn(!tmpPlayer1Turn);
            }
        }

        //restore the correct turn
        players.setPlayer1Turn(tmpPlayer1Turn);

        //return our result
        return result;
    }

    protected static void promote(BasicRenderer renderer, Players players, Piece selection) {

        //get the current player
        Player player = players.isPlayer1Turn() ? players.getPlayer1() : players.getPlayer2();

        //identified promotion piece
        Piece promote = null;

        for (int i = 0; i < player.getPieceCount(); i++) {

            //get the current piece
            Piece piece = player.getPiece(i, false);

            //if the piece doesn't exist, continue
            if (piece == null)
                continue;

            //we are only interested in pawns
            if (piece.getType() != Piece.Type.Pawn)
                continue;

            //make sure we are at the end
            if (player.hasDirection(Player.Direction.North)) {

                if (piece.getRow() == 0) {
                    promote = piece;
                    break;
                }

            } else {

                if (piece.getRow() == ROWS - 1) {
                    promote = piece;
                    break;
                }

            }
        }

        //we have the pawn piece we want to promote
        if (promote != null) {

            //if no selection we automatically promote to queen
            if (selection == null) {

                //check all the pieces
                for (int index = 0; index < players.getPromotions().size(); index++) {

                    Piece piece = players.getPromotions().get(index);

                    if (piece.getType() == Piece.Type.Queen) {
                        selection = piece;
                        break;
                    }
                }
            }

            //assign the piece type
            promote.setType(selection.getType());

            //remove from scene
            renderer.getCurrentScene().removeChild(promote.getObject3D());

            //unregister from object picker
            renderer.getObjectPicker().unregisterObject(promote.getObject3D());

            //get the object position
            Vector3 position = promote.getObject3D().getPosition();

            //assign the 3d model
            promote.setObject3D(selection.getObject3D().clone());

            //make sure it is displayed in the correct location
            promote.getObject3D().setPosition(position);

            //add new 3d model to the scene
            renderer.getCurrentScene().addChild(promote.getObject3D());

            //register object picker
            renderer.getObjectPicker().registerObject(promote.getObject3D());

            //hide the promotion pieces
            for (Piece tmp : players.getPromotions()) {
                tmp.getObject3D().setVisible(false);
            }
        }
    }

    protected static boolean hasPromotion(Players players) {

        //get the current player
        Player player = players.isPlayer1Turn() ? players.getPlayer1() : players.getPlayer2();

        boolean result = false;

        for (int i = 0; i < player.getPieceCount(); i++) {

            //get the current piece
            Piece piece = player.getPiece(i, false);

            //if the piece doesn't exist, continue
            if (piece == null)
                continue;

            //we are only interested in pawns
            if (piece.getType() != Piece.Type.Pawn)
                continue;

            //make sure we are at the end
            if (player.hasDirection(Player.Direction.North)) {

                if (piece.getRow() == 0) {
                    result = true;
                    break;
                }

            } else {

                if (piece.getRow() == ROWS - 1) {
                    result = true;
                    break;
                }
            }
        }

        //if we have promotion, display our options
        if (result) {
            if (player.isHuman()) {
                for (Piece tmp : players.getPromotions()) {
                    tmp.getObject3D().setVisible(true);
                }
            }
        }

        //return our result
        return result;
    }

    protected static void addPromotionPieces(Players players, BasicRenderer renderer) {

        //create our list of promotional pieces
        List<Piece> promotions = new ArrayList<>();

        for (Piece.Type type : Piece.Type.values()) {

            switch (type) {

                //we can't promote these pieces
                case King:
                case Pawn:
                    break;

                default:

                    //get this piece for the short term
                    Piece tmp = players.getPlayer1().getPiece(type);

                    //clone the 3d model
                    Object3D object3D = tmp.getObject3D().clone();

                    //update to be outside the board
                    object3D.setPosition(getCoordinate(-2), object3D.getY(), COORDINATE_MIN + (promotions.size() * COORDINATE_INCREMENT));

                    //make object invisible
                    object3D.setVisible(false);

                    //create the piece
                    Piece piece = new Piece(object3D, type, -1, -1);

                    //add the piece to our promotions list
                    promotions.add(piece);

                    //add object to the scene
                    renderer.getCurrentScene().addChild(object3D);

                    //make sure we can pick the model
                    renderer.getObjectPicker().registerObject(object3D);
                    break;
            }
        }

        //assign our promotion pieces
        players.setPromotions(promotions);
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