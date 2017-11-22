package com.gamesbykevin.chess.players;

import com.gamesbykevin.androidframeworkv2.base.Cell;
import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.game.Game;
import com.gamesbykevin.chess.game.GameHelper;
import com.gamesbykevin.chess.opengl.BasicRenderer;
import com.gamesbykevin.chess.piece.Piece;
import com.gamesbykevin.chess.piece.PieceHelper;
import com.gamesbykevin.chess.piece.PieceHelper.Type;
import com.gamesbykevin.chess.util.UtilityHelper;

import org.rajawali3d.Object3D;
import org.rajawali3d.loader.LoaderSTL;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.renderer.Renderer;

import java.util.ArrayList;
import java.util.List;

import static com.gamesbykevin.chess.game.Game.INDEX_REPLAY;
import static com.gamesbykevin.chess.game.GameHelper.sendLatestMove;
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

    public static void reset(Player player, BasicRenderer renderer, Material material) {

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
        player.addPiece(new Piece(Type.Pawn, 0, row1));
        player.addPiece(new Piece(Type.Pawn, 1, row1));
        player.addPiece(new Piece(Type.Pawn, 2, row1));
        player.addPiece(new Piece(Type.Pawn, 3, row1));
        player.addPiece(new Piece(Type.Pawn, 4, row1));
        player.addPiece(new Piece(Type.Pawn, 5, row1));
        player.addPiece(new Piece(Type.Pawn, 6, row1));
        player.addPiece(new Piece(Type.Pawn, 7, row1));

        //populate all the pieces for row 2
        player.addPiece(new Piece(Type.Rook,      0, row2));
        player.addPiece(new Piece(Type.Knight,    1, row2));
        player.addPiece(new Piece(Type.Bishop,    2, row2));
        player.addPiece(new Piece(Type.Queen,     3, row2));
        player.addPiece(new Piece(Type.King,      4, row2));
        player.addPiece(new Piece(Type.Bishop,    5, row2));
        player.addPiece(new Piece(Type.Knight,    6, row2));
        player.addPiece(new Piece(Type.Rook,      7, row2));

        //load the object models
        loadModels(player, renderer, material);
    }

    public static void loadModels(Player player, BasicRenderer renderer, Material material) {

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

                //now that we have assigned the object, set the destination coordinates
                piece.setDestinationCoordinates((float)x, (float)z);

            } catch (Exception e) {
                UtilityHelper.handleException(e);
            }
        }
    }

    public static void loadBoardSelection(Game game, Renderer renderer, Material material) {

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
            game.setBoardSelection(obj);

        } catch (Exception e) {
            UtilityHelper.handleException(e);
        }
    }

    /**
     * Get the list of available moves
     * @param player The current players turn
     * @param opponent The opponent they are facing
     * @return List of available moves which don't put the player in check
     */
    protected static List<Move> getMoves(Player player, Player opponent, final boolean checkForCheck, final boolean order) {

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

                //update score of move (if ordering)
                if (order)
                    tmp.scoreOrder = PieceHelper.getOrderScore(player, opponent, tmp);

                //add the move to the list
                options.add(tmp);
            }
        }

        //do we want to order the moves
        if (order) {

            //quickSortOrderMoves(options, 0, options.size() - 1);
            bubbleSortOrderMoves(options);
            //selectionSortOrderMoves(options);
        }

        return options;
    }

    private static void selectionSortOrderMoves(List<Move> moves) {

        for (int i = 0; i < moves.size() - 1; i++) {

            int min = i;

            for (int j = i+1; j < moves.size(); j++) {

                if (moves.get(j).scoreOrder > moves.get(min).scoreOrder)
                    min = j;

                swapValues(moves, i, min);
            }
        }
    }

    private static void bubbleSortOrderMoves(List<Move> moves) {

        //order moves in order to find the best move first
        for (int i = (moves.size() - 1); i >= 0; i--) {
            for (int j = 1; j <= i; j++) {

                int score1 = moves.get(j).scoreOrder;
                int score2 = moves.get(j - 1).scoreOrder;

                //if score 1 is better move to the front of the list
                if (score1 > score2) {

                    //swap values
                    swapValues(moves, j - 1, j);
                }
            }
        }
    }

    private static void quickSortOrderMoves(List<Move> moves, int lowerIndex, int higherIndex) {

        int i = lowerIndex;
        int j = higherIndex;

        // calculate pivot number, I am taking pivot as middle index number
        int pivot = moves.get(lowerIndex+(higherIndex-lowerIndex)/2).scoreOrder;

        // Divide into two arrays
        while (i <= j) {

            /**
             * In each iteration, we will identify a number from left side which
             * is greater then the pivot value, and also we will identify a number
             * from right side which is less then the pivot value. Once the search
             * is done, then we exchange both numbers.
             */
            while (moves.get(i).scoreOrder < pivot) {
                i++;
            }
            while (moves.get(j).scoreOrder > pivot) {
                j--;
            }
            if (i <= j) {

                //swap values
                swapValues(moves, i, j);

                //move index to next position on both sides
                i++;
                j--;
            }
        }
        // call quickSort() method recursively
        if (lowerIndex < j)
            quickSortOrderMoves(moves, lowerIndex, j);
        if (i < higherIndex)
            quickSortOrderMoves(moves, i, higherIndex);
    }

    private static void swapValues(List<Move> moves, int i, int j) {

        //get tmp reference
        Move move = moves.get(i).copy();

        //switch values
        moves.set(i, moves.get(j));
        moves.set(j, move);
    }

    /**
     * Update the state for the opposing player (check, checkmate, stalemate)
     * @param opposing
     * @param attacking
     */
    public static void updateState(Player opposing, Player attacking) {

        //get list of all possible moves for players
        List<Move> movesOpposingPlayer = getMoves(opposing, attacking, true, false);
        List<Move> movesAttackingPlayer = getMoves(attacking, opposing, true, false);

        //get the king for opposing player
        Piece king = opposing.getPiece(Type.King);

        //do we have check
        boolean hasCheck = false;

        //check every attacking player move to see if it can capture opposing player's king
        for (int i = 0; i < movesAttackingPlayer.size(); i++) {

            //if the location matches the king, we have check
            if ((int)king.getCol() == movesAttackingPlayer.get(i).destCol && (int)king.getRow() == movesAttackingPlayer.get(i).destRow) {
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
                king = opposing.getPiece(Type.King);

                //get the new list of attacking player moves available
                movesAttackingPlayer = getMoves(attacking, opposing, true, false);

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
            if (result) {
                GameHelper.GAME_OVER_FRAMES = 0;
                PlayerVars.STATE = PLAYER_1_TURN ? PlayerVars.State.WinPlayer1 : PlayerVars.State.WinPlayer2;
            }
        }

        //if we aren't in check / checkmate, but we have no more moves available
        if (movesOpposingPlayer.isEmpty() && !opposing.hasCheck())
            PlayerVars.STATE = PlayerVars.State.Stalemate;
    }

    public static void promote(Game game, Piece selection) {

        //get the current player
        Player player = game.getPlayer();

        //identified promotion piece
        Piece promote = null;

        for (int i = 0; i < player.getPieceCount(); i++) {

            //get the current piece
            Piece piece = player.getPiece(i, false);

            //if the piece doesn't exist, continue
            if (piece == null)
                continue;

            //we are only interested in pawns
            if (piece.getType() != Type.Pawn)
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
                for (int index = 0; index < game.getPromotions().size(); index++) {

                    Piece piece = game.getPromotions().get(index);

                    if (piece.getType() == Type.Queen) {
                        selection = piece;
                        break;
                    }
                }
            }

            //assign the piece type
            promote.setType(selection.getType());

            //remove from scene
            game.getActivity().getSurfaceView().getRenderer().getCurrentScene().removeChild(promote.getObject3D());

            //unregister from object picker
            game.getActivity().getSurfaceView().getRenderer().getObjectPicker().unregisterObject(promote.getObject3D());

            //get the object position
            Vector3 position = promote.getObject3D().getPosition();

            //assign the 3d model
            promote.setObject3D(selection.getObject3D().clone());

            //make sure it is displayed in the correct location
            promote.getObject3D().setPosition(position);

            //make sure the correct texture is displayed
            promote.getObject3D().setMaterial(PLAYER_1_TURN ? game.getTextureWhite() : game.getTextureWood());

            //add new 3d model to the scene
            game.getActivity().getSurfaceView().getRenderer().getCurrentScene().addChild(promote.getObject3D());

            //register object picker
            game.getActivity().getSurfaceView().getRenderer().getObjectPicker().registerObject(promote.getObject3D());

            //hide the promotion pieces
            for (Piece tmp : game.getPromotions()) {
                tmp.getObject3D().setVisible(false);
            }

            //if the player is online, notify opponent it's now their turn
            if (player.isOnline())
                game.getActivity().displayMessage(R.string.next_turn);

        }
    }

    public static boolean hasPromotion(Game game) {

        //get the current player
        Player player = game.getPlayer();

        boolean result = false;

        for (int i = 0; i < player.getPieceCount(); i++) {

            //get the current piece
            Piece piece = player.getPiece(i, false);

            //if the piece doesn't exist, continue
            if (piece == null)
                continue;

            //we are only interested in pawns
            if (piece.getType() != Type.Pawn)
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

        //return our result
        return result;
    }

    public static void displayPromotion(Game game) {

        if (!game.getPlayer().isHuman()) {

            //ai will always promote to queen
            promote(game, null);

            //switch turns
            game.switchTurns();

        } else {

            if (game.hasReplay()) {

                Move move = game.getHistory().get(INDEX_REPLAY);

                for (int i = 0; i < game.getPromotions().size(); i++) {

                    if (game.getPromotions().get(i).getType() == move.promotion) {

                        promote(game, game.getPromotions().get(i));

                        //switch turns
                        game.switchTurns();
                        break;
                    }
                }

            } else {

                for (Piece tmp : game.getPromotions()) {
                    tmp.getObject3D().setVisible(true);
                }

                //display the correct texture
                for (Piece piece : game.getPromotions()) {
                    piece.getObject3D().setMaterial(PLAYER_1_TURN ? game.getTextureWhite() : game.getTextureWood());
                }

                PlayerVars.STATUS = PlayerVars.Status.Promote;
            }
        }
    }

    public static void addPromotionPieces(Game game) {

        //if the pieces exist, remove existing
        if (game.getPromotions() != null && !game.getPromotions().isEmpty()) {
            for (Piece piece : game.getPromotions()) {

                game.getActivity().getSurfaceView().getRenderer().getCurrentScene().removeChild(piece.getObject3D());
                game.getActivity().getSurfaceView().getRenderer().getObjectPicker().unregisterObject(piece.getObject3D());
            }
        }

        //create our list of promotional pieces
        List<Piece> promotions = new ArrayList<>();

        for (Type type : Type.values()) {

            switch (type) {

                //we can't promote these pieces
                case King:
                case Pawn:
                    break;

                default:

                    //get this piece for the short term
                    Piece tmp = game.getPlayer1().getPiece(type);

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
                    game.getActivity().getSurfaceView().getRenderer().getCurrentScene().addChild(object3D);

                    //make sure we can pick the model
                    game.getActivity().getSurfaceView().getRenderer().getObjectPicker().registerObject(object3D);
                    break;
            }
        }

        //assign our promotion pieces
        game.setPromotions(promotions);
    }

    public static void setupMove(Game game, Move move) {
        setupMove(game, move.sourceCol, move.sourceRow, move.destCol, move.destRow);
    }

    public static void setupMove(Game game, float sourceCol, float sourceRow, float destCol, float destRow) {

        //get the current player playing
        Player player = game.getPlayer();

        //get the piece at the source
        Piece piece = player.getPiece((int)sourceCol, (int)sourceRow);

        //assign where we are coming from to track each move
        piece.setSourceCol(sourceCol);
        piece.setSourceRow(sourceRow);

        //assign our selected piece, in case not yet selected
        game.setSelected(piece);

        //place at the destination
        piece.setCol(destCol);
        piece.setRow(destRow);

        //set the destination where it will be rendered
        piece.setDestinationCoordinates(getCoordinate((int)piece.getCol()), getCoordinate((int)piece.getRow()));

        switch (piece.getType()) {

            case Knight:

                //we will always jump the knight
                piece.setJumping(true);
                break;

            case King:

                //the king can't move in order to castle
                if (!piece.hasMoved()) {

                    //we have to be on the same row to castle
                    if (sourceRow == destRow) {

                        //the king can move 2 columns in either direction to castle
                        if (sourceCol - destCol > 1 || destCol - sourceCol > 1) {

                            //we need to get the correct rook
                            Piece rook = null;

                            //which rook do we get?
                            if (sourceCol > destCol) {

                                //get the rook
                                rook = player.getPiece(0, (int)destRow);

                                //move to the right of the king
                                rook.setCol(destCol + 1);

                            } else {

                                //get the rook
                                rook = player.getPiece(COLS - 1, (int)destRow);

                                //move to the left of the king
                                rook.setCol(destCol - 1);
                            }

                            //set the new destination
                            rook.setDestinationCoordinates(getCoordinate((int)rook.getCol()), getCoordinate((int)rook.getRow()));

                            //flag that the rook has moved
                            rook.setMoved(true);

                            //we will jump the rook
                            rook.setJumping(true);
                        }
                    }

                } else {

                    //if the king has previously moved, it shouldn't be allowed to move 2 places and castle
                    if (sourceCol - destCol > 1 || destCol - sourceCol > 1)
                        throw new RuntimeException("King isn't allowed to castle");
                }
                break;

            case Pawn:

                //if we haven't moved the piece yet
                if (!piece.hasMoved()) {

                    //if the chess piece moved 2 places on the first move, it can be captured via "en passant"
                    piece.setPassant(sourceRow - destRow > 1 || destRow - sourceRow > 1);
                }
                break;
        }

        //flag the piece as moved
        piece.setMoved(true);

        //flag that we are moving a piece
        PlayerVars.STATUS = PlayerVars.Status.Move;
    }

    public static class Move {

        //where the piece is heading
        public int destCol, destRow;
        public int sourceCol, sourceRow;

        public int scoreOrder = 0;

        //the piece captured during the move (if exists)
        public Piece pieceCaptured;

        //the promotion piece (if applicable)
        public Type promotion = null;

        public Move() {
            //default constructor
        }

        public String toString() {
            return getLetter(sourceRow) + "" + (sourceCol+1) + " - " + getLetter(destRow) + "" + (destCol+1);
        }

        private String getLetter(final int row) {
            switch (row) {
                case 0:
                    return "A";
                case 1:
                    return "B";
                case 2:
                    return "C";
                case 3:
                    return "D";
                case 4:
                    return "E";
                case 5:
                    return "F";
                case 6:
                    return "G";
                case 7:
                    return "H";

                default:
                    throw new RuntimeException("Row not handled: " + row);
            }
        }

        public boolean hasMatch(Move move) {

            return (move.sourceCol == sourceCol &&
                    move.sourceRow == sourceRow &&
                    move.destCol == destCol &&
                    move.destRow == destRow
            );
        }

        public Move copy() {

            //create new instance
            Move move = new Move();

            move.destCol = destCol;
            move.destRow = destRow;
            move.sourceCol = sourceCol;
            move.sourceRow = sourceRow;
            move.pieceCaptured = pieceCaptured;
            move.promotion = promotion;
            move.scoreOrder = scoreOrder;

            return move;
        }
    }
}