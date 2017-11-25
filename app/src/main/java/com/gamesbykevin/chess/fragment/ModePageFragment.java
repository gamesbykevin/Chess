package com.gamesbykevin.chess.fragment;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.activity.GameActivity;

import static com.gamesbykevin.chess.activity.GameActivity.getGame;
import static com.gamesbykevin.chess.services.BaseGameActivity.ONLINE;

public class ModePageFragment extends PageFragment {

    public ModePageFragment() {

        //default constructor
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //inflate the layout to access the ui elements
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_mode_page, container, false);

        ImageView imageView =   view.findViewById(R.id.modeImage);
        TextView textView =     view.findViewById(R.id.modeText);

        final int resIdImage;
        final int resIdText;

        switch (getPageNumber()) {

            case 0:
                resIdImage = R.drawable.versus_cpu;
                resIdText = R.string.game_mode_single_player;
                imageView.setEnabled(true);
                textView.setEnabled(true);
                break;

            case 1:
                resIdImage = R.drawable.versus_cpu;
                resIdText = R.string.game_mode_single_player_timed;
                imageView.setEnabled(true);
                textView.setEnabled(true);
                break;

            case 2:
                resIdImage = R.drawable.versus_human;
                resIdText = R.string.game_mode_two_player_offline;
                imageView.setEnabled(true);
                textView.setEnabled(true);
                break;

            case 3:
                if (ONLINE) {
                    resIdImage = R.drawable.versus_human;
                    resIdText = R.string.game_mode_two_player_online_timed;
                    imageView.setEnabled(true);
                    textView.setEnabled(true);
                } else {
                    resIdImage = R.drawable.versus_human_disabled;
                    resIdText = R.string.game_mode_two_player_online_timed_disabled;
                    imageView.setEnabled(false);
                    textView.setEnabled(false);
                }
                break;

            case 4:
                resIdImage = R.drawable.watch_replay;
                resIdText = R.string.game_mode_replay;
                imageView.setEnabled(true);
                textView.setEnabled(true);
                break;

            default:
                throw new RuntimeException("Page # not defined: " + getPageNumber());
        }

        //update bitmap accordingly
        imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), resIdImage));

        //assign the appropriate instruction text
        textView.setText(resIdText);

        // Inflate the layout for this fragment
        return view;
    }
}