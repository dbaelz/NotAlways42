package de.dbaelz.na42.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;

import java.util.ArrayList;

import de.dbaelz.na42.Constants;
import de.dbaelz.na42.MainActivity;
import de.dbaelz.na42.R;
import de.dbaelz.na42.RoundState;
import de.dbaelz.na42.event.GameEndedEvent;
import de.dbaelz.na42.model.MultiplayerData;
import de.greenrobot.event.EventBus;

public class MultiplayerFragment extends Fragment {
    private final int MIN_NUMBER = 1;
    private final int MAX_NUMBER = 5;
    private final int MAX_ROUND = 5;

    private MainActivity mActivity;

    private LinearLayout mMultiplayerGame;

    private TextView mUserName;
    private TextView[] mUserRoundIndicators;

    private TextView mOpponentName;
    private TextView[] mOpponentRoundIndicators;

    private LinearLayout mLayoutInput;
    private TextView mTextViewRound;
    private EditText mEditText;
    private Button mSendButton;

    private TextView mGameFinishedText;
    private Button mGameFinishedButton;

    public MultiplayerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_multiplayer, container, false);

        mActivity = (MainActivity) getActivity();

        mMultiplayerGame = (LinearLayout) view.findViewById(R.id.multiplayer_layout);

        mUserName = (TextView) view.findViewById(R.id.multiplayer_name_user);
        View scoreboardUser = view.findViewById(R.id.multiplayer_scorboard_user);
        mUserRoundIndicators = new TextView[MAX_ROUND];
        mUserRoundIndicators[0] = (TextView) scoreboardUser.findViewById(R.id.round_indicator_1);
        mUserRoundIndicators[1] = (TextView) scoreboardUser.findViewById(R.id.round_indicator_2);
        mUserRoundIndicators[2] = (TextView) scoreboardUser.findViewById(R.id.round_indicator_3);
        mUserRoundIndicators[3] = (TextView) scoreboardUser.findViewById(R.id.round_indicator_4);
        mUserRoundIndicators[4] = (TextView) scoreboardUser.findViewById(R.id.round_indicator_5);

        mOpponentName = (TextView) view.findViewById(R.id.multiplayer_name_opponent);
        View scoreboardOpponent = view.findViewById(R.id.multiplayer_scorboard_opponent);
        mOpponentRoundIndicators = new TextView[MAX_ROUND];
        mOpponentRoundIndicators[0] = (TextView) scoreboardOpponent.findViewById(R.id.round_indicator_1);
        mOpponentRoundIndicators[1] = (TextView) scoreboardOpponent.findViewById(R.id.round_indicator_2);
        mOpponentRoundIndicators[2] = (TextView) scoreboardOpponent.findViewById(R.id.round_indicator_3);
        mOpponentRoundIndicators[3] = (TextView) scoreboardOpponent.findViewById(R.id.round_indicator_4);
        mOpponentRoundIndicators[4] = (TextView) scoreboardOpponent.findViewById(R.id.round_indicator_5);

        mLayoutInput = (LinearLayout) view.findViewById(R.id.guess);
        mLayoutInput.setVisibility(View.GONE);

        mTextViewRound = (TextView) view.findViewById(R.id.guess_textview_round);
        mEditText = (EditText) view.findViewById(R.id.guess_edittext);

        mSendButton = (Button) view.findViewById(R.id.guess_button);

        mGameFinishedText = (TextView) view.findViewById(R.id.game_finished_text);
        mGameFinishedButton = (Button) view.findViewById(R.id.game_finished_button);
        mGameFinishedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new GameEndedEvent(false));
            }
        });

        mMultiplayerGame.setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Intent intent = Games.TurnBasedMultiplayer.getSelectOpponentsIntent(mActivity.getGoogleApiClient(), 1, 1, true);
        startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_PLAYER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CODE_SELECT_PLAYER) {
            if (resultCode != Activity.RESULT_OK) {
                Log.d("TEST", "RESULTCODE != OK");
                return;
            }

            final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

            Bundle autoMatchCriteria = null;

            int minAutoMatchPlayers = data.getIntExtra(
                    Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
            int maxAutoMatchPlayers = data.getIntExtra(
                    Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

            if (minAutoMatchPlayers > 0) {
                autoMatchCriteria = RoomConfig.createAutoMatchCriteria(minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            } else {
                autoMatchCriteria = null;
            }

            TurnBasedMatchConfig config = TurnBasedMatchConfig.builder().addInvitedPlayers(invitees)
                    .setAutoMatchCriteria(autoMatchCriteria).build();

            Games.TurnBasedMultiplayer.createMatch(mActivity.getGoogleApiClient(), config).setResultCallback(
                    new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
                        @Override
                        public void onResult(TurnBasedMultiplayer.InitiateMatchResult result) {
                            // TODO: Wait if automatch and no other player
                            startNewGame(result);
                        }
                    });
            // TODO: Loading indicator
        }
    }

    private void startNewGame(TurnBasedMultiplayer.InitiateMatchResult result) {
        TurnBasedMatch match = result.getMatch();

        int status = result.getStatus().getStatusCode();
        if (status != GamesStatusCodes.STATUS_OK && status != GamesStatusCodes.STATUS_NETWORK_ERROR_OPERATION_DEFERRED) {
            return;
        }

        // Initial setup
        if (match.getData() == null) {
            GoogleApiClient client = mActivity.getGoogleApiClient();

            String playerId = Games.Players.getCurrentPlayerId(client);
            String myParticipantId = match.getParticipantId(playerId);

            mUserName.setText(match.getParticipant(myParticipantId).getDisplayName());
            mOpponentName.setText("Opponent");

            ArrayList<String> participantIds = match.getParticipantIds();
            String nextPlayerId = "";
            for (String participantId : participantIds) {
                if (participantId != myParticipantId) {
                    nextPlayerId = participantId;
                    return;
                }
            }

            MultiplayerData initialData = new MultiplayerData(MAX_ROUND, myParticipantId, nextPlayerId);
            Games.TurnBasedMultiplayer.takeTurn(client, match.getMatchId(), initialData.toBytes(), myParticipantId).setResultCallback(
                    new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                        @Override
                        public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
                            mMultiplayerGame.setVisibility(View.VISIBLE);
                            processTurn(result);
                        }
            });
        }
    }

    private void processTurn(TurnBasedMultiplayer.UpdateMatchResult result) {
        final TurnBasedMatch match = result.getMatch();
        if (match.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN) {
            Log.d("TEST", "MY TURN!");

            final MultiplayerData data = new MultiplayerData(match.getData());
            Log.d("TEST", data.toString());

            changeRoundIndicators(data);
            final boolean isChallenge = data.getCurrentChallenge() == -1;
            if (isChallenge) {
                // CHALLENGE
                Log.d("TEST", "CHALLENGE");
                mTextViewRound.setText(String.format(getString(R.string.multiplayer_round_challenge), data.getCurrentRound()));
                mEditText.setHint(getString(R.string.challenge_hint));
                mSendButton.setText(getString(R.string.challenge_button_text));
                mLayoutInput.setVisibility(View.VISIBLE);
            } else {
                // GUESS
                Log.d("TEST", "GUESS");
                mTextViewRound.setText(String.format(getString(R.string.multiplayer_round_guess), data.getCurrentRound()));
                mEditText.setHint(getString(R.string.guess_hint));
                mSendButton.setText(getString(R.string.guess_button_text));
                mLayoutInput.setVisibility(View.VISIBLE);
            }

            String playerId = Games.Players.getCurrentPlayerId(mActivity.getGoogleApiClient());
            final String myParticipantId = match.getParticipantId(playerId);

            mSendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivity.hideSoftKeyboard();
                    int inputNumber = 0;
                    try {
                        inputNumber = Integer.parseInt(mEditText.getText().toString());
                    } catch (NumberFormatException nfe) {
                    }

                    if (inputNumber >= MIN_NUMBER && inputNumber <= MAX_NUMBER) {
                        if (isChallenge) {
                            data.setCurrentChallenge(inputNumber);
                        } else {
                            int round = data.getCurrentRound();
                            boolean correctGuess = (inputNumber == data.getCurrentChallenge()) ? true : false;
                            if (myParticipantId.equals(data.getPlayer1ID())) {
                                if (correctGuess) {
                                    data.setResultPlayer1(round, RoundState.WON);
                                } else {
                                    data.setResultPlayer1(round, RoundState.LOST);
                                }
                            } else {
                                if (correctGuess) {
                                    data.setResultPlayer2(round, RoundState.WON);
                                } else {
                                    data.setResultPlayer2(round, RoundState.LOST);
                                }
                            }
                            data.setCurrentChallenge(-1);
                        }

                        String nextParticipant;
                        if (myParticipantId.equals(data.getPlayer1ID())) {
                            nextParticipant = data.getPlayer2ID();
                        } else {
                            nextParticipant = data.getPlayer1ID();
                        }
                        Games.TurnBasedMultiplayer.takeTurn(mActivity.getGoogleApiClient(), match.getMatchId(), data.toBytes(), nextParticipant).setResultCallback(
                                new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                                    @Override
                                    public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
                                        processTurn(result);
                                    }
                                });
                    } else {
                        Toast.makeText(mActivity, getString(R.string.singleplayer_invalid_number), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            mLayoutInput.setVisibility(View.GONE);
        }
    }

    private void changeRoundIndicators(MultiplayerData data) {
        int[] resultsPlayer1 = data.getResultPlayer1();
        for (int i = 0; i < resultsPlayer1.length; i++) {
            int color = getResources().getColor(R.color.round_indicator_default);
            if (resultsPlayer1[i] == RoundState.WON.getValue()) {
                color = getResources().getColor(R.color.round_indicator_won);
            } else if (resultsPlayer1[i] == RoundState.LOST.getValue()) {
                color = getResources().getColor(R.color.round_indicator_lost);
            }
            mUserRoundIndicators[i].setBackgroundColor(color);
        }

        int[] resultsPlayer2 = data.getResultPlayer2();
        for (int i = 0; i < resultsPlayer2.length; i++) {
            int color = getResources().getColor(R.color.round_indicator_default);
            if (resultsPlayer2[i] == RoundState.WON.getValue()) {
                color = getResources().getColor(R.color.round_indicator_won);
            } else if (resultsPlayer2[i] == RoundState.LOST.getValue()) {
                color = getResources().getColor(R.color.round_indicator_lost);
            }
            mOpponentRoundIndicators[i].setBackgroundColor(color);
        }
    }
}
