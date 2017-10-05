package com.gamesbykevin.chess.game;

import android.view.MotionEvent;

import com.gamesbykevin.chess.activity.GameActivity;

import static com.gamesbykevin.chess.game.GameHelper.FRAMES;
import static com.gamesbykevin.chess.game.GameHelper.GAME_OVER;
import static com.gamesbykevin.chess.game.GameHelper.GAME_OVER_DELAY_FRAMES;
import static com.gamesbykevin.chess.opengl.OpenGLRenderer.LOADED;

/**
 * Created by Kevin on 7/19/2017.
 */
public class Game implements IGame {

    //store activity reference
    private final GameActivity activity;

    //are we pressing on the screen
    private boolean press = false;

    //did we perform the first render
    public static boolean INITIAL_RENDER = false;

    /**
     * The list of steps in the game
     */
    public enum Step {
        Start, Reset, Loading, GameOver, Running
    }

    //what is the current step that we are on
    public static Step STEP = Step.Loading;

    public Game(GameActivity activity) {

        //store activity reference
        this.activity = activity;

        //default to loading
        STEP = Step.Loading;
    }

    public GameActivity getActivity() {
        return this.activity;
    }

    @Override
    public void onPause() {
        //do we need to do anything here
    }

    @Override
    public void onResume() {
        //do we need to resume anything
    }

    @Override
    public void reset() {

        //reset initial render flag
        INITIAL_RENDER = false;

        //reset game components
        GameHelper.reset(this);

        //track game started in analytics
        //AnalyticsHelper.trackPuzzleStarted(getActivity(), this);
    }

    @Override
    public void update() {

        switch (STEP) {

            //we are loading
            case Loading:

                //if the textures have finished loading
                if (LOADED)
                    STEP = Step.Reset;
                break;

            //do nothing
            case Start:
                break;

            //we are resetting the game
            case Reset:

                //reset level
                reset();

                //after resetting, next step is updating
                STEP = Step.Running;
                break;

            //the main game occurs here
            case Running:

                //if the game is over, move to the next step
                if (GAME_OVER) {

                    //track game completed in analytics
                    //AnalyticsHelper.trackPuzzleCompleted(getActivity(), this);

                    //unlock any achievements we achieved
                    //AchievementHelper.completedGame(getActivity(), this);

                    //update the leader board as well (in milliseconds)
                    //LeaderboardHelper.updateLeaderboard(getActivity(), this);

                    //reset frames count
                    FRAMES = 0;

                    //move to game over step
                    STEP = Step.GameOver;

                    //vibrate the phone
                    activity.vibrate();

                } else {


                    //update

                    //if we already rendered the board once, lets display it
                    if (INITIAL_RENDER) {

                        if (getActivity().getScreen() == GameActivity.Screen.Loading) {

                            //if still loading switch to ready
                            getActivity().setScreen(GameActivity.Screen.Ready);

                        } else {

                            //if we are playing the game and the pieces have been placed, update the timer
                            if (getActivity().getScreen() == GameActivity.Screen.Ready)
                                getActivity().getTimer().update(getActivity());
                        }
                    }
                }
                break;

            //the game has ended
            case GameOver:

                //switch to game over screen if enough time passed and we haven't set yet
                if (FRAMES >= GAME_OVER_DELAY_FRAMES && getActivity().getScreen() != GameActivity.Screen.GameOver) {
                    //if enough time passed go to game over screen
                    getActivity().setScreen(GameActivity.Screen.GameOver);
                } else if (FRAMES <= GAME_OVER_DELAY_FRAMES) {
                    //keep track of time
                    FRAMES++;
                }
                break;
        }
    }

    /**
     * Recycle objects
     */
    @Override
    public void dispose() {
        GameHelper.dispose();
    }

    @Override
    public boolean onTouchEvent(final int action, float x, float y) {

        //don't continue if we aren't ready yet
        if (STEP != Step.Running)
            return true;

        if (action == MotionEvent.ACTION_UP) {
            //check the board for rotations
            //if (this.press)
                //getBoard().touch(x, y);

            //un-flag press
            this.press = false;
        } else if (action == MotionEvent.ACTION_DOWN) {
            //flag that we pressed down
            this.press = true;
        } else if (action == MotionEvent.ACTION_MOVE) {
            //flag press
            this.press = true;
        }

        //return true to keep receiving events
        return true;
    }

    @Override
    public void render(float[] m) {

        //don't display if we aren't ready
        if (STEP != Step.Running && STEP != Step.GameOver)
            return;

        //render everything on screen
        GameHelper.render(m);

        //flag that we have performed the initial render
        INITIAL_RENDER = true;
    }
}