package com.gamesbykevin.chess.game;

import com.gamesbykevin.chess.activity.GameActivity;
import com.gamesbykevin.chess.players.PlayerVars;
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
        PlayerVars.STATUS = PlayerVars.Status.Interrupt;
    }

    @Override
    public void onResume() {

        //don't interrupt our game
        PlayerVars.STATUS = PlayerVars.Status.Select;
    }

    @Override
    public void reset() {

        try {

            //create the players and reset the pieces
            //this.players = new Players(Players.Mode.CpuVsCpu);
            this.players = new Players(getActivity(), Players.Mode.HumVsCpu);
            //this.players = new Players(getActivity(), Players.Mode.HumVsHum);
            this.players.reset();

        } catch (Exception e) {
            UtilityHelper.handleException(e);
        }

        //track game started in analytics
        //AnalyticsHelper.trackPuzzleStarted(getActivity(), this);
    }

    @Override
    public void update() {

        //update the players if needed
        if (getPlayers() != null)
            getPlayers().update();
    }

    /**
     * Recycle objects
     */
    @Override
    public void dispose() {
        this.players = null;
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