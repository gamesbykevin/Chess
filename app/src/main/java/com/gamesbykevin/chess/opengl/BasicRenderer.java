package com.gamesbykevin.chess.opengl;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.opengl.CustomArcballCamera;
import com.gamesbykevin.chess.players.Player;
import com.gamesbykevin.chess.players.Players;
import com.gamesbykevin.chess.util.UtilityHelper;

import org.rajawali3d.Object3D;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.util.ObjectColorPicker;
import org.rajawali3d.util.OnObjectPickedListener;

import static com.gamesbykevin.chess.activity.GameActivity.getGame;
import static com.gamesbykevin.chess.util.UtilityHelper.DEBUG;

/**
 * Created by Kevin on 10/12/2017.
 */
public class BasicRenderer extends Renderer implements OnObjectPickedListener {

    private CustomArcballCamera camera;

    private Context context;

    private View view;

    private Object3D board;

    private ObjectColorPicker objectPicker;

    //did we render our board at least once?
    public static boolean INIT = false;

    public BasicRenderer(Context context, View view) {
        super(context);
        this.context = context;
        this.view = view;
        setFrameRate(60);
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

    @Override
    public void initScene() {

        //flag false until our first render
        INIT = false;

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

        if (getGame() != null && getGame().getPlayers() != null)
            getGame().getPlayers().createMaterials();

        //create arc ball camera to rotate around the board
        resetCamera();
    }

    @Override
    public void onNoObjectPicked() {

        //nothing selected
        if (DEBUG)
            UtilityHelper.logEvent("Nothing selected");
    }

    @Override
    public void onObjectPicked(Object3D object3D) {

        if (object3D == null)
            return;

        if (getGame() == null)
            return;

        //select our chess piece
        if (getGame().getPlayers().getSelected() == null) {

            //set our selection
            getGame().getPlayers().select(this, object3D);

        } else {

            //place at object (if possible)
            getGame().getPlayers().place(this, object3D);
        }
    }

    @Override
    public void onTouchEvent(MotionEvent event){

        //if not started don't do anything
        if (!INIT)
            return;

        if (getGame() == null || getGame().getPlayers() == null)
            return;

        //if a chess piece is moving, we can't do anything
        if (getGame().getPlayers().isMoving())
            return;

        //get the current player
        Player player = getGame().getPlayers().isPlayer1Turn() ? getGame().getPlayers().getPlayer1() : getGame().getPlayers().getPlayer2();

        //if the player isn't human, we can't select anything right now
        if (!player.isHuman())
            return;

        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:

                //let's see if we selected a 3d model
                getObjectPicker().getObjectAt(event.getRawX(), event.getY());
                break;

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                break;

            case MotionEvent.ACTION_MOVE:
                break;
        }
    }

    public ObjectColorPicker getObjectPicker() {
        return this.objectPicker;
    }

    public void resetCamera() {

        if (camera == null) {
            this.camera = new CustomArcballCamera(this, context, view, board);
            this.camera.setPosition(1.75, 1.75, 0);
            getCurrentScene().replaceAndSwitchCamera(getCurrentCamera(), this.camera);
        } else {
            //initialize the camera again
            ((CustomArcballCamera)getCurrentCamera()).initialize();
            ((CustomArcballCamera)getCurrentCamera()).setPosition(1.75, 1.75, 0);
        }

        //this.camera.;
        //white on left
        //this.camera.setPosition(1.75, 1.75, 0);

        //black on left
        //this.camera.setPosition(-1.75, 1.75, 0);

        //this.camera.setPosition(-1.75 / 2, 1.75, 0);
        /*
        camera.getFarPlane();
        camera.getFieldOfView();
        camera.getGraphNode();
        camera.getLookAt();
        camera.getNearPlane();
        camera.getOrientation();
        camera.getProjectionMatrix();
        cam.setFarPlane(mFarPlane);
        cam.setFieldOfView(mFieldOfView);
        cam.setGraphNode(mGraphNode, mInsideGraph);
        cam.setLookAt(mLookAt.clone());
        cam.setNearPlane(mNearPlane);
        cam.setOrientation(mOrientation.clone());
        cam.setPosition(mPosition.clone());
        cam.setProjectionMatrix(mLastWidth, mLastHeight);
        */
    }

    @Override
    public void onOffsetsChanged(float x, float y, float z, float w, int i, int j) {

    }

    @Override
    public void onRender(final long elapsedTime, final double deltaTime) {

        if (getGame().getPlayers() == null)
            getGame().reset();

        //call parent to render objects
        super.onRender(elapsedTime, deltaTime);

        //flag that the board has been initialized
        INIT = true;
    }
}