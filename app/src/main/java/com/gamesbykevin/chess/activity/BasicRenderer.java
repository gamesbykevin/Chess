package com.gamesbykevin.chess.activity;

import android.content.Context;
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

    private Context context;

    private DirectionalLight mDirectionalLight;

    private Sphere mEarthSphere;

    private Object3D mObject;

    public BasicRenderer(Context context) {
        super(context);
        this.context = context;
        setFrameRate(60);
    }

    @Override
    public void initScene() {
        mDirectionalLight = new DirectionalLight(3f, 1f, 4f);
        mDirectionalLight.setColor(1.0f, 1.0f, 1.0f);
        mDirectionalLight.setPower(2);
        getCurrentScene().addLight(mDirectionalLight);

        /*
        Material material = new Material();
        material.enableLighting(true);
        material.setDiffuseMethod(new DiffuseMethod.Lambert());
        material.setColorInfluence(0);
        Texture earthTexture = new Texture("Earth", R.drawable.marble);
        try{
            material.addTexture(earthTexture);

        } catch (ATexture.TextureException error){
            error.printStackTrace();
        }
        */

        //mEarthSphere = new Sphere(1, 24, 24);
        //mEarthSphere.setMaterial(material);
        //getCurrentScene().addChild(mEarthSphere);
        //getCurrentCamera().setZ(4.2f);

        try {

            LoaderOBJ objParser = new LoaderOBJ(mContext.getResources(), mTextureManager, R.raw.chess_obj);
            objParser.parse();

            mObject = objParser.getParsedObject();
            mObject.setX(0);
            mObject.setY(0);
            mObject.setZ(0);
            mObject.setScale(.2);
            getCurrentScene().addChild(mObject);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onTouchEvent(MotionEvent event){
    }

    @Override
    public void onOffsetsChanged(float x, float y, float z, float w, int i, int j){
    }

    @Override
    public void onRender(final long elapsedTime, final double deltaTime) {
        super.onRender(elapsedTime, deltaTime);
        //mEarthSphere.rotate(Vector3.Axis.Y, 1.0);
        mObject.rotate(Vector3.Axis.Y, 1.0);
        mObject.rotate(Vector3.Axis.X, 1.0);
    }
}
