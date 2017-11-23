package com.gamesbykevin.chess.util;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;

import java.util.Random;

/**
 * Created by Kevin on 9/2/2017.
 */
public class UtilityHelper {

    /**
     * Create a random object which the seed as the current time stamp
     */
    private static Random RANDOM;

    /**
     * Are we debugging the application
     */
    public static final boolean DEBUG = false;

    /**
     * Are we running unit tests
     */
    public static boolean UNIT_TEST = false;

    /**
     * Is this an amazon app?
     */
    public static final boolean AMAZON = false;

    /**
     * App name for our framework
     */
    public static final String TAG = "AndroidFrameworkV2";

    public static Random getRandom() {

        //if null, create our object
        if (RANDOM == null) {

            //get the current timestamp
            final long time = System.nanoTime();

            //create instance with seed
            RANDOM = new Random(time);

            //print seed
            if (DEBUG)
                UtilityHelper.logEvent("Random seed: " + time);
        }

        //return our result
        return RANDOM;
    }

    public static void handleException(final Exception exception) {

        if (DEBUG) {

            if (UNIT_TEST) {

                System.out.print(exception);

            } else {

                //log as error
                Log.e(TAG, exception.getMessage(), exception);
            }

        } else {

            try {

                //if this app isn't for amazon log in fire base
                if (!AMAZON) {

                    //report exception to fire base
                    FirebaseCrash.report(exception);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //handle process
        exception.printStackTrace();
    }

    public static void logEvent(final String message) {

        //if not debugging, don't continue
        if (!DEBUG)
            return;

        //don't do anything if null
        if (message == null)
            return;

        //length limit of each line we print
        int maxLogSize = 4000;

        //if the string is too long
        if (message.length() > maxLogSize) {

            //we will display a portion at a time
            for (int i = 0; i <= message.length() / maxLogSize; i++) {

                int start = i * maxLogSize;
                int end = (i + 1) * maxLogSize;
                end = end > message.length() ? message.length() : end;

                if (UNIT_TEST) {
                    System.out.println(message.substring(start, end));
                } else {
                    Log.i(TAG, message.substring(start, end));
                }
            }

        } else {

            if (UNIT_TEST) {
                System.out.println(message);
            } else {
                //log string as information
                Log.i(TAG, message);
            }
        }
    }

    public static void displayMessage(final Context context, final String message) {

        //if not debugging, don't continue
        if (!DEBUG)
            return;

        //show text
        Toast.makeText(context, message , Toast.LENGTH_SHORT).show();
    }

    /**
     * Display the density of the phone
     * @param activity Activity where we can get the display metrics
     */
    public static void displayDensity(final Activity activity) {

        //if not debugging, don't continue
        if (!DEBUG)
            return;

        //the description of our screen density
        String desc = "Unknown";

        //determine which density the phone has
        switch (activity.getResources().getDisplayMetrics().densityDpi) {

            case DisplayMetrics.DENSITY_LOW:
                desc = "LDPI";
                break;

            case DisplayMetrics.DENSITY_MEDIUM:
                desc = "MDPI";
                break;

            case DisplayMetrics.DENSITY_HIGH:
                desc = "HDPI";
                break;

            case DisplayMetrics.DENSITY_XHIGH:
                desc = "XHDPI";
                break;

            case DisplayMetrics.DENSITY_XXHIGH:
                desc = "XXHDPI";
                break;

            case DisplayMetrics.DENSITY_XXXHIGH:
                desc = "XXXHDPI";
                break;

            default:
                desc = "Unknown";
                break;
                //throw new RuntimeException("Density not detected: " + activity.getResources().getDisplayMetrics().densityDpi);
        }

        //display the screen density
        logEvent("Screen density: " + desc);
    }
}