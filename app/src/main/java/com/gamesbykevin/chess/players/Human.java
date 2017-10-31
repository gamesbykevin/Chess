package com.gamesbykevin.chess.players;

import com.gamesbykevin.chess.game.Game;

/**
 * Created by Kevin on 10/15/2017.
 */
public class Human extends Player {

    public Human(final Direction direction) {
        super(true, direction);
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public void update(Game game) {
        //do anything here
    }
}