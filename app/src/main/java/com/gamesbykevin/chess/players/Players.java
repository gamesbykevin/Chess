package com.gamesbykevin.chess.players;

import android.graphics.Color;
import android.widget.Toast;

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

    //are we moving a chess piece?
    private boolean moving = false;

    //are we promoting a chess piece?
    private boolean promoting = false;

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

    public void setPromoting(final boolean promoting) {
        this.promoting = promoting;
    }

    public boolean isPromoting() {
        return this.promoting;
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

    public void reset(BasicRenderer renderer) {

        //load the board selection visible / non-visible
        PlayerHelper.loadBoardSelection(this, renderer, getTextureValid());

        //player 1 always moves north
        PlayerHelper.reset(getPlayer1(), renderer, getTextureWhite());

        //player 2 always moves south
        PlayerHelper.reset(getPlayer2(), renderer, getTextureWood());

        //register all the chess pieces as click-able so we can select if needed for the humans
        for (int index = 0; index < getPlayer1().getPieceCount(); index++) {
            renderer.getObjectPicker().registerObject(getPlayer1().getPiece(index, true).getObject3D());
        }

        for (int index = 0; index < getPlayer2().getPieceCount(); index++) {
            renderer.getObjectPicker().registerObject(getPlayer2().getPiece(index, true).getObject3D());
        }

        //add each chess piece so we can do piece promotion
        PlayerHelper.addPromotionPieces(this, renderer);
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

    private void deselect() {

        //make sure we have a selected piece
        if (getSelected() != null) {

            //restore the normal texture
            getSelected().getObject3D().setMaterial(isPlayer1Turn() ? getTextureWhite() : getTextureWood());

            //we no longer have a selected piece
            this.selected = null;
        }
    }

    public void select(BasicRenderer renderer, Object3D object3D) {

        //if we previously have a selected piece don't interact with anything else
        if (getSelected() != null)
            return;

        //if the piece is still moving, don't continue
        if (isMoving())
            return;

        //is the game over
        if (isGameOver())
            return;

        //have we found a piece?
        boolean found = false;

        Player player = isPlayer1Turn() ? getPlayer1() : getPlayer2();
        Player opponent = isPlayer1Turn() ? getPlayer2() : getPlayer1();

        //if we haven't found a piece yet, and it is the current player's turn, and the player is human
        if (player.isHuman()) {

            //if we are promoting
            if (isPromoting()) {

                //check all the promotion pieces
                for (int index = 0; index < getPromotions().size(); index++) {

                    Piece piece = getPromotions().get(index);

                    //is this the piece we have selected?
                    if (piece.getObject3D().equals(object3D)) {
                        //promote the piece
                        PlayerHelper.promote(renderer, this, piece);

                        //switch turns
                        setPlayer1Turn(!isPlayer1Turn());
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
        }

        //if we selected a piece we need to calculate the moves
        if (found) {

            //get list of valid moves
            this.moves = getSelected().getMoves(player, opponent);

            //update list of available moves (in case player is in check)
            PlayerHelper.updateMoves(this.moves, this);

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

                    //this will remove the texture
                    if (1 == 0)
                        obj.setMaterial(null);

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

                //update if the player is in check
                opponent.setCheck(PlayerHelper.hasCheck(player, opponent));

                //check if the opponent is in check
                if (DEBUG && opponent.hasCheck())
                    UtilityHelper.logEvent(isPlayer1Turn() ? "Player 2 is in check" : "Player 1 is in check");

                //check if the game is over if already in  check
                if (opponent.hasCheck())
                    opponent.setCheckMate(PlayerHelper.hasCheckMate(this));

                if (DEBUG && opponent.hasCheckMate())
                    UtilityHelper.logEvent("Checkmate");

                //check if the game ends in a stalemate if we aren't in check
                if (!opponent.hasCheck())
                    opponent.setStalemate(PlayerHelper.hasStalemate(this));

                if (DEBUG && opponent.hasStalemate())
                    UtilityHelper.logEvent("Stalemate");

                //we are at our destination, de-select the chess piece
                deselect();

                //reset
                player.reset();

                //check if we have a promotion
                setPromoting(PlayerHelper.hasPromotion(this));

                //we are no longer moving
                setMoving(false);


                //if we aren't promoting a piece, switch turns
                if (!isPromoting()) {

                    setPlayer1Turn(!isPlayer1Turn());

                } else {

                    //display the correct texture
                    for (Piece piece : getPromotions()) {
                        piece.getObject3D().setMaterial(isPlayer1Turn() ? getTextureWhite() : getTextureWood());
                    }

                    //if not human, we will auto promote
                    if (!player.isHuman()) {

                        //promote the pawn piece
                        PlayerHelper.promote(renderer, this, null);

                        //flag promoting false
                        setPromoting(false);

                        //switch turns
                        setPlayer1Turn(!isPlayer1Turn());
                    }
                }
            }
        }
    }

    public boolean isGameOver() {
        return (getPlayer1().hasCheckMate() ||
                getPlayer2().hasCheckMate() ||
                getPlayer1().hasStalemate() ||
                getPlayer2().hasStalemate());
    }

    public void update(BasicRenderer renderer) {

        //don't do anything if the game is over
        if (isGameOver())
            return;

        if (isMoving()) {

            updatePiece(renderer);

        } else {

            //get the current player
            Player player = isPlayer1Turn() ? getPlayer1() : getPlayer2();

            //if the player is not human, the ai will update
            player.update(this);
        }
    }
}