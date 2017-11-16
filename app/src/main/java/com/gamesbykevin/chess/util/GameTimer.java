package com.gamesbykevin.chess.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.gamesbykevin.androidframeworkv2.base.Disposable;
import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.activity.GameActivity;
import com.gamesbykevin.chess.opengl.OpenGLSurfaceView;

/**
 * Created by Kevin on 9/17/2017.
 */
public class GameTimer implements Disposable {

    //temp tracker
    private long tmp;

    //how much time has lapsed
    private long lapsed;

    //array of images for each number
    private Bitmap[] images;

    //values for our display timer
    private int clock1, clock2, clock3, clock4;

    //image object for the timer
    private ImageView time1, time2, time3, time4;

    /**
     * Character to separate the timer
     */
    public static final String TIMER_SEPARATOR = ",";

    //are we counting up / down ?
    private boolean ascending = true;

    /**
     * Default duration for our countdown timer 5 minutes 05:00
     */
    public static final String DEFAULT_COUNTDOWN_DURATION = "0" + TIMER_SEPARATOR + "5" + TIMER_SEPARATOR + "0" + TIMER_SEPARATOR + "0" + TIMER_SEPARATOR + "0";

    /**
     * Default duration for our count up timer 00:00
     */
    public static final String DEFAULT_STARTUP_DURATION = "0" + TIMER_SEPARATOR + "0" + TIMER_SEPARATOR + "0" + TIMER_SEPARATOR + "0" + TIMER_SEPARATOR + "0";

    /**
     * Default constructor
     */
    public GameTimer(GameActivity activity) {

        //call parent
        super();

        //load images into array
        this.images = new Bitmap[10];
        this.images[0] = BitmapFactory.decodeResource(activity.getResources(), R.drawable.zero);
        this.images[1] = BitmapFactory.decodeResource(activity.getResources(), R.drawable.one);
        this.images[2] = BitmapFactory.decodeResource(activity.getResources(), R.drawable.two);
        this.images[3] = BitmapFactory.decodeResource(activity.getResources(), R.drawable.three);
        this.images[4] = BitmapFactory.decodeResource(activity.getResources(), R.drawable.four);
        this.images[5] = BitmapFactory.decodeResource(activity.getResources(), R.drawable.five);
        this.images[6] = BitmapFactory.decodeResource(activity.getResources(), R.drawable.six);
        this.images[7] = BitmapFactory.decodeResource(activity.getResources(), R.drawable.seven);
        this.images[8] = BitmapFactory.decodeResource(activity.getResources(), R.drawable.eight);
        this.images[9] = BitmapFactory.decodeResource(activity.getResources(), R.drawable.nine);

        //obtain reference to our images that display the timer
        this.time1 = activity.findViewById(R.id.clock1);
        this.time2 = activity.findViewById(R.id.clock2);
        this.time3 = activity.findViewById(R.id.clock3);
        this.time4 = activity.findViewById(R.id.clock4);

        //reset timer progress
        reset();
    }

    public void setAscending(final boolean ascending) {
        this.ascending = ascending;
    }

    public boolean isAscending() {
        return this.ascending;
    }

    @Override
    public void dispose() {

        if (images != null) {
            for (Bitmap image : images) {
                if (image != null) {
                    image.recycle();
                    image = null;
                }
            }

            images = null;
        }

        time1 = null;
        time2 = null;
        time3 = null;
        time4 = null;
    }

    public void reset() {

        //reset temp timer
        tmp = -1;

        //time lapsed reset to 0
        lapsed = 0;

        //reset all values to 0
        clock1 = 0;
        clock2 = 0;
        clock3 = 0;
        clock4 = 0;
    }

    /**
     * Get the time lapsed
     * @return The total time (milliseconds) elapsed
     */
    public long getLapsed() {
        return this.lapsed;
    }

    public void update(Activity activity) {

        //if first time, update immediately
        if (tmp < 0) {
            updateImageView(activity, clock1, time1);
            updateImageView(activity, clock2, time2);
            updateImageView(activity, clock3, time3);
            updateImageView(activity, clock4, time4);
        }

        //keep track of time elapsed (temporary timer)
        tmp += OpenGLSurfaceView.FRAME_DURATION;

        //keep track of time elapsed (overall)
        lapsed += OpenGLSurfaceView.FRAME_DURATION;

        //if 1 second has passed, update our timer
        if (tmp >= OpenGLSurfaceView.MILLISECONDS_PER_SECOND) {

            //reset lapsed time back to 0
            tmp = 0;

            //keep track of what fields have changed
            boolean flag1 = false;
            boolean flag2 = false;
            boolean flag3 = false;
            boolean flag4 = true;

            if (isAscending()) {

                //increase the seconds
                clock4++;

                //10 seconds
                if (clock4 > 9) {

                    //reset
                    clock4 = 0;

                    //increase the tens (seconds)
                    clock3++;

                    //flag change
                    flag3 = true;
                }

                //60 seconds
                if (clock3 > 5) {

                    //reset
                    clock3 = 0;

                    //increase the minutes
                    clock2++;

                    //flag change
                    flag2 = true;
                }

                //10 minutes
                if (clock2 > 9) {

                    //reset
                    clock2 = 0;

                    //increase the tens (minutes)
                    clock1++;

                    //flag change
                    flag1 = true;
                }

            } else {

                //decrease the seconds
                clock4--;

                //10 seconds
                if (clock4 < 0) {

                    //reset
                    if (clock3 > 0 || clock2 > 0 || clock1 > 0) {
                        clock4 = 9;
                    } else {
                        clock4 = 0;
                    }

                    //decrease the tens (seconds)
                    clock3--;
                    flag3 = true;
                }

                //60 seconds
                if (clock3 < 0) {

                    //reset back to 59
                    if (clock2 > 0 || clock1 > 0) {
                        clock3 = 5;
                    } else {
                        clock3 = 0;
                    }

                    //decrease the minutes
                    clock2--;

                    //flag change
                    flag2 = true;
                }

                //10 minutes
                if (clock2 < 0) {

                    //reset
                    if (clock1 > 0) {
                        clock2 = 9;
                    } else {
                        clock2 = 0;
                    }

                    //decrease the tens (minutes)
                    clock1--;

                    //flag change
                    flag1 = true;
                }

                if (clock1 < 0)
                    clock1 = 0;
            }

            //update any changes
            if (flag4)
                updateImageView(activity, clock4, time4);
            if (flag3)
                updateImageView(activity, clock3, time3);
            if (flag2)
                updateImageView(activity, clock2, time2);
            if (flag1)
                updateImageView(activity, clock1, time1);
        }
    }

    private void updateImageView(Activity activity, int value, final ImageView imageView) {

        //assign the index value, if >= 100 minutes time will remain 99:99
        final int index = (clock1 > 9) ? 9 : value;

        //run on ui thread
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //update bitmap accordingly
                imageView.setImageBitmap(images[index]);
            }
        });
    }

    public String getTime() {
        return clock1 + TIMER_SEPARATOR + clock2 + TIMER_SEPARATOR + clock3 + TIMER_SEPARATOR + clock4 + TIMER_SEPARATOR + lapsed;
    }

    public void restoreTime(String time) {

        //split the values up by the delimiter
        String[] values = time.split(TIMER_SEPARATOR);

        //get and parse each value
        clock1 = Integer.parseInt(values[0]);
        clock2 = Integer.parseInt(values[1]);
        clock3 = Integer.parseInt(values[2]);
        clock4 = Integer.parseInt(values[3]);
        lapsed = Long.parseLong(values[4]);
    }

    public String getTimeDesc() {
        return clock1 + "" + clock2 + ":" + clock3 + "" + clock4;
    }

    public boolean hasExpired() {

        if (isAscending()) {
            return false;
        } else {
            return (clock1 <= 0 && clock2 <= 0 && clock3 <= 0 && clock4 <= 0);
        }
    }
}