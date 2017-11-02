package com.gamesbykevin.chess.game;

import android.content.SharedPreferences;

import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.activity.GameActivity;
import com.gamesbykevin.chess.players.PlayerHelper.Move;
import com.gamesbykevin.chess.players.PlayerVars;

import java.util.List;

import static com.gamesbykevin.androidframeworkv2.activity.BaseActivity.GSON;
import static com.gamesbykevin.androidframeworkv2.activity.BaseActivity.getSharedPreferences;

/**
 * Created by Kevin on 10/30/2017.
 */
public class GameHelper {

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
}