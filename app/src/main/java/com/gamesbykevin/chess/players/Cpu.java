package com.gamesbykevin.chess.players;

import com.gamesbykevin.chess.game.Game;
import com.gamesbykevin.chess.piece.Piece;
import com.gamesbykevin.chess.piece.PieceHelper;
import com.gamesbykevin.chess.util.UtilityHelper;

import java.util.ArrayList;
import java.util.List;

import static com.gamesbykevin.chess.players.PlayerHelper.getMoves;
import static com.gamesbykevin.chess.players.PlayerHelper.Move;
import static com.gamesbykevin.chess.util.UtilityHelper.DEBUG;
import static com.gamesbykevin.chess.util.UtilityHelper.getRandom;

/**
 * Created by Kevin on 10/17/2017.
 */
public class Cpu extends Player {

    //number of moves we look into the future
    private int DEFAULT_DEPTH = 2;

    //list of the best moves
    private List<PlayerHelper.Move> bestMoves;

    private Player player1, player2;
    private boolean player1Turn;

    //track how many moves were thought of
    private int total = 0, analyzed = 0;

    public Cpu(final Direction direction, final int depth) {
        super(false, direction);

        //assign the depth (moves thinking ahead)
        DEFAULT_DEPTH = depth;

        if (DEBUG)
            UtilityHelper.logEvent("Cpu depth = " + depth);

        //create new list of best moves
        this.bestMoves = new ArrayList<>();
    }

    @Override
    public void dispose() {
        super.dispose();

        if (this.bestMoves != null)
            this.bestMoves.clear();

        this.bestMoves = null;
        this.player1 = null;
        this.player2 = null;
    }

    @Override
    public void update(Game game) {

        //the best score
        int bestScore = Integer.MIN_VALUE;

        //reset back to 0
        this.total = 0;
        this.analyzed = 0;

        //clear list
        this.bestMoves.clear();

        //store for reference
        this.player1 = game.getPlayer1();
        this.player2 = game.getPlayer2();
        this.player1Turn = game.isPlayer1Turn();

        //get a new list for all the available moves
        List<PlayerHelper.Move> moves = getMoves(getPlayer(), getOpponent(), true, true);

        if (DEBUG)
            UtilityHelper.logEvent("Thinking... size: " + moves.size());

        //count the number of moves
        int count = 0;

        //keep track of total
        this.total += moves.size();
        this.analyzed += moves.size();

        //create a thread for each move
        for (Move move : moves) {

            //update the ai progress
            count++;
            game.getActivity().updateProgress((int)(((float)count / (float)moves.size()) * 100));

            //if game is interrupted don't continue right now
            if (PlayerVars.STATUS == PlayerVars.Status.Interrupt)
                return;

            //execute the current move
            executeMove(move);

            //calculate the score
            final int tmpScore = negaMax(DEFAULT_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE);

            //undo the previous move
            undoMove(move);

            //if we have a better score
            if (tmpScore >= bestScore) {

                //if best score, remove previous list
                if (tmpScore > bestScore)
                    bestMoves.clear();

                //this is the new score to beat
                bestScore = tmpScore;

                //add best move to the list
                bestMoves.add(move);
            }

            //make sure board piece positions are correct
            player1.correct();
            player2.correct();
        }

        if (DEBUG) {
            UtilityHelper.logEvent("Total moves: " + total);
            UtilityHelper.logEvent("Moves analyzed: " + analyzed);
        }

        final int index = getRandom().nextInt(bestMoves.size());
        Move bestMove = bestMoves.get(index);

        //setup the next move
        PlayerHelper.setupMove(game, bestMove);

        //reset progress back to 0
        game.getActivity().updateProgress(0);
    }

    private int getScore() {

        //get score for each player
        int score1 = getPlayer().calculateScore();
        int score2 = getOpponent().calculateScore();

        //determine how the player score is calculated
        if (getPlayer().getName().equalsIgnoreCase(getName())) {
            score2 = -score2;
        } else {
            score1 = -score1;
        }

        //return result
        return (score1 + score2);
    }

    private int negaMax(int depth, float alpha, float beta) {

        Player player = getPlayer();
        Player opponent = getOpponent();

        //if done searching moves, return score
        if (depth <= 0)
            return getScore();

        //get list of all valid moves based on the current state of the board
        List<PlayerHelper.Move> currentMoves = getMoves(player, opponent, false, true);

        //keep track of total
        this.total += currentMoves.size();

        //are we trying to maximize this player's score
        if (player.getName().equalsIgnoreCase(getName())) {

            //keep track of the best score
            int bestScore = Integer.MIN_VALUE;

            //check every valid move
            for (PlayerHelper.Move currentMove : currentMoves) {

                //keep track that we analyzed a move
                analyzed++;

                //if we want to interrupt the game
                if (PlayerVars.STATUS == PlayerVars.Status.Interrupt)
                    break;

                //execute the move
                executeMove(currentMove);

                //calculate the score
                bestScore = Math.max(bestScore, negaMax(depth - 1, alpha, beta));

                //undo the move
                undoMove(currentMove);

                //get the max value for the min variable
                alpha = Math.max(alpha, bestScore);

                //if the max is less than the min, return score
                if (beta <= alpha)
                    break;
            }

            currentMoves.clear();
            currentMoves = null;

            //return the best score
            return bestScore;

        } else {

            //keep track of the best score
            int bestScore = Integer.MAX_VALUE;

            //check every valid move
            for (PlayerHelper.Move currentMove : currentMoves) {

                //keep track that we analyzed a move
                analyzed++;

                //if we want to interrupt the game
                if (PlayerVars.STATUS == PlayerVars.Status.Interrupt)
                    break;

                //execute the move
                executeMove(currentMove);

                //calculate the score
                bestScore = Math.min(bestScore, negaMax(depth - 1, alpha, beta));

                //undo the move
                undoMove(currentMove);

                //get the min value for the max variable
                beta = Math.min(beta, bestScore);

                //if the max is less than the min, return score
                if (beta <= alpha)
                    break;
            }

            currentMoves.clear();
            currentMoves = null;

            //return the best score
            return bestScore;
        }
    }

    private void executeMove(PlayerHelper.Move move) {

        Player player = getPlayer();
        Player opponent = getOpponent();

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
        this.player1Turn = !this.player1Turn;
    }

    private void undoMove(PlayerHelper.Move move) {

        //let's try to get the piece at the destination location for player 1
        Piece piece = player1.getPiece(move.destCol, move.destRow);
        boolean player1Turn = true;

        //if the piece doesn't exist it must be player 2's turn
        if (piece == null) {
            piece = player2.getPiece(move.destCol, move.destRow);
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
        this.player1Turn = player1Turn;
    }

    private Player getPlayer() {
        return (player1Turn) ? player1 : player2;
    }

    private Player getOpponent() {
        return (player1Turn) ? player2 : player1;
    }
}