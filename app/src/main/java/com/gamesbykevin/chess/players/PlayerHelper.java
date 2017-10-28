package com.gamesbykevin.chess.players;

import com.gamesbykevin.androidframeworkv2.base.Cell;
import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.opengl.BasicRenderer;
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
import static com.gamesbykevin.chess.players.PlayerVars.PLAYER_1_TURN;
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

        final int row1, row2;

        //the rows will depend on the direction the player is targeting
        if (player.hasDirection(Player.Direction.North)) {
            row1 = 6;
            row2 = 7;
        } else {
            row1 = 1;
            row2 = 0;
        }

        //populate all the pieces for row 1
        addPiece(player, Piece.Type.Pawn, 0, row1);
        addPiece(player, Piece.Type.Pawn, 1, row1);
        addPiece(player, Piece.Type.Pawn, 2, row1);
        addPiece(player, Piece.Type.Pawn, 3, row1);
        addPiece(player, Piece.Type.Pawn, 4, row1);
        addPiece(player, Piece.Type.Pawn, 5, row1);
        addPiece(player, Piece.Type.Pawn, 6, row1);
        addPiece(player, Piece.Type.Pawn, 7, row1);

        //populate all the pieces for row 2
        addPiece(player, Piece.Type.Rook,   0, row2);
        addPiece(player, Piece.Type.Knight, 1, row2);
        addPiece(player, Piece.Type.Bishop, 2, row2);
        addPiece(player, Piece.Type.Queen,  3, row2);
        addPiece(player, Piece.Type.King,   4, row2);
        addPiece(player, Piece.Type.Bishop, 5, row2);
        addPiece(player, Piece.Type.Knight, 6, row2);
        addPiece(player, Piece.Type.Rook,   7, row2);

        //load the object models
        loadModels(player, renderer, material);
    }

    protected static void loadModels(Player player, BasicRenderer renderer, Material material) {

        for (int i = 0; i < player.getPieceCount(); i++) {

            //get the current piece
            Piece piece = player.getPiece(i, true);

            //if the piece does not exist skip to the next
            if (piece == null)
                continue;

            //remove previous, if it exists
            if (piece.getObject3D() != null) {
                renderer.getCurrentScene().removeChild(piece.getObject3D());
                renderer.getObjectPicker().unregisterObject(piece.getObject3D());
            }

            final double x = getCoordinate((int)piece.getCol());
            final double y = Y;
            final double z = getCoordinate((int)piece.getRow());

            try {

                //parse our 3d model
                LoaderSTL stlParser = new LoaderSTL(renderer.getContext().getResources(), renderer.getTextureManager(), piece.getType().getResId());
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

                //add to the scene as long as it isn't captured
                if (!piece.isCaptured())
                    renderer.getCurrentScene().addChild(obj);

                //assign our model to the piece
                piece.setObject3D(obj);

            } catch (Exception e) {
                UtilityHelper.handleException(e);
            }
        }
    }

    private static void addPiece(Player player, Piece.Type type, int col, int row) {

        //add piece to the player
        player.addPiece(new Piece(type, col, row));
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
        List<Move> moves = getMoves(player, opponent, player.hasCheck());

        //count the number of moves
        int count = 0;

        if (DEBUG)
            UtilityHelper.logEvent("Thinking... size:" + moves.size());

        //go through all of the moves
        for (Move move : moves) {

            //if we want to interrupt the game
            if (PlayerVars.STATUS == PlayerVars.Status.Interrupt)
                break;

            //update the ai progress
            count++;
            players.getActivity().updateProgress((int)(((float)count / (float)moves.size()) * 100));

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

        players.getActivity().updateProgress(0);

        //return the best move
        return bestMove;
    }

    private static int negaMax(int depth, Players players) {

        Player player = players.isPlayer1Turn() ? players.getPlayer1() : players.getPlayer2();
        Player opponent = players.isPlayer1Turn() ? players.getPlayer2() : players.getPlayer1();

        if (depth <= 0)
            return (player.calculateScore() - opponent.calculateScore());

        //get list of all valid moves based on the current state of the board
        List<Move> currentMoves = getMoves(player, opponent, player.hasCheck());

        //keep track of the best score
        int bestScore = Integer.MIN_VALUE;

        //check every valid move
        for (Move currentMove : currentMoves) {

            //if we want to interrupt the game
            if (PlayerVars.STATUS == PlayerVars.Status.Interrupt)
                break;

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

    /**
     * Get the list of available moves
     * @param player The current players turn
     * @param opponent The opponent they are facing
     * @return List of available moves which don't put the player in check
     */
    private static List<Move> getMoves(Player player, Player opponent, final boolean checkForCheck) {

        //create a new list for all the moves
        List<Move> options = new ArrayList<>();

        for (int i = 0; i < player.getPieceCount(); i++) {

            //get the current (non-captured) piece
            Piece piece = player.getPiece(i, false);

            //if there is no piece skip
            if (piece == null)
                continue;

            //get a list of moves for the current piece
            List<Cell> moves = piece.getMoves(player, opponent, checkForCheck);

            //add all possible moves to the list of options
            for (Cell move : moves) {

                //create our new move
                Move tmp = new Move();
                tmp.sourceCol = (int) piece.getCol();
                tmp.sourceRow = (int) piece.getRow();
                tmp.destCol = (int) move.getCol();
                tmp.destRow = (int) move.getRow();

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

        //since we executed a move switch turns
        players.setPlayer1Turn(!players.isPlayer1Turn());
    }

    private static void undoMove(Move move, Players players) {

        //let's try to get the piece at the destination location for player 1
        Piece piece = players.getPlayer1().getPiece(move.destCol, move.destRow);
        boolean player1Turn = true;

        //if the piece doesn't exist it must be player 2's turn
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
     * Update the state for the opposing player (check, checkmate, stalemate)
     * @param opposing
     * @param attacking
     */
    protected static void updateStatus(Player opposing, Player attacking) {

        //get list of all possible moves for players
        List<Move> movesOpposingPlayer = getMoves(opposing, attacking, true);
        List<Move> movesAttackingPlayer = getMoves(attacking, opposing, true);

        //get the king for opposing player
        Piece king = opposing.getPiece(Piece.Type.King);

        //do we have check
        boolean hasCheck = false;

        //check every attacking player move to see if it can capture opposing player's king
        for (int i = 0; i < movesAttackingPlayer.size(); i++) {

            //if the location matches the king, we have check
            if (king.hasLocation(movesAttackingPlayer.get(i).destCol, movesAttackingPlayer.get(i).destRow)) {
                hasCheck = true;
                break;
            }
        }

        //update check status if check is true
        opposing.setCheck(hasCheck);

        //if the player is in check, let's see if any of player 1's moves can fix that
        if (opposing.hasCheck()) {

            //assume checkmate (for now)
            boolean result = true;

            //check every opposing player move to see if we can avoid check
            for (int index = 0; index < movesOpposingPlayer.size(); index++) {

                //get the current piece
                Piece piece = opposing.getPiece(movesOpposingPlayer.get(index).sourceCol, movesOpposingPlayer.get(index).sourceRow);

                //place at destination
                piece.setCol(movesOpposingPlayer.get(index).destCol);
                piece.setRow(movesOpposingPlayer.get(index).destRow);

                //get the captured piece (if exists)
                movesOpposingPlayer.get(index).pieceCaptured = attacking.getPiece(movesOpposingPlayer.get(index).destCol, movesOpposingPlayer.get(index).destRow);

                //if the piece exists, capture it
                if (movesOpposingPlayer.get(index).pieceCaptured != null)
                    movesOpposingPlayer.get(index).pieceCaptured.setCaptured(true);

                //get the current king chess piece
                king = opposing.getPiece(Piece.Type.King);

                //get the new list of attacking player moves available
                movesAttackingPlayer = getMoves(attacking, opposing, true);

                boolean tmpCheck = false;

                //check every attacking player's move to see if it can capture opposing player's king
                for (int i = 0; i < movesAttackingPlayer.size(); i++) {

                    //if the location matches the king, we still have check
                    if (king.hasLocation(movesAttackingPlayer.get(i).destCol, movesAttackingPlayer.get(i).destRow)) {
                        tmpCheck = true;
                        break;
                    }
                }

                //if the piece exists, un-capture it
                if (movesOpposingPlayer.get(index).pieceCaptured != null)
                    movesOpposingPlayer.get(index).pieceCaptured.setCaptured(false);

                //restore the piece to it's original location
                piece.setCol(movesOpposingPlayer.get(index).sourceCol);
                piece.setRow(movesOpposingPlayer.get(index).sourceRow);

                //if this move doesn't result in check, we can avoid checkmate!!!!
                if (!tmpCheck) {
                    result = false;
                    break;
                }
            }

            //if the player is in check and there are no moves, checkmate
            if (result)
                PlayerVars.STATE = PLAYER_1_TURN ? PlayerVars.State.LosePlayer1 : PlayerVars.State.LosePlayer2;
        }

        //if we aren't in check / checkmate, but we have no more moves available
        if (movesOpposingPlayer.isEmpty() && !opposing.hasCheck())
            PlayerVars.STATE = PlayerVars.State.Stalemate;
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

        //if the pieces exist, remove existing
        if (players.getPromotions() != null && !players.getPromotions().isEmpty()) {
            for (Piece piece : players.getPromotions()) {
                renderer.getCurrentScene().removeChild(piece.getObject3D());
                renderer.getObjectPicker().unregisterObject(piece.getObject3D());
            }
        }

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
                    Piece piece = new Piece(type, -1, -1);
                    piece.setObject3D(object3D);

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

    public static class Move {

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