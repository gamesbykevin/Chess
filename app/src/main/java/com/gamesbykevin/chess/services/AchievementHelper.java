package com.gamesbykevin.chess.services;

import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.game.Game;
import com.gamesbykevin.chess.players.Player;
import com.gamesbykevin.chess.players.PlayerVars;
import com.gamesbykevin.chess.util.UtilityHelper;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

import static com.gamesbykevin.chess.activity.GameActivity.getGame;

/**
 * Created by Kevin on 8/29/2017.
 */
public class AchievementHelper {

    public static void trackAchievements(final BaseGameActivity activity) {

        try {

            if (getGame() == null || activity == null)
                return;

            if (!activity.getApiClient().isConnected())
                return;

            //obtain the players in the game
            Player player1 = getGame().getPlayer1();
            Player player2 = getGame().getPlayer2();

            //if player(s) don't exist, we can't continue (this shouldn't happen)
            if (player1 == null || player2 == null)
                return;

            //achievements will depend on the game mode played
            switch (getGame().getMode()) {

                case HumVsCpu:
                case HumVsCpuTimed:
                    activity.unlockAchievement(R.string.achievement_play_1_game_vs_ai);
                    activity.incrementAchievement(R.string.achievement_play_10_games_vs_ai, 1);
                    activity.incrementAchievement(R.string.achievement_play_50_games_vs_ai, 1);
                    activity.incrementAchievement(R.string.achievement_play_100_games_vs_ai, 1);

                    //update additional achievements
                    if (hasWin(player1, player2)) {
                        activity.unlockAchievement(R.string.achievement_win_1_game_vs_ai);
                        activity.incrementAchievement(R.string.achievement_win_10_games_vs_ai, 1);
                        activity.incrementAchievement(R.string.achievement_win_50_games_vs_ai, 1);
                        activity.incrementAchievement(R.string.achievement_play_100_games_vs_ai, 1);

                        //unlock achievement based on difficulty
                        switch (activity.getIntValue(R.string.difficulty_file_key)) {

                            //easy
                            case 0:
                                activity.unlockAchievement(R.string.achievement_beat_the_ai_easy);
                                break;

                            //medium
                            case 1:
                                activity.unlockAchievement(R.string.achievement_beat_the_ai_medium);
                                break;

                            //hard
                            case 2:
                                activity.unlockAchievement(R.string.achievement_beat_the_ai_hard);
                                break;

                            //harder
                            case 3:
                                activity.unlockAchievement(R.string.achievement_beat_the_ai_harder);
                                break;
                        }
                    }
                    break;

                case HumVsHumOnlineTimed:
                    activity.unlockAchievement(R.string.achievement_play_1_game_online);
                    activity.incrementAchievement(R.string.achievement_play_10_games_online, 1);
                    activity.incrementAchievement(R.string.achievement_play_50_games_online, 1);
                    activity.incrementAchievement(R.string.achievement_play_100_games_online, 1);

                    //update additional achievements
                    if (hasWin(player1, player2)) {
                        activity.unlockAchievement(R.string.achievement_win_1_game_online);
                        activity.incrementAchievement(R.string.achievement_win_10_games_online, 1);
                        activity.incrementAchievement(R.string.achievement_win_50_games_online, 1);
                        activity.incrementAchievement(R.string.achievement_win_100_games_online, 1);
                    }
                    break;
            }

        } catch (Exception e) {
            UtilityHelper.handleException(e);
        }
    }

    private static boolean hasWin(Player p1, Player p2) {

        switch (PlayerVars.STATE) {

            case WinPlayer1:
            case Player2TimeUp:

                //if player 1 is human and not online, and player 2 is online or cpu then we won!!!!
                if (p1.isHuman() && !p1.isOnline() && (p2.isOnline() || !p2.isHuman()))
                    return true;

                break;

            case WinPlayer2:
            case Player1TimeUp:

                //if player 2 is human and not online, and player 1 is online or cpu then we won!!!!
                if (p2.isHuman() && !p2.isOnline() && (p1.isOnline() || !p1.isHuman()))
                    return true;

                break;

            default:
                return false;
        }

        //we didn't find a win
        return false;
    }

    public static void trackAchievementSaveReplay(final BaseGameActivity activity) {

        if (activity == null || !activity.getApiClient().isConnected())
            return;

        try {
            activity.unlockAchievement(R.string.achievement_save_a_replay);
        } catch (Exception e) {
            UtilityHelper.handleException(e);
        }
    }

    public static void trackAchievementWatchReplay(final BaseGameActivity activity) {

        if (activity == null || !activity.getApiClient().isConnected())
            return;

        try {
            activity.unlockAchievement(R.string.achievement_watch_a_replay);
        } catch (Exception e) {
            UtilityHelper.handleException(e);
        }
    }
}