package com.gamesbykevin.chess.activity;

import android.widget.ListView;

import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.game.GameHelper;

import static com.gamesbykevin.chess.activity.GameActivity.getScreen;

/**
 * Created by Kevin on 11/19/2017.
 */
public class GameActivityHelper {

    public static void updateListView(final GameActivity activity, final int position) {

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //assign the current item
                activity.selectedListItem = position;

                //position at the latest move
                ListView listView = activity.findViewById(R.id.listViewHistory);
                listView.setSelection(position);
                listView.setVerticalScrollBarEnabled(false);
                listView.setHorizontalScrollBarEnabled(false);

                if (position + 5 < listView.getCount() && position > 5) {
                    listView.smoothScrollToPosition(position + 5);
                } else {
                    listView.smoothScrollToPosition(position);
                }

                activity.adapter.notifyDataSetChanged();
            }
        });
    }

    public static void updateListView(final GameActivity activity, final String description) {

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run () {

                //display who executed the move
                final String bonus = (activity.adapter.getCount() % 2 == 0) ? "W: " : "B: ";

                //add our content
                activity.adapter.add(bonus + description);

                //position at the latest move
                updateListView(activity, activity.adapter.getCount() - 1);
            }
        });
    }

    public static void toggleSettings(final GameActivity activity, final boolean visible) {

        if (visible) {
            activity.setScreen(R.id.layoutGameSettings, getScreen() == R.id.layoutLoadingScreen);
        } else {
            activity.setScreen(R.id.layoutGameControls, true);
        }

        //we display the positions if we are viewing the settings screen
        GameHelper.displayPositions(activity.getGame(), getScreen() == R.id.layoutGameSettings);
    }
}