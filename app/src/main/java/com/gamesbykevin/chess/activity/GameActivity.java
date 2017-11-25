package com.gamesbykevin.chess.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gamesbykevin.androidframeworkv2.base.Disposable;
import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.game.Game;
import com.gamesbykevin.chess.opengl.BasicRenderer;
import com.gamesbykevin.chess.opengl.OpenGLSurfaceView;
import com.gamesbykevin.chess.players.PlayerVars;
import com.gamesbykevin.chess.util.GameTimer;
import com.gamesbykevin.chess.util.UtilityHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.VISIBLE;
import static com.gamesbykevin.chess.activity.GameActivityHelper.toggleSettings;
import static com.gamesbykevin.chess.activity.GameActivityHelper.updateListView;
import static com.gamesbykevin.chess.util.UtilityHelper.DEBUG;

public class GameActivity extends MultiplayerActivity implements Disposable {

    //our open GL surface view
    private OpenGLSurfaceView glSurfaceView;

    //Our game manager class
    private static Game GAME;

    //has the activity been paused
    public boolean paused = false;

    //did we pause while playing multi player?
    private boolean pausedMulti = false;

    //our layout parameters
    private LinearLayout.LayoutParams layoutParams;

    //a list of layouts on the game screen, separate from open gl layout
    private List<ViewGroup> layouts;

    //keep track of game time
    private GameTimer timer;

    //our visual progress bar
    private ProgressBar progressBar;

    //our array adapter for the list views
    protected ArrayAdapter<String> adapter;

    //what is the current selected item
    protected int selectedListItem;

    //the current screen we are on
    private static int SCREEN = R.id.layoutLoadingScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (DEBUG)
            UtilityHelper.logEvent("onCreate");

        //call parent
        super.onCreate(savedInstanceState);

        //set the content view
        setContentView(R.layout.activity_game);

        //loading the game
        SCREEN = R.id.layoutLoadingScreen;

        //assign to calculate
        PlayerVars.STATUS = PlayerVars.Status.Select;

        //obtain our open gl surface view object for reference
        this.glSurfaceView = findViewById(R.id.openGLView);

        //obtain our progress bar
        this.progressBar = findViewById(R.id.simpleProgressBar);

        //obtain our list view
        final ListView listView = findViewById(R.id.listViewHistory);

        //create our data array adapter, use ArrayList so we can make it dynamic
        this.adapter = new ArrayAdapter<String>(this, R.layout.layout_history, new ArrayList<String>()) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                final View renderer = super.getView(position, convertView, parent);
                if (position == selectedListItem) {
                    renderer.setBackgroundResource(R.color.colorGray);
                } else  {
                    renderer.setBackgroundResource(R.color.colorLightGray);
                }
                return renderer;
            }
        };

        //update our list view with the adapter
        listView.setAdapter(this.adapter);

        //add the layouts to our list
        this.layouts = new ArrayList<>();
        this.layouts.add((ViewGroup)findViewById(R.id.layoutGameSettings));
        this.layouts.add((ViewGroup)findViewById(R.id.layoutLoadingScreen));
        this.layouts.add((ViewGroup)findViewById(R.id.layoutGameControls));
        this.layouts.add((ViewGroup)findViewById(R.id.layoutGameReplayPrompt));
        this.layouts.add((ViewGroup)findViewById(R.id.layoutGameMultiplayer));
        this.layouts.add((ViewGroup)findViewById(R.id.layoutGameMultiplayerPrompt));

        //create our game manager
        if (MULTI_PLAYER) {

            setScreen(R.id.layoutGameMultiplayer, true);

        } else {

            //create our game object
            createGame();

            //assign the current screen
            setScreen(getScreen(), true);
        }

        //add our history to the array adapter
        if (getGame() != null) {
            for (int i = 0; i < GAME.getHistory().size(); i++) {
                updateListView(this, GAME.getHistory().get(i).toString());
            }
        }

        //default start at top
        listView.setSelection(0);

        //flag paused false
        pausedMulti = false;
        paused = false;

        //play the main theme again from the beginning
        playSound(R.raw.theme, true, true);
    }

    @Override
    protected void onStart() {

        if (DEBUG)
            UtilityHelper.logEvent("onStart");

        //call parent
        super.onStart();
    }

    @Override
    protected void onDestroy() {

        if (DEBUG)
            UtilityHelper.logEvent("onDestroy");

        //call parent
        super.onDestroy();

        //cleanup resources
        if (GAME != null) {
            try {
                GAME.dispose();
            } catch (Exception e) {
                UtilityHelper.handleException(e);
            }
        }

        if (layouts != null) {

            for (ViewGroup view : layouts) {
                if (view != null) {
                    try {
                        view.removeAllViews();
                        view = null;
                    } catch (Exception e) {
                        UtilityHelper.handleException(e);
                    }
                }
            }

            layouts.clear();
            layouts = null;
        }

        if (timer != null) {
            timer.dispose();
            timer = null;
        }

        glSurfaceView = null;
        layoutParams = null;
        GAME = null;
    }

    @Override
    protected void onPause() {

        //flag paused true
        this.paused = true;

        if (DEBUG)
            UtilityHelper.logEvent("onPause");

        //call parent
        super.onPause();

        //pause the game
        if (getGame() != null)
            getGame().onPause();

        //if playing multi player
        if (MULTI_PLAYER) {

            //if we are in middle of playing a game
            if (getScreen() == R.id.layoutGameSettings || getScreen() == R.id.layoutGameControls)
                pausedMulti = true;
        }

        if (glSurfaceView != null) {

            //pause the game view
            glSurfaceView.onPause();

            //flag for recycling
            glSurfaceView = null;
        }

        //go back to loading screen
        setScreen(R.id.layoutLoadingScreen, true);

        //flag render false
        BasicRenderer.RENDER = false;

        //stop all sound
        stopSound();
    }

    @Override
    protected void onResume() {

        if (DEBUG)
            UtilityHelper.logEvent("onResume");

        //call parent
        super.onResume();

        //resume the game object
        if (getGame() != null)
            getGame().onResume();

        //if the game was previously paused we need to re-initialize the views
        if (this.paused) {

            //resume theme if the game was paused
            super.playTheme();

            //flag paused false
            this.paused = false;

            //create a new OpenGL surface view
            glSurfaceView = new OpenGLSurfaceView(this);

            //resume the game view
            getSurfaceView().onResume();

            //remove layouts from the parent view
            for (int i = 0; i < layouts.size(); i++) {
                ((ViewGroup)layouts.get(i).getParent()).removeView(layouts.get(i));
            }

            //set the content view for our open gl surface view
            setContentView(getSurfaceView());

            //add the layouts to the current content view
            for (int i = 0; i < layouts.size(); i++) {
                super.addContentView(layouts.get(i), getLayoutParams());
            }

        } else {

            //resume the game view
            getSurfaceView().onResume();
        }

        //determine what screen(s) are displayed
        setScreen(getScreen(), true);

        //update the progress bar
        updateProgress(0);

        //if we resumed in middle of a multi player game
        if (pausedMulti) {
            pausedMulti = false;
            displayMessage(R.string.resume_match_end);
            setScreen(R.id.layoutGameMultiplayer, true);
        }
    }

    @Override
    public void onBackPressed() {

        if (DEBUG)
            UtilityHelper.logEvent("onBackPressed");

        //if the game is over and not a replay, ask if they want to save the replay
        if (PlayerVars.isGameover() && getGame() != null && !getGame().hasReplay()) {

            //ask the player if they want to save the replay
            setScreen(R.id.layoutGameReplayPrompt, false);

            //don't continue
            return;

        } else if (MULTI_PLAYER) {

            if ((getScreen() == R.id.layoutGameControls || getScreen() == R.id.layoutGameSettings) && mRoomId != null) {

                //display the exit prompt
                setScreen(R.id.layoutGameMultiplayerPrompt, false);

            } else if (getScreen() == R.id.layoutGameMultiplayerPrompt) {

                //don't do anything here
                return;

            } else {

                //go back to mode selection
                super.onBackPressed();

                //finish activity
                finish();
            }

        } else {

            //call parent
            super.onBackPressed();

            //finish activity
            finish();
        }
    }

    public void updateProgress(final int progress) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(progress);
            }
        });
    }

    public OpenGLSurfaceView getSurfaceView() {
        return this.glSurfaceView;
    }

    public void onClickResetCamera(View view) {
        getSurfaceView().getRenderer().changeCamera();
    }

    public void createGame() {
        GAME = new Game(this);
    }

    public static Game getGame() {
        return GAME;
    }

    public GameTimer getTimer() {

        //create timer if null
        if (this.timer == null)
            this.timer = new GameTimer(this);

        //return our timer object
        return this.timer;
    }

    public void displayTimer(final boolean visible) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.tableGameTimer).setVisibility(visible ? VISIBLE : View.INVISIBLE);
            }
        });

    }

    public static int getScreen() {
        return SCREEN;
    }

    public void setScreen(final int screen, final boolean hide) {

        //default all layouts to hidden
        if (hide) {
            for (int i = 0; i < layouts.size(); i++) {
                setLayoutVisibility(layouts.get(i), false);
            }
        }

        //only display the correct screens
        switch (screen) {

            //show loading screen
            case R.id.layoutLoadingScreen:
                setLayoutVisibility((ViewGroup)findViewById(R.id.layoutLoadingScreen), true);
                break;

            //decide which game over screen is displayed
            case R.id.layoutGameSettings:
                setLayoutVisibility((ViewGroup)findViewById(R.id.layoutGameSettings), true);
                break;

            //don't re-enable anything
            case R.id.layoutGameControls:
                setLayoutVisibility((ViewGroup)findViewById(R.id.layoutGameControls), true);
                break;

            case R.id.layoutGameReplayPrompt:
                setLayoutVisibility((ViewGroup)findViewById(R.id.layoutGameReplayPrompt), true);
                break;

            case R.id.layoutGameMultiplayer:
                setLayoutVisibility((ViewGroup)findViewById(R.id.layoutGameMultiplayer), true);
                break;

            case R.id.layoutGameMultiplayerPrompt:
                setLayoutVisibility((ViewGroup)findViewById(R.id.layoutGameMultiplayerPrompt), true);
                break;

            default:
                throw new RuntimeException("Screen not handled: " + screen);
        }

        //assign screen to view
        this.SCREEN = screen;
    }

    private LinearLayout.LayoutParams getLayoutParams() {

        if (this.layoutParams == null)
            this.layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT , LinearLayout.LayoutParams.MATCH_PARENT);

        return this.layoutParams;
    }

    public void onClickSettings(View view) {

        //switch our current choice
        toggleSettings(this, getScreen() != R.id.layoutGameSettings);
    }

    public void onClickMultiYes(View view) {

        //go back to multi-player lobby
        setScreen(R.id.layoutGameMultiplayer, true);

        //make sure we leave the game room
        leaveRoom();
    }

    public void onClickMultiNo(View view) {

        //go back to the game controls screen
        setScreen(R.id.layoutGameControls, true);
    }

    public void onClickNo(View view) {

        //go back to the previous page
        super.onBackPressed();
    }

    public void onClickYes(View view) {

        //flag save true
        ReplayActivity.SAVE = true;
        startActivity(new Intent(this, ReplayActivity.class));
    }

    // Handle the result of the "Select players UI" we launched when the user clicked the
    // "Invite friends" button. We react by creating a room with those players.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == RC_SIGN_IN) {

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intent);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                onConnected(account);
            } catch (ApiException apiException) {
                String message = apiException.getMessage();
                if (message == null || message.isEmpty())
                    message = getString(R.string.signin_other_error);

                onDisconnected();

                new AlertDialog.Builder(this)
                        .setMessage(message)
                        .setNeutralButton(android.R.string.ok, null)
                        .show();
            }

        } else if (requestCode == RC_SELECT_PLAYERS) {

            // we got the result from the "select players" UI -- ready to create the room
            handleSelectPlayersResult(resultCode, intent);

        } else if (requestCode == RC_INVITATION_INBOX) {

            // we got the result from the "select invitation" UI (invitation inbox). We're
            // ready to accept the selected invitation:
            handleInvitationInboxResult(resultCode, intent);

        } else if (requestCode == RC_WAITING_ROOM) {

            // we got the result from the "waiting room" UI.
            if (resultCode == Activity.RESULT_OK) {

                // ready to start playing
                Log.d(TAG, "Starting game (waiting room returned OK).");

                //display loading screen
                setScreen(R.id.layoutLoadingScreen, true);

                //determine who is going first
                selectFirstTurn();

                //create the game
                createGame();

            } else if (resultCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {

                // player indicated that they want to leave the room
                leaveRoom();

                //go back to multi-player lobby
                setScreen(R.id.layoutGameMultiplayer, true);

            } else if (resultCode == Activity.RESULT_CANCELED) {

                // Dialog was cancelled (user pressed back key, for instance). In our game,
                // this means leaving the room too. In more elaborate games, this could mean
                // something else (like minimizing the waiting room UI).
                leaveRoom();

                //go back to multi-player lobby
                setScreen(R.id.layoutGameMultiplayer, true);
            }
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }
}