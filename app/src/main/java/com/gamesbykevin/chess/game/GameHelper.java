package com.gamesbykevin.chess.game;

import android.content.SharedPreferences;

import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.activity.GameActivity;
import com.gamesbykevin.chess.players.PlayerHelper.Move;

import java.util.List;

import static com.gamesbykevin.androidframeworkv2.activity.BaseActivity.GSON;
import static com.gamesbykevin.androidframeworkv2.activity.BaseActivity.getSharedPreferences;

/**
 * Created by Kevin on 10/30/2017.
 */

public class GameHelper {

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
