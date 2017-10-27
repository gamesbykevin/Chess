package com.gamesbykevin.chess.players;

import com.gamesbykevin.chess.piece.Piece;

/**
 * Created by Kevin on 10/17/2017.
 */
public class Cpu extends Player {

    //number of moves we look into the future
    public static final int DEFAULT_DEPTH = 2;

    private boolean calculate = false;

    protected Cpu(final Direction direction) {
        super(false, direction);
    }

    @Override
    public void reset() {

    }

    @Override
    public void update(Players players) {

        if (!calculate) {
            //get the best move
            PlayerHelper.Move bestMove = PlayerHelper.getBestMove(players);

            //if game is interrupted don't continue right now
            if (PlayerVars.STATUS == PlayerVars.Status.Interrupt)
                return;

            //get the piece for the move
            Piece piece = getPiece(bestMove.sourceCol, bestMove.sourceRow);

            //make our best move
            piece.setCol(bestMove.destCol);
            piece.setRow(bestMove.destRow);

            //assign our destination coordinates
            piece.setDestinationCoordinates(
                PlayerHelper.getCoordinate(bestMove.destCol),
                PlayerHelper.getCoordinate(bestMove.destRow)
            );

            //flag that we moved the piece at least once
            piece.setMoved(true);

            //also mark this as our selected piece
            players.setSelected(piece);

            //flag that the cpu is moving a piece
            PlayerVars.STATUS = PlayerVars.Status.Move;

            calculate = true;
        }
    }
}