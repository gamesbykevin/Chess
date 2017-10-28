package com.gamesbykevin.chess.players;

import com.gamesbykevin.androidframeworkv2.base.Cell;
import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.activity.GameActivity;
import com.gamesbykevin.chess.opengl.BasicRenderer;
import com.gamesbykevin.chess.piece.Piece;
import com.gamesbykevin.chess.util.UtilityHelper;

import org.rajawali3d.Object3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.Texture;

import java.util.ArrayList;
import java.util.List;

import static com.gamesbykevin.chess.opengl.BasicRenderer.INIT;
import static com.gamesbykevin.chess.players.PlayerVars.PLAYER_1_TURN;
import static com.gamesbykevin.chess.util.UtilityHelper.DEBUG;

/**
 * Created by Kevin on 10/17/2017.
 */
public class Players {

    //our players
    private Player player1;
    private Player player2;

    //our texture materials for the pieces
    private Material textureWhite;
    private Material textureWood;
    private Material textureHighlight;
    private Material textureValid;

    //our highlighted selection to place for all valid moves
    private Object3D selection;

    //list of targets to choose from
    private List<Object3D> targets;

    //list of valid moves
    private List<Cell> moves;

    /**
     * These are the different kinds of game modes
     */
    public enum Mode {
        HumVsHum,
        HumVsCpu,
        CpuVsCpu
    }

    //store the game mode
    private final Mode mode;

    //the current selected piece
    private Piece selected;

    //player 1 starts first
    private boolean player1Turn = true;

    //list of promotional pieces when a pawn reaches the end
    private List<Piece> promotions;

    private final GameActivity activity;

    public Players(final GameActivity activity, final Mode mode) {

        //store activity reference
        this.activity = activity;

        //assign the mode of game play
        this.mode = mode;

        //create list of target destinations
        this.targets = new ArrayList<>();

        //player 1 always starts first
        setPlayer1Turn(true);

        switch (getMode()) {

            case HumVsHum:
                this.player1 = new Human(Player.Direction.North);
                this.player2 = new Human(Player.Direction.South);
                break;

            case HumVsCpu:
                this.player1 = new Human(Player.Direction.North);
                this.player2 = new Cpu(Player.Direction.South);
                break;

            case CpuVsCpu:
                this.player1 = new Cpu(Player.Direction.North);
                this.player2 = new Cpu(Player.Direction.South);
                break;
        }

        if (player1.hasDirection(Player.Direction.North) && player2.hasDirection(Player.Direction.North))
            throw new RuntimeException("Each player has to face a different direction");
        if (player1.hasDirection(Player.Direction.South) && player2.hasDirection(Player.Direction.South))
            throw new RuntimeException("Each player has to face a different direction");

        PlayerVars.PLAYER_1_TURN = true;
    }

    public GameActivity getActivity() {
        return this.activity;
    }

    public void setPromotions(List<Piece> promotions) {
        this.promotions = promotions;
    }

    public List<Piece> getPromotions() {
        return this.promotions;
    }

    public boolean isPlayer1Turn() {
        return this.player1Turn;
    }

    public void setPlayer1Turn(final boolean player1Turn) {
        this.player1Turn = player1Turn;
    }

    protected void switchTurns() {
        setPlayer1Turn(!isPlayer1Turn());
        PLAYER_1_TURN = !PLAYER_1_TURN;
        PlayerVars.STATUS = PlayerVars.Status.Select;
    }

    public void reset() {

        //create our textures for the pieces
        createMaterials();

        //load the board selection visible / non-visible
        PlayerHelper.loadBoardSelection(this, getActivity().getSurfaceView().getRenderer(), getTextureValid());

        //if the player already has pieces, we just need to re-load the models
        if (getPlayer1().getPieceCount() != 0) {

            //restore chess pieces (if necessary)
            getPlayer1().restore();
            getPlayer2().restore();

            //just reload the models
            PlayerHelper.loadModels(getPlayer1(), getActivity().getSurfaceView().getRenderer(), getTextureWhite());
            PlayerHelper.loadModels(getPlayer2(), getActivity().getSurfaceView().getRenderer(), getTextureWood());

        } else {

            //create the pieces and reload the models
            PlayerHelper.reset(getPlayer1(), getActivity().getSurfaceView().getRenderer(), getTextureWhite());
            PlayerHelper.reset(getPlayer2(), getActivity().getSurfaceView().getRenderer(), getTextureWood());

            getPlayer1().copy();
            getPlayer2().copy();
        }

        //register all the chess pieces as click-able so we can select if needed for the humans
        for (int index = 0; index < getPlayer1().getPieceCount(); index++) {

            //we need to be able to select the piece if it isn't captured
            if (!getPlayer1().getPiece(index, true).isCaptured())
                getActivity().getSurfaceView().getRenderer().getObjectPicker().registerObject(getPlayer1().getPiece(index, true).getObject3D());
        }

        for (int index = 0; index < getPlayer2().getPieceCount(); index++) {

            //we need to be able to select the piece if it isn't captured
            if (!getPlayer2().getPiece(index, true).isCaptured())
                getActivity().getSurfaceView().getRenderer().getObjectPicker().registerObject(getPlayer2().getPiece(index, true).getObject3D());
        }

        //add each chess piece so we can do piece promotion
        PlayerHelper.addPromotionPieces(this, getActivity().getSurfaceView().getRenderer());
    }

    public void setBoardSelection(final Object3D selection) {
        this.selection = selection;
    }

    public Object3D getBoardSelection() {
        return this.selection;
    }

    public void createMaterials() {

        try {

            this.textureWhite = new Material();
            this.textureWhite.enableLighting(true);
            this.textureWhite.setDiffuseMethod(new DiffuseMethod.Lambert());
            this.textureWhite.setColorInfluence(0);
            this.textureWhite.addTexture(new Texture("textureWhite", R.drawable.white));

            this.textureWood = new Material();
            this.textureWood.enableLighting(true);
            this.textureWood.setDiffuseMethod(new DiffuseMethod.Lambert());
            this.textureWood.setColorInfluence(0);
            this.textureWood.addTexture(new Texture("textureWood", R.drawable.wood));

            this.textureHighlight = new Material();
            this.textureHighlight.enableLighting(true);
            this.textureHighlight.setDiffuseMethod(new DiffuseMethod.Lambert());
            this.textureHighlight.setColorInfluence(0);
            this.textureHighlight.addTexture(new Texture("textureHighlighted", R.drawable.highlighted));

            this.textureValid = new Material();
            this.textureValid.enableLighting(true);
            this.textureValid.setDiffuseMethod(new DiffuseMethod.Lambert());
            this.textureValid.setColorInfluence(0);
            this.textureValid.addTexture(new Texture("textureValid", R.drawable.valid));

        } catch (Exception e) {
            UtilityHelper.handleException(e);
        }
    }

    public Mode getMode() {
        return this.mode;
    }

    public Material getTextureWhite() {
        return this.textureWhite;
    }

    public Material getTextureWood() {
        return this.textureWood;
    }

    public Material getTextureHighlight() {
        return this.textureHighlight;
    }

    public Material getTextureValid() {
        return this.textureValid;
    }

    public Player getPlayer1() {
        return this.player1;
    }

    public Player getPlayer2() {
        return this.player2;
    }

    public Piece getSelected() {
        return this.selected;
    }

    protected void setSelected(final Piece selected) {
        this.selected = selected;
    }

    protected void deselect() {

        //make sure we have a selected piece
        if (getSelected() != null) {

            //restore the normal texture
            getSelected().getObject3D().setMaterial(isPlayer1Turn() ? getTextureWhite() : getTextureWood());

            //we no longer have a selected piece
            this.selected = null;
        }
    }

    public void select(Object3D object3D) {

        //if we previously have a selected piece don't interact with anything else
        if (getSelected() != null)
            return;

        //is the game over
        if (PlayerVars.isGameover())
            return;

        //if we aren't selecting or promoting
        if (PlayerVars.STATUS != PlayerVars.Status.Select && PlayerVars.STATUS != PlayerVars.Status.Promote)
            return;

        Player player = PLAYER_1_TURN ? getPlayer1() : getPlayer2();
        Player opponent = PLAYER_1_TURN ? getPlayer2() : getPlayer1();

        //if the current player isn't human
        if (!player.isHuman())
            return;

        //have we found a piece?
        boolean found = false;

        //if we are promoting a pawn
        if (PlayerVars.STATUS == PlayerVars.Status.Promote) {

            //check all the promotion pieces
            for (int index = 0; index < getPromotions().size(); index++) {

                Piece piece = getPromotions().get(index);

                //is this the piece we have selected?
                if (piece.getObject3D().equals(object3D)) {

                    //promote the piece
                    PlayerHelper.promote(getActivity().getSurfaceView().getRenderer(), this, piece);

                    //switch turns
                    switchTurns();
                }
            }

            //don't continue
            return;
        }

        //check all the pieces
        for (int index = 0; index < player.getPieceCount(); index++) {

            Piece piece = player.getPiece(index, false);

            if (piece == null)
                continue;

            //if we select the piece or the floor the piece is on, this is our selected piece
            if (piece.getObject3D().equals(object3D)) {
                selected = piece;
                object3D.setMaterial(getTextureHighlight());
                found = true;
                break;
            }
        }

        //if we selected a piece we need to calculate the moves
        if (found) {

            //get list of valid moves that don't put us in check
            this.moves = getSelected().getMoves(player, opponent, true);

            //make sure we aren't going to move the player in check
            getSelected().checkForCheck(moves, player, opponent);

            //make sure moves exist for the chess piece
            if (this.moves.isEmpty()) {

                //if no moves, de-select the piece
                deselect();

                //go back to selecting
                PlayerVars.STATUS = PlayerVars.Status.Select;

            } else {

                //add a target for every possible move
                for (int i = 0; i < this.moves.size(); i++) {

                    Object3D obj = getBoardSelection().clone();

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
                    getActivity().getSurfaceView().getRenderer().getCurrentScene().addChild(obj);

                    //register the object to be chosen
                    getActivity().getSurfaceView().getRenderer().getObjectPicker().registerObject(obj);
                }
            }
        }
    }

    /**
     * Place our selected piece
     */
    public void place(Object3D object3D) {

        //is the game over
        if (PlayerVars.isGameover())
            return;

        //if not placing, don't continue
        if (PlayerVars.STATUS != PlayerVars.Status.Select)
            return;

        //get the location where this object is at
        final int col = PlayerHelper.getCol(object3D.getX());
        final int row = PlayerHelper.getRow(object3D.getZ());

        //check if we selected a valid move
        for (int i = 0; i < this.moves.size(); i++) {

            //get the possible move
            Cell cell = this.moves.get(i);

            //make sure we are making a valid move
            if (col == (int)cell.getCol() && row == (int)cell.getRow())
                movePiece(object3D);
        }
    }

    private void movePiece(Object3D object3D) {

        //update the location of the selection on the chess board
        getSelected().setCol(PlayerHelper.getCol(object3D.getX()));
        getSelected().setRow(PlayerHelper.getRow(object3D.getZ()));

        //assign our destination
        getSelected().setDestinationCoordinates((float)object3D.getX(), (float)object3D.getZ());

        //flag the piece as moved
        getSelected().setMoved(true);

        //remove all targets from the scene
        for (int x = 0; x < targets.size(); x++) {
            getActivity().getSurfaceView().getRenderer().getObjectPicker().unregisterObject(targets.get(x));
            getActivity().getSurfaceView().getRenderer().getCurrentScene().removeChild(targets.get(x));
        }

        //clear list
        targets.clear();

        //flag that we are moving a piece
        PlayerVars.STATUS = PlayerVars.Status.Move;
    }

    private void move() {

        if (getSelected() == null)
            return;

        if (!getSelected().hasDestination()) {

            //move
            getSelected().move();

            //don't continue yet
            return;
        }

        //check if we captured an opponent piece during this move
        Player player = PLAYER_1_TURN ? getPlayer1() : getPlayer2();
        Player opponent = PLAYER_1_TURN ? getPlayer2() : getPlayer1();

        //check if we selected an opponent piece to capture it
        for (int i = 0; i < opponent.getPieceCount(); i++) {

            //get the current chess piece
            Piece piece = opponent.getPiece(i, false);

            if (piece == null)
                continue;

            //if the opponents piece is at the same location, we can capture
            if ((int)piece.getCol() == (int)getSelected().getCol() &&
                (int)piece.getRow() == (int)getSelected().getRow()) {

                //remove from object picker
                getActivity().getSurfaceView().getRenderer().getObjectPicker().unregisterObject(piece.getObject3D());

                //remove from scene
                getActivity().getSurfaceView().getRenderer().getCurrentScene().removeChild(piece.getObject3D());

                //flag opponent piece captured
                piece.setCaptured(true);

                //no need to check any other pieces
                break;
            }
        }

        //update the state of the game (check, checkmate, stalemate)
        PlayerHelper.updateStatus(opponent, player);

        //check if the opponent is in check
        if (DEBUG && opponent.hasCheck())
            getActivity().displayMessage(isPlayer1Turn() ? "Player 2 is in check" : "Player 1 is in check");

        if (DEBUG && (PlayerVars.STATE == PlayerVars.State.LosePlayer1 || PlayerVars.STATE == PlayerVars.State.LosePlayer2))
            getActivity().displayMessage("Checkmate");

        if (DEBUG && PlayerVars.STATE == PlayerVars.State.Stalemate)
            getActivity().displayMessage("Stalemate");

        //we are at our destination, de-select the chess piece
        deselect();

        //check if we have a promotion
        if (PlayerHelper.hasPromotion(this))
            PlayerVars.STATUS = PlayerVars.Status.Promote;

        //if we aren't promoting a piece, switch turns
        if (PlayerVars.STATUS != PlayerVars.Status.Promote) {

            //switch turns
            switchTurns();

        } else {

            //display the correct texture
            for (Piece piece : getPromotions()) {
                piece.getObject3D().setMaterial(isPlayer1Turn() ? getTextureWhite() : getTextureWood());
            }

            //if not human, we will auto promote
            if (!player.isHuman()) {

                //promote the pawn piece
                PlayerHelper.promote(getActivity().getSurfaceView().getRenderer(), this, null);

                //switch turns
                switchTurns();
            }
        }

        //copy the pieces in case the game is interrupted
        getPlayer1().copy();
        getPlayer2().copy();
    }


    public void update() {

        //don't do anything if the game is over
        if (PlayerVars.isGameover())
            return;

        //if the open gl context has not yet rendered, don't continue
        if (!INIT)
            return;

        switch(PlayerVars.STATUS) {

            case Move:
                move();
                break;

            case Select:

                //get the current player
                Player player = PLAYER_1_TURN ? getPlayer1() : getPlayer2();

                //if the player is not human, the ai will update
                player.update(this);
                break;
        }

    }
}