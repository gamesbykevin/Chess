package com.gamesbykevin.chess.opengl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;

import com.gamesbykevin.chess.util.UtilityHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.gamesbykevin.chess.activity.GameActivity.getGame;
import static com.gamesbykevin.chess.game.GameHelper.getSquareBackground;
import static com.gamesbykevin.chess.opengl.OpenGLSurfaceView.FRAME_DURATION;
import static com.gamesbykevin.chess.opengl.OpenGLSurfaceView.HEIGHT;
import static com.gamesbykevin.chess.opengl.OpenGLSurfaceView.WIDTH;
import static com.gamesbykevin.chess.opengl.OpenGLSurfaceViewHelper.OFFSET_ORIGINAL_X;
import static com.gamesbykevin.chess.opengl.OpenGLSurfaceViewHelper.OFFSET_ORIGINAL_Y;
import static com.gamesbykevin.chess.opengl.OpenGLSurfaceViewHelper.OFFSET_X;
import static com.gamesbykevin.chess.opengl.OpenGLSurfaceViewHelper.OFFSET_Y;
import static com.gamesbykevin.chess.util.UtilityHelper.DEBUG;

/**
 * Created by Kevin on 6/1/2017.
 */
public class OpenGLRenderer implements Renderer {

    private ObjLoader obj;

    //the default starting zoom
    public static final float ZOOM_DEFAULT = 1.0f;

    /**
     * How far do we zoom in/out
     */
    private static float ZOOM_RATIO = 1.0f;

    /**
     * How much can we adjust the zoom at one time
     */
    public static float ZOOM_RATIO_ADJUST = 0.1f;

    /**
     * The maximum amount we can zoom out
     */
    private static float ZOOM_RATIO_MAX = 5.0f;

    /**
     * The minimum amount we can zoom in
     */
    private static float ZOOM_RATIO_MIN = 0.2f;

    //get the ratio of the users screen compared to the default dimensions for the motion event
    private static float originalScaleMotionX = 0, originalScaleMotionY = 0;

    //do we reset the zoom factor
    public static boolean RESET_ZOOM = true;

    //store the zoom for motion events as well
    public static float ZOOM_SCALE_MOTION_X, ZOOM_SCALE_MOTION_Y;

    //the actual dimensions of the users phone
    private static int screenWidth, screenHeight;

    /**
     * Have all textures been loaded?
     */
    public static boolean LOADED = false;

    //object containing all the texture ids
    private Textures textures;

    //our matrices window/camera/view etc...
    private final float[] mtrxProjection;
    private final float[] mtrxView;
    private final float[] mtrxProjectionAndView;

    //store the new screen dimensions in case of zoom
    public static int NEW_WIDTH = WIDTH, NEW_HEIGHT = HEIGHT;

    //calculate the center of the screen
    public static final float mx = (WIDTH / 2), my = (HEIGHT / 2);

    //the zoom window screen
    public static float LEFT = 0f, RIGHT = WIDTH, BOTTOM = HEIGHT, TOP = 0f;

    public OpenGLRenderer(Context activity) {

        this.obj = new ObjLoader(activity);

        //create object for reference to textures
        this.textures = new Textures(activity);

        //flag the textures loaded as false
        LOADED = false;

        //create new array
        mtrxProjection = new float[16];
        mtrxView = new float[16];
        mtrxProjectionAndView = new float[16];
    }

    public void onPause() {

        //clear our matrices
        if (mtrxProjectionAndView != null) {
            for (int i = 0; i < mtrxProjectionAndView.length; i++) {
                mtrxProjection[i] = 0.0f;
                mtrxView[i] = 0.0f;
                mtrxProjectionAndView[i] = 0.0f;
            }
        }
    }

    public void onResume() {
        //re-load the textures if needed?
    }

    /**
     * Restore the scale values as when the surface was first created
     */
    public static void resetZoom() {

        //store the zoom variables as the same
        ZOOM_SCALE_MOTION_X = originalScaleMotionX;
        ZOOM_SCALE_MOTION_Y = originalScaleMotionY;
        ZOOM_RATIO = ZOOM_DEFAULT;
        OFFSET_X = 0;
        OFFSET_Y = 0;
        OFFSET_ORIGINAL_X = OFFSET_X;
        OFFSET_ORIGINAL_Y = OFFSET_Y;

        //reset the display window
        LEFT = 0f;
        RIGHT = WIDTH;
        TOP = 0f;
        BOTTOM = HEIGHT;

        //flag false
        RESET_ZOOM = false;
    }

    /**
     * Adjust the zoom
     * @param adjust The ratio amount to adjust our screen dimensions
     */
    public static void adjustZoom(final float adjust) {

        //don't continue if not loaded
        if (!LOADED)
            return;

        //make adjustment
        ZOOM_RATIO += adjust;

        //keep the zoom within the boundary
        if (ZOOM_RATIO > ZOOM_RATIO_MAX)
            ZOOM_RATIO = ZOOM_RATIO_MAX;
        if (ZOOM_RATIO < ZOOM_RATIO_MIN)
            ZOOM_RATIO = ZOOM_RATIO_MIN;

        //calculate the  new dimensions
        NEW_WIDTH = (int)(WIDTH * ZOOM_RATIO);
        NEW_HEIGHT = (int)(HEIGHT * ZOOM_RATIO);

        //store the ratio when touching the screen
        ZOOM_SCALE_MOTION_X = ((float)NEW_WIDTH  / (float)screenWidth);
        ZOOM_SCALE_MOTION_Y = ((float)NEW_HEIGHT / (float)screenHeight);

        //update the offset (x, y)
        OFFSET_X = OFFSET_ORIGINAL_X * ZOOM_SCALE_MOTION_X;
        OFFSET_Y = OFFSET_ORIGINAL_Y * ZOOM_SCALE_MOTION_Y;

        //calculate the zoom screen
        LEFT = mx - (NEW_WIDTH / 2);
        RIGHT = mx + (NEW_WIDTH / 2);
        BOTTOM = my + (NEW_HEIGHT / 2);
        TOP = my - (NEW_HEIGHT / 2);
    }

    /**
     * Called once to set up the view's OpenGL ES environment
     */
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        //set the clear color to black
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1);

        //create the shader's, solid color
        int vertexShader = riGraphicTools.loadShader(GLES20.GL_VERTEX_SHADER, riGraphicTools.vs_SolidColor);
        int fragmentShader = riGraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER, riGraphicTools.fs_SolidColor);
        riGraphicTools.sp_SolidColor = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(riGraphicTools.sp_SolidColor, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(riGraphicTools.sp_SolidColor, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(riGraphicTools.sp_SolidColor);                  // creates OpenGL ES program executables

        //create the shader's, images
        vertexShader = riGraphicTools.loadShader(GLES20.GL_VERTEX_SHADER, riGraphicTools.vs_Image);
        fragmentShader = riGraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER, riGraphicTools.fs_Image);
        riGraphicTools.sp_Image = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(riGraphicTools.sp_Image, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(riGraphicTools.sp_Image, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(riGraphicTools.sp_Image);                  // creates OpenGL ES program executables

        //set our shader program
        GLES20.glUseProgram(riGraphicTools.sp_Image);

        //flag that we have not yet loaded the textures
        LOADED = false;

        //load our textures
        this.textures.loadTextures();
    }

    /**
     *  Called if the geometry of the view changes.<br>
     *  For example when the device's screen orientation changes
     * @param width pixel width of surface
     * @param height pixel height of surface
     */
    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {

        //store screen dimensions
        screenWidth = width;
        screenHeight = height;

        //make the viewport fullscreen on the users phone by using their screen width/height
        GLES20.glViewport(0, 0, (int)screenWidth, (int)screenHeight);

        //clear our matrices
        for(int i = 0; i < mtrxProjectionAndView.length; i++)
        {
            mtrxProjection[i] = 0.0f;
            mtrxView[i] = 0.0f;
            mtrxProjectionAndView[i] = 0.0f;
        }

        //setup our screen width and height for our intended screen size
        Matrix.orthoM(mtrxProjection, 0, 0f, WIDTH, HEIGHT, 0f, 0f, 50f);

        //offset the screen
        Matrix.translateM(mtrxProjection, 0, 0.0f, 0.0f, 0.0f);

        //set the camera position (matrix)
        Matrix.setLookAtM(mtrxView, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        //calculate the projection and view transformation
        Matrix.multiplyMM(mtrxProjectionAndView, 0, mtrxProjection, 0, mtrxView, 0);

        //store the ratio when touching the screen
        originalScaleMotionX = (float)WIDTH  / (float)screenWidth;
        originalScaleMotionY = (float)HEIGHT / (float)screenHeight;

        //set the zoom values same as original
        if (RESET_ZOOM)
            resetZoom();

        //flag that we have loaded the textures & screens
        LOADED = true;
    }

    /**
     * Called for each redraw of the view
     */
    @Override
    public void onDrawFrame(GL10 unused) {

        //get the current time
        long time = System.currentTimeMillis();

        //clear the screen and depth buffer, we have set the clear color as black.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        //support transparency
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_BLEND_SRC_ALPHA);


        this.obj.draw();

        /*
        //render our background
        setupBackground();
        getSquareBackground().render(mtrxProjectionAndView);

        //restore the zoom and pan coordinates for the game
        restoreZoomPan();

        //render game elements
        getGame().render(mtrxProjectionAndView);
        */

        if (DEBUG) {

            //calculate how long it took to render a single frame
            long duration = System.currentTimeMillis() - time;

            //if it took too long, notify command line
            if (duration > FRAME_DURATION)
                UtilityHelper.logEvent("Single render duration: " + (System.currentTimeMillis() - time));
        }
    }

    private void restoreZoomPan() {

        //setup the window that the user will see
        Matrix.orthoM(mtrxProjection, 0, LEFT, RIGHT, BOTTOM, TOP, 0f, 50f);

        //offset the screen
        Matrix.translateM(mtrxProjection, 0, OFFSET_X, OFFSET_Y, 0.0f);

        //calculate the projection and view transformation
        Matrix.multiplyMM(mtrxProjectionAndView, 0, mtrxProjection, 0, mtrxView, 0);
    }

    /**
     * Setup the background to the normal screen size to be rendered
     */
    private void setupBackground() {

        //set the correct texture for rendering
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, Textures.TEXTURE_ID_BACKGROUND);

        //reset to normal screen size so background is displayed without transformation
        Matrix.orthoM(mtrxProjection, 0, 0f, WIDTH, HEIGHT, 0f, 0f, 50f);
        Matrix.translateM(mtrxProjection, 0, 0.0f, 0.0f, 0.0f);
        Matrix.multiplyMM(mtrxProjectionAndView, 0, mtrxProjection, 0, mtrxView, 0);
    }
}