package com.gamesbykevin.chess.game;

import android.content.SharedPreferences;

import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.activity.GameActivity;
import com.gamesbykevin.chess.piece.Piece;
import com.gamesbykevin.chess.piece.PieceHelper;
import com.gamesbykevin.chess.players.Player;
import com.gamesbykevin.chess.players.PlayerHelper;
import com.gamesbykevin.chess.players.PlayerHelper.Move;
import com.gamesbykevin.chess.players.PlayerVars;
import com.gamesbykevin.chess.services.AchievementHelper;
import com.gamesbykevin.chess.services.EventHelper;
import com.gamesbykevin.chess.util.UtilityHelper;

import org.rajawali3d.Object3D;
import org.rajawali3d.loader.LoaderSTL;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.gamesbykevin.androidframeworkv2.activity.BaseActivity.GSON;
import static com.gamesbykevin.androidframeworkv2.activity.BaseActivity.getSharedPreferences;
import static com.gamesbykevin.chess.players.PlayerHelper.Y;
import static com.gamesbykevin.chess.players.PlayerHelper.getCoordinate;
import static com.gamesbykevin.chess.players.PlayerVars.PLAYER_1_TURN;
import static com.gamesbykevin.chess.players.PlayerVars.STATE;
import static com.gamesbykevin.chess.util.UtilityHelper.DEBUG;

/**
 * Created by Kevin on 10/30/2017.
 */
public class GameHelper {

    //how many frames have elapsed since game over
    public static int GAME_OVER_FRAMES = 0;

    public static void checkDraw(Game game) {

        //don't check if we are replaying the match
        if (game.hasReplay())
            return;

        //get the history
        List<Move> history = game.getHistory();

        //we don't have enough history to check for a draw
        if (history.size() < 10)
            return;

        //check every 10 moves to see if we have the same moves (a.k.a. draw)
        for (int i = 0; i < history.size(); i++) {

            //we can't check if out of bounds
            if (i + 9 >= history.size())
                break;

            //check 3 moves
            Move p1move1 = history.get(i + 0);
            Move p1move2 = history.get(i + 4);
            Move p1move3 = history.get(i + 8);

            //if one doesn't match, there is no draw
            if (!p1move1.hasMatch(p1move2) || !p1move1.hasMatch(p1move3))
                continue;

            //check 3 moves
            Move p2move1 = history.get(i + 1);
            Move p2move2 = history.get(i + 5);
            Move p2move3 = history.get(i + 9);

            //if one doesn't match, there is no draw
            if (!p2move1.hasMatch(p2move2) || !p2move1.hasMatch(p2move3))
                continue;

            //we have enough matches to declare a draw
            PlayerVars.STATE = PlayerVars.State.Stalemate;

            //exit the loop
            break;
        }
    }

    public static boolean saveHistory(Game game, final int position) {

        //was the save successful?
        boolean result = false;

        try {

            //obtain editor
            SharedPreferences.Editor editor = getSharedPreferences().edit();

            //get our history
            List<Move> history = game.getHistory();

            //assign setting
            if (history != null) {

                //our desired date format
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

                //store our game history and time description
                editor.putString(game.getActivity().getString(getResidFile(position)), GSON.toJson(history));
                editor.putString(game.getActivity().getString(getResidDesc(position)), dateFormat.format(Calendar.getInstance().getTime()));
            }

            //save setting
            editor.apply();

            //flag success
            result = true;

        } catch (Exception e) {
            UtilityHelper.handleException(e);
        }

        //return our result
        return result;
    }

    public static final int getResidDesc(final int position) {

        final int resId;

        switch (position) {

            case 0:
                resId = R.string.saved_match_1_desc_key;
                break;

            case 1:
                resId = R.string.saved_match_2_desc_key;
                break;

            case 2:
                resId = R.string.saved_match_3_desc_key;
                break;

            case 3:
                resId = R.string.saved_match_4_desc_key;
                break;

            case 4:
                resId = R.string.saved_match_5_desc_key;
                break;

            default:
                throw new RuntimeException("Position not handled: " + position);
        }

        return resId;
    }

    public static final int getResidFile(final int position) {

        final int resId;

        switch (position) {

            case 0:
                resId = R.string.saved_match_1_file_key;
                break;

            case 1:
                resId = R.string.saved_match_2_file_key;
                break;

            case 2:
                resId = R.string.saved_match_3_file_key;
                break;

            case 3:
                resId = R.string.saved_match_4_file_key;
                break;

            case 4:
                resId = R.string.saved_match_5_file_key;
                break;

            default:
                throw new RuntimeException("Position not handled: " + position);
        }

        return resId;
    }

    protected static void move(Game game) {

        //if we never selected a piece, we can't move
        if (game.getSelected() == null)
            return;

        Player player = PLAYER_1_TURN ? game.getPlayer1() : game.getPlayer2();
        Player opponent = PLAYER_1_TURN ? game.getPlayer2() : game.getPlayer1();

        //if not at destination, move it
        if (!player.hasDestination()) {

            //move all necessary pieces
            player.move();

            //don't continue yet
            return;
        }

        //check if we selected an opponent piece to capture it
        for (int i = 0; i < opponent.getPieceCount(); i++) {

            //get the current chess piece
            Piece piece = opponent.getPiece(i, false);

            if (piece == null)
                continue;

            //if the opponents piece is at the same location, we can capture
            if ((int)piece.getCol() == (int)game.getSelected().getCol() &&
                (int)piece.getRow() == (int)game.getSelected().getRow()) {

                //remove from object picker
                game.getActivity().getSurfaceView().getRenderer().getObjectPicker().unregisterObject(piece.getObject3D());

                //remove from scene
                game.getActivity().getSurfaceView().getRenderer().getCurrentScene().removeChild(piece.getObject3D());

                //flag opponent piece captured
                piece.setCaptured(true);

                //no need to check any other pieces
                break;

            } else {

                //skip if the piece isn't a pawn and we can't capture "en passant"
                if (piece.getType() != PieceHelper.Type.Pawn)
                    continue;
                if (!piece.hasPassant())
                    continue;

                //both pieces have to be pawns
                if (game.getSelected().getType() != PieceHelper.Type.Pawn)
                    continue;

                //if the columns don't match, we can't capture
                if (piece.getCol() != game.getSelected().getCol())
                    continue;

                //only check if directly above chess piece
                if (player.hasDirection(Player.Direction.North) && piece.getRow() - 1 != game.getSelected().getRow())
                    continue;
                if (player.hasDirection(Player.Direction.South) && piece.getRow() + 1 != game.getSelected().getRow())
                    continue;

                //remove from object picker
                game.getActivity().getSurfaceView().getRenderer().getObjectPicker().unregisterObject(piece.getObject3D());

                //remove from scene
                game.getActivity().getSurfaceView().getRenderer().getCurrentScene().removeChild(piece.getObject3D());

                //flag opponent piece captured
                piece.setCaptured(true);

                //no need to check any other pieces
                break;
            }
        }

        //we only have 1 turn to capture the opponent's pawn via "en passant"
        opponent.removePassant();

        //track the move
        game.track(game.getSelected());

        //update the state of the game (check, checkmate, stalemate)
        PlayerHelper.updateState(opponent, player);

        //check for a draw (stalemate)
        checkDraw(game);

        //display state
        displayState(game, opponent);

        //updates if game is over
        if (PlayerVars.isGameover()) {

            //track our events
            EventHelper.trackEvents(game.getActivity());

            //track our achievements
            AchievementHelper.trackAchievements(game.getActivity());
        }

        //we are at our destination, de-select the chess piece
        game.deselect();

        //check if we have a promotion
        if (PlayerHelper.hasPromotion(game)) {

            PlayerHelper.displayPromotion(game);

        } else {

            //if we aren't promoting a piece, switch turns
            game.switchTurns();

            if (opponent.isOnline()) {
                //if our opponent is online let's send the move to our opponent
                sendLatestMove(game);
            } else if (player.isOnline()) {
                //if the player is online, notify opponent it's now their turn
                game.getActivity().displayMessage(R.string.next_turn);
            }
        }


        //if we have replay enabled, change the index
        if (game.hasReplay())
            Game.INDEX_REPLAY++;
    }

    public static void sendLatestMove(Game game) {

        //get the last move
        Move move = game.getHistory().get(game.getHistory().size() - 1);

        //send move to our opponent
        game.getActivity().sendMove(move.sourceCol, move.sourceRow, move.destCol, move.destRow, move.promotion);
    }

    public static void displayState(Game game, Player opponent) {

        switch (STATE) {

            case WinPlayer2:
                game.getActivity().displayMessage(R.string.game_status_player_1_checkmate);
                break;

            case WinPlayer1:
                game.getActivity().displayMessage(R.string.game_status_player_2_checkmate);
                break;

            case Stalemate:
                game.getActivity().displayMessage(R.string.game_status_stalemate);
                break;

            case Player1TimeUp:
                game.getActivity().displayMessage(R.string.game_status_player_1_time);
                break;

            case Player2TimeUp:
                game.getActivity().displayMessage(R.string.game_status_player_2_time);
                break;

            default:

                if (opponent.hasCheck() && !game.hasReplay()) {
                    game.getActivity().displayMessage(PLAYER_1_TURN ? R.string.game_status_player_2_check : R.string.game_status_player_1_check);
                } else if (!opponent.isHuman()) {
                    //getActivity().displayMessage("Thinking...");
                }
                break;
        }
    }

    protected static void updateGameOver(Game game) {

        //our chess piece reference
        Piece piece = null;

        //don't animate if 90 frames have passed
        if (GAME_OVER_FRAMES >= 90)
            return;

        //get the appropriate king
        switch (STATE) {

            case WinPlayer1:
                piece = game.getPlayer2().getPiece(PieceHelper.Type.King);
                break;

            case WinPlayer2:
                piece = game.getPlayer1().getPiece(PieceHelper.Type.King);
                break;
        }

        //if we have a king, topple it
        if (piece != null) {

            //keep track of time elapsed
            GAME_OVER_FRAMES++;

            final int col = (int)piece.getCol();
            final int row = (int)piece.getRow();

            //check north
            if (!game.getPlayer1().hasPiece(col, row - 1) && !game.getPlayer1().hasPiece(col,row - 2) &&
                !game.getPlayer2().hasPiece(col, row - 1) && !game.getPlayer2().hasPiece(col,row - 2)) {

                //rotate north
                piece.getObject3D().rotate(Vector3.X, 1);

            } else if ( !game.getPlayer1().hasPiece(col, row + 1) && !game.getPlayer1().hasPiece(col,row + 2) &&
                        !game.getPlayer2().hasPiece(col, row + 1) && !game.getPlayer2().hasPiece(col,row + 2)) {

                //rotate south
                piece.getObject3D().rotate(Vector3.X, -1);

            } else if ( !game.getPlayer1().hasPiece(col - 1, row) && !game.getPlayer1().hasPiece(col - 2,row) &&
                        !game.getPlayer2().hasPiece(col - 1, row) && !game.getPlayer2().hasPiece(col - 2,row)) {

                //rotate west
                piece.getObject3D().rotate(Vector3.Z, -1);

            } else if ( !game.getPlayer1().hasPiece(col + 1, row) && !game.getPlayer1().hasPiece(col + 2,row) &&
                        !game.getPlayer2().hasPiece(col + 1, row) && !game.getPlayer2().hasPiece(col + 2,row)) {

                //rotate east
                piece.getObject3D().rotate(Vector3.Z, 1);

            } else {


                //check north
                if (!game.getPlayer1().hasPiece(col, row - 1) && !game.getPlayer2().hasPiece(col, row - 1)) {

                    //rotate north
                    piece.getObject3D().rotate(Vector3.X, .5);

                } else if (!game.getPlayer1().hasPiece(col, row + 1) && !game.getPlayer2().hasPiece(col, row + 1)) {

                    //rotate south
                    piece.getObject3D().rotate(Vector3.X, -.5);

                } else if (!game.getPlayer1().hasPiece(col - 1, row) && !game.getPlayer2().hasPiece(col - 1, row)) {

                    //rotate west
                    piece.getObject3D().rotate(Vector3.Z, -.5);

                } else if (!game.getPlayer1().hasPiece(col + 1, row) && !game.getPlayer2().hasPiece(col + 1, row)) {

                    //rotate east
                    piece.getObject3D().rotate(Vector3.Z, .5);
                } else {

                    //rotate east by default
                    piece.getObject3D().rotate(Vector3.Z, .25);
                }
            }
        }
    }

    protected static void createMaterials(Game game) {

        try {

            game.textureWhite = new Material();
            game.textureWhite.enableLighting(true);
            game.textureWhite.setDiffuseMethod(new DiffuseMethod.Lambert());
            game.textureWhite.setColorInfluence(0);
            game.textureWhite.addTexture(new Texture("textureWhite", R.drawable.white));

            game.textureWood = new Material();
            game.textureWood.enableLighting(true);
            game.textureWood.setDiffuseMethod(new DiffuseMethod.Lambert());
            game.textureWood.setColorInfluence(0);
            game.textureWood.addTexture(new Texture("textureWood", R.drawable.wood));

            game.textureHighlight = new Material();
            game.textureHighlight.enableLighting(true);
            game.textureHighlight.setDiffuseMethod(new DiffuseMethod.Lambert());
            game.textureHighlight.setColorInfluence(0);
            game.textureHighlight.addTexture(new Texture("textureHighlighted", R.drawable.highlighted));

            game.textureValid = new Material();
            game.textureValid.enableLighting(true);
            game.textureValid.setDiffuseMethod(new DiffuseMethod.Lambert());
            game.textureValid.setColorInfluence(0);
            game.textureValid.addTexture(new Texture("textureValid", R.drawable.valid));

            game.texturePosition = new Material();
            game.texturePosition.setColorInfluence(0f);
            game.texturePosition.addTexture(new Texture("number_texture", R.drawable.number));

        } catch (Exception e) {
            UtilityHelper.handleException(e);
        }
    }

    protected static void loadPositions(Game game) {

        if (game.positions != null) {

            for (int i = 0; i < game.positions.size(); i++) {

                //remove any existing children
                game.getActivity().getSurfaceView().getRenderer().getCurrentScene().removeChild(game.positions.get(i).getObject3D());
            }

        } else {

            //create a new list
            game.positions = new ArrayList<>();
        }

        try {

            //load the letters
            loadCustomPosition(game, R.raw.letter_a_stl, false, .003, -1, 0);
            loadCustomPosition(game, R.raw.letter_b_stl, false, .003, -1, 1);
            loadCustomPosition(game, R.raw.letter_c_stl, false, .003, -1, 2);
            loadCustomPosition(game, R.raw.letter_d_stl, false, .003, -1, 3);
            loadCustomPosition(game, R.raw.letter_e_stl, false, .003, -1, 4);
            loadCustomPosition(game, R.raw.letter_f_stl, false, .003, -1, 5);
            loadCustomPosition(game, R.raw.letter_g_stl, false, .003, -1, 6);
            loadCustomPosition(game, R.raw.letter_h_stl, false, .003, -1, 7);

            //load the numbers
            loadCustomPosition(game, R.raw.number1_stl, true, .2, 0, -1);
            loadCustomPosition(game, R.raw.number2_stl, true, .2, 1, -1);
            loadCustomPosition(game, R.raw.number3_stl, true, .2, 2, -1);
            loadCustomPosition(game, R.raw.number4_stl, true, .2, 3, -1);
            loadCustomPosition(game, R.raw.number5_stl, true, .2, 4, -1);
            loadCustomPosition(game, R.raw.number6_stl, true, .2, 5, -1);
            loadCustomPosition(game, R.raw.number7_stl, true, .2, 6, -1);
            loadCustomPosition(game, R.raw.number8_stl, true, .2, 7, -1);

        } catch (Exception e) {
            UtilityHelper.handleException(e);
        }
    }

    public static void displayPositions(Game game, final boolean visible) {

        if (game.positions != null) {
            for (int i = 0; i < game.positions.size(); i++) {
                if (game.positions.get(i).getObject3D() != null)
                    game.positions.get(i).getObject3D().setVisible(visible);
            }
        }
    }

    private static void loadCustomPosition(final Game game, final int resId, final boolean horizontal, final double scale, final int col, final int row) {

        try {

            //get our 3d object
            Object3D object3D = loadPositonModel(game, resId, horizontal, scale, col, row);

            CustomPosition customPosition = new CustomPosition(horizontal);
            customPosition.setCol(col);
            customPosition.setRow(row);
            customPosition.setObject3D(object3D);

            //add to the scene
            game.getActivity().getSurfaceView().getRenderer().getCurrentScene().addChild(object3D);

            //add to our array list
            game.positions.add(customPosition);

        } catch (Exception e) {
            UtilityHelper.handleException(e);
        }
    }

    private static Object3D loadPositonModel(Game game, int resid, boolean horizontal, double scale, int col, int row) {

        Object3D obj = null;

        try {

            //parse our 3d model
            LoaderSTL stlParser = new LoaderSTL(
                game.getActivity().getSurfaceView().getRenderer().getContext().getResources(),
                game.getActivity().getSurfaceView().getRenderer().getTextureManager(),
                resid);
            stlParser.parse();

            //get our 3d object
            obj = stlParser.getParsedObject();

            //figure out where to render the 3d object
            updatePositionModel(obj, horizontal, col, row, Vector3.Axis.X, 90);

            //our model is too large so we need to shrink it
            obj.setScale(scale);

            //assign texture to 3d object
            obj.setMaterial(game.texturePosition);

        } catch (Exception e) {
            UtilityHelper.handleException(e);
        }

        return obj;
    }

    public static void adjustPositionModels(Game game) {

        if (game.positions != null) {

            for (int i = 0; i < game.positions.size(); i++) {
                CustomPosition customPosition = game.positions.get(i);

                customPosition.changePositions();
                updatePositionModel(
                        customPosition.getObject3D(),
                        customPosition.isHorizontal(),
                        (int)customPosition.getCol(),
                        (int)customPosition.getRow(),
                        Vector3.Axis.Y,
                        180);
            }
        }
    }

    private static void updatePositionModel(Object3D obj, boolean horizontal, int col, int row, Vector3.Axis axis, double angle) {

        if (horizontal) {

            //figure out where to render the 3d object
            final double x = getCoordinate(col) - .025;
            final double y = Y;
            final double z = getCoordinate(row);

            //rotate piece 90 degrees so it is standing up
            obj.rotate(axis, angle);

            //assign the location
            obj.setPosition(x, y, z);

        } else {

            //figure out where to render the 3d object
            final double x = getCoordinate(col) - .1;
            final double y = Y;
            final double z = getCoordinate(row) - .05;

            //rotate piece 90 degrees so it is standing up
            obj.rotate(axis, angle);

            //assign the location
            obj.setPosition(x, y, z);
        }
    }

    protected static void recycleModels(Game game) {

        if (game.textureWhite != null) {
            game.textureWhite.unbindTextures();
            game.textureWhite = null;
        }

        if (game.textureWood != null) {
            game.textureWood.unbindTextures();
            game.textureWood = null;
        }

        if (game.textureHighlight != null) {
            game.textureHighlight.unbindTextures();
            game.textureHighlight = null;
        }

        if (game.textureValid != null) {
            game.textureValid.unbindTextures();
            game.textureValid = null;
        }

        if (game.texturePosition != null) {
            game.texturePosition.unbindTextures();
            game.texturePosition = null;
        }

        if (game.positions != null) {
            for (int i = 0; i < game.positions.size(); i++) {
                if (game.positions.get(i) != null && game.positions.get(i).getObject3D() != null) {
                    if (game.getActivity().getSurfaceView() != null && game.getActivity().getSurfaceView().getRenderer() != null)
                        game.getActivity().getSurfaceView().getRenderer().getCurrentScene().removeChild(game.positions.get(i).getObject3D());

                    if (game.positions.get(i).getObject3D().getMaterial() != null)
                        game.positions.get(i).getObject3D().setMaterial(null);

                    game.positions.get(i).getObject3D().destroy();
                    game.positions.get(i).setObject3D(null);
                }
            }

            game.positions.clear();
            game.positions = null;
        }

        if (game.promotions != null) {
            for (int i = 0; i < game.promotions.size(); i++) {
                if (game.promotions.get(i) != null) {
                    if (game.getActivity().getSurfaceView() != null && game.getActivity().getSurfaceView().getRenderer() != null) {
                        game.getActivity().getSurfaceView().getRenderer().getCurrentScene().removeChild(game.promotions.get(i).getObject3D());
                        game.getActivity().getSurfaceView().getRenderer().getObjectPicker().unregisterObject(game.promotions.get(i).getObject3D());
                    }

                    game.promotions.get(i).dispose();
                    game.promotions.set(i, null);
                }
            }

            game.promotions.clear();
            game.promotions = null;
        }

        if (game.player1 != null) {

            for (int i = 0; i < game.player1.getPieceCount(); i++) {

                //get the current piece
                Piece piece = game.player1.getPiece(i, true);

                if (piece == null)
                    continue;

                //remove previous, if it exists
                if (piece.getObject3D() != null) {
                    if (game.getActivity().getSurfaceView() != null && game.getActivity().getSurfaceView().getRenderer() != null) {
                        game.getActivity().getSurfaceView().getRenderer().getCurrentScene().removeChild(piece.getObject3D());
                        game.getActivity().getSurfaceView().getRenderer().getObjectPicker().unregisterObject(piece.getObject3D());
                    }

                    if (piece.getObject3D().getMaterial() != null)
                        piece.getObject3D().setMaterial(null);

                    piece.getObject3D().destroy();
                    piece.setObject3D(null);
                }
            }
        }

        if (game.player2 != null) {

            for (int i = 0; i < game.player2.getPieceCount(); i++) {

                //get the current piece
                Piece piece = game.player2.getPiece(i, true);

                if (piece == null)
                    continue;

                //remove previous, if it exists
                if (piece.getObject3D() != null) {
                    if (game.getActivity().getSurfaceView() != null && game.getActivity().getSurfaceView().getRenderer() != null) {
                        game.getActivity().getSurfaceView().getRenderer().getCurrentScene().removeChild(piece.getObject3D());
                        game.getActivity().getSurfaceView().getRenderer().getObjectPicker().unregisterObject(piece.getObject3D());
                    }

                    if (piece.getObject3D().getMaterial() != null)
                        piece.getObject3D().setMaterial(null);

                    piece.getObject3D().destroy();
                    piece.setObject3D(null);
                }
            }
        }
    }
}