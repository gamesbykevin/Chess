package com.gamesbykevin.chess.game;

import android.opengl.GLES20;

import com.gamesbykevin.androidframeworkv2.base.Entity;
import com.gamesbykevin.chess.opengl.Square;

import static com.gamesbykevin.chess.activity.GameActivity.getGame;
import static com.gamesbykevin.chess.opengl.OpenGLSurfaceView.FPS;
import static com.gamesbykevin.chess.opengl.OpenGLSurfaceView.HEIGHT;
import static com.gamesbykevin.chess.opengl.OpenGLSurfaceView.WIDTH;

public final class GameHelper {

    //used to render the background
	private static Entity entityBackground = null;
    private static Square squareBackground = null;

    //did we flag the game over?
    public static boolean GAME_OVER = false;

    //do we want to display the timer
    public static boolean TIMER = true;

    //how long do we wait until displaying the game over overlay
    public static final int GAME_OVER_DELAY_FRAMES = (FPS * 3);

    //keep track of elapsed frames
    public static int FRAMES = 0;

    private static Entity getEntityBackground() {

        //only need to setup once
        if (entityBackground == null) {
            entityBackground = new Entity();
            entityBackground.setX(0);
            entityBackground.setY(0);
            entityBackground.setAngle(0f);
            entityBackground.setWidth(WIDTH);
            entityBackground.setHeight(HEIGHT);
        }

        return entityBackground;
    }

    public static Square getSquareBackground() {

        //only need to setup once
        if (squareBackground == null) {
            squareBackground = new Square();
            squareBackground.setupImage();
            squareBackground.setupTriangle();
            squareBackground.setupVertices(getEntityBackground().getVertices());
        }

        return squareBackground;
    }

    public static void dispose() {
        squareBackground = null;
        entityBackground = null;
    }

    public static void reset(Game game) {

        //flag game over false
        GAME_OVER = false;
    }

    /**
     * Render the game accordingly
     * @throws Exception
     */
    public static void render(float[] m) {

		//make sure we are supporting alpha for transparency
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

		//render the pieces on the board
        getGame().render(m);

        //we can now disable alpha transparency
        GLES20.glDisable(GLES20.GL_BLEND);
    }
}