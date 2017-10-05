package com.gamesbykevin.chess.opengl;

import android.view.MotionEvent;

import com.gamesbykevin.chess.base.Entity;
import com.gamesbykevin.chess.game.GameHelper;

import static com.gamesbykevin.chess.activity.GameActivity.getGame;
import static com.gamesbykevin.chess.opengl.OpenGLRenderer.ZOOM_RATIO_ADJUST;
import static com.gamesbykevin.chess.opengl.OpenGLRenderer.ZOOM_SCALE_MOTION_X;
import static com.gamesbykevin.chess.opengl.OpenGLRenderer.ZOOM_SCALE_MOTION_Y;

/**
 * Created by Kevin on 9/10/2017.
 */

public class OpenGLSurfaceViewHelper {

    /**
     * Can we zoom in / out of the game?
     */
    public static boolean ZOOM_ENABLED = true;

    /**
     * Can the player drag the screen
     */
    public static boolean DRAG_ENABLED = true;

    /**
     * How many fingers are touching the screen
     */
    protected static int FINGERS = 0;

    /**
     * How many fingers do we need to zoom in/out
     */
    private static final int FINGERS_ZOOM = 2;

    /**
     * What is the distance between our finger coordinates?
     * Need to measure zoom in / out
     */
    private static double PINCH_DISTANCE = 0;

    /**
     * The minimum distance required to be considered a valid pinch
     */
    private static final float PINCH_THRESHOLD = 5;

    /**
     * The minimum pixel distance required to be considered a valid drag
     */
    private static final float DRAG_THRESHOLD = 5;

    //where are we moving our finger
    private static float motionMoveX = 0.0f, motionMoveY = 0.0f;

    //do we offset the render for our game
    public static float OFFSET_X = 0.0f, OFFSET_Y = 0.0f;

    //do we offset the render for our game
    public static float OFFSET_ORIGINAL_X = 0.0f, OFFSET_ORIGINAL_Y = 0.0f;

    //are we zooming?
    private static boolean ZOOMING = false;

    //are we dragging
    private static boolean DRAG = false;

    protected static float getCheckX(OpenGLSurfaceView view, MotionEvent event) {

        if (view.getOpenGlRenderer() != null) {
            return (event.getX() * ZOOM_SCALE_MOTION_X) - OFFSET_X + OpenGLRenderer.LEFT;
        } else {
            return (event.getX() * ZOOM_SCALE_MOTION_X) - OFFSET_X;
        }
    }

    protected static float getCheckY(OpenGLSurfaceView view, MotionEvent event) {
        if (view.getOpenGlRenderer() != null) {
            return (event.getY() * ZOOM_SCALE_MOTION_Y) - OFFSET_Y + OpenGLRenderer.TOP;
        } else {
            return (event.getY() * ZOOM_SCALE_MOTION_Y) - OFFSET_Y;
        }
    }

    protected static void onTouchEvent(OpenGLSurfaceView view, MotionEvent event) {

        final float x = event.getX();
        final float y = event.getY();

        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            //keep track of how many fingers are on the screen
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:

                //keep track of how many fingers we have on screen
                FINGERS++;

                //reset distance
                PINCH_DISTANCE = 0;

                //store the coordinates
                motionMoveX = x;
                motionMoveY = y;
                break;

            //keep track of how many fingers are on the screen
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:

                //keep track of how many fingers we have on screen
                FINGERS--;

                //reset distance
                PINCH_DISTANCE = 0;

                //if we are zooming
                if (ZOOMING) {

                    //if no more fingers are touching the screen, flag zoom over
                    if (FINGERS < 1)
                        ZOOMING = false;

                    //don't continue because we don't want to update the game at this point
                    return;
                }

                //if we were dragging
                if (DRAG) {

                    //flag false
                    DRAG = false;

                    //don't continue because we don't want to update the game at this point
                    return;
                }
                break;

            case MotionEvent.ACTION_MOVE:

                //if there are 2 coordinates and we recorded 2 fingers
                if (FINGERS == FINGERS_ZOOM && event.getPointerCount() == FINGERS_ZOOM) {

                    //don't continue if zoom is not enabled
                    if (!ZOOM_ENABLED)
                        return;

                    //flag that we are zooming
                    ZOOMING = true;

                    //get the distance between the two points
                    double distance = Entity.getDistance(event.getX(0), event.getY(0), event.getX(1), event.getY(1));

                    if (PINCH_DISTANCE == 0) {

                        //store the previous distance for our zoom
                        PINCH_DISTANCE = distance;

                    } else {

                        //calculate the difference
                        double diff = (distance > PINCH_DISTANCE) ? distance - PINCH_DISTANCE : PINCH_DISTANCE - distance;

                        //make sure the finger distance is great enough to be valid
                        if (diff > PINCH_THRESHOLD) {

                            if (PINCH_DISTANCE > distance) {

                                //if the distance is greater we are zooming in
                                OpenGLRenderer.adjustZoom(ZOOM_RATIO_ADJUST);

                            } else {

                                //if the distance is shorter we are zooming out
                                OpenGLRenderer.adjustZoom(-ZOOM_RATIO_ADJUST);
                            }

                            //reset pinch distance so if doesn't zoom too fast
                            PINCH_DISTANCE = 0;
                        }
                    }

                    //don't interact with the game since that is not the intention
                    return;

                } else if (FINGERS == 1 && event.getPointerCount() == 1) {

                    //if we are zooming we can't offset the board
                    if (ZOOMING)
                        return;

                    //adjust the coordinates where touch event occurred
                    final float x1 = x;
                    final float y1 = y;
                    final float x2 = motionMoveX;
                    final float y2 = motionMoveY;

                    //get our move difference
                    float xDiff = ((x2 > x1) ? -(x2 - x1) : (x1 - x2));
                    float yDiff = ((y2 > y1) ? -(y2 - y1) : (y1 - y2));

                    //only continue if dragging is enabled
                    if (!DRAG_ENABLED)
                        return;

                    //don't continue if we didn't move enough to register
                    if (Math.abs(xDiff) < DRAG_THRESHOLD && Math.abs(yDiff) < DRAG_THRESHOLD)
                        return;

                    //flag that we are dragging
                    DRAG = true;

                    //keep track of original coordinates
                    OFFSET_ORIGINAL_X += xDiff;
                    OFFSET_ORIGINAL_Y += yDiff;

                    //if one finger is moved, offset the x,y coordinates
                    OFFSET_X += (xDiff * ZOOM_SCALE_MOTION_X);
                    OFFSET_Y += (yDiff * ZOOM_SCALE_MOTION_Y);

                    //update the coordinates
                    motionMoveX = x1;
                    motionMoveY = y1;

                    //don't interact with the game since that is not the intention
                    return;
                }
                break;
        }
    }
}