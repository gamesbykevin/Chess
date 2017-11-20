package com.gamesbykevin.chess.opengl;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.players.Player;
import com.gamesbykevin.chess.players.PlayerVars;
import com.gamesbykevin.chess.util.UtilityHelper;

import org.rajawali3d.Object3D;
import org.rajawali3d.cameras.Camera;
import org.rajawali3d.cameras.Camera2D;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.primitives.Plane;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.util.ObjectColorPicker;
import org.rajawali3d.util.OnObjectPickedListener;

import javax.microedition.khronos.opengles.GL10;

import static com.gamesbykevin.chess.activity.GameActivity.getGame;
import static com.gamesbykevin.chess.util.UtilityHelper.DEBUG;

/**
 * Created by Kevin on 10/12/2017.
 */
public class BasicRenderer extends Renderer implements OnObjectPickedListener {

    //arc ball camera to move around our target board
    private CustomArcballCamera camera;

    //our 2d camera
    private Camera2D camera2D;

    //store the context
    private Context context;

    //our view reference
    private View view;

    //the object representing the board
    private Object3D board;

    //used to detect if we select a 3d model
    private ObjectColorPicker objectPicker;

    //did we render our board at least once?
    public static boolean RENDER = false;

    //has the 3d models etc... been initialized
    private static boolean INIT = false;

    //are we making changes
    private boolean analyzing = false;

    //background
    private Plane plane;

    //texture to apply to 2d background
    private Material material;

    //different camera angles
    private int cameraAngle = 0;

    public BasicRenderer(Context context, View view) {

        super(context);
        super.setFrameRate(60);

        this.context = context;
        this.view = view;

        //create our camera
        this.camera2D = new Camera2D();
        this.camera2D.setZ(0);
        this.camera2D.setLookAt(0, 0, 0);
    }

    @Override
    public void initScene() {

        //flag false until our first render
        RENDER = false;

        //flag false until init is complete
        INIT = false;

        //flag analyze false
        PlayerVars.STATUS = PlayerVars.Status.Select;

        //reset camera angle
        this.cameraAngle = 0;

        //create our object picker to select the pieces
        this.objectPicker = new ObjectColorPicker(this);
        this.objectPicker.setOnObjectPickedListener(this);

        //add light to the scene
        addLight();

        //add the 2d background
        addBackground();

        //add the board to the scene
        addBoard();

        //reset our game (if exists)
        if (getGame() != null)
            getGame().reset();

        //create arc ball camera to rotate around the board
        resetCamera();

        //we completed initialization
        INIT = true;
    }

    private void addLight() {

        //create light so we can see the pieces
        DirectionalLight light = new DirectionalLight(1f, 10f, 1f);
        light.setColor(1.0f, 1.0f, 1.0f);
        light.setPower(1);
        getCurrentScene().addLight(light);
    }

    private void addBoard() {

        try {

            //remove if already existing
            if (board != null)
                getCurrentScene().removeChild(board);

            LoaderOBJ objParser = new LoaderOBJ(mContext.getResources(), mTextureManager, R.raw.board_obj);
            objParser.parse();

            board = objParser.getParsedObject();
            board.setScale(.2);
            getCurrentScene().addChild(board);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addBackground() {

        try {

            material = new Material();
            material.setColorInfluence(0f);
            material.addTexture(new Texture("game_background", R.drawable.game_background));

            plane = new Plane(2,2,1,1,1);
            plane.setDoubleSided(true);
            plane.setMaterial(material);
            plane.setZ(1);

        } catch (Exception e) {
            UtilityHelper.handleException(e);
        }
    }

    @Override
    public void onNoObjectPicked() {

        //nothing selected
        if (DEBUG)
            UtilityHelper.logEvent("Nothing selected");
    }

    @Override
    public void onObjectPicked(Object3D object3D) {

        //if not started don't do anything
        if (!RENDER || !INIT)
            return;

        if (analyzing)
            return;

        if (object3D == null)
            return;

        if (getGame() == null)
            return;

        switch (PlayerVars.STATUS) {

            case Select:
            case Promote:
                break;

            default:
                return;
        }

        if (DEBUG)
            UtilityHelper.logEvent("OnObjectPicked");

        //flag true
        analyzing = true;

        //set our selection
        getGame().select(object3D);

        //flag false
        analyzing = false;
    }

    @Override
    public void onTouchEvent(MotionEvent event){

        //if analyzing action
        if (analyzing)
            return;

        //if not started don't do anything
        if (!RENDER || !INIT)
            return;

        if (getGame() == null)
            return;

        if (PlayerVars.STATUS != PlayerVars.Status.Select && PlayerVars.STATUS != PlayerVars.Status.Promote)
            return;

        //if the current player isn't human or remotely away online, we can't select anything
        if (!getGame().getPlayer().isHuman() || getGame().getPlayer().isOnline())
            return;

        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:

                if (DEBUG)
                    UtilityHelper.logEvent("OnTouchEvent ACTION_UP");

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

    public void changeCamera() {

        //change the camera angle
        cameraAngle++;

        //keep it within range
        if (cameraAngle > 3)
            cameraAngle = 0;

        //update the camera angle
        resetCamera();
    }

    public void resetCamera() {

        if (this.camera == null) {

            //create our camera
            this.camera = new CustomArcballCamera(this, context, view, board);

            //update the camera angle
            updateCameraAngle(this.camera);

            //update the camera
            getCurrentScene().replaceAndSwitchCamera(getCurrentCamera(), this.camera);

        } else {

            //re-initialize the camera again
            ((CustomArcballCamera) getCurrentCamera()).initialize();

            //update the camera angle
            updateCameraAngle(getCurrentCamera());
        }
    }

    private void updateCameraAngle(Camera tmpCamera) {

        //alter based on the camera angle
        switch(cameraAngle) {

            case 0:
                tmpCamera.setPosition(0, 2, 1.25);
                tmpCamera.setRotZ(60);
                break;

            case 1:
                tmpCamera.setPosition(0,2,-1.25);
                tmpCamera.setRotZ(60);
                ((CustomArcballCamera)tmpCamera).initialize();
                break;

            //white on left
            case 2:
                tmpCamera.setPosition(1.75, 1.75, 0);
                break;

            //black on left
            case 3:
                tmpCamera.setPosition(-1.75, 1.75, 0);
                break;
        }
    }

    @Override
    public void onOffsetsChanged(float x, float y, float z, float w, int i, int j) {
        //do something here
    }

    @Override
    public void onRenderFrame(GL10 unused) {
        super.onRenderFrame(unused);
    }

    @Override
    public void onRenderSurfaceSizeChanged(GL10 gl, int width, int height) {
        super.onRenderSurfaceSizeChanged(gl, width, height);
    }


    @Override
    public void onRender(final long elapsedTime, final double deltaTime) {

        if (!INIT)
            return;

        if (getGame() == null)
            return;

        if (getGame().getPlayer() == null)
            getGame().reset();

        //call parent to render objects
        super.onRender(elapsedTime, deltaTime);

        //render background
        plane.render(camera2D, camera2D.getModelMatrix(), camera2D.getProjectionMatrix(), camera2D.getViewMatrix(), material);

        //flag that the board has been initialized
        RENDER = true;
    }
}