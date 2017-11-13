package com.gamesbykevin.chess.players;

import com.gamesbykevin.androidframeworkv2.base.Cell;
import com.gamesbykevin.chess.game.Game;
import com.gamesbykevin.chess.game.GameHelper;
import com.gamesbykevin.chess.piece.Piece;
import com.gamesbykevin.chess.piece.PieceHelper;

import org.rajawali3d.Object3D;
import org.rajawali3d.math.vector.Vector3;

import java.util.ArrayList;
import java.util.List;

import static com.gamesbykevin.chess.players.PlayerVars.PLAYER_1_TURN;

/**
 * Created by Kevin on 10/15/2017.
 */
public class Human extends Player {

    //list of targets to choose from
    private List<Object3D> targets;

    //list of valid moves
    private List<Cell> moves;

    public Human(final Direction direction) {
        super(true, direction);

        //create list of target destinations
        this.targets = new ArrayList<>();
    }

    @Override
    public void dispose() {
        super.dispose();

        if (this.targets != null) {

            for (int i = 0; i < this.targets.size(); i++) {
                if (this.targets.get(i) != null) {
                    this.targets.get(i).destroy();
                    this.targets.set(i, null);
                }
            }

            this.targets.clear();
        }

        if (this.moves != null)
            this.moves.clear();

        this.targets = null;
        this.moves = null;
    }

    @Override
    public void update(Game game) {

        //is the game over
        if (PlayerVars.isGameover())
            return;

        //if replay mode, don't continue
        if (game.hasReplay())
            return;

        //update the game timer
        game.getActivity().getTimer().update(game.getActivity());

        //if we didn't pick anything, don't continue
        if (game.getPicked() == null)
            return;

        //only continue if we are selecting or promoting
        switch (PlayerVars.STATUS) {
            case Select:
            case Promote:
                break;

            //if not selecting or promoting, we can't continue
            default:
                return;
        }

        //the other player is our opponent
        Player opponent = PLAYER_1_TURN ? game.getPlayer2() : game.getPlayer1();

        //if we are promoting a pawn
        if (PlayerVars.STATUS == PlayerVars.Status.Promote) {

            //check all the promotion pieces
            for (int index = 0; index < game.getPromotions().size(); index++) {

                Piece piece = game.getPromotions().get(index);

                //is this the piece we have selected?
                if (piece.getObject3D().equals(game.getPicked())) {

                    //promote the piece
                    PlayerHelper.promote(game, piece);

                    //save the promotion piece selected
                    game.track(piece.getType());

                    //un-select the piece
                    game.deselect();

                    //update the state of the game (check, checkmate, stalemate)
                    PlayerHelper.updateStatus(opponent, this);

                    //display the game status
                    GameHelper.displayStatus(game, opponent);

                    //switch turns
                    game.switchTurns();
                }
            }

            //don't continue
            return;
        }

        //if we haven't selected a piece yet
        if (game.getSelected() == null) {

            //did we select a piece?
            boolean found = false;

            //check all the pieces
            for (int index = 0; index < getPieceCount(); index++) {

                Piece piece = getPiece(index, false);

                if (piece == null)
                    continue;

                //if we select the piece or the floor the piece is on, this is our selected piece
                if (piece.getObject3D().equals(game.getPicked())) {
                    game.setSelected(piece);
                    game.getPicked().setMaterial(game.getTextureHighlight());
                    found = true;
                    break;
                }
            }

            //if we selected a piece we need to calculate the moves
            if (found) {

                //get list of valid moves that don't put us in check
                this.moves = game.getSelected().getMoves(this, opponent, true);

                //make sure moves exist for the chess piece
                if (this.moves.isEmpty()) {

                    //if no moves, de-select the piece
                    game.deselect();

                    //go back to selecting
                    PlayerVars.STATUS = PlayerVars.Status.Select;

                } else {

                    //clear the list if any exist
                    this.targets.clear();

                    //add a target for every possible move
                    for (int i = 0; i < this.moves.size(); i++) {

                        Object3D obj = game.getBoardSelection().clone();

                        //place at the correct location
                        obj.setX(PlayerHelper.getCoordinate((int) moves.get(i).getCol()));
                        obj.setY(PlayerHelper.Y + .001);
                        obj.setZ(PlayerHelper.getCoordinate((int) moves.get(i).getRow()));

                        //this will remove the texture
                        if (1 == 0)
                            obj.setMaterial(null);

                        //add to targets list
                        this.targets.add(obj);

                        //add child to scene
                        game.getActivity().getSurfaceView().getRenderer().getCurrentScene().addChild(obj);

                        //register the object to be chosen
                        game.getActivity().getSurfaceView().getRenderer().getObjectPicker().registerObject(obj);
                    }
                }
            }

        } else {

            //we have a selected piece and are trying to place

            //get the location where this object is at
            final int col = PlayerHelper.getCol(game.getPicked().getX());
            final int row = PlayerHelper.getRow(game.getPicked().getZ());

            //check if we selected a valid move
            for (int i = 0; i < this.moves.size(); i++) {

                //make sure we are making a valid move
                if (this.moves.get(i).hasLocation(col, row)) {

                    //setup the move
                    PlayerHelper.setupMove(game, (int)game.getSelected().getCol(), (int)game.getSelected().getRow(), col, row);

                    //remove all targets from the scene
                    for (int x = 0; x < targets.size(); x++) {
                        game.getActivity().getSurfaceView().getRenderer().getObjectPicker().unregisterObject(targets.get(x));
                        game.getActivity().getSurfaceView().getRenderer().getCurrentScene().removeChild(targets.get(x));
                    }

                    //clear list
                    targets.clear();
                }
            }
        }

        //remove the picked object
        game.setPicked(null);
    }
}