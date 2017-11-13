package com.gamesbykevin.chess.game;

import android.content.SharedPreferences;

import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.activity.GameActivity;
import com.gamesbykevin.chess.activity.GameActivity.Screen;
import com.gamesbykevin.chess.activity.PagerActivity;
import com.gamesbykevin.chess.piece.Piece;
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
import static com.gamesbykevin.chess.opengl.BasicRenderer.RENDER;
import static com.gamesbykevin.chess.players.PlayerVars.PLAYER_1_TURN;
import static com.gamesbykevin.chess.util.UtilityHelper.DEBUG;

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

    //what object has the user picked
    private Object3D picked;

    //maintain a list of moves
    private List<PlayerHelper.Move> history;

    //is this game a replay
    private boolean replay = false;

    /**
     * These are the different kinds of game modes
     */
    public enum Mode {
        HumVsCpu,
        HumVsCpuTimed,
        HumVsHumOffline,
        HumVsHumOnline,
        HumVsHumOnlineTimed
    }

    //store the game mode
    private final Mode mode;

    //the current selected piece
    private Piece selected;

    //player 1 starts first
    private boolean player1Turn = true;

    //list of promotional pieces when a pawn reaches the end
    private List<Piece> promotions;

    //current list in our replay
    public static int INDEX_REPLAY = 0;

    public Game(GameActivity activity) {

        //store activity reference
        this.activity = activity;

        //assign the correct mode
        this.mode = Mode.values()[PagerActivity.CURRENT_PAGE];

        //create list for move history
        this.history = new ArrayList<>();

        //player 1 always starts first
        setPlayer1Turn(true);

        //what is our difficulty setting
        final int depth = (activity.getIntValue(R.string.difficulty_file_key)) + 1;

        switch (getMode()) {

            case HumVsHumOffline:
            case HumVsHumOnline:
            case HumVsHumOnlineTimed:
                this.player1 = new Human(Player.Direction.North);
                this.player2 = new Human(Player.Direction.South);
                break;

            case HumVsCpu:
            case HumVsCpuTimed:
                this.player1 = new Human(Player.Direction.North);
                this.player2 = new Cpu(Player.Direction.South, depth);
                break;

            default:
                throw new RuntimeException("Mode not handled: " + getMode().toString());
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

            //get our history and convert back into java object
            List<Move> tmpHistory = GSON.fromJson(json, listType);

            if (tmpHistory != null) {
                INDEX_REPLAY = 0;
                setHistory(tmpHistory);
                setReplay(true);
            } else {
                setReplay(false);
            }
        }
    }

    public void setPicked(final Object3D picked) {
        this.picked = picked;
    }

    public Object3D getPicked() {
        return this.picked;
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
        getPlayer1().copy();
        getPlayer2().copy();
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

        //update list view
        getActivity().updateListView(move.toString());

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
            PlayerHelper.addPromotionPieces(this);

            //if we have promotion
            if (PlayerHelper.hasPromotion(this)) {

                //if human promoting, display promotion pieces
                if (getPlayer().isHuman())
                    PlayerHelper.displayPromotion(this);
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
        if (PlayerVars.isGameover()) {
            GameHelper.updateGameOver(this);
            return;
        }

        //if the open gl context has not yet rendered, don't continue
        if (!RENDER)
            return;

        //if still loading display the game
        if (getActivity().getScreen() == Screen.Loading) {
            getActivity().setScreen(Screen.Ready, true);
            return;
        }

        switch(PlayerVars.STATUS) {

            case Move:
                move();
                break;

            case Select:
            case Promote:

                //get the current player
                Player player = PLAYER_1_TURN ? getPlayer1() : getPlayer2();

                if (hasReplay()) {

                    if (INDEX_REPLAY < getHistory().size()) {

                        //update to our current move
                        getActivity().updateListView(INDEX_REPLAY);

                        //if we have replay get the current move
                        Move move = getHistory().get(INDEX_REPLAY);

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

        if (this.history != null)
            this.history.clear();

        this.player1 = null;
        this.player2 = null;
        this.textureWhite = null;
        this.textureWood = null;
        this.textureHighlight = null;
        this.textureValid = null;
        this.selection = null;
        this.selected = null;
        this.promotions = null;
        this.history = null;
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

    public void deselect() {

        //make sure we have a selected piece
        if (getSelected() != null) {

            //restore the normal texture
            getSelected().getObject3D().setMaterial(isPlayer1Turn() ? getTextureWhite() : getTextureWood());

            //we no longer have a selected piece
            this.selected = null;
        }
    }

    public void select(Object3D object3D) {

        //assign our picked object
        setPicked(object3D);
    }

    public Player getPlayer() {
        return PLAYER_1_TURN ? getPlayer1() : getPlayer2();
    }

    private void move() {
        GameHelper.move(this);
    }
}