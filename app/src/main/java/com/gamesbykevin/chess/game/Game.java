package com.gamesbykevin.chess.game;

import android.content.SharedPreferences;

import com.gamesbykevin.androidframeworkv2.base.Cell;
import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.activity.GameActivity;
import com.gamesbykevin.chess.piece.Piece;
import com.gamesbykevin.chess.piece.PieceHelper;
import com.gamesbykevin.chess.players.Cpu;
import com.gamesbykevin.chess.players.Human;
import com.gamesbykevin.chess.piece.PieceHelper.Type;
import com.gamesbykevin.chess.players.Player;
import com.gamesbykevin.chess.players.PlayerHelper;
import com.gamesbykevin.chess.players.PlayerHelper.Move;
import com.gamesbykevin.chess.players.PlayerVars;
import com.gamesbykevin.chess.players.PlayerVars.Status;
import com.gamesbykevin.chess.players.PlayerVars.State;
import com.gamesbykevin.chess.util.UtilityHelper;
import com.google.gson.reflect.TypeToken;

import org.rajawali3d.Object3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.Texture;

import java.util.ArrayList;
import java.util.List;

import static com.gamesbykevin.androidframeworkv2.activity.BaseActivity.GSON;
import static com.gamesbykevin.chess.opengl.BasicRenderer.INIT;
import static com.gamesbykevin.chess.players.PlayerVars.PLAYER_1_TURN;
import static com.gamesbykevin.chess.players.PlayerVars.STATE;
import static com.gamesbykevin.chess.util.UtilityHelper.DEBUG;
import static com.gamesbykevin.chess.util.UtilityHelper.getRandom;

/**
 * Created by Kevin on 7/19/2017.
 */
public class Game implements IGame {

    //store activity reference
    private final GameActivity activity;

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

    //maintain a list of moves
    private List<PlayerHelper.Move> history;

    //is this game a replay
    private boolean replay = false;

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

    public Game(GameActivity activity) {

        //store activity reference
        this.activity = activity;

        //assign game mode
        this.mode = Mode.HumVsCpu;

        //create list of target destinations
        this.targets = new ArrayList<>();

        //create list for move history
        this.history = new ArrayList<>();

        //player 1 always starts first
        setPlayer1Turn(true);

        switch (getMode()) {

            case HumVsHum:
                this.player1 = new Human(Player.Direction.North);
                this.player2 = new Human(Player.Direction.South);
                break;

            case HumVsCpu:
                this.player1 = new Human(Player.Direction.North);
                this.player2 = new Cpu(Player.Direction.South, getRandom().nextInt(3) + 1);
                break;

            case CpuVsCpu:
                this.player1 = new Cpu(Player.Direction.North, getRandom().nextInt(3) + 1);
                this.player2 = new Cpu(Player.Direction.South, getRandom().nextInt(3) + 1);
                break;
        }

        //give the object a name
        this.player1.setName("Player 1");
        this.player2.setName("Player 2");

        if (player1.hasDirection(Player.Direction.North) && player2.hasDirection(Player.Direction.North))
            throw new RuntimeException("Each player has to face a different direction");
        if (player1.hasDirection(Player.Direction.South) && player2.hasDirection(Player.Direction.South))
            throw new RuntimeException("Each player has to face a different direction");

        //we are playing with player 1 going first
        PlayerVars.PLAYER_1_TURN = true;
        PlayerVars.STATE = State.Playing;
        PlayerVars.STATUS = Status.Select;

        //get the shared preferences
        SharedPreferences preferences = getActivity().getSharedPreferences();

        if (preferences.contains(getActivity().getString(R.string.saved_match_file_key))) {
            final String json = preferences.getString(getActivity().getString(R.string.saved_match_file_key), "");
            java.lang.reflect.Type listType = new TypeToken<List<Move>>(){}.getType();

            List<Move> tmpHistory = (List<Move>)GSON.fromJson(json, listType);

            if (tmpHistory != null) {
                setHistory(tmpHistory);
                setReplay(true);
            } else {
                setReplay(false);
            }
        }
    }

    public void setReplay(final boolean replay) {
        this.replay = replay;
    }

    public boolean hasReplay() {
        return this.replay;
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

    public void switchTurns() {
        setPlayer1Turn(!isPlayer1Turn());
        PLAYER_1_TURN = !PLAYER_1_TURN;
        PlayerVars.STATUS = Status.Select;
    }

    public void track(Piece piece) {

        if (hasReplay())
            return;

        //create the move
        Move move = new Move();
        move.sourceCol = piece.getSourceCol();
        move.sourceRow = piece.getSourceRow();
        move.destCol = (int)piece.getCol();
        move.destRow = (int)piece.getRow();

        //add to our history
        this.history.add(move);

        if (DEBUG)
            UtilityHelper.logEvent("History saved (" + move.sourceCol +  "," + move.sourceRow + ") (" + move.destCol + "," + move.destRow + ")");
    }

    public void track(Type type) {

        if (hasReplay())
            return;

        //get the last move in our history
        Move move = this.history.get(this.history.size() - 1);

        //save the promotion piece type
        move.promotion = type;

        if (DEBUG)
            UtilityHelper.logEvent("Promotion saved (" + move.sourceCol +  "," + move.sourceRow + ") (" + move.destCol + "," + move.destRow + ") " + move.promotion.toString());
    }

    public List<Move> getHistory() {
        return this.history;
    }

    public void setHistory(List<Move> history) {
        this.history = history;
    }

    @Override
    public void onPause() {

        //interrupt our game
        PlayerVars.STATUS = Status.Interrupt;
    }

    @Override
    public void onResume() {

        //don't interrupt our game
        PlayerVars.STATUS = Status.Select;
    }

    @Override
    public void reset() {

        try {

            //remove selection (if exists)
            setSelected(null);

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

            //if we have promotion
            if (PlayerHelper.hasPromotion(this)) {

                //if human promoting, display promotion pieces
                if (getPlayer().isHuman())
                    PlayerHelper.displayPromotion(this, getActivity().getSurfaceView().getRenderer());
            }


        } catch (Exception e) {
            UtilityHelper.handleException(e);
        }

        //track game started in analytics
        //AnalyticsHelper.trackPuzzleStarted(getActivity(), this);
    }

    @Override
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

                if (hasReplay()) {

                    if (!getHistory().isEmpty()) {

                        //if we have replay get the current move
                        Move move = getHistory().get(0);

                        //setup the move
                        PlayerHelper.setupMove(this, move);
                    }

                } else {

                    //if the player is not human, the ai will update
                    player.update(this);
                }
                break;
        }
    }

    /**
     * Recycle objects
     */
    @Override
    public void dispose() {

        if (this.player1 != null)
            this.player1.dispose();
        if (this.player2 != null)
            this.player2.dispose();
        if (this.textureWhite != null)
            this.textureWhite.unbindTextures();
        if (this.textureWood != null)
            this.textureWood.unbindTextures();
        if (this.textureHighlight != null)
            this.textureHighlight.unbindTextures();
        if (this.textureValid != null)
            this.textureValid.unbindTextures();
        if (this.selection != null)
            this.selection.destroy();
        if (this.targets != null) {

            for (int i = 0; i < this.targets.size(); i++) {
                if (this.targets.get(i) != null) {
                    this.targets.get(i).destroy();
                    this.targets.set(i, null);
                }
            }

            this.targets.clear();
        }
        if (this.selected != null)
            this.selected.dispose();
        if (this.promotions != null) {
            for (int i = 0; i < promotions.size(); i++) {
                if (this.promotions.get(i) != null)
                    this.promotions.get(i).dispose();

                this.promotions.set(i, null);
            }

            this.promotions.clear();
        }

        if (this.moves != null)
            this.moves.clear();
        if (this.history != null)
            this.history.clear();

        this.player1 = null;
        this.player2 = null;
        this.textureWhite = null;
        this.textureWood = null;
        this.textureHighlight = null;
        this.textureValid = null;
        this.selection = null;
        this.targets = null;
        this.moves = null;
        this.selected = null;
        this.promotions = null;
        this.history = null;
    }

    @Override
    public boolean onTouchEvent(final int action, float x, float y) {
        //return true to keep receiving events
        return true;
    }

    @Override
    public void render(float[] m) {

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

    public void setSelected(final Piece selected) {
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

        if (hasReplay())
            return;

        //if we previously have a selected piece don't interact with anything else
        if (getSelected() != null)
            return;

        //is the game over
        if (PlayerVars.isGameover())
            return;

        switch (PlayerVars.STATUS) {
            case Select:
            case Promote:
                break;

            //if not selecting or promoting, we can't continue
            default:
                return;
        }

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

                    //save the promotion piece selected
                    track(piece.getType());

                    //un-select the piece
                    deselect();

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

            //make sure moves exist for the chess piece
            if (this.moves.isEmpty()) {

                //if no moves, de-select the piece
                deselect();

                //go back to selecting
                PlayerVars.STATUS = PlayerVars.Status.Select;

            } else {

                //clear the list if any exist
                this.targets.clear();

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

        if (hasReplay())
            return;

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

            //make sure we are making a valid move
            if (this.moves.get(i).hasLocation(col, row)) {

                //setup the move
                PlayerHelper.setupMove(this, (int)getSelected().getCol(), (int)getSelected().getRow(), col, row);

                //remove all targets from the scene
                for (int x = 0; x < targets.size(); x++) {
                    getActivity().getSurfaceView().getRenderer().getObjectPicker().unregisterObject(targets.get(x));
                    getActivity().getSurfaceView().getRenderer().getCurrentScene().removeChild(targets.get(x));
                }

                //clear list
                targets.clear();
            }
        }
    }

    public Player getPlayer() {
        return PLAYER_1_TURN ? getPlayer1() : getPlayer2();
    }

    private void move() {

        //if we never selected a piece, we can't move
        if (getSelected() == null)
            return;

        Player player = PLAYER_1_TURN ? getPlayer1() : getPlayer2();
        Player opponent = PLAYER_1_TURN ? getPlayer2() : getPlayer1();

        //if not at destination, move it
        if (!player.hasDestination()) {

            //move all necessary pieces
            player.move();

            //don't continue yet
            return;
        }

        //check if we selected an opponent piece to capture it
        for (int i = 0; i < opponent.getPieceCount(); i++) {

            //get the current chess piece
            Piece piece = opponent.getPiece(i, false);

            if (piece == null)
                continue;

            //if the opponents piece is at the same location, we can capture
            if ((int) piece.getCol() == (int) getSelected().getCol() &&
                    (int) piece.getRow() == (int) getSelected().getRow()) {

                //remove from object picker
                getActivity().getSurfaceView().getRenderer().getObjectPicker().unregisterObject(piece.getObject3D());

                //remove from scene
                getActivity().getSurfaceView().getRenderer().getCurrentScene().removeChild(piece.getObject3D());

                //flag opponent piece captured
                piece.setCaptured(true);

                //no need to check any other pieces
                break;

            } else {

                //skip if the piece isn't a pawn and we can't capture "en passant"
                if (piece.getType() != PieceHelper.Type.Pawn)
                    continue;
                if (!piece.hasPassant())
                    continue;

                //both pieces have to be pawns
                if (getSelected().getType() != PieceHelper.Type.Pawn)
                    continue;

                //if the columns don't match, we can't capture
                if (piece.getCol() != getSelected().getCol())
                    continue;

                //only check if directly above chess piece
                if (player.hasDirection(Player.Direction.North) && piece.getRow() - 1 != getSelected().getRow())
                    continue;
                if (player.hasDirection(Player.Direction.South) && piece.getRow() + 1 != getSelected().getRow())
                    continue;

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

        //we only have 1 move to capture the opponent's pawn via "en passant"
        opponent.removePassant();

        //track the move
        track(getSelected());

        //update the state of the game (check, checkmate, stalemate)
        PlayerHelper.updateStatus(opponent, player);

        //check for a draw (stalemate)
        GameHelper.checkDraw(this);

        if (DEBUG) {

            switch (STATE) {

                case WinPlayer2:
                    getActivity().displayMessage("Player 1 Checkmate");
                    break;

                case WinPlayer1:
                    getActivity().displayMessage("Player 2 Checkmate");
                    break;

                case Stalemate:
                    getActivity().displayMessage("Stalemate");
                    break;

                default:

                    if (opponent.hasCheck() && !hasReplay()) {
                        getActivity().displayMessage(isPlayer1Turn() ? "Player 2 is in check" : "Player 1 is in check");
                    } else if (!opponent.isHuman()) {
                        //getActivity().displayMessage("Thinking...");
                    }
                    break;
            }
        }

        //we are at our destination, de-select the chess piece
        deselect();

        //check if we have a promotion
        if (PlayerHelper.hasPromotion(this)) {

            PlayerHelper.displayPromotion(this, getActivity().getSurfaceView().getRenderer());

        } else {

            //if we aren't promoting a piece, switch turns
            switchTurns();
        }

        if (hasReplay())
            this.history.remove(0);

        //copy the pieces in case the game is interrupted
        getPlayer1().copy();
        getPlayer2().copy();
    }
}