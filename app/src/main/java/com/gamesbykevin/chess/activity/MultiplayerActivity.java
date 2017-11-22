package com.gamesbykevin.chess.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.game.GameHelper;
import com.gamesbykevin.chess.piece.Piece;
import com.gamesbykevin.chess.piece.PieceHelper;
import com.gamesbykevin.chess.players.PlayerHelper;
import com.gamesbykevin.chess.players.PlayerVars;
import com.gamesbykevin.chess.services.BaseGameActivity;
import com.gamesbykevin.chess.services.BaseGameUtils;
import com.gamesbykevin.chess.util.UtilityHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.GamesClientStatusCodes;
import com.google.android.gms.games.InvitationsClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.InvitationCallback;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.OnRealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateCallback;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import static com.gamesbykevin.chess.activity.GameActivity.getGame;
import static com.gamesbykevin.chess.players.PlayerVars.STATE;

public class MultiplayerActivity extends BaseGameActivity {

    // Request codes for the UIs that we show with startActivityForResult:
    protected final static int RC_SELECT_PLAYERS = 10000;
    protected final static int RC_INVITATION_INBOX = 10001;
    protected final static int RC_WAITING_ROOM = 10002;

    // Request code used to invoke sign in user interactions.
    protected static final int RC_SIGN_IN = 9001;

    // Client used to sign in with Google APIs
    private GoogleSignInClient mGoogleSignInClient = null;

    // Client used to interact with the real time multi-player system.
    private RealTimeMultiplayerClient mRealTimeMultiplayerClient = null;

    // Client used to interact with the Invitation system.
    private InvitationsClient mInvitationsClient = null;

    // Room ID where the currently active game is taking place; null if we're
    // not playing.
    String mRoomId = null;

    // Holds the configuration of the current room.
    RoomConfig mRoomConfig;

    // Are we playing in multi-player mode?
    boolean mMultiplayer = false;

    // My participant ID in the currently active game
    String mMyId = null;

    // If non-null, this is the id of the invitation we received via the
    // invitation listener
    String mIncomingInvitationId = null;

    // Message buffer for sending messages
    byte[] mMsgBuf = new byte[7];

    // This array lists all the individual screens our game has.
    final static int[] SCREENS = {
        R.id.screen_main, R.id.screen_wait
    };

    // The participants in the currently active game
    ArrayList<Participant> mParticipants = null;

    int mCurScreen = -1;

    //random generated number to determine who goes first
    private int random;

    /**
     * Are we playing a multi player game?
     */
    public static boolean MULTI_PLAYER = false;

    //are we player 1?
    public static boolean PLAYER_1 = false;

    //did we decide who started the game?
    public static boolean STARTED = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //don't continue if no multi-player
        if (!MULTI_PLAYER)
            return;

        //flag started false
        STARTED = false;

        // Create the client used to sign in.
        mGoogleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);

        //pick random number which will tell us who goes first
        this.random = UtilityHelper.getRandom().nextInt(128);

        //display main screen
        switchToScreen(R.id.screen_main);
    }

    @Override
    protected void onResume() {

        super.onResume();

        //don't continue if no multi-player
        if (!MULTI_PLAYER)
            return;

        // Since the state of the signed in user can change when the activity is not active
        // it is recommended to try and sign in silently from when the app resumes.
        signInSilently();
    }

    @Override
    protected void onPause() {
        super.onPause();

        //don't continue if no multi-player
        if (!MULTI_PLAYER)
            return;

        // unregister our listeners.  They will be re-registered via onResume->signInSilently->onConnected.
        if (mInvitationsClient != null) {
            mInvitationsClient.unregisterInvitationCallback(mInvitationCallback);
        }
    }

    public void onClickInvite(View view) {
        switchToScreen(R.id.screen_wait);

        // show list of players who can be invited
        mRealTimeMultiplayerClient.getSelectOpponentsIntent(1, 1).addOnSuccessListener(
            new OnSuccessListener<Intent>() {
                @Override
                public void onSuccess(Intent intent) {
                    startActivityForResult(intent, RC_SELECT_PLAYERS);
                }
            }
        ).addOnFailureListener(createFailureListener("There was a problem selecting opponents."));
    }

    public void onClickViewInvitations(View view) {
        switchToScreen(R.id.screen_wait);

        // show list of pending invitations
        mInvitationsClient.getInvitationInboxIntent().addOnSuccessListener(
            new OnSuccessListener<Intent>() {
                @Override
                public void onSuccess(Intent intent) {
                    startActivityForResult(intent, RC_INVITATION_INBOX);
                }
            }
        ).addOnFailureListener(createFailureListener("There was a problem getting the inbox."));
    }

    public void onClickAcceptInvitation(View view) {

        // user wants to accept the invitation shown on the invitation popup
        // (the one we got through the OnInvitationReceivedListener).
        acceptInviteToRoom(mIncomingInvitationId);
        mIncomingInvitationId = null;
    }

    public void onClickQuickMatch(View view) {

        //we are not ready to start
        STARTED = false;

        // quick-start a game with 1 randomly selected opponent
        final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 1;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS, MAX_OPPONENTS, 0);
        switchToScreen(R.id.screen_wait);
        keepScreenOn();

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();
        mRealTimeMultiplayerClient.create(mRoomConfig);
    }

    /**
     * Try to sign in without displaying dialogs to the user.
     * <p>
     * If the user has already signed in previously, it will not show dialog.
     */
    public void signInSilently() {
        Log.d(TAG, "signInSilently()");

        mGoogleSignInClient.silentSignIn().addOnCompleteListener(this,
            new OnCompleteListener<GoogleSignInAccount>() {
                @Override
                public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInSilently(): success");
                        onConnected(task.getResult());
                    } else {
                        Log.d(TAG, "signInSilently(): failure", task.getException());
                        onDisconnected();
                    }
                }
        });
    }

    void switchToScreen(int screenId) {

        // make the requested screen visible; hide all others.
        for (int id : SCREENS) {

            View v = findViewById(id);

            if (v != null) {
                v.setVisibility(screenId == id ? View.VISIBLE : View.GONE);
            }
        }
        mCurScreen = screenId;

        // should we show the invitation popup?
        boolean showInvPopup;
        if (mIncomingInvitationId == null) {
            // no invitation, so no popup
            showInvPopup = false;
        } else if (mMultiplayer) {
            // if in multi player, only show invitation on main screen
            showInvPopup = (mCurScreen == R.id.screen_main);
        } else {
            // single-player: show on main screen and gameplay screen
            showInvPopup = (mCurScreen == R.id.screen_main);
        }

        View v = findViewById(R.id.invitation_popup);

        if (v != null)
            v.setVisibility(showInvPopup ? View.VISIBLE : View.GONE);
    }

    protected void handleSelectPlayersResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** select players UI cancelled, " + response);
            switchToScreen(R.id.screen_main);
            return;
        }

        Log.d(TAG, "Select players UI succeeded.");

        // get the invitee list
        final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
        Log.d(TAG, "Invitee count: " + invitees.size());

        // get the auto-match criteria
        Bundle autoMatchCriteria = null;
        int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
        if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
            autoMatchCriteria = RoomConfig.createAutoMatchCriteria(minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            Log.d(TAG, "Automatch criteria: " + autoMatchCriteria);
        }

        // create the room
        Log.d(TAG, "Creating room...");
        switchToScreen(R.id.screen_wait);
        keepScreenOn();

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .addPlayersToInvite(invitees)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .setAutoMatchCriteria(autoMatchCriteria).build();
        mRealTimeMultiplayerClient.create(mRoomConfig);
        Log.d(TAG, "Room created, waiting for it to be ready...");
    }

    // Handle the result of the invitation inbox UI, where the player can pick an invitation
    // to accept. We react by accepting the selected invitation, if any.
    protected void handleInvitationInboxResult(int response, Intent data) {

        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** invitation inbox UI cancelled, " + response);
            switchToScreen(R.id.screen_main);
            return;
        }

        Log.d(TAG, "Invitation inbox UI succeeded.");
        Invitation invitation = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);

        // accept invitation
        if (invitation != null) {
            acceptInviteToRoom(invitation.getInvitationId());
        }
    }

    // Accept the given invitation.
    void acceptInviteToRoom(String invitationId) {

        // accept the invitation
        Log.d(TAG, "Accepting invitation: " + invitationId);

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
            .setInvitationIdToAccept(invitationId)
            .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
            .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
            .build();

        switchToScreen(R.id.screen_wait);
        keepScreenOn();

        mRealTimeMultiplayerClient.join(mRoomConfig).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Room Joined Successfully!");
            }
        });
    }

    // Activity is going to the background. We have to leave the current room.
    @Override
    public void onStop() {

        //don't continue if no multi-player
        if (!MULTI_PLAYER) {
            super.onStop();
            return;
        }

        Log.d(TAG, "**** got onStop");

        // if we're in a room, leave it.
        leaveRoom();

        // stop trying to keep the screen on
        stopKeepingScreenOn();

        //go to the main screen
        switchToScreen(R.id.screen_main);

        //call parent
        super.onStop();
    }

    // Leave the room.
    protected void leaveRoom() {
        Log.d(TAG, "Leaving room.");
        stopKeepingScreenOn();
        if (mRoomId != null) {
            mRealTimeMultiplayerClient.leave(mRoomConfig, mRoomId).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    mRoomId = null;
                    mRoomConfig = null;
                }
            });

            switchToScreen(R.id.screen_main);

        } else {

            switchToScreen(R.id.screen_main);
        }
    }

    // Show the waiting room UI to track the progress of other players as they enter the
    // room and get connected.
    void showWaitingRoom(Room room) {
        // minimum number of players required for our game
        // For simplicity, we require everyone to join the game before we start it
        // (this is signaled by Integer.MAX_VALUE).
        final int MIN_PLAYERS = Integer.MAX_VALUE;
        mRealTimeMultiplayerClient.getWaitingRoomIntent(room, MIN_PLAYERS).addOnSuccessListener(new OnSuccessListener<Intent>() {
            @Override
            public void onSuccess(Intent intent) {
                // show waiting room UI
                startActivityForResult(intent, RC_WAITING_ROOM);
            }
        }).addOnFailureListener(createFailureListener("There was a problem getting the waiting room!"));
    }

    private InvitationCallback mInvitationCallback = new InvitationCallback() {
        // Called when we get an invitation to play a game. We react by showing that to the user.
        @Override
        public void onInvitationReceived(@NonNull Invitation invitation) {

            // We got an invitation to play a game! So, store it in
            // mIncomingInvitationId
            // and show the popup on the screen.
            mIncomingInvitationId = invitation.getInvitationId();
            ((TextView) findViewById(R.id.incoming_invitation_text)).setText(invitation.getInviter().getDisplayName() + " " + getString(R.string.is_inviting_you));
            switchToScreen(mCurScreen); // This will show the invitation popup
        }

        @Override
        public void onInvitationRemoved(@NonNull String invitationId) {

            if (mIncomingInvitationId.equals(invitationId) && mIncomingInvitationId != null) {
                mIncomingInvitationId = null;
                switchToScreen(mCurScreen); // This will hide the invitation popup
            }
        }
    };


    /*
     * CALLBACKS SECTION. This section shows how we implement the several games
     * API callbacks.
     */

    private String mPlayerId;

    // The currently signed in account, used to check the account has changed outside of this activity when resuming.
    GoogleSignInAccount mSignedInAccount = null;

    protected void onConnected(GoogleSignInAccount googleSignInAccount) {
        Log.d(TAG, "onConnected(): connected to Google APIs");
        if (mSignedInAccount != googleSignInAccount) {

            mSignedInAccount = googleSignInAccount;

            // update the clients
            mRealTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(this, googleSignInAccount);
            mInvitationsClient = Games.getInvitationsClient(MultiplayerActivity.this, googleSignInAccount);

            // get the playerId from the PlayersClient
            PlayersClient playersClient = Games.getPlayersClient(this, googleSignInAccount);
            playersClient.getCurrentPlayer().addOnSuccessListener(
                new OnSuccessListener<Player>() {
                    @Override
                    public void onSuccess(Player player) {
                        mPlayerId = player.getPlayerId();
                    }
                }
            );
        }

        // register listener so we are notified if we receive an invitation to play
        // while we are in the game
        mInvitationsClient.registerInvitationCallback(mInvitationCallback);

        // get the invitation from the connection hint
        // Retrieve the TurnBasedMatch from the connectionHint
        GamesClient gamesClient = Games.getGamesClient(MultiplayerActivity.this, googleSignInAccount);
        gamesClient.getActivationHint().addOnSuccessListener(new OnSuccessListener<Bundle>() {
            @Override
            public void onSuccess(Bundle hint) {
                if (hint != null) {
                    Invitation invitation = hint.getParcelable(Multiplayer.EXTRA_TURN_BASED_MATCH);

                    if (invitation != null && invitation.getInvitationId() != null) {
                        // retrieve and cache the invitation ID
                        Log.d(TAG, "onConnected: connection hint has a room invite!");
                        acceptInviteToRoom(invitation.getInvitationId());
                    }
                }
            }
        }).addOnFailureListener(createFailureListener("There was a problem getting the activation hint!"));

        switchToScreen(R.id.screen_main);
    }

    private OnFailureListener createFailureListener(final String string) {
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                handleException(e, string);
            }
        };
    }

    public void onDisconnected() {
        Log.d(TAG, "onDisconnected()");

        mRealTimeMultiplayerClient = null;
        mInvitationsClient = null;

        if (mRoomId != null) {
            mRoomId = null;
            getGame().getActivity().setScreen(R.id.layoutGameMultiplayer, true);
        }
    }

    private RoomStatusUpdateCallback mRoomStatusUpdateCallback = new RoomStatusUpdateCallback() {
        // Called when we are connected to the room. We're not ready to play yet! (maybe not everybody
        // is connected yet).
        @Override
        public void onConnectedToRoom(Room room) {
            Log.d(TAG, "onConnectedToRoom.");

            //get participants and my ID:
            mParticipants = room.getParticipants();
            mMyId = room.getParticipantId(mPlayerId);

            // save room ID if its not initialized in onRoomCreated() so we can leave cleanly before the game starts.
            if (mRoomId == null) {
                mRoomId = room.getRoomId();
            }

            // print out the list of participants (for debug purposes)
            Log.d(TAG, "Room ID: " + mRoomId);
            Log.d(TAG, "My ID " + mMyId);
            Log.d(TAG, "<< CONNECTED TO ROOM>>");
        }

        // Called when we get disconnected from the room. We return to the main screen.
        @Override
        public void onDisconnectedFromRoom(Room room) {
            Log.d(TAG, "onDisconnectedFromRoom");
            mRoomConfig = null;
            showGameError();
            displayMessage(R.string.player_left_game);

            if (mRoomId != null) {
                mRoomId = null;
                getGame().getActivity().setScreen(R.id.layoutGameMultiplayer, true);
            }
        }

        // We treat most of the room update callbacks in the same way: we update our list of
        // participants and update the display. In a real game we would also have to check if that
        // change requires some action like removing the corresponding player avatar from the screen,
        // etc.
        @Override
        public void onPeerDeclined(Room room, @NonNull List<String> arg1) {
            updateRoom(room);
        }

        @Override
        public void onPeerInvitedToRoom(Room room, @NonNull List<String> arg1) {
            updateRoom(room);
        }

        @Override
        public void onP2PDisconnected(@NonNull String participant) {
        }

        @Override
        public void onP2PConnected(@NonNull String participant) {
        }

        @Override
        public void onPeerJoined(Room room, @NonNull List<String> arg1) {
            updateRoom(room);
        }

        @Override
        public void onPeerLeft(Room room, @NonNull List<String> peersWhoLeft) {
            Log.d(TAG, "onPeerLeft");

            updateRoom(room);
            displayMessage(R.string.player_left_game);

            if (mRoomId != null) {
                mRoomId = null;
                getGame().getActivity().setScreen(R.id.layoutGameMultiplayer, true);
            }
        }

        @Override
        public void onRoomAutoMatching(Room room) {
            updateRoom(room);
        }

        @Override
        public void onRoomConnecting(Room room) {
            updateRoom(room);
        }

        @Override
        public void onPeersConnected(Room room, @NonNull List<String> peers) {
            updateRoom(room);
        }

        @Override
        public void onPeersDisconnected(Room room, @NonNull List<String> peers) {
            updateRoom(room);
        }
    };

    // Show error message about game being cancelled and return to main screen.
    void showGameError() {
        BaseGameUtils.makeSimpleDialog(this, getString(R.string.game_problem));
        switchToScreen(R.id.screen_main);
    }

    private RoomUpdateCallback mRoomUpdateCallback = new RoomUpdateCallback() {

        // Called when room has been created
        @Override
        public void onRoomCreated(int statusCode, Room room) {
            Log.d(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");
            if (statusCode != GamesCallbackStatusCodes.OK) {
                Log.e(TAG, "*** Error: onRoomCreated, status " + statusCode);
                showGameError();
                return;
            }

            // save room ID so we can leave cleanly before the game starts.
            mRoomId = room.getRoomId();

            // show the waiting room UI
            showWaitingRoom(room);
        }

        // Called when room is fully connected.
        @Override
        public void onRoomConnected(int statusCode, Room room) {
            Log.d(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");
            if (statusCode != GamesCallbackStatusCodes.OK) {
                Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
                showGameError();
                return;
            }
            updateRoom(room);
        }

        @Override
        public void onJoinedRoom(int statusCode, Room room) {
            Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ")");
            if (statusCode != GamesCallbackStatusCodes.OK) {
                Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
                showGameError();
                return;
            }

            // show the waiting room UI
            showWaitingRoom(room);
        }

        // Called when we've successfully left the room (this happens a result of voluntarily leaving
        // via a call to leaveRoom(). If we get disconnected, we get onDisconnectedFromRoom()).
        @Override
        public void onLeftRoom(int statusCode, @NonNull String roomId) {
            // we have left the room; return to main screen.
            Log.d(TAG, "onLeftRoom, code " + statusCode);
            switchToScreen(R.id.screen_main);
        }
    };

    void updateRoom(Room room) {

        if (room != null) {

            //get the list of participants in the game
            mParticipants = room.getParticipants();
        }
    }

    /*
     * COMMUNICATIONS SECTION. Methods that implement the game's network
     * protocol.
     */

    // Called when we receive a real-time message from the network.
    // Messages in our game are made up of 2 bytes: the first one is 'F' or 'U'
    // indicating
    // whether it's a final or interim score. The second byte is the score.
    // There is also the
    // 'S' message, which indicates that the game should start.
    OnRealTimeMessageReceivedListener mOnRealTimeMessageReceivedListener = new OnRealTimeMessageReceivedListener() {

        @Override
        public void onRealTimeMessageReceived(@NonNull RealTimeMessage realTimeMessage) {

            byte[] buf = realTimeMessage.getMessageData();
            String sender = realTimeMessage.getSenderParticipantId();

            //parse what we are trying to do
            if (buf[0] == 'S') {

                Log.d(TAG, "Message received: " + (char)buf[0] + "/" + (int)buf[1]);

                //if we are starting, determine who goes first
                int tmpRandom = (int)buf[1];

                //if our number is greater than the received number
                PLAYER_1 = (random > tmpRandom);

                //flag that we have now started
                STARTED = true;

            } else if (buf[0] == 'M') {

                Log.d(TAG, "Message received: " + (char)buf[0] + "/" + (int)buf[1] + "," + (int)buf[2] + " " + (int)buf[3] + "," + (int)buf[4]);

                //get the move that was made
                int sCol = (int)buf[1];
                int sRow = (int)buf[2];
                int dCol = (int)buf[3];
                int dRow = (int)buf[4];
                int promoteIndex = (int)buf[5];
                int stateIndex = (int)buf[6];

                //if the game state changed, display it
                if (stateIndex > -1) {

                    //update the state value
                    STATE = PlayerVars.State.values()[stateIndex];

                    //display the game state
                    GameHelper.displayState(getGame(), null);
                }

                //setup the next move to be run
                PlayerHelper.setupMove(getGame(), sCol, sRow, dCol, dRow);

                //if a piece was promoted, we need to promote it
                if (promoteIndex > -1) {

                    //check all the promotion pieces
                    for (int index = 0; index < getGame().getPromotions().size(); index++) {

                        //get the current piece
                        Piece piece = getGame().getPromotions().get(index);

                        //if the type matches, this will be the piece we promote
                        if (piece.getType() == PieceHelper.Type.values()[promoteIndex]) {

                            //make visible for now
                            piece.getObject3D().setVisible(true);

                            //promote immediately
                            PlayerHelper.promote(getGame(), piece);
                            break;
                        }
                    }
                }

            } else if (buf[0] == 'C') {

                int stateIndex = (int)buf[6];

                //if the game state changed, display it
                if (stateIndex > -1) {

                    //update the state value
                    STATE = PlayerVars.State.values()[stateIndex];

                    //display the game state
                    GameHelper.displayState(getGame(), null);
                }
            }
        }
    };

    public void sendGameState() {

        //send to every other participant.
        for (Participant p : mParticipants) {

            //don't send message to self
            if (p.getParticipantId().equals(mMyId))
                continue;

            //ignore players who haven't joined the match
            if (p.getStatus() != Participant.STATUS_JOINED)
                continue;

            //pass 'C' so we know we are changing the state
            mMsgBuf[0] = (byte) 'C';

            for (int i = 0; i < PlayerVars.State.values().length; i++) {
                if (PlayerVars.State.values()[i] == STATE) {
                    mMsgBuf[6] = (byte) i;
                    break;
                }
            }

            //send message to opponent
            sendReliableMessage(p.getParticipantId());
        }
    }

    public void sendMove(int sCol, int sRow, int dCol, int dRow, PieceHelper.Type type) {

        //send to every other participant.
        for (Participant p : mParticipants) {

            //don't send message to self
            if (p.getParticipantId().equals(mMyId))
                continue;

            //ignore players who haven't joined the match
            if (p.getStatus() != Participant.STATUS_JOINED)
                continue;

            //pass 'M' so we know we are making a move
            mMsgBuf[0] = (byte) 'M';

            //store move coordinates
            mMsgBuf[1] = (byte)sCol;
            mMsgBuf[2] = (byte)sRow;
            mMsgBuf[3] = (byte)dCol;
            mMsgBuf[4] = (byte)dRow;
            mMsgBuf[5] = (byte)-1;
            mMsgBuf[6] = (byte)-1;

            //figure out the index
            if (type != null) {
                for (int i = 0; i < PieceHelper.Type.values().length; i++) {
                    if (PieceHelper.Type.values()[i] == type) {
                        mMsgBuf[5] = (byte) i;
                        break;
                    }
                }
            } else {
                mMsgBuf[5] = (byte)-1;
            }

            //if the game is over, pass the value
            if (PlayerVars.isGameover()) {

                /*
                for (int i = 0; i < PlayerVars.State.values().length; i++) {
                    if (PlayerVars.State.values()[i] == STATE) {
                        mMsgBuf[6] = (byte) i;
                        break;
                    }
                }
                */

            } else {
                mMsgBuf[6] = (byte)-1;
            }

            //send message to opponent
            sendReliableMessage(p.getParticipantId());
        }
    }

    protected void selectFirstTurn() {

        //send to every other participant.
        for (Participant p : mParticipants) {

            //don't send message to self
            if (p.getParticipantId().equals(mMyId))
                continue;

            //ignore players who haven't joined the match
            if (p.getStatus() != Participant.STATUS_JOINED)
                continue;

            //pass 'S' so we know we are starting the game
            mMsgBuf[0] = (byte)'S';

            //pick random number to determine who goes first
            mMsgBuf[1] = (byte)this.random;

            //send message to opponent
            sendReliableMessage(p.getParticipantId());

            // it's an interim score notification, so we can use unreliable
            //mRealTimeMultiplayerClient.sendUnreliableMessage(mMsgBuf, mRoomId, p.getParticipantId());
        }
    }

    private void sendReliableMessage(final String participantId) {

        //send message to opponent
        mRealTimeMultiplayerClient.sendReliableMessage(
                mMsgBuf,
                mRoomId,
                participantId,
                new RealTimeMultiplayerClient.ReliableMessageSentCallback() {
                    @Override
                    public void onRealTimeMessageSent(int statusCode, int tokenId, String recipientParticipantId) {
                        Log.d(TAG, "RealTime message sent");
                        Log.d(TAG, "  statusCode: " + statusCode);
                        Log.d(TAG, "  tokenId: " + tokenId);
                        Log.d(TAG, "  recipientParticipantId: " + recipientParticipantId);
                    }
                }).addOnSuccessListener(new OnSuccessListener<Integer>() {
            @Override
            public void onSuccess(Integer tokenId) {
                Log.d(TAG, "Created a reliable message with tokenId: " + tokenId);
            }
        });
    }

    // Sets the flag to keep this screen on. It's recommended to do that during
    // the
    // handshake when setting up a game, because if the screen turns off, the
    // game will be
    // cancelled.
    void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Clears the flag that keeps the screen on.
    void stopKeepingScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * Since a lot of the operations use tasks, we can use a common handler for whenever one fails.
     *
     * @param exception The exception to evaluate.  Will try to display a more descriptive reason for the exception.
     * @param details   Will display alongside the exception if you wish to provide more details for why the exception
     *                  happened
     */
    protected void handleException(Exception exception, String details) {
        int status = 0;

        if (exception instanceof ApiException) {
            ApiException apiException = (ApiException) exception;
            status = apiException.getStatusCode();
        }

        String errorString = null;

        switch (status) {
            case GamesCallbackStatusCodes.OK:
                break;
            case GamesClientStatusCodes.MULTIPLAYER_ERROR_NOT_TRUSTED_TESTER:
                errorString = getString(R.string.status_multiplayer_error_not_trusted_tester);
                break;
            case GamesClientStatusCodes.MATCH_ERROR_ALREADY_REMATCHED:
                errorString = getString(R.string.match_error_already_rematched);
                break;
            case GamesClientStatusCodes.NETWORK_ERROR_OPERATION_FAILED:
                errorString = getString(R.string.network_error_operation_failed);
                break;
            case GamesClientStatusCodes.INTERNAL_ERROR:
                errorString = getString(R.string.internal_error);
                break;
            case GamesClientStatusCodes.MATCH_ERROR_INACTIVE_MATCH:
                errorString = getString(R.string.match_error_inactive_match);
                break;
            case GamesClientStatusCodes.MATCH_ERROR_LOCALLY_MODIFIED:
                errorString = getString(R.string.match_error_locally_modified);
                break;
            default:
                errorString = getString(R.string.unexpected_status, GamesClientStatusCodes.getStatusCodeString(status));
                break;
        }

        if (errorString == null) {
            return;
        }

        String message = getString(R.string.status_exception_error, details, status, exception);

        new AlertDialog.Builder(MultiplayerActivity.this)
            .setTitle("Error")
            .setMessage(message + "\n" + errorString)
            .setNeutralButton(android.R.string.ok, null)
            .show();
    }
}