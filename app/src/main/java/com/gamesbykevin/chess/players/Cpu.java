package com.gamesbykevin.chess.players;

import com.gamesbykevin.androidframeworkv2.base.Cell;
import com.gamesbykevin.chess.piece.Piece;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin on 10/17/2017.
 */
public class Cpu extends Player {

    //did we calculate the next move?
    private boolean calculate = false;

    //number of moves we look into the future
    public static final int DEFAULT_DEPTH = 2;

    private List<Cell> piecesPlayer1, piecesPlayer2;

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

        //did we calculate our move yet?
        if (!calculate) {

            if (this.piecesPlayer1 != null && this.piecesPlayer2 != null) {

                //our game was interrupted, so restore the chess pieces
                for (int i = 0; i < piecesPlayer1.size(); i++) {

                    //get the current piece
                    Piece piece = players.getPlayer1().getPiece((int)piecesPlayer1.get(i).getCol(), (int)piecesPlayer1.get(i).getRow());
                }

                for (int i = 0; i < piecesPlayer2.size(); i++) {

                }

                ss;

            } else {

                //store locations of pieces on the board in case this process is interrupted
                this.piecesPlayer1 = new ArrayList<>();
                this.piecesPlayer2 = new ArrayList<>();
            }

            //store the current turn
            boolean player1Turn = new Boolean(players.isPlayer1Turn());

            //flag calculate true
            calculate = true;

            //get the best move
            PlayerHelper.Move bestMove = PlayerHelper.getBestMove(players);

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

            //flag that the cpu is moving a piece
            players.setMoving(true);

            //also mark this as our selected piece
            players.setSelected(piece);

            //restore the correct player's turn
            players.setPlayer1Turn(player1Turn);

            //remove these objects
            this.piecesPlayer1.clear();
            this.piecesPlayer1 = null;
            this.piecesPlayer2.clear();
            this.piecesPlayer2 = null;
        }
    }
}