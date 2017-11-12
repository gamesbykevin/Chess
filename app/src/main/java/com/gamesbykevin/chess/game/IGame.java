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
    void onPause();

    /**
     * Handle onResume activity event
     */
    void onResume();
}