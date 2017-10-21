package com.gamesbykevin.chess.activity;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.players.Player;
import com.gamesbykevin.chess.players.Players;

import org.rajawali3d.Object3D;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.util.ObjectColorPicker;
import org.rajawali3d.util.OnObjectPickedListener;

/**
 * Created by Kevin on 10/12/2017.
 */
public class BasicRenderer extends Renderer implements OnObjectPickedListener {

    private int fingers = 0;

    private TestArcballCamera camera;

    private Context context;

    private View view;

    private Object3D board;

    private Players players;

    //private RayPicker rayPicker;
    private ObjectColorPicker objectPicker;

    //did we render our board at least once?
    private boolean init = false;

    //are we rotating the board?
    private boolean rotate = false;

    public BasicRenderer(Context context, View view) {
        super(context);
        this.context = context;
        this.view = view;
        setFrameRate(60);
    }

    @Override
    public void initScene() {

        //flag false until our first render
        init = false;

        //create our object picker to select the pieces
        this.objectPicker = new ObjectColorPicker(this);
        this.objectPicker.setOnObjectPickedListener(this);

        //create light so we can see the pieces
        DirectionalLight light = new DirectionalLight(1f, 10f, 1f);
        light.setColor(1.0f, 1.0f, 1.0f);
        light.setPower(1);
        getCurrentScene().addLight(light);

        //add the board to the scene
        addBoard();

        try {

            //create the players and reset the pieces
            //this.players = new Players(Players.Mode.CpuVsCpu);
            this.players = new Players(Players.Mode.HumVsCpu);
            this.players.reset(this);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //create arc ball camera to rotate around the board
        setupCamera(board);
    }

    @Override
    public void onNoObjectPicked() {
        //nothing selected
        System.out.println("Nothing selected");
    }

    @Override
    public void onObjectPicked(Object3D object3D) {

        if (object3D == null)
            return;

        //select our chess piece
        if (getPlayers().getSelected() == null) {

            //set our selection
            getPlayers().select(this, object3D);

        } else {

            //place at object (if possible)
            getPlayers().place(this, object3D);
        }
    }

    @Override
    public void onTouchEvent(MotionEvent event){

        //if not started don't do anything
        if (!init)
            return;

        //if a chess piece is moving, we can't do anything
        if (getPlayers().isMoving())
            return;

        //get the current player
        Player player = players.isPlayer1Turn() ? getPlayers().getPlayer1() : getPlayers().getPlayer2();

        //if the player isn't human, we can't select anything right now
        if (!player.isHuman())
            return;

        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:

                //we can only select a piece if 1 finger is on the screen
                if (fingers == 1) {

                    //let's see if we selected a 3d model
                    getObjectPicker().getObjectAt(event.getRawX(), event.getY());
                }

                //keep track of how many fingers we have touching the screen
                fingers--;
                break;

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                //keep track of how many fingers we have touching the screen
                fingers++;
                break;

            case MotionEvent.ACTION_MOVE:
                break;
        }
    }

    public Players getPlayers() {
        return this.players;
    }

    public ObjectColorPicker getObjectPicker() {
        return this.objectPicker;
    }

    public void enableRotateCamera(boolean enabled) {

        this.rotate = enabled;

        if (this.rotate) {
            camera.addListeners();
            getCurrentScene().replaceAndSwitchCamera(getCurrentCamera(), camera);
        } else {
            camera.removeListeners();
            getCurrentScene().replaceAndSwitchCamera(getCurrentCamera(), camera);
        }
    }

    public void resetCamera() {
        setupCamera(board);
    }

    private void setupCamera(Object3D obj) {

        this.camera = new TestArcballCamera(context, view, obj);
        this.camera.setPosition(1.6, 1.6, 0);

        enableRotateCamera(rotate);
    }

    @Override
    public void onOffsetsChanged(float x, float y, float z, float w, int i, int j) {

    }

    @Override
    public void onRender(final long elapsedTime, final double deltaTime) {

        //update the players if needed
        getPlayers().update(this);

        //call parent to render objects
        super.onRender(elapsedTime, deltaTime);

        //flag that the board has been initialized
        init = true;
    }

    private void addBoard() {

        try {

            LoaderOBJ objParser = new LoaderOBJ(mContext.getResources(), mTextureManager, R.raw.board_obj);
            objParser.parse();

            board = objParser.getParsedObject();
            board.setScale(.2);
            getCurrentScene().addChild(board);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}