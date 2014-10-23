/*
 * Copyright 2014 Daniel Bälz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.dbaelz.na42.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdateReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;

import java.util.ArrayList;
import java.util.Random;

import de.dbaelz.na42.Constants;
import de.dbaelz.na42.MainActivity;
import de.dbaelz.na42.R;
import de.dbaelz.na42.RoundState;
import de.dbaelz.na42.event.GameEndedEvent;
import de.dbaelz.na42.model.MultiplayerData;
import de.greenrobot.event.EventBus;

public class MultiplayerFragment extends Fragment implements OnTurnBasedMatchUpdateReceivedListener, OnInvitationReceivedListener {
    private final int MIN_NUMBER = 1;
    private final int MAX_NUMBER = 5;
    private final int MAX_ROUND = 5;

    private MainActivity mActivity;

    private LinearLayout mMultiplayerSelection;
    private LinearLayout mMultiplayerGame;

    private Button mSelectionNewButton;
    private Button mSelectionSearchButton;

    private TextView mPlayer1Name;
    private TextView[] mPlayer1RoundIndicators;

    private TextView mPlayer2Name;
    private TextView[] mPlayer2RoundIndicators;

    private LinearLayout mLayoutInput;
    private TextView mTextViewRound;
    private EditText mEditText;
    private Button mSendButton;

    private LinearLayout mLayoutGameFinished;
    private TextView mGameFinishedText;
    private Button mGameFinishedButton;

    private MultiplayerData mData;

    public MultiplayerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_multiplayer, container, false);

        mActivity = (MainActivity) getActivity();

        mMultiplayerSelection = (LinearLayout) view.findViewById(R.id.multiplayer_selection);
        mMultiplayerGame = (LinearLayout) view.findViewById(R.id.multiplayer_layout);

        mPlayer1Name = (TextView) view.findViewById(R.id.multiplayer_name_player1);
        View scoreboardUser = view.findViewById(R.id.multiplayer_scorboard_player1);
        mPlayer1RoundIndicators = new TextView[MAX_ROUND];
        mPlayer1RoundIndicators[0] = (TextView) scoreboardUser.findViewById(R.id.round_indicator_1);
        mPlayer1RoundIndicators[1] = (TextView) scoreboardUser.findViewById(R.id.round_indicator_2);
        mPlayer1RoundIndicators[2] = (TextView) scoreboardUser.findViewById(R.id.round_indicator_3);
        mPlayer1RoundIndicators[3] = (TextView) scoreboardUser.findViewById(R.id.round_indicator_4);
        mPlayer1RoundIndicators[4] = (TextView) scoreboardUser.findViewById(R.id.round_indicator_5);

        mPlayer2Name = (TextView) view.findViewById(R.id.multiplayer_name_player2);
        View scoreboardOpponent = view.findViewById(R.id.multiplayer_scorboard_player2);
        mPlayer2RoundIndicators = new TextView[MAX_ROUND];
        mPlayer2RoundIndicators[0] = (TextView) scoreboardOpponent.findViewById(R.id.round_indicator_1);
        mPlayer2RoundIndicators[1] = (TextView) scoreboardOpponent.findViewById(R.id.round_indicator_2);
        mPlayer2RoundIndicators[2] = (TextView) scoreboardOpponent.findViewById(R.id.round_indicator_3);
        mPlayer2RoundIndicators[3] = (TextView) scoreboardOpponent.findViewById(R.id.round_indicator_4);
        mPlayer2RoundIndicators[4] = (TextView) scoreboardOpponent.findViewById(R.id.round_indicator_5);

        mLayoutInput = (LinearLayout) view.findViewById(R.id.guess);
        mLayoutInput.setVisibility(View.GONE);

        mTextViewRound = (TextView) view.findViewById(R.id.guess_textview_round);
        mEditText = (EditText) view.findViewById(R.id.guess_edittext);
        mSendButton = (Button) view.findViewById(R.id.guess_button);

        mEditText.setHint(mActivity.getString(R.string.guess_hint));
        mSendButton.setText(mActivity.getString(R.string.guess_button_text));

        mLayoutGameFinished = (LinearLayout) view.findViewById(R.id.game_finished);
        mGameFinishedText = (TextView) view.findViewById(R.id.game_finished_text);
        mGameFinishedButton = (Button) view.findViewById(R.id.game_finished_button);
        mGameFinishedButton.setText(R.string.multiplayer_end_game);

        //TODO: Handle game finished
        mGameFinishedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new GameEndedEvent(false));
            }
        });

        mSelectionNewButton = (Button) mMultiplayerSelection.findViewById(R.id.multiplayer_selection_new);
        mSelectionNewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = Games.TurnBasedMultiplayer.getSelectOpponentsIntent(mActivity.getGoogleApiClient(), 1, 1, false);
                startActivityForResult(intent, Constants.REQUEST_CODE_MP_NEW_GAME);
            }
        });
        mSelectionSearchButton = (Button) mMultiplayerSelection.findViewById(R.id.multiplayer_selection_search);
        mSelectionSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = Games.TurnBasedMultiplayer.getInboxIntent(mActivity.getGoogleApiClient());
                startActivityForResult(intent, Constants.REQUEST_CODE_MP_SEARCH_GAME);
            }
        });

        mMultiplayerSelection.setVisibility(View.VISIBLE);
        mMultiplayerGame.setVisibility(View.GONE);

        GoogleApiClient client = mActivity.getGoogleApiClient();
        if (client.isConnected()) {
            Games.Invitations.registerInvitationListener(client, this);
            Games.TurnBasedMultiplayer.registerMatchUpdateListener(client, this);
        }
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.multiplayer, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_cancel:
                EventBus.getDefault().post(new GameEndedEvent(false));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CODE_MP_NEW_GAME) {
            if (resultCode != Activity.RESULT_OK) {
                Log.d(Constants.LOG_TAG, "RESULTCODE != OK");
                return;
            }

            final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
            Bundle autoMatchCriteria = null;

            int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
            int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
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
                            startNewGame(result);
                        }
                    });
        } else if (requestCode == Constants.REQUEST_CODE_MP_SEARCH_GAME) {
            if (resultCode != Activity.RESULT_OK) {
                Log.d(Constants.LOG_TAG, "RESULTCODE != OK");
                return;
            }

            TurnBasedMatch match = data.getParcelableExtra(Multiplayer.EXTRA_TURN_BASED_MATCH);

            if (match != null) {
                updateGame(match);
                switchMenuAndGame(true);
            }
        }
    }

    @Override
    public void onInvitationReceived(Invitation invitation) {
        Log.d(Constants.LOG_TAG, "INVITATION RECEIVED");
    }

    @Override
    public void onInvitationRemoved(String s) {
        Log.d(Constants.LOG_TAG, "INVITATION REMOVED");
    }

    @Override
    public void onTurnBasedMatchReceived(TurnBasedMatch turnBasedMatch) {
        // May not fired...
        // See issue https://code.google.com/p/play-games-platform/issues/detail?id=128
        Log.d(Constants.LOG_TAG, "MATCH RECEIVED");
        updateGame(turnBasedMatch);
    }

    @Override
    public void onTurnBasedMatchRemoved(String s) {
        Log.d(Constants.LOG_TAG, "MATCH REMOVED");
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

            ArrayList<String> participantIds = match.getParticipantIds();
            String nextParticipantId = "";
            for (String participantId : participantIds) {
                if (participantId != myParticipantId) {
                    nextParticipantId = participantId;
                    break;
                }
            }
            String player1Name = match.getParticipant(myParticipantId).getDisplayName();
            String player2Name = "";
            if (!nextParticipantId.isEmpty()) {
                player2Name = match.getParticipant(nextParticipantId).getDisplayName();
            }

            MultiplayerData initialData = new MultiplayerData(MAX_ROUND, myParticipantId, nextParticipantId, player1Name, player2Name);

            mPlayer1Name.setText(initialData.getPlayer1Name());
            mPlayer2Name.setText(initialData.getPlayer2Name());
            changeRoundIndicators(initialData);

            takeTurn(match.getMatchId(), initialData.toBytes(), nextParticipantId);
        }
    }

    private void updateGame(TurnBasedMatch match) {
        int status = match.getStatus();
        int turnStatus = match.getTurnStatus();

        switch (status) {
            case TurnBasedMatch.MATCH_STATUS_AUTO_MATCHING:
                Toast.makeText(mActivity, mActivity.getString(R.string.multiplayer_match_automatching), Toast.LENGTH_SHORT).show();
                return;
            case TurnBasedMatch.MATCH_STATUS_EXPIRED:
                Toast.makeText(mActivity, mActivity.getString(R.string.multiplayer_match_expired), Toast.LENGTH_SHORT).show();
                return;
            case TurnBasedMatch.MATCH_STATUS_CANCELED:
                Toast.makeText(mActivity, mActivity.getString(R.string.multiplayer_match_canceled), Toast.LENGTH_SHORT).show();
                return;
            case TurnBasedMatch.MATCH_STATUS_COMPLETE:
                if (turnStatus == TurnBasedMatch.MATCH_TURN_STATUS_COMPLETE) {
                    Toast.makeText(mActivity, mActivity.getString(R.string.multiplayer_match_complete), Toast.LENGTH_SHORT).show();
                    EventBus.getDefault().post(new GameEndedEvent(false));
                }
                break;
            default:
        }

        if (turnStatus == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN) {
            playTurn(match);
        } else {
            mData = new MultiplayerData(match.getData());
            mPlayer1Name.setText(mData.getPlayer1Name());
            mPlayer2Name.setText(mData.getPlayer2Name());
            changeRoundIndicators(mData);
            mLayoutInput.setVisibility(View.GONE);
        }
    }


    private void playTurn(final TurnBasedMatch match) {
        mData = new MultiplayerData(match.getData());
        Log.d(Constants.LOG_TAG, "Play my turn!");
        Log.d(Constants.LOG_TAG, mData.toString());

        mPlayer1Name.setText(mData.getPlayer1Name());
        mPlayer2Name.setText(mData.getPlayer2Name());
        changeRoundIndicators(mData);

        String playerId = Games.Players.getCurrentPlayerId(mActivity.getGoogleApiClient());
        String myParticipantId = match.getParticipantId(playerId);
        final boolean isPlayer1 = myParticipantId.equals(mData.getPlayer1ID());

        int currentRound = isPlayer1 ? mData.getCurrentRoundPlayer1() : mData.getCurrentRoundPlayer2();
        if (isPlayer1 && mData.getCurrentRoundPlayer1() > MAX_ROUND) {
            mGameFinishedButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Games.TurnBasedMultiplayer.finishMatch(mActivity.getGoogleApiClient(), match.getMatchId())
                            .setResultCallback(new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                                @Override
                                public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
                                    processResult(result);
                                }
                            });
                }
            });
            mGameFinishedText.setText(String.format(mActivity.getString(R.string.multiplayer_winner), mData.getWinnerName()));
            mLayoutGameFinished.setVisibility(View.VISIBLE);
            return;
        } else if (!isPlayer1 && mData.getCurrentRoundPlayer2() > MAX_ROUND) {
            mGameFinishedButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Games.TurnBasedMultiplayer.finishMatch(mActivity.getGoogleApiClient(), match.getMatchId())
                            .setResultCallback(new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                                @Override
                                public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
                                    processResult(result);
                                }
                            });
                }
            });
            mGameFinishedText.setText(String.format(mActivity.getString(R.string.multiplayer_winner), mData.getWinnerName()));
            mLayoutGameFinished.setVisibility(View.VISIBLE);
            return;
        }


        mTextViewRound.setText(String.format(mActivity.getString(R.string.singleplayer_round), currentRound));
        mLayoutInput.setVisibility(View.VISIBLE);
        switchMenuAndGame(true);
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
                    Random r = new Random();
                    int randomNumber = r.nextInt(MAX_NUMBER) + 1;

                    boolean hasWon = randomNumber == inputNumber;
                    if (hasWon) {
                        Toast.makeText(mActivity, mActivity.getString(R.string.singleplayer_correct_guess), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mActivity, mActivity.getString(R.string.singleplayer_wrong_guess), Toast.LENGTH_SHORT).show();
                    }

                    RoundState state = hasWon ? RoundState.WON : RoundState.LOST;
                    String nextParticipant;
                    if (isPlayer1) {
                        mData.setResultPlayer1(mData.getCurrentRoundPlayer1(), state);
                        mData.incrementRoundPlayer1();
                        nextParticipant = mData.getPlayer2ID();
                    } else {
                        mData.setResultPlayer2(mData.getCurrentRoundPlayer2(), state);
                        mData.incrementRoundPlayer2();
                        nextParticipant = mData.getPlayer1ID();
                    }

                    changeRoundIndicators(mData);
                    mLayoutInput.setVisibility(View.GONE);
                    takeTurn(match.getMatchId(), mData.toBytes(), nextParticipant);
                } else {
                    Toast.makeText(mActivity, mActivity.getString(R.string.singleplayer_invalid_number), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void processResult(TurnBasedMultiplayer.UpdateMatchResult result) {
        TurnBasedMatch match = result.getMatch();
        int status = result.getStatus().getStatusCode();
        if (status != GamesStatusCodes.STATUS_OK && status != GamesStatusCodes.STATUS_NETWORK_ERROR_OPERATION_DEFERRED) {
            return;
        }

        int turnStatus = match.getTurnStatus();
        if (turnStatus == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN) {
            playTurn(match);
        } else if (turnStatus == TurnBasedMatch.MATCH_TURN_STATUS_COMPLETE) {
            EventBus.getDefault().post(new GameEndedEvent(false));
        }
    }

    private void takeTurn(String matchId, byte[] data, String participantId) {
        Games.TurnBasedMultiplayer.takeTurn(mActivity.getGoogleApiClient(), matchId, data, participantId).setResultCallback(
                new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
                        mMultiplayerGame.setVisibility(View.VISIBLE);
                        processResult(result);
                    }
                });
    }

    private void changeRoundIndicators(MultiplayerData data) {
        int[] resultsPlayer1 = data.getResultPlayer1();
        for (int i = 0; i < resultsPlayer1.length; i++) {
            int color = mActivity.getResources().getColor(R.color.round_indicator_default);
            if (resultsPlayer1[i] == RoundState.WON.getValue()) {
                color = mActivity.getResources().getColor(R.color.round_indicator_won);
            } else if (resultsPlayer1[i] == RoundState.LOST.getValue()) {
                color = mActivity.getResources().getColor(R.color.round_indicator_lost);
            }
            mPlayer1RoundIndicators[i].setBackgroundColor(color);
        }

        int[] resultsPlayer2 = data.getResultPlayer2();
        for (int i = 0; i < resultsPlayer2.length; i++) {
            int color = mActivity.getResources().getColor(R.color.round_indicator_default);
            if (resultsPlayer2[i] == RoundState.WON.getValue()) {
                color = mActivity.getResources().getColor(R.color.round_indicator_won);
            } else if (resultsPlayer2[i] == RoundState.LOST.getValue()) {
                color = mActivity.getResources().getColor(R.color.round_indicator_lost);
            }
            mPlayer2RoundIndicators[i].setBackgroundColor(color);
        }
    }

    private void switchMenuAndGame(boolean showGame) {
        if (showGame) {
            mMultiplayerSelection.setVisibility(View.GONE);
            mMultiplayerGame.setVisibility(View.VISIBLE);
        } else {
            mMultiplayerSelection.setVisibility(View.VISIBLE);
            mMultiplayerGame.setVisibility(View.GONE);
        }
    }
}
