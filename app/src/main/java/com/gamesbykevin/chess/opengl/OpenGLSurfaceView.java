package com.gamesbykevin.chess.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.gamesbykevin.chess.util.UtilityHelper;

import org.rajawali3d.view.SurfaceView;

import static com.gamesbykevin.chess.activity.GameActivity.getGame;
import static com.gamesbykevin.chess.util.UtilityHelper.DEBUG;

/**
 * Created by Kevin on 6/1/2017.
 */
public class OpenGLSurfaceView extends SurfaceView implements Runnable {

    /**
     * Frames per second
     */
    public static final int FPS = 90;

    /**
     * The version of open GL we are using
     */
    public static final int OPEN_GL_VERSION = 2;

    //our object where we render our pixel data
    private BasicRenderer renderer;

    //our game mechanics will run on this thread
    private Thread thread;

    //keep our thread running
    private volatile boolean running = true;

    //track the time to keep a steady game speed
    private long previous;
    private long previousUpdate;
    private long previousDraw;
    private long postUpdate;
    private long postDraw;

    /**
     * How many milliseconds per second
     */
    public static final long MILLISECONDS_PER_SECOND = 1000L;

    /**
     * The duration of each frame (milliseconds)
     */
    public static final long FRAME_DURATION = (long)(MILLISECONDS_PER_SECOND / FPS);

    //count the number of frames for debugging purposes
    private int frames = 0;

    //keep track of time for debug purposes
    private long timestamp = System.currentTimeMillis();

    public OpenGLSurfaceView(Context activity) {

        //call overloaded constructor
        this(activity, null);
    }

    public OpenGLSurfaceView(Context context, AttributeSet attrs) {

        //call parent constructor
        super(context, attrs);

        //create an OpenGL ES 1.0 context.
        setEGLContextClientVersion(OPEN_GL_VERSION);

        //preserve the context on pause (not guaranteed to work)
        setPreserveEGLContextOnPause(true);

        //create a new instance of our renderer
        this.renderer = new BasicRenderer(context, this);

        //set the renderer for drawing on the gl surface view
        setSurfaceRenderer(getRenderer());

        //assign the frame rate
        setFrameRate(FPS);

        //set render mode to only draw when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    /**
     * Get our OpenGL Renderer
     * @return Object used for all texture mapping
     */
    public BasicRenderer getRenderer() {
        return this.renderer;
    }

    /**
     * If the game is paused stop our thread
     */
    @Override
    public void onPause() {

        //call parent function
        super.onPause();

        //pause the open gl renderer
        if (getRenderer() != null) {
            getRenderer().onPause();
            getRenderer().RENDER = false;
        }

        //flag that we don't want our thread to continue running
        this.running = false;

        try {

            //wait for thread to finish
            if (this.thread != null) {
                this.thread.join();
                this.thread = null;
            }

        } catch (Exception e) {
            UtilityHelper.handleException(e);
        }
    }

    /**
     * Once we resume start a new thread again
     */
    @Override
    public void onResume() {

        //don't create another thread if already running
        if (this.running || this.thread != null)
            return;

        //flag running true
        this.running = true;

        //call parent function
        super.onResume();

        //resume the open gl renderer
        getRenderer().onResume();

        //create the thread
        this.thread = new Thread(this);

        if (DEBUG) {
            this.thread.setName("Thread " + System.nanoTime());
            UtilityHelper.logEvent(this.thread.getName());
        }

        //start the thread
        this.thread.start();
    }

    @Override
    public void run() {

        //do we continue to loop
        while (running) {

            try {

                //get the current time
                this.previous = System.currentTimeMillis();

                //update the game state
                update();

                //render the image
                draw();

                //control the game speed
                control();

            } catch (Exception e) {

                //handle exception
                UtilityHelper.handleException(e);

                //if debugging exit loop on exception
                if (DEBUG) {
                    running = false;
                    break;
                }
            }
        }
    }

    /**
     * Make sure we maintain game speed
     * @throws InterruptedException
     */
    private void control() throws InterruptedException {

        //calculate how much time had passed
        final long duration = System.currentTimeMillis() - this.previous;

        //we want each loop to have the same duration to maintain fps
        long remaining = FRAME_DURATION - duration;

        //log event id this loop is running slow
        if (DEBUG && remaining <= 0) {
            UtilityHelper.logEvent("Slow: " + remaining);
            UtilityHelper.logEvent("Update duration: " + (this.postUpdate - this.previousUpdate));
            UtilityHelper.logEvent("Draw   duration: " + (this.postDraw - this.previousDraw));
        }

        //make sure we sleep at least 1 millisecond
        if (remaining < 1)
            remaining = 1;

        //make sure time remaining is a valid number
        remaining = (remaining <= 0) ? 1 : remaining;

        //sleep the thread to maintain a steady game speed
        thread.sleep(remaining);

        //track performance (if debugging)
        trackProgress();
    }

    /**
     * Track progress for debugging purposes
     */
    private void trackProgress() {

        //don't continue if not debugging
        if (!DEBUG)
            return;

        //keep track of the frames
        frames++;

        //if 1 second has passed, print fps counter
        if (System.currentTimeMillis() - timestamp >= MILLISECONDS_PER_SECOND) {

            //print progress
            UtilityHelper.logEvent("FPS: " + frames);

            //reset timer for next update
            timestamp = System.currentTimeMillis();

            //reset frame count
            frames = 0;
        }
    }

    /**
     * Update the game state
     */
    private void update() {

        //track time before update
        this.previousUpdate = System.currentTimeMillis();

        //update game logic here
        if (getGame() != null)
            getGame().update();

        //track time after update
        this.postUpdate = System.currentTimeMillis();
    }

    /**
     * Render the game image to screen surface
     */
    private void draw() {

        //track time before draw
        this.previousDraw = System.currentTimeMillis();

        try {

            //render game objects
            requestRender();

        } catch (Exception e) {
            UtilityHelper.handleException(e);
        }

        //track time after draw
        this.postDraw = System.currentTimeMillis();
    }
}