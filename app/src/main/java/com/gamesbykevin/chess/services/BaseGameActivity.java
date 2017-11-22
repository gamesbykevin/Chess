package com.gamesbykevin.chess.services;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.gamesbykevin.chess.activity.BaseActivity;
import com.gamesbykevin.chess.util.UtilityHelper;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;

import static com.gamesbykevin.chess.util.UtilityHelper.DEBUG;

/**
 * Created by Kevin on 8/1/2017.
 */
public abstract class BaseGameActivity extends BaseActivity implements GameHelper.GameHelperListener {

    // The game helper object. This class is mainly a wrapper around this object.
    private GameHelper mHelper;

    // We expose these constants here because we don't want users of this class
    // to have to know about GameHelper at all.
    private static final int CLIENT_GAMES = GameHelper.CLIENT_GAMES;
    private static final int CLIENT_PLUS = GameHelper.CLIENT_PLUS;
    private static final int CLIENT_ALL = GameHelper.CLIENT_ALL;

    // Requested clients. By default, that's just the games client.
    private int mRequestedClients = CLIENT_GAMES;

    /**
     * Did we want to access the achievements
     */
    protected boolean ACCESS_ACHIEVEMENT = false;

    /**
     * Did we want to access the leader boards
     */
    protected boolean ACCESS_LEADERBOARD = false;

    /**
     * The leader board id we want to display
     */
    protected static String LEADERBOARD_ID = "";

    public static boolean ONLINE = false;

    /**
     * Do we skip future login? (this is in case the player does not want to sign in)
     */
    public static boolean BYPASS_LOGIN = false;

    /**
     * Constructs a BaseGameActivity with default client (GamesClient)
     */
    protected BaseGameActivity() {
        super();
    }

    /**
     * Constructs a BaseGameActivity with the requested clients.
     * @param requestedClients The requested clients (a combination of CLIENT_GAMES,
     *         CLIENT_PLUS).
     */
    protected BaseGameActivity(int requestedClients) {
        super();
        setRequestedClients(requestedClients);
    }

    /**
     * Sets the requested clients. The preferred way to set the requested clients is
     * via the constructor, but this method is available if for some reason your code
     * cannot do this in the constructor. This must be called before onCreate or getGameHelper()
     * in order to have any effect. If called after onCreate()/getGameHelper(), this method
     * is a no-op.
     *
     * @param requestedClients A combination of the flags CLIENT_GAMES, CLIENT_PLUS
     *         or CLIENT_ALL to request all available clients.
     */
    private void setRequestedClients(int requestedClients) {
        mRequestedClients = requestedClients;
    }

    protected GameHelper getGameHelper() {
        if (mHelper == null) {
            mHelper = new GameHelper(this, mRequestedClients);
            mHelper.enableDebugLog(DEBUG);
        }
        return mHelper;
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (mHelper == null) {
            getGameHelper();
        }
        mHelper.setup(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //only call game helper once
        mHelper.onStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHelper.onStop();
    }

    @Override
    protected void onActivityResult(int request, int response, Intent data) {
        super.onActivityResult(request, response, data);
        mHelper.onActivityResult(request, response, data);
    }

    protected GoogleApiClient getApiClient() {
        return getGameHelper().getApiClient();
    }

    public void unlockAchievement(final int resId) {

        try {

            //don't continue if not connected
            if (!getApiClient().isConnected())
                return;

            String achievementId = getString(resId);

            if (DEBUG)
                UtilityHelper.logEvent("Unlocking achievement: " + achievementId);

            Games.Achievements.unlock(getApiClient(), achievementId);

            if (DEBUG)
                UtilityHelper.logEvent("Done Unlocking achievement: " + achievementId);

        } catch (Exception e) {
            UtilityHelper.handleException(e);
        }
    }

    public void incrementAchievement(final int resId, final int incrementValue) {

        try {

            //don't continue if not connected
            if (!getApiClient().isConnected())
                return;

            String achievementId = getString(resId);

            if (DEBUG)
                UtilityHelper.logEvent("Incrementing achievement: " + achievementId + ", " + incrementValue);

            Games.Achievements.increment(getApiClient(), achievementId, incrementValue);

            if (DEBUG)
                UtilityHelper.logEvent("Done Incrementing achievement: " + achievementId + ", " + incrementValue);

        } catch (Exception e) {
            UtilityHelper.handleException(e);
        }
    }

    public void updateLeaderboard(final int resId, final long score) {

        try {

            //don't continue if not connected
            if (!getApiClient().isConnected())
                return;

            String leaderboardId = getString(resId);

            if (DEBUG)
                UtilityHelper.logEvent("Updating leader board: " + leaderboardId + ", " + score);

            Games.Leaderboards.submitScore(getApiClient(), leaderboardId, score);

            if (DEBUG)
                UtilityHelper.logEvent("Done Updating leader board: " + leaderboardId + ", " + score);

        } catch (Exception e) {
            UtilityHelper.handleException(e);
        }
    }

    protected boolean isSignedIn() {
        return mHelper.isSignedIn();
    }

    protected void beginUserInitiatedSignIn() {
        mHelper.beginUserInitiatedSignIn();
    }

    protected void signOut() {
        try {
            mHelper.signOut();
        } catch (Exception e) {
            UtilityHelper.handleException(e);
        }
    }

    protected void showAlert(String message) {
        mHelper.makeSimpleDialog(message).show();
    }

    protected void showAlert(String title, String message) {
        mHelper.makeSimpleDialog(title, message).show();
    }

    protected void enableDebugLog(boolean enabled) {
        if (mHelper != null) {
            mHelper.enableDebugLog(enabled);
        }
    }

    @Deprecated
    protected void enableDebugLog(boolean enabled, String tag) {
        Log.w(TAG, "BaseGameActivity.enabledDebugLog(bool,String) is deprecated. Use enableDebugLog(boolean)");
        enableDebugLog(enabled);
    }

    protected String getInvitationId() {
        return mHelper.getInvitationId();
    }

    protected void reconnectClient() {
        mHelper.reconnectClient();
    }

    protected boolean hasSignInError() {
        return mHelper.hasSignInError();
    }

    protected GameHelper.SignInFailureReason getSignInError() {
        return mHelper.getSignInError();
    }

    public void onClickAchievements(View view) {

        //if we are connected, display default achievements ui
        if (getApiClient().isConnected()) {
            displayAchievementUI();
        } else {

            if (DEBUG)
                UtilityHelper.logEvent("beginUserInitiatedSignIn() before");

            //if not connected, re-attempt google play login
            beginUserInitiatedSignIn();

            if (DEBUG)
                UtilityHelper.logEvent("beginUserInitiatedSignIn() after");

            //flag that we want to open the achievements
            ACCESS_ACHIEVEMENT = true;
        }
    }

    protected void displayAchievementUI() {

        if (getApiClient().isConnected()) {

            if (DEBUG)
                UtilityHelper.logEvent("Displaying achievement ui");

            startActivityForResult(Games.Achievements.getAchievementsIntent(getApiClient()), 1);

        } else {

            if (DEBUG)
                UtilityHelper.logEvent("Logging in google play");

            //flag that we want to open the achievements
            ACCESS_ACHIEVEMENT = true;

            //if not connected, re-attempt google play login
            beginUserInitiatedSignIn();
        }
    }

    protected void displayLeaderboardUI(final String leaderboardId) {

        if (getApiClient().isConnected()) {

            if (DEBUG)
                UtilityHelper.logEvent("Displaying leaderboard ui " + leaderboardId);

            if (leaderboardId == null || leaderboardId.length() < 1) {

                //start all leader board activity
                startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(getApiClient()), 1);

            } else {

                Intent intent = Games.Leaderboards.getLeaderboardIntent(
                    getApiClient(),
                    leaderboardId,
                    LeaderboardVariant.TIME_SPAN_ALL_TIME,
                    LeaderboardVariant.COLLECTION_PUBLIC
                );

                // REQUEST_LEADERBOARD is an arbitrary constant to check for in onActivityResult
                startActivityForResult(intent, 1);
            }


        } else {

            if (DEBUG)
                UtilityHelper.logEvent("Logging in google play");

            //assign the leader board, so if we login we know where to go
            LEADERBOARD_ID = leaderboardId;

            //flag that we want to open the achievements
            ACCESS_LEADERBOARD = true;

            //if not connected, re-attempt google play login
            beginUserInitiatedSignIn();
        }
    }

    @Override
    public void onSignInSucceeded() {

        if (DEBUG)
            UtilityHelper.displayMessage(this, "Google Play login worked!");

        if (ACCESS_ACHIEVEMENT) {

            //if we just came from achievements button and are now signed in, display ui
            displayAchievementUI();

            //flag back false
            ACCESS_ACHIEVEMENT = false;

        } else if (ACCESS_LEADERBOARD) {

            //if we just logged in trying to access leader board display it
            displayLeaderboardUI(LEADERBOARD_ID);

            ACCESS_LEADERBOARD = false;
        }

        //don't bypass auto login
        BYPASS_LOGIN = false;

        //flag we are online
        ONLINE = true;
    }

    @Override
    public void onSignInFailed() {

        if (DEBUG)
            UtilityHelper.displayMessage(this, "Google play login failed!");

        //bypass auto login
        BYPASS_LOGIN = true;

        //flag we are offline
        ONLINE = false;
    }
}