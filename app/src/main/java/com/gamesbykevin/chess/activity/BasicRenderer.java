package com.gamesbykevin.chess.activity;

import android.content.Context;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.gamesbykevin.chess.R;

import org.rajawali3d.Object3D;
import org.rajawali3d.cameras.ArcballCamera;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.renderer.Renderer;

import java.util.ArrayList;

/**
 * Created by Kevin on 10/12/2017.
 */
public class BasicRenderer extends Renderer {

    private static final float COORD_Y = -.4f;
    private static final float COORD_Z = .0f;

    private int fingers = 0;

    private PointF previous;

    private ArcballCamera camera;

    private float angle = 0f;

    public enum Type {
        Pawn(R.raw.pawn_obj, -.65,      COORD_Y, COORD_Z),
        Bishop(R.raw.bishop_obj, -.45,  COORD_Y, COORD_Z),
        Knight(R.raw.knight_obj, -.25,  COORD_Y, COORD_Z),
        Rook(R.raw.rook_obj, -.05,      COORD_Y, COORD_Z),
        Queen(R.raw.queen_obj, .15,     COORD_Y, COORD_Z),
        King(R.raw.king_obj, .35,       COORD_Y, COORD_Z);

        private final int resId;

        private final float x, y, z;

        private Type(int resId, double x, double y, double z) {
            this(resId, (float)x, (float)y, (float)z);
        }

        private Type(int resId, float x, float y, float z) {
            this.resId = resId;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private Context context;

    private View view;

    private DirectionalLight mDirectionalLight;

    private Sphere mEarthSphere;

    private Object3D mObject, board;

    public BasicRenderer(Context context, View view) {
        super(context);
        this.context = context;
        this.view = view;
        setFrameRate(60);
    }

    @Override
    public void initScene() {

        mDirectionalLight = new DirectionalLight(1f, 15f, 1f);
        mDirectionalLight.setColor(1.0f, 1.0f, 1.0f);
        mDirectionalLight.setPower(2);
        getCurrentScene().addLight(mDirectionalLight);

        Material material = new Material();

        try{
            material.enableLighting(true);
            material.setDiffuseMethod(new DiffuseMethod.Lambert());
            material.setColorInfluence(0);
            Texture texture = new Texture("ChessPiece", R.drawable.marble1);
            material.addTexture(texture);
        } catch (Exception e) {
            e.printStackTrace();
        }

        addBoard();

        //create arc camera
        this.camera = new ArcballCamera(context, view, board);


        try {

            Object3D obj = null;
            //addPiece(obj, Type.King, 0, 0, 0);

            for (Type type : Type.values()) {
                addPiece(obj, type, type.x, type.y, type.z);
            }

            camera.setPosition(0,0,3); //optional
            getCurrentScene().replaceAndSwitchCamera(getCurrentCamera(), camera);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTouchEvent(MotionEvent event){

        final float x = event.getX(0);
        final float y = event.getY(0);

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                fingers--;
                getCurrentCamera().setPosition(0,0,3); //optional
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

                    //getCurrentCamera().setPosition(s);

                } else if (fingers == 2) {

                }

                //final double distance = Math.sqrt(Math.pow(previous.x - x, 2) - Math.pow(previous.y - y, 2));

                previous.x = x;
                previous.y = y;
                break;
        }
    }

    @Override
    public void onOffsetsChanged(float x, float y, float z, float w, int i, int j) {

    }

    @Override
    public void onRender(final long elapsedTime, final double deltaTime) {
        super.onRender(elapsedTime, deltaTime);
        //mEarthSphere.rotate(Vector3.Axis.Y, 1.0);
        //mObject.rotate(Vector3.Axis.Y, 1.0);
        //mObject.rotate(Vector3.Axis.X, 1.5);

        //board.rotate(Vector3.Axis.X, -1);
        ArrayList<Object3D> children = getCurrentScene().getChildrenCopy();


        for (Object3D child : children) {
            //child.rotate(Vector3.Axis.Y, 1);
        }

        /*
        for (Object3D child : children) {
            child.rotateAround(
                //new Vector3(child.getX(), child.getY(), child.getZ()),
                    new Vector3(child.getX(), 0, 0),
                    //new Vector3(0, child.getY(), 0),
                    //new Vector3(0, 0, child.getZ()),
                1
            );
        }
        */
    }

    private void addPiece(Object3D obj, Type type, float x, float y, float z) throws Exception {

        LoaderOBJ objParser = new LoaderOBJ(mContext.getResources(), mTextureManager, type.resId);
        objParser.parse();

        obj = objParser.getParsedObject();
        obj.setX(x);
        obj.setY(y);
        obj.setZ(z);
        obj.rotate(Vector3.Axis.Y, 55);
        obj.rotate(Vector3.Axis.X, -45);
        obj.setScale(.05);

        //obj.setMaterial(material);
        //obj.setDoubleSided(true);
        getCurrentScene().addChild(obj);
    }

    private void addBoard() {
        try {
            LoaderOBJ objParser = new LoaderOBJ(mContext.getResources(), mTextureManager, R.raw.board_obj);
            objParser.parse();

            board = objParser.getParsedObject();
            board.setZ(-.5);
            board.setScale(.2);
            board.rotate(Vector3.Axis.X, -45);
            getCurrentScene().addChild(board);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}