package com.gamesbykevin.chess.services;

import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.util.UtilityHelper;
import com.google.android.gms.games.Games;

import static com.gamesbykevin.chess.activity.GameActivity.getGame;
import static com.gamesbykevin.chess.util.UtilityHelper.DEBUG;

/**
 * Created by Kevin on 11/24/2017.
 */
public class EventHelper {

    public static void trackEvents(final BaseGameActivity activity) {

        try {

            if (activity == null || getGame() == null || !activity.getApiClient().isConnected())
                return;

            switch (getGame().getMode()) {

                case HumVsCpu:
                    trackEventGamePlayedNormalAi(activity);
                    break;

                case HumVsCpuTimed:
                    trackEventGamePlayedSpeedAi(activity);
                    break;

                case HumVsHumOffline:
                    trackEventGamePlayedTotal(activity);
                    break;

                case HumVsHumOnlineTimed:
                    trackEventGamePlayedOnline(activity);
                    break;
            }

        } catch (Exception e) {
            UtilityHelper.handleException(e);
        }
    }

    public static void trackEventGameWatchReplay(final BaseGameActivity activity) {
        updateEvent(activity, R.string.event_global_watched_replays, 1);
    }

    public static void trackEventGameSavedReplay(final BaseGameActivity activity) {
        updateEvent(activity, R.string.event_global_saved_replays, 1);
    }

    private static void trackEventGamePlayedOnline(final BaseGameActivity activity) {
        updateEvent(activity, R.string.event_global_games_played_online, 1);
        trackEventGamePlayedTotal(activity);
    }

    private static void trackEventGamePlayedSpeedAi(final BaseGameActivity activity) {
        updateEvent(activity, R.string.event_global_games_played_speed_vs_ai, 1);
        trackEventGamePlayedTotal(activity);
    }

    private static void trackEventGamePlayedNormalAi(final BaseGameActivity activity) {
        updateEvent(activity, R.string.event_global_games_played_normal_vs_ai, 1);
        trackEventGamePlayedTotal(activity);
    }

    private static void trackEventGamePlayedTotal(final BaseGameActivity activity) {
        updateEvent(activity, R.string.event_global_games_played_total, 1);
    }

    private static void updateEvent(final BaseGameActivity activity, final int resId, final int amount) {
        updateEvent(activity, activity.getString(resId), amount);
    }

    private static void updateEvent(final BaseGameActivity activity, final String id, final int amount) {

        try {

            if (activity == null || !activity.getApiClient().isConnected())
                return;

            if (DEBUG)
                UtilityHelper.logEvent("Updating event: " + id + " - " + amount);

            //track the event
            Games.Events.increment(activity.getApiClient(), id, amount);

            if (DEBUG)
                UtilityHelper.logEvent("Done event: " + id + " - " + amount);

        } catch (Exception e) {
            UtilityHelper.handleException(e);
        }

    }
}