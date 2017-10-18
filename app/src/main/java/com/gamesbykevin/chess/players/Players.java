package com.gamesbykevin.chess.players;

import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.activity.BasicRenderer;
import com.gamesbykevin.chess.piece.Piece;
import com.gamesbykevin.chess.util.UtilityHelper;

import org.rajawali3d.Object3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.renderer.Renderer;

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

        //player 1 always starts first
        this.player1Turn = true;

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

    public void reset(BasicRenderer renderer) {

        //player 1 always moves north
        PlayerHelper.reset(getPlayer1(), renderer, getTextureWhite());

        //player 2 always moves south
        PlayerHelper.reset(getPlayer2(), renderer, getTextureBlack());

        //register all the chess pieces as click-able so we can select if needed for the humans
        if (getPlayer1().isHuman()) {
            for (Piece piece : getPlayer1().getPieces()) {
                renderer.getObjectPicker().registerObject(piece.getObj());
            }
        }

        if (getPlayer2().isHuman()) {
            for (Piece piece : getPlayer2().getPieces()) {
                renderer.getObjectPicker().registerObject(piece.getObj());
            }
        }
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

    public Player getPlayer1() {
        return this.player1;
    }

    public Player getPlayer2() {
        return this.player2;
    }

    public Piece getSelected() {
        return this.selected;
    }

    private void deselect() {

        //make sure we have a selected piece
        if (getSelected() != null) {

            //restore the normal texture
            getSelected().getObj().setMaterial(getSelected().getMaterial());

            //we no longer have a selected piece
            this.selected = null;
        }
    }

    public void select(Object3D object3D) {

        //if we previously have a selected piece don't interact with anything else
        if (getSelected() != null)
            return;

        //have we found a piece?
        boolean found = false;

        //if we haven't found a piece yet, and it is the current player's turn, and the player is human
        if (!found && player1Turn && getPlayer1().isHuman()) {
            for (Piece piece : player1.getPieces()) {
                if (piece.getObj().equals(object3D)) {
                    selected = piece;
                    object3D.setMaterial(getTextureHighlight());
                    found = true;
                    break;
                }
            }
        }

        //if we haven't found a piece yet, and it is the current player's turn, and the player is human
        if (!found && !player1Turn && getPlayer2().isHuman()) {
            for (Piece piece : player2.getPieces()) {
                if (piece.getObj().equals(object3D)) {
                    selected = piece;
                    object3D.setMaterial(getTextureHighlight());
                    found = true;
                    break;
                }
            }
        }
    }

    /**
     * Place our selected piece
     */
    public void place() {

        //make sure we can place piece here
        getSelected().checkValid(player1Turn ? getPlayer1() : getPlayer2(), player1Turn ? getPlayer2() : getPlayer1());

        if (!getSelected().isValid())
            return;

        //determine which location we are closest to
        final int col = PlayerHelper.getCol(getSelected().getObj().getX());
        final int row = PlayerHelper.getRow(getSelected().getObj().getZ());

        //place piece in that position
        getSelected().getObj().setX(PlayerHelper.getCoordinate(col));
        getSelected().getObj().setZ(PlayerHelper.getCoordinate(row));

        //update the location
        getSelected().setCol(col);
        getSelected().setRow(row);

        //de select the piece
        deselect();

        //switch turns
        player1Turn = !player1Turn;

        if (player1Turn) {

        } else {

        }
    }

    public void move(BasicRenderer renderer, final float x, final float y) {

        //don't continue if we don't have a selected piece
        if (getSelected() == null)
            return;

        final double y1 = getSelected().getObj().getY();
        getSelected().getObj().setScreenCoordinates(x, y, renderer.getViewportWidth(), renderer.getViewportHeight(), renderer.getCurrentCamera().getZ());
        getSelected().getObj().setZ(getSelected().getObj().getY());
        getSelected().getObj().setY(y1);

        final int col = PlayerHelper.getCol(getSelected().getObj().getX());
        final int row = PlayerHelper.getRow(getSelected().getObj().getZ());

        //keep piece in bounds
        if (col == -1 || row == -1)
            PlayerHelper.correctPiece(getSelected());

        //don't continue if we are at the same position
        if (col == getSelected().getMoveCol() && row == getSelected().getMoveRow())
            return;

        //check if we are currently valid
        boolean validPrevious = getSelected().isValid();

        boolean valid = true;

        if (player1Turn) {
            valid = getSelected().checkValid(getPlayer1(), getPlayer2());
        } else {
            valid = getSelected().checkValid(getPlayer2(), getPlayer1());
        }

        //if our state hasn't changed no need to continue
        if ((valid && validPrevious) || (!valid && !validPrevious))
            return;

        getSelected().getObj().setMaterial((valid) ? getTextureHighlight() : getTextureInvalid());
    }
}