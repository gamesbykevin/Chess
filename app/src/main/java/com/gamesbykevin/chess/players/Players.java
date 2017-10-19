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

    //where we want to move our chess piece to
    private Object3D target;

    //list of targets to choose from
    private List<Object3D> targets;

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
        for (Piece piece : getPlayer1().getPieces()) {
            renderer.getObjectPicker().registerObject(piece.getObj());
        }

        for (Piece piece : getPlayer2().getPieces()) {
            renderer.getObjectPicker().registerObject(piece.getObj());
        }

        //load the valid texture
        PlayerHelper.loadSelected(this, renderer, getTextureValid());
    }

    public void setSelection(final Object3D selection) {
        this.selection = selection;
    }

    public Object3D getSelection() {
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

    private void deselect() {

        //make sure we have a selected piece
        if (getSelected() != null) {

            //restore the normal texture
            getSelected().getObj().setMaterial((player1Turn) ? getTextureWhite() : getTextureBlack());

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

        //if we selected a piece we need to calculate the moves
        if (found) {

            //get list of valid moves
            List<Cell> moves = getSelected().getMoves(player1Turn ? getPlayer1() : getPlayer2(), player1Turn ? getPlayer2() : getPlayer1());

            //make sure moves exist for the chess piece
            if (moves.isEmpty()) {

                //if no moves, de-select the piece
                deselect();

            } else {

                //add a target for every possible move
                for (int i = 0; i < moves.size(); i++) {

                    Object3D obj = getSelection().clone();

                    //place at the correct location
                    obj.setX(PlayerHelper.getCoordinate((int) moves.get(i).getCol()));
                    obj.setY(PlayerHelper.Y + .01);
                    obj.setZ(PlayerHelper.getCoordinate((int) moves.get(i).getRow()));

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

        //check if we selected a target
        for (int i = 0; i < targets.size(); i++) {

            //get the current 3d object
            Object3D tmp = targets.get(i);

            //if the object matches we know where to go
            if (tmp.equals(object3D)) {

                //move the piece accordingly
                movePiece(renderer, object3D);

                //exit the loop
                break;
            }
        }

        //get the player based on the current turn
        Player opponent = (player1Turn) ? getPlayer2() : getPlayer1();

        //check if we selected an opponent piece
        for (int i = 0; i < opponent.getPieces().size(); i++) {

            Piece piece = opponent.getPieces().get(i);

            if (piece.getObj().equals(object3D)) {

                //also make sure piece is part of the targets
                if (piece.hasTarget(targets)) {

                    //capture the piece
                    capture(renderer, opponent, object3D);

                    //move the piece
                    movePiece(renderer, object3D);

                    //exit the loop
                    break;

                }
            }
        }
    }

    private void capture(BasicRenderer renderer, Player player, Object3D object3D) {

        //remove from object picker
        renderer.getObjectPicker().unregisterObject(object3D);

        //remove from scene
        renderer.getCurrentScene().removeChild(object3D);

        //remove from the players pieces
        for (int i = 0; i < player.getPieces().size(); i++) {

            if (player.getPieces().get(i).getObj().equals(object3D)) {
                player.getPieces().remove(i);
                break;
            }
        }
    }

    private void movePiece(BasicRenderer renderer, Object3D object3D) {

        //place the selected piece at the position
        getSelected().getObj().setPosition(object3D.getPosition());

        //update the location of the selection
        getSelected().setCol(PlayerHelper.getCol(object3D.getX()));
        getSelected().setRow(PlayerHelper.getRow(object3D.getZ()));

        //flag the piece as moved
        getSelected().setMoved(true);

        //de-select the piece
        deselect();

        //remove all targets from the scene
        for (int x = 0; x < targets.size(); x++) {
            renderer.getObjectPicker().unregisterObject(targets.get(x));
            renderer.getCurrentScene().removeChild(targets.get(x));
        }

        //clear list
        targets.clear();

        //switch turns
        player1Turn = !player1Turn;
    }
}