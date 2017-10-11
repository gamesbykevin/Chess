package com.gamesbykevin.chess.activity;

import com.gamesbykevin.chess.R;

import min3d.core.Object3dContainer;
import min3d.core.RendererActivity;
import min3d.parser.IParser;
import min3d.parser.Parser;
import min3d.vos.Light;

/**
 * Created by Kevin on 10/9/2017.
 */

public class Obj3DView extends RendererActivity {

    private Object3dContainer rook3d, rook2;

    @Override
    public void initScene()
    {
        scene.lights().add(new Light());
        //Light myLight = new Light();
        //myLight.position.setZ(200);
        //scene.lights().add(myLight);

        IParser myParser = Parser.createParser(Parser.Type.OBJ, getResources(), getString(R.string.package_name) + ":raw/chess_obj", true);
        myParser.parse();
        rook3d = myParser.getParsedObject();
        rook3d.position().x = rook3d.position().y = rook3d.position().z = 0;
        rook3d.scale().x = rook3d.scale().y = rook3d.scale().z = 2.0f;
        // Depending on the model you will need to change the scale faceObject3D.scale().x = faceObject3D.scale().y = faceObject3D.scale().z = 0.009f;        scene.addChild(faceObject3D);

        scene.addChild(rook3d);


        IParser myParser2 = Parser.createParser(Parser.Type.OBJ, getResources(), getString(R.string.package_name) + ":raw/chess_obj", true);
        myParser2.parse();
        rook2 = myParser2.getParsedObject();
        rook2.position().x = .5f;
        rook2.position().y = .5f;
        rook2.position().z = .5f;
        rook2.scale().x = 2.0f;
        rook2.scale().y = 2.0f;
        rook2.scale().z = 2.0f;

        scene.addChild(rook2);
    }

    @Override
    public void updateScene() {
        rook3d.rotation().x += 0.5;
        rook3d.rotation().z += .01;
        //rook3d.rotation().y += .5;

        rook2.rotation().z += .01;
    }

}