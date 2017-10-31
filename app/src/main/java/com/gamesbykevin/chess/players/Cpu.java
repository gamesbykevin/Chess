package com.gamesbykevin.chess.players;

import com.gamesbykevin.chess.game.Game;
import com.gamesbykevin.chess.piece.Piece;
import com.gamesbykevin.chess.util.UtilityHelper;

import java.util.ArrayList;
import java.util.List;

import static com.gamesbykevin.chess.activity.GameActivity.getGame;
import static com.gamesbykevin.chess.players.PlayerHelper.getMoves;
import static com.gamesbykevin.chess.util.UtilityHelper.DEBUG;

/**
 * Created by Kevin on 10/17/2017.
 */
public class Cpu extends Player {

    //number of moves we look into the future
    public static final int DEFAULT_DEPTH = 2;

    //list of the best moves
    private List<PlayerHelper.Move> bestMoves;

    public Cpu(final Direction direction) {
        super(false, direction);

        //create new list for the best moves
        this.bestMoves = new ArrayList<>();
    }

    @Override
    public void dispose() {
        super.dispose();

        if (bestMoves != null)
            bestMoves.clear();

        bestMoves = null;
    }

    @Override
    public void update(Game game) {

        //get the best move
        PlayerHelper.Move bestMove = getBestMove(game);

        //if game is interrupted don't continue right now
        if (PlayerVars.STATUS == PlayerVars.Status.Interrupt)
            return;

        //setup the next move
        PlayerHelper.setupMove(game, bestMove);
    }

    private PlayerHelper.Move getBestMove(Game game) {

        //remove list if previous moves exist
        bestMoves.clear();

        //keep track of the score for the best move
        int score = Integer.MIN_VALUE;

        Player player = game.isPlayer1Turn() ? game.getPlayer1() : game.getPlayer2();
        Player opponent = game.isPlayer1Turn() ? game.getPlayer2() : game.getPlayer1();

        //create a new list for all the moves
        List<PlayerHelper.Move> moves = getMoves(player, opponent, true);//player.hasCheck() || DEFAULT_DEPTH < 2);

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
            game.getActivity().updateProgress((int)(((float)count / (float)moves.size()) * 100));

            //execute the current move
            executeMove(move, game);

            //calculate the score
            final int tmpScore = -1 * negaMax(DEFAULT_DEPTH, game);

            //undo the previous move
            undoMove(move, game);

            //if we have a better score
            if (tmpScore >= score) {

                //if best score, remove previous list
                if (tmpScore > score)
                    bestMoves.clear();

                //this is the new score to beat
                score = tmpScore;

                //add best move to the list
                bestMoves.add(move);
            }
        }

        moves.clear();
        moves = null;

        game.getActivity().updateProgress(0);

        //pick a random index
        final int index = UtilityHelper.getRandom().nextInt(bestMoves.size());

        //pick our best move
        PlayerHelper.Move bestMove = bestMoves.get(index);

        //return the best move
        return bestMove;
    }

    private int negaMax(int depth, Game game) {

        Player player = game.isPlayer1Turn() ? game.getPlayer1() : game.getPlayer2();
        Player opponent = game.isPlayer1Turn() ? game.getPlayer2() : game.getPlayer1();

        if (depth <= 0)
            return (player.calculateScore() - opponent.calculateScore());

        //get list of all valid moves based on the current state of the board
        List<PlayerHelper.Move> currentMoves = getMoves(player, opponent, player.hasCheck() && DEFAULT_DEPTH < 2);

        //keep track of the best score
        int bestScore = Integer.MIN_VALUE;

        //check every valid move
        for (PlayerHelper.Move currentMove : currentMoves) {

            //if we want to interrupt the game
            if (PlayerVars.STATUS == PlayerVars.Status.Interrupt)
                break;

            //execute the move
            executeMove(currentMove, game);

            //calculate the score
            final int tmpScore = -1 * negaMax(depth - 1, game);

            //undo the move
            undoMove(currentMove, game);

            //if there is a new best score
            if (tmpScore > bestScore)
                bestScore = tmpScore;
        }

        currentMoves.clear();
        currentMoves = null;

        //return the best score we found
        return bestScore;
    }

    private void executeMove(PlayerHelper.Move move, Game game) {

        Player player = game.isPlayer1Turn() ? game.getPlayer1() : game.getPlayer2();
        Player opponent = game.isPlayer1Turn() ? game.getPlayer2() : game.getPlayer1();

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
        game.setPlayer1Turn(!game.isPlayer1Turn());
    }

    private void undoMove(PlayerHelper.Move move, Game game) {

        //let's try to get the piece at the destination location for player 1
        Piece piece = game.getPlayer1().getPiece(move.destCol, move.destRow);
        boolean player1Turn = true;

        //if the piece doesn't exist it must be player 2's turn
        if (piece == null) {
            piece = game.getPlayer2().getPiece(move.destCol, move.destRow);
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
        game.setPlayer1Turn(player1Turn);
    }
}