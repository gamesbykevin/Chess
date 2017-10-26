package com.gamesbykevin.chess.game;

import com.gamesbykevin.chess.activity.GameActivity;
import com.gamesbykevin.chess.players.Players;
import com.gamesbykevin.chess.util.UtilityHelper;

/**
 * Created by Kevin on 7/19/2017.
 */
public class Game implements IGame {

    //store activity reference
    private final GameActivity activity;

    private Players players;

    public Game(GameActivity activity) {

        //store activity reference
        this.activity = activity;
    }

    public Players getPlayers() {
        return this.players;
    }

    public GameActivity getActivity() {
        return this.activity;
    }

    @Override
    public void onPause() {

        //interrupt our game
        GameActivity.INTERRUPT = true;
    }

    @Override
    public void onResume() {

        //don't interrupt our game
        GameActivity.INTERRUPT = false;
    }

    @Override
    public void reset() {

        try {

            //create the players and reset the pieces
            //this.players = new Players(Players.Mode.CpuVsCpu);
            this.players = new Players(getActivity(), Players.Mode.HumVsCpu);
            this.players.reset(getActivity().getSurfaceView().getRenderer());

        } catch (Exception e) {
            UtilityHelper.handleException(e);
        }



        //track game started in analytics
        //AnalyticsHelper.trackPuzzleStarted(getActivity(), this);
    }

    @Override
    public void update() {

        //update the players if needed
        if (getPlayers() != null && getActivity() != null && getActivity().getSurfaceView() != null)
            getPlayers().update(getActivity().getSurfaceView().getRenderer());
    }

    /**
     * Recycle objects
     */
    @Override
    public void dispose() {

    }

    @Override
    public boolean onTouchEvent(final int action, float x, float y) {
        //return true to keep receiving events
        return true;
    }

    @Override
    public void render(float[] m) {

    }
}