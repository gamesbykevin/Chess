package com.gamesbykevin.chess.activity;

import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;

import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.game.GameHelper;
import com.gamesbykevin.chess.opengl.OpenGLSurfaceViewHelper;
import com.gamesbykevin.chess.services.BaseGameActivity;
import com.gamesbykevin.chess.util.UtilityHelper;

import static com.gamesbykevin.chess.util.UtilityHelper.AMAZON;

public class OptionsActivity extends BaseActivity {

    /**
     * List of toggle buttons in the options
     */
    public enum Buttons {
        Sound(R.id.toggleButtonSound, R.string.sound_file_key),
        Vibrate(R.id.toggleButtonVibrate, R.string.vibrate_file_key),
        Zoom(R.id.toggleButtonZoom, R.string.open_gl_zoom_file_key),
        GoogleAutoLogin(R.id.toggleButtonGoogleLogin, R.string.google_play_auto_login_file_key),
        Timer(R.id.toggleButtonTimer, R.string.timer_file_key);

        public final int buttonId, settingId;

        private Buttons(final int buttonId, final int settingId) {
            this.buttonId = buttonId;
            this.settingId = settingId;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_options);

        //retrieve our buttons so we can update based on current setting (shared preferences)
        for (Buttons button : Buttons.values()) {

            //get the current button
            ToggleButton tmp = findViewById(button.buttonId);

            //load the setting accordingly
            tmp.setChecked(getBooleanValue(button.settingId));

            //if this is for amazon, google play will be off (disabled)
            if (AMAZON && button.buttonId == R.id.toggleButtonGoogleLogin)
                tmp.setChecked(false);
        }
    }

    @Override
    public void onPause() {

        //call parent
        super.onPause();

        //stop sound
        super.stopSound();
    }

    @Override
    public void onResume() {

        //call parent
        super.onResume();

        //play menu
        super.playMenu();
    }

    /**
     * Override the back pressed so we save the shared preferences
     */
    @Override
    public void onBackPressed() {

        try {

            //get the editor so we can change the shared preferences
            Editor editor = getSharedPreferences().edit();

            //store the setting based on the toggle button state
            for (Buttons button : Buttons.values()) {

                //update the file setting with the according key and value
                editor.putBoolean(getString(button.settingId), ((ToggleButton)findViewById(button.buttonId)).isChecked());
            }

            //make it final by committing the change
            editor.apply();

        } catch (Exception e) {

            //handle exception
            UtilityHelper.handleException(e);
        }

        //call parent function
        super.onBackPressed();
    }

    public void onClickVibrate(View view) {

        //get the button
        ToggleButton button = view.findViewById(R.id.toggleButtonVibrate);

        //if the button is checked we will vibrate the phone
        if (button.isChecked())
            super.vibrate(true);
    }

    public void onClickSound(View view) {

        //get the button
        ToggleButton button = view.findViewById(R.id.toggleButtonSound);

        //assign global sound setting
        SOUND_ENABLED = button.isChecked();

        if (!button.isChecked()) {

            //if not enabled stop all sound
            super.stopSound();

        } else {

            //if enabled play menu theme
            super.playMenu();
        }
    }

    public void onClickZoom(View view) {

        //get the button
        ToggleButton button = view.findViewById(R.id.toggleButtonZoom);

        //update the view  options
        OpenGLSurfaceViewHelper.ZOOM_ENABLED = button.isChecked();
        OpenGLSurfaceViewHelper.DRAG_ENABLED = button.isChecked();
    }

    public void onClickTimer(View view) {

        //get the button
        ToggleButton button = view.findViewById(R.id.toggleButtonTimer);

        //update the timer display
        GameHelper.TIMER = button.isChecked();
    }

    public void onClickGoogleLogin(View view) {

        //get the button
        ToggleButton button = view.findViewById(R.id.toggleButtonGoogleLogin);

        //if this is for amazon, it will always be false
        if (AMAZON)
            button.setChecked(false);

        //update the login setting
        BaseGameActivity.BYPASS_LOGIN = (!button.isChecked());
    }
}