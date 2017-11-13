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

import org.rajawali3d.math.vector.Vector3;

import java.util.List;

import static com.gamesbykevin.androidframeworkv2.activity.BaseActivity.GSON;
import static com.gamesbykevin.androidframeworkv2.activity.BaseActivity.getSharedPreferences;
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

    public static void saveHistory(GameActivity activity, Game game) {

        //obtain editor
        SharedPreferences.Editor editor = getSharedPreferences().edit();

        if (game.hasReplay()) {

            //assign setting
            editor.putString(activity.getString(R.string.saved_match_file_key), "");

        } else {

            //get our history
            List<Move> history = game.getHistory();

            //assign setting
            if (history != null)
                editor.putString(activity.getString(R.string.saved_match_file_key), GSON.toJson(history));
        }

        //save setting
        editor.apply();
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
            if ((int) piece.getCol() == (int)game.getSelected().getCol() &&
                    (int) piece.getRow() == (int)game.getSelected().getRow()) {

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
        PlayerHelper.updateStatus(opponent, player);

        //check for a draw (stalemate)
        checkDraw(game);

        //display status
        displayStatus(game, opponent);

        //we are at our destination, de-select the chess piece
        game.deselect();

        //check if we have a promotion
        if (PlayerHelper.hasPromotion(game)) {

            PlayerHelper.displayPromotion(game);

        } else {

            //if we aren't promoting a piece, switch turns
            game.switchTurns();
        }

        //if we have replay enabled, change the index
        if (game.hasReplay())
            Game.INDEX_REPLAY++;
    }

    public static void displayStatus(Game game, Player opponent) {

        if (DEBUG) {

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

                default:

                    if (opponent.hasCheck() && !game.hasReplay()) {
                        game.getActivity().displayMessage(PLAYER_1_TURN ? R.string.game_status_player_2_check : R.string.game_status_player_1_check);
                    } else if (!opponent.isHuman()) {
                        //getActivity().displayMessage("Thinking...");
                    }
                    break;
            }
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
}