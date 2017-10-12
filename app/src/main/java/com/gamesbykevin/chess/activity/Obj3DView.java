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

    private Object3dContainer rook3d, rook3d_other, board;

    private Light myLight;

    private float xVel = 0;

    @Override
    public void initScene()
    {
        //scene.lights().add(new Light());
        myLight = new Light();
        myLight.position.setZ(200f);
        scene.lights().add(myLight);

        IParser myParser = Parser.createParser(Parser.Type.OBJ, getResources(), getString(R.string.package_name) + ":raw/rook_obj", true);
        myParser.parse();
        rook3d = myParser.getParsedObject();
        rook3d.position().x = rook3d.position().y = rook3d.position().z = 0f;
        rook3d.scale().x = rook3d.scale().y = rook3d.scale().z = .5f;
        rook3d.position().x = -.95f;
        rook3d.position().y = -.6f;
        rook3d.position().z = .35f;
        rook3d.rotation().x = 45f;

        scene.addChild(rook3d);

        IParser myParser1 = Parser.createParser(Parser.Type.OBJ, getResources(), getString(R.string.package_name) + ":raw/rook_obj", true);
        myParser1.parse();
        rook3d_other = myParser1.getParsedObject();
        rook3d_other.position().x = rook3d_other.position().y = rook3d_other.position().z = 0f;
        rook3d_other.scale().x = rook3d_other.scale().y = rook3d_other.scale().z = .5f;
        rook3d_other.position().x = .75f;
        rook3d_other.position().y = -.6f;
        rook3d_other.position().z = .35f;
        rook3d_other.rotation().x = 45f;

        scene.addChild(rook3d_other);

        IParser myParser2 = Parser.createParser(Parser.Type.OBJ, getResources(), getString(R.string.package_name) + ":raw/board_obj", true);
        myParser2.parse();
        board = myParser2.getParsedObject();
        board.position().x = board.position().y = 0; board.position().z = -.5f;
        board.scale().x = board.scale().y = board.scale().z = .25f;
        board.rotation().x = 45f;
        //board.rotation().y += 10f;

        scene.addChild(board);
    }

    @Override
    public void updateScene() {
        //rook3d.rotation().x += 0.5;
        //rook3d.rotation().z += .01;
        //rook3d.rotation().y += .5;

        //rook2.rotation().z += .01;
    }
}