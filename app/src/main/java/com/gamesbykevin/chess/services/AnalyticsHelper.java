package com.gamesbykevin.chess.services;

import android.os.Bundle;

import com.gamesbykevin.chess.activity.GameActivity;
import com.gamesbykevin.chess.game.Game;
import com.gamesbykevin.chess.util.UtilityHelper;

import static com.gamesbykevin.chess.util.UtilityHelper.DEBUG;

/**
 * Created by Kevin on 9/24/2017.
 */

public class AnalyticsHelper {

    private static final String KEY_PUZZLE_STARTED = "puzzle_started";

    private static final String KEY_PUZZLE_COMPLETED = "puzzle_completed";

    private static final String KEY_PUZZLE_DURATION = "puzzle_duration";

    private static final String KEY_PUZZLE_SIZE = "puzzle_size";

    private static final String KEY_PUZZLE_ROTATE = "puzzle_rotate";

    public static void trackStarted(final GameActivity activity, final Game game) {

        if (DEBUG)
            UtilityHelper.logEvent("Analytics puzzle started START");

        //create new bundle
        Bundle params = new Bundle();

        //add the parameter
        //params.putInt(KEY_PUZZLE_SIZE, size);

        //log the event
        activity.getFirebaseAnalytics().logEvent(KEY_PUZZLE_STARTED, params);

        if (DEBUG)
            UtilityHelper.logEvent("Analytics puzzle started END");
    }

    public static void trackCompleted(final GameActivity activity, final Game game) {

        if (DEBUG)
            UtilityHelper.logEvent("Analytics puzzle completed START");

        //create new bundle
        Bundle params = new Bundle();

        //add the puzzle size as a parameter
        //params.putInt(KEY_PUZZLE_SIZE, size);

        //track the duration
        params.putLong(KEY_PUZZLE_DURATION, activity.getTimer().getLapsed());

        //log the event
        activity.getFirebaseAnalytics().logEvent(KEY_PUZZLE_COMPLETED, params);

        if (DEBUG)
            UtilityHelper.logEvent("Analytics puzzle completed END");
    }
}