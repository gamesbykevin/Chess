package com.gamesbykevin.chess.services;

import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.game.Game;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Created by Kevin on 8/29/2017.
 */
public class AchievementHelper {

    public static void completedGame(final BaseGameActivity activity, final Game game) {

        //unlock achievement, we completed our first puzzle
        //activity.unlockAchievement(R.string.achievement_completed_a_puzzle);

        //keep incrementing the progress of these achievements
        //activity.incrementAchievement(R.string.achievement_complete_50_puzzles, 1);
    }
}