package com.gamesbykevin.chess.players;

/**
 * Created by Kevin on 10/17/2017.
 */
public class Cpu extends Player {

    //did we calculate the next move?
    private boolean calculate = false;

    //number of moves we look into the future
    public static final int DEFAULT_DEPTH = 2;

    protected Cpu(final Direction direction) {
        super(false, direction);
    }

    @Override
    public void reset() {

        //we will need to calculate again next time
        this.calculate = false;
    }

    @Override
    public void update(Players players) {

        //store the current turn
        boolean player1Turn = new Boolean(players.isPlayer1Turn());

        //did we calculate our move yet?
        if (!calculate) {

            //flag calculate true
            calculate = true;

            //get the best move
            PlayerHelper.Move bestMove = PlayerHelper.getBestMove(players);

            //make our best move
            bestMove.piece.setCol(bestMove.destCol);
            bestMove.piece.setRow(bestMove.destRow);

            //assign our destination coordinates
            bestMove.piece.setDestinationCoordinates(
                PlayerHelper.getCoordinate(bestMove.destCol),
                PlayerHelper.getCoordinate(bestMove.destRow)
            );

            //flag that we moved the piece at least once
            bestMove.piece.setMoved(true);

            //flag that the cpu is moving a piece
            players.setMoving(true);

            //also mark this as our selected piece
            players.setSelected(bestMove.piece);
        }

        //restore the correct player's turn
        players.setPlayer1Turn(player1Turn);
    }
}