package com.gamesbykevin.chess.activity;

import android.content.Context;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;

import com.gamesbykevin.chess.R;

import org.rajawali3d.Object3D;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.renderer.Renderer;

/**
 * Created by Kevin on 10/12/2017.
 */

public class BasicRenderer extends Renderer {

    private static final float COORD_Y = -.4f;
    private static final float COORD_Z = .2f;

    private int fingers = 0;

    private PointF previous;

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

    private DirectionalLight mDirectionalLight;

    private Sphere mEarthSphere;

    private Object3D mObject, board;

    public BasicRenderer(Context context) {
        super(context);
        this.context = context;
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

        try {

            for (Type type : Type.values()) {
                Object3D obj = null;

                addPiece(obj, type, type.x, type.y, type.z);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onTouchEvent(MotionEvent event){
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                fingers--;
                break;

            case MotionEvent.ACTION_DOWN:
                fingers++;
                break;

            case MotionEvent.ACTION_MOVE:

                if (fingers == 1) {
                    final float x = event.getX(0);
                    final float y = event.getY(0);

                    if (previous == null)
                        previous = new PointF(x, y);

                    final float xDiff = previous.x - x;
                    final float yDiff = previous.y - y;

                    if (xDiff <= -5) {
                        getCurrentCamera().rotate(Vector3.Axis.X, 5);
                    } else if (xDiff >= 5) {
                        getCurrentCamera().rotate(Vector3.Axis.X, -5);
                    } else if (yDiff <= -5) {
                        getCurrentCamera().rotate(Vector3.Axis.Y, 5);
                    } else if (yDiff >= 5) {
                        getCurrentCamera().rotate(Vector3.Axis.Y, -5);
                    }

                    final double distance = Math.sqrt(Math.pow(previous.x - x, 2) - Math.pow(previous.y - y, 2));
                }
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
    }

    private void addPiece(Object3D obj, Type type, float x, float y, float z) throws Exception {

        LoaderOBJ objParser = new LoaderOBJ(mContext.getResources(), mTextureManager, type.resId);
        objParser.parse();

        obj = objParser.getParsedObject();
        obj.setX(x);
        obj.setY(y);
        obj.setZ(z);
        obj.rotate(Vector3.Axis.X, -45);
        //obj.setMaterial(material);
        obj.setScale(.05);
        //mObject.setDoubleSided(true);
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