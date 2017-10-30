package com.gamesbykevin.chess.players;

/**
 * Created by Kevin on 10/26/2017.
 */

public class PlayerVars {

    /**
     * Is it player 1's turn?
     */
    public static boolean PLAYER_1_TURN = true;

    public enum Status {
        Select,
        Move,
        Promote,
        Interrupt
    }

    public enum State {
        Playing,
        WinPlayer1,
        WinPlayer2,
        Stalemate
    }

    public static boolean isGameover() {
        return (STATE != State.Playing);
    }

    /**
     * What is the current status
     */
    public static Status STATUS = Status.Select;

    /**
     * What is going on with the game at the moment
     */
    public static State STATE = State.Playing;
}
