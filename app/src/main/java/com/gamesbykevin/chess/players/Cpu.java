package com.gamesbykevin.chess.players;

import com.gamesbykevin.chess.piece.Piece;
import com.gamesbykevin.chess.util.UtilityHelper;

import java.util.List;

import static com.gamesbykevin.chess.players.PlayerHelper.getMoves;
import static com.gamesbykevin.chess.util.UtilityHelper.DEBUG;

/**
 * Created by Kevin on 10/17/2017.
 */
public class Cpu extends Player {

    //number of moves we look into the future
    public static final int DEFAULT_DEPTH = 2;

    protected Cpu(final Direction direction) {
        super(false, direction);
    }

    @Override
    public void update(Players players) {

        //get the best move
        PlayerHelper.Move bestMove = getBestMove(players);

        //if game is interrupted don't continue right now
        if (PlayerVars.STATUS == PlayerVars.Status.Interrupt)
            return;

        //setup the next move
        PlayerHelper.setupMove(players, bestMove);
    }

    private PlayerHelper.Move getBestMove(Players players) {

        //keep track of the score for the best move
        int score = Integer.MIN_VALUE;

        //keep track of our best move
        PlayerHelper.Move bestMove = null;

        Player player = players.isPlayer1Turn() ? players.getPlayer1() : players.getPlayer2();
        Player opponent = players.isPlayer1Turn() ? players.getPlayer2() : players.getPlayer1();

        //create a new list for all the moves
        List<PlayerHelper.Move> moves = getMoves(player, opponent, player.hasCheck());

        //count the number of moves
        int count = 0;

        if (DEBUG)
            UtilityHelper.logEvent("Thinking... size: " + moves.size());

        //go through all of the moves
        for (PlayerHelper.Move move : moves) {

            //if we want to interrupt the game
            if (PlayerVars.STATUS == PlayerVars.Status.Interrupt)
                break;

            //update the ai progress
            count++;
            players.getActivity().updateProgress((int)(((float)count / (float)moves.size()) * 100));

            //execute the current move
            executeMove(move, players);

            //calculate the score
            final int tmpScore = -1 * negaMax(DEFAULT_DEPTH, players);

            //undo the previous move
            undoMove(move, players);

            //if we have a better score
            if (tmpScore > score) {

                //this is the new score to beat
                score = tmpScore;

                //this is now the best move
                bestMove = move;
            }
        }

        moves.clear();
        moves = null;

        players.getActivity().updateProgress(0);

        //return the best move
        return bestMove;
    }

    private int negaMax(int depth, Players players) {

        Player player = players.isPlayer1Turn() ? players.getPlayer1() : players.getPlayer2();
        Player opponent = players.isPlayer1Turn() ? players.getPlayer2() : players.getPlayer1();

        if (depth <= 0)
            return (player.calculateScore() - opponent.calculateScore());

        //get list of all valid moves based on the current state of the board
        List<PlayerHelper.Move> currentMoves = getMoves(player, opponent, player.hasCheck());

        //keep track of the best score
        int bestScore = Integer.MIN_VALUE;

        //check every valid move
        for (PlayerHelper.Move currentMove : currentMoves) {

            //if we want to interrupt the game
            if (PlayerVars.STATUS == PlayerVars.Status.Interrupt)
                break;

            //execute the move
            executeMove(currentMove, players);

            //calculate the score
            final int tmpScore = -1 * negaMax(depth - 1, players);

            //undo the move
            undoMove(currentMove, players);

            //if there is a new best score
            if (tmpScore > bestScore)
                bestScore = tmpScore;
        }

        currentMoves.clear();
        currentMoves = null;

        //return the best score we found
        return bestScore;
    }

    private void executeMove(PlayerHelper.Move move, Players players) {

        Player player = players.isPlayer1Turn() ? players.getPlayer1() : players.getPlayer2();
        Player opponent = players.isPlayer1Turn() ? players.getPlayer2() : players.getPlayer1();

        //get the player piece at the source location
        Piece piece = player.getPiece(move.sourceCol, move.sourceRow);


        //get the captured piece at the destination
        Piece captured = opponent.getPiece(move.destCol, move.destRow);

        //place at the destination (if it exists)
        if (piece != null) {

            piece.setCol(move.destCol);
            piece.setRow(move.destRow);
        }

        //flag the piece captured (if it exists)
        if (captured != null) {

            move.pieceCaptured = captured;
            move.pieceCaptured.setCaptured(true);
        }

        //since we executed a move switch turns
        players.setPlayer1Turn(!players.isPlayer1Turn());
    }

    private void undoMove(PlayerHelper.Move move, Players players) {

        //let's try to get the piece at the destination location for player 1
        Piece piece = players.getPlayer1().getPiece(move.destCol, move.destRow);
        boolean player1Turn = true;

        //if the piece doesn't exist it must be player 2's turn
        if (piece == null) {
            piece = players.getPlayer2().getPiece(move.destCol, move.destRow);
            player1Turn = false;
        }

        //move back to the source
        piece.setCol(move.sourceCol);
        piece.setRow(move.sourceRow);

        //flag the piece captured false (if it exists)
        if (move.pieceCaptured != null) {
            move.pieceCaptured.setCaptured(false);
            move.pieceCaptured.setCol(move.destCol);
            move.pieceCaptured.setRow(move.destRow);
        }

        //assign the correct turn
        players.setPlayer1Turn(player1Turn);
    }
}