package com.gamesbykevin.chess.game;

import com.gamesbykevin.chess.common.ICommon;

/**
 * Game interface methods
 * @author GOD
 */
public interface IGame extends ICommon
{
    /**
     * Handle onPause activity event
     */
    public void onPause();

    /**
     * Handle onResume activity event
     */
    public void onResume();

    /**
     * Update the game based on a motion event
     * @param action The action of the MotionEvent
     * @param x (x-coordinate)
     * @param y (y-coordinate)
     * @throws Exception
     * @return true if we want to keep receiving events, false otherwise
     */
    boolean onTouchEvent(final int action, final float x, final float y) throws Exception;
}