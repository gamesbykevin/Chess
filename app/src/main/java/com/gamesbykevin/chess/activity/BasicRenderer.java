package com.gamesbykevin.chess.activity;

import android.content.Context;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;

import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.piece.Piece;
import com.gamesbykevin.chess.players.Human;
import com.gamesbykevin.chess.players.Player;
import com.gamesbykevin.chess.players.PlayerHelper;

import org.rajawali3d.Object3D;
import org.rajawali3d.cameras.ArcballCamera;
import org.rajawali3d.cameras.Camera;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.util.ObjectColorPicker;
import org.rajawali3d.util.OnObjectPickedListener;

/**
 * Created by Kevin on 10/12/2017.
 */
public class BasicRenderer extends Renderer implements OnObjectPickedListener {

    private static final float COORD_Y = -.4f;
    private static final float COORD_Z = .0f;

    private int fingers = 0;

    private PointF previous;

    private TestArcballCamera camera;

    private Camera cameraOld;

    private float angle = 0f;

    private Context context;

    private View view;

    private DirectionalLight mDirectionalLight;

    private Object3D board;

    private Material materialWhite, materialBlack;

    private Player player1, player2;

    //private RayPicker rayPicker;
    private ObjectColorPicker rayPicker;

    private boolean init = false;

    public BasicRenderer(Context context, View view) {
        super(context);
        this.context = context;
        this.view = view;
        setFrameRate(60);
    }

    @Override
    public void initScene() {

        init = false;

        this.rayPicker = new ObjectColorPicker(this);
        this.rayPicker.setOnObjectPickedListener(this);

        mDirectionalLight = new DirectionalLight(1f, 15f, 1f);
        mDirectionalLight.setColor(1.0f, 1.0f, 1.0f);
        mDirectionalLight.setPower(1);
        getCurrentScene().addLight(mDirectionalLight);

        try {

            materialWhite = new Material();
            materialWhite.enableLighting(true);
            materialWhite.setDiffuseMethod(new DiffuseMethod.Lambert());
            materialWhite.setColorInfluence(0);
            materialWhite.addTexture(new Texture("chessPieceWhite", R.drawable.white));

            materialBlack = new Material();
            materialBlack.enableLighting(true);
            materialBlack.setDiffuseMethod(new DiffuseMethod.Lambert());
            materialBlack.setColorInfluence(0);
            materialBlack.addTexture(new Texture("chessPieceBlack", R.drawable.black));

        } catch (Exception e) {
            e.printStackTrace();
        }

        addBoard();

        //create the player
        player1 = new Human();
        player2 = new Human();

        try {

            //reset the pieces on the board
            PlayerHelper.reset(player2, this, materialBlack, false);
            PlayerHelper.reset(player1, this, materialWhite, true);

        } catch (Exception e) {
            e.printStackTrace();
        }

        for (Piece piece : player1.getPieces()) {
            this.rayPicker.registerObject(piece.getObj());
        }

        for (Piece piece : player2.getPieces()) {
            this.rayPicker.registerObject(piece.getObj());
        }

        //board.rotate(Vector3.Axis.X, -45);

        cameraOld = getCurrentCamera();

        //create arc ball camera
        assignCamera(board);

        //this.camera.rotate(Vector3.Axis.X, 15);
        //this.camera.rotateAround(new Vector3(0, 0, 0), 45);

        init = true;
    }

    @Override
    public void onTouchEvent(MotionEvent event){

        if (!init)
            return;

        final float x = event.getX(0);
        final float y = event.getY(0);

        rayPicker.getObjectAt(event.getRawX(), event.getRawY());

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                fingers--;
                //getCurrentCamera().setPosition(0,0,3); //optional
                break;

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                fingers++;

                if (previous == null)
                    previous = new PointF();

                previous.x = x;
                previous.y = y;
                break;

            case MotionEvent.ACTION_MOVE:

                final float xd = previous.x - x;
                final float yd = previous.y - y;

                Vector3 s = getCurrentCamera().getPosition();

                final float touchTurn = xd / 200f;
                final float touchTurnUp = yd / 200f;

                if (fingers == 1) {

                    s.x += (touchTurn * 2);

                    if (touchTurnUp != 0)
                        s.z += (touchTurnUp * 2);

                    getCurrentCamera().setPosition(s);

                } else if (fingers == 2) {

                }

                //final double distance = Math.sqrt(Math.pow(previous.x - x, 2) - Math.pow(previous.y - y, 2));

                previous.x = x;
                previous.y = y;
                break;
        }
    }

    public void resetCamera() {
        Vector3 position = getCurrentCamera().getPosition();

        cameraOld.setPosition(position);

        //switch camera
        getCurrentScene().replaceAndSwitchCamera(camera, cameraOld);
    }

    private void assignCamera(Object3D obj) {

        this.camera = new TestArcballCamera(context, view);
        this.camera.setTarget(obj);
        this.camera.setPosition(2, 2, 1);
        getCurrentScene().replaceAndSwitchCamera(getCurrentCamera(), this.camera);
    }

    @Override
    public void onNoObjectPicked() {
        //System.out.println("Nothing selected!!!!!!!");
    }

    @Override
    public void onObjectPicked(Object3D object3D) {

        if (object3D == null)
            return;

        //assignCamera(object3D);

        /*
        boolean found = false;

        for (Piece piece : player1.getPieces()) {

            if (piece.getObj().equals(object3D)) {
                found = true;
                System.out.println("Piece found: " + piece.getType() + " - " + piece.getCol() + ", " + piece.getRow());
                break;
            }
        }

        if (!found) {
            for (Piece piece : player2.getPieces()) {
                if (piece.getObj().equals(object3D)) {
                    found = true;
                    System.out.println("Piece found: " + piece.getType() + " - " + piece.getCol() + ", " + piece.getRow());
                    break;
                }
            }
        }
        */
    }

    @Override
    public void onOffsetsChanged(float x, float y, float z, float w, int i, int j) {

    }

    @Override
    public void onRender(final long elapsedTime, final double deltaTime) {
        super.onRender(elapsedTime, deltaTime);
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