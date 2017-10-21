package com.gamesbykevin.chess.players;

import com.gamesbykevin.androidframeworkv2.base.Cell;
import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.activity.BasicRenderer;
import com.gamesbykevin.chess.piece.Piece;
import com.gamesbykevin.chess.util.UtilityHelper;

import org.rajawali3d.Object3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.Texture;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin on 10/17/2017.
 */
public class Players {

    //our players
    private Player player1;
    private Player player2;

    //our texture materials for the pieces
    private Material textureWhite;
    private Material textureBlack;
    private Material textureHighlight;
    private Material textureInvalid;
    private Material textureValid;

    //our highlighted selection to place for all valid moves
    private Object3D selection;

    //list of targets to choose from
    private List<Object3D> targets;

    //list of valid moves
    private List<Cell> moves;

    //are we moving a chess piece?
    public boolean moving = false;

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

    public Players(final Mode mode) {

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

        //create our textures for the pieces
        createMaterials();
    }

    public boolean isPlayer1Turn() {
        return this.player1Turn;
    }

    public void setPlayer1Turn(final boolean player1Turn) {
        this.player1Turn = player1Turn;
    }

    public void reset(BasicRenderer renderer) {

        //load the valid texture and save
        PlayerHelper.loadSelected(this, renderer, getTextureValid());

        //player 1 always moves north
        PlayerHelper.reset(getPlayer1(), renderer, getTextureWhite());

        //player 2 always moves south
        PlayerHelper.reset(getPlayer2(), renderer, getTextureBlack());

        //register all the chess pieces as click-able so we can select if needed for the humans
        for (int index = 0; index < getPlayer1().getPieceCount(); index++) {
            renderer.getObjectPicker().registerObject(getPlayer1().getPiece(index, true).getObject3D());
        }

        for (int index = 0; index < getPlayer2().getPieceCount(); index++) {
            renderer.getObjectPicker().registerObject(getPlayer2().getPiece(index, true).getObject3D());
        }
    }

    public void setMoving(final boolean moving) {
        this.moving = moving;
    }

    public boolean isMoving() {
        return this.moving;
    }

    public void setBoardSelection(final Object3D selection) {
        this.selection = selection;
    }

    public Object3D getBoardSelection() {
        return this.selection;
    }

    private void createMaterials() {

        try {

            this.textureWhite = new Material();
            this.textureWhite.enableLighting(true);
            this.textureWhite.setDiffuseMethod(new DiffuseMethod.Lambert());
            this.textureWhite.setColorInfluence(0);
            this.textureWhite.addTexture(new Texture("textureWhite", R.drawable.white));

            this.textureBlack = new Material();
            this.textureBlack.enableLighting(true);
            this.textureBlack.setDiffuseMethod(new DiffuseMethod.Lambert());
            this.textureBlack.setColorInfluence(0);
            this.textureBlack.addTexture(new Texture("textureBlack", R.drawable.black));

            this.textureHighlight = new Material();
            this.textureHighlight.enableLighting(true);
            this.textureHighlight.setDiffuseMethod(new DiffuseMethod.Lambert());
            this.textureHighlight.setColorInfluence(0);
            this.textureHighlight.addTexture(new Texture("textureHighlighted", R.drawable.highlighted));

            this.textureInvalid = new Material();
            this.textureInvalid.enableLighting(true);
            this.textureInvalid.setDiffuseMethod(new DiffuseMethod.Lambert());
            this.textureInvalid.setColorInfluence(0);
            this.textureInvalid.addTexture(new Texture("textureInvalid", R.drawable.invalid));

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

    public Material getTextureBlack() {
        return this.textureBlack;
    }

    public Material getTextureHighlight() {
        return this.textureHighlight;
    }

    public Material getTextureInvalid() {
        return this.textureInvalid;
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

    private void deselect() {

        //make sure we have a selected piece
        if (getSelected() != null) {

            //restore the normal texture
            getSelected().getObject3D().setMaterial(isPlayer1Turn() ? getTextureWhite() : getTextureBlack());

            //we no longer have a selected piece
            this.selected = null;
        }
    }

    public void select(BasicRenderer renderer, Object3D object3D) {

        //if we previously have a selected piece don't interact with anything else
        if (getSelected() != null)
            return;

        //have we found a piece?
        boolean found = false;

        Player player = isPlayer1Turn() ? getPlayer1() : getPlayer2();
        Player opponent = isPlayer1Turn() ? getPlayer2() : getPlayer1();

        //if we haven't found a piece yet, and it is the current player's turn, and the player is human
        if (player.isHuman()) {

            //check all the pieces
            for (int index = 0; index < player.getPieceCount(); index++) {

                Piece piece = player.getPiece(index, false);

                if (piece != null && piece.getObject3D().equals(object3D)) {
                    selected = piece;
                    object3D.setMaterial(getTextureHighlight());
                    found = true;
                    break;
                }
            }
        }

        //if we selected a piece we need to calculate the moves
        if (found) {

            //get list of valid moves
            this.moves = getSelected().getMoves(player, opponent);

            //make sure moves exist for the chess piece
            if (this.moves.isEmpty()) {

                //if no moves, de-select the piece
                deselect();

            } else {

                //add a target for every possible move
                for (int i = 0; i < this.moves.size(); i++) {

                    Object3D obj = getBoardSelection().clone();

                    //place at the correct location
                    obj.setX(PlayerHelper.getCoordinate((int) moves.get(i).getCol()));
                    obj.setY(PlayerHelper.Y + .001);
                    obj.setZ(PlayerHelper.getCoordinate((int) moves.get(i).getRow()));

                    obj.setAlpha(0);

                    //obj.setVisible(false);

                    //add to targets list
                    this.targets.add(obj);

                    //add child to scene
                    renderer.getCurrentScene().addChild(obj);

                    //register the object to be chosen
                    renderer.getObjectPicker().registerObject(obj);
                }
            }
        }
    }

    /**
     * Place our selected piece
     */
    public void place(BasicRenderer renderer, Object3D object3D) {

        //get the location where this object is at
        final int col = PlayerHelper.getCol(object3D.getX());
        final int row = PlayerHelper.getRow(object3D.getZ());

        //check if we selected a valid move
        for (int i = 0; i < this.moves.size(); i++) {

            //get the possible move
            Cell cell = this.moves.get(i);

            //make sure we are making a valid move
            if (col == (int)cell.getCol() && row == (int)cell.getRow())
                movePiece(renderer, object3D);
        }
    }

    private void movePiece(BasicRenderer renderer, Object3D object3D) {

        //update the location of the selection on the chess board
        getSelected().setCol(PlayerHelper.getCol(object3D.getX()));
        getSelected().setRow(PlayerHelper.getRow(object3D.getZ()));

        //assign our destination
        getSelected().setDestinationCoordinates((float)object3D.getX(), (float)object3D.getZ());

        //flag the piece as moved
        getSelected().setMoved(true);

        //remove all targets from the scene
        for (int x = 0; x < targets.size(); x++) {
            renderer.getObjectPicker().unregisterObject(targets.get(x));
            renderer.getCurrentScene().removeChild(targets.get(x));
        }

        //clear list
        targets.clear();

        //flag that we are moving a piece
        setMoving(true);
    }

    public void updatePiece(BasicRenderer renderer) {

        if (getSelected() != null && !getSelected().hasDestination()) {

            getSelected().move();

            //if we have the destination, can we capture an opponent piece
            if (getSelected().hasDestination()) {

                //check if we captured an opponent piece during this move
                Player player = isPlayer1Turn() ? getPlayer1() : getPlayer2();
                Player opponent = isPlayer1Turn() ? getPlayer2() : getPlayer1();

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
                        renderer.getObjectPicker().unregisterObject(piece.getObject3D());

                        //remove from scene
                        renderer.getCurrentScene().removeChild(piece.getObject3D());

                        //flag opponent piece captured
                        piece.setCaptured(true);

                        //no need to check any other pieces
                        break;
                    }
                }

                //we are no longer moving
                setMoving(false);

                //we are at our destination, de-select the chess piece
                deselect();

                //reset
                player.reset();

                //switch turns
                setPlayer1Turn(!isPlayer1Turn());
            }
        }
    }

    public void update(BasicRenderer renderer) {

        if (isMoving()) {

            updatePiece(renderer);

        } else {

            //get the current player
            Player player = isPlayer1Turn() ? getPlayer1() : getPlayer2();
            Player opponent = isPlayer1Turn() ? getPlayer2() : getPlayer1();

            //if the player is not human, the ai will update
            if (!player.isHuman()) {
                player.update(this);
            }
        }
    }
}