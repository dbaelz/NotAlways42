package de.dbaelz.na42.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.games.snapshot.Snapshots;

import java.util.Random;

import de.dbaelz.na42.Constants;
import de.dbaelz.na42.MainActivity;
import de.dbaelz.na42.R;
import de.dbaelz.na42.RoundState;
import de.dbaelz.na42.event.GameEndedEvent;
import de.dbaelz.na42.model.SingleplayerSavegame;
import de.greenrobot.event.EventBus;

public class SingleplayerFragment extends Fragment {
    private final int MIN_NUMBER = 1;
    private final int MAX_NUMBER = 5;
    private final int MAX_ROUND = 5;

    private MainActivity mActivity;

    private LinearLayout mGuess;
    private LinearLayout mGameFinished;

    private TextView mTextViewRound;
    private EditText mEditText;
    private Button mGuessButton;

    private TextView mRoundIndicator1;
    private TextView mRoundIndicator2;
    private TextView mRoundIndicator3;
    private TextView mRoundIndicator4;
    private TextView mRoundIndicator5;

    private TextView mGameFinishedText;
    private Button mGameFinishedButton;

    private SingleplayerSavegame mSavegame;
    private int mInvalidNumberCounter;

    public SingleplayerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_singleplayer, container, false);

        mActivity = (MainActivity) getActivity();

        mGuess = (LinearLayout) view.findViewById(R.id.guess);
        mGameFinished = (LinearLayout) view.findViewById(R.id.game_finished);

        mRoundIndicator1 = (TextView) view.findViewById(R.id.round_indicator_1);
        mRoundIndicator2 = (TextView) view.findViewById(R.id.round_indicator_2);
        mRoundIndicator3 = (TextView) view.findViewById(R.id.round_indicator_3);
        mRoundIndicator4 = (TextView) view.findViewById(R.id.round_indicator_4);
        mRoundIndicator5 = (TextView) view.findViewById(R.id.round_indicator_5);

        mTextViewRound = (TextView) view.findViewById(R.id.guess_textview_round);
        mTextViewRound.setText(String.format(getString(R.string.singleplayer_round), 1));

        mEditText = (EditText) view.findViewById(R.id.guess_edittext);

        mGuessButton = (Button) view.findViewById(R.id.guess_button);
        mGuessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.hideSoftKeyboard();
                int inputNumber = 0;
                try {
                    inputNumber = Integer.parseInt(mEditText.getText().toString());
                } catch (NumberFormatException nfe) {
                }

                if (inputNumber >= MIN_NUMBER && inputNumber <= MAX_NUMBER) {
                    handleRound(inputNumber);
                } else {
                    mInvalidNumberCounter++;
                    Toast.makeText(mActivity, getString(R.string.singleplayer_invalid_number), Toast.LENGTH_SHORT).show();
                }
            }
        });

        mGameFinishedText = (TextView) view.findViewById(R.id.game_finished_text);
        mGameFinishedButton = (Button) view.findViewById(R.id.game_finished_button);
        mGameFinishedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameEnded(false);
            }
        });

        Bundle arguments = getArguments();
        if (arguments != null) {
            mSavegame = arguments.getParcelable(Constants.SAVEGAME_PARCEL);
            initLoadedSavegame();
        } else {
            mSavegame = new SingleplayerSavegame(new int[5], 1, 0);
        }
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        GoogleApiClient client = mActivity.getGoogleApiClient();
        if (client != null && client.isConnected()) {
            inflater.inflate(R.menu.singleplayer_signed_in, menu);
        } else {
            inflater.inflate(R.menu.singleplayer, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveGame();
                return true;
            case R.id.action_cancel:
                gameEnded(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void handleRound(int inputNumber) {
        Random r = new Random();
        int randomNumber = r.nextInt(MAX_NUMBER) + 1;

        int currentRound = mSavegame.getCurrentRound();
        if (randomNumber == inputNumber) {
            Toast.makeText(mActivity, getString(R.string.singleplayer_correct_guess), Toast.LENGTH_SHORT).show();
            changeRoundIndicator(currentRound, getResources().getColor(R.color.round_indicator_won));
            mSavegame.setRound(currentRound, RoundState.WON);
            mSavegame.incrementWonRounds();
        } else {
            Toast.makeText(mActivity, getString(R.string.singleplayer_wrong_guess), Toast.LENGTH_SHORT).show();
            changeRoundIndicator(currentRound, getResources().getColor(R.color.round_indicator_lost));
            mSavegame.setRound(currentRound, RoundState.LOST);
        }


        if ((mSavegame.getCurrentRound() + 1) > MAX_ROUND) {
            mGuess.setVisibility(View.GONE);
            mGameFinished.setVisibility(View.VISIBLE);
            boolean hasWon = mSavegame.getWonRounds() >= 3;
            if (hasWon) {
                mGameFinishedText.setText(getString(R.string.game_finished_won));
            } else {
                mGameFinishedText.setText(getString(R.string.game_finished_lost));
            }
            processRewards(hasWon);
        } else {
            mSavegame.incrementCurrentRound();
        }
    }

    private void initLoadedSavegame() {
        for (int i = 1; i < mSavegame.getCurrentRound(); i++) {
            int color;
            int state = mSavegame.getRound(i);
            if (state == 1) {
                color = getResources().getColor(R.color.round_indicator_won);
            } else {
                color = getResources().getColor(R.color.round_indicator_lost);
            }
            changeRoundIndicator(i, color);
        }
        mTextViewRound.setText(String.format(getString(R.string.singleplayer_round), mSavegame.getCurrentRound()));
    }

    private void changeRoundIndicator(int round, int color) {
        switch (round) {
            case 1:
                mRoundIndicator1.setBackgroundColor(color);
                break;
            case 2:
                mRoundIndicator2.setBackgroundColor(color);
                break;
            case 3:
                mRoundIndicator3.setBackgroundColor(color);
                break;
            case 4:
                mRoundIndicator4.setBackgroundColor(color);
                break;
            case 5:
                mRoundIndicator5.setBackgroundColor(color);
                break;
        }
    }

    private void processRewards(boolean hasWonTheGame) {
        GoogleApiClient client = mActivity.getGoogleApiClient();
        if (client != null && client.isConnected()) {
            if (hasWonTheGame) {
                Games.Achievements.unlock(client, getString(R.string.achievement_v_for_victory));
                Games.Achievements.increment(client, getString(R.string.achievement_good_guess), 1);
                Games.Achievements.increment(client, getString(R.string.achievement_persistent_player), 1);
                Games.Achievements.increment(client, getString(R.string.achievement_42), 1);
            } else {
                Games.Achievements.unlock(client, getString(R.string.achievement_with_no_luck));
            }

            // Achievements for won/lost rounds
            int wonRounds = mSavegame.getWonRounds();
            if (wonRounds > 0) {
                Games.Achievements.increment(client, getString(R.string.achievement_clairvoyant), wonRounds);
            }
            if ((MAX_ROUND - wonRounds) > 0) {
                Games.Achievements.increment(client, getString(R.string.achievement_so_unlucky), MAX_ROUND - wonRounds);
            }

            // Event when game finished
            Games.Events.increment(client, getString(R.string.event_finish_a_game), 1);

            // Singleplayer Leaderboard
            int winBonus = hasWonTheGame ? 100 : 0;
            int scoreLeaderboard = wonRounds * 100 + winBonus;
            Games.Leaderboards.submitScore(client, getString(R.string.leaderboard_singleplayer), scoreLeaderboard);
        }
    }

    private void saveGame() {
        if (mSavegame.getRound(5) != RoundState.NOT_PLAYED.getValue()) {
            Toast.makeText(mActivity, getString(R.string.savegame_nosave_game_finished), Toast.LENGTH_SHORT).show();
            return;
        }

        GoogleApiClient client = mActivity.getGoogleApiClient();
        if (client != null && client.isConnected()) {
            new SaveSavegameTask().execute();
        } else {
            Toast.makeText(mActivity, getString(R.string.menu_need_signin), Toast.LENGTH_SHORT).show();
        }
    }

    private void gameEnded(boolean canceled) {
        EventBus.getDefault().post(new GameEndedEvent(canceled));
        GoogleApiClient client = mActivity.getGoogleApiClient();
        if (client != null && client.isConnected()) {
            Games.Events.increment(client, getString(R.string.event_typed_an_invalid_number), mInvalidNumberCounter);
        }
    }

    private class SaveSavegameTask extends AsyncTask<Void, Void, Snapshots.CommitSnapshotResult> {

        @Override
        protected Snapshots.CommitSnapshotResult doInBackground(Void... params) {
            SnapshotMetadataChange metadata = new SnapshotMetadataChange.Builder()
                    .setDescription("Round " + mSavegame.getCurrentRound() + " with " + mSavegame.getWonRounds() + " games won")
                    .build();
            byte[] data = mSavegame.toBytes();
            Snapshots.OpenSnapshotResult openSnapshot = Games.Snapshots.open(mActivity.getGoogleApiClient(), mSavegame.getUUID(), true).await();

            int status = openSnapshot.getStatus().getStatusCode();
            if (status == GamesStatusCodes.STATUS_OK) {
                Snapshot snapshot = openSnapshot.getSnapshot();
                snapshot.writeBytes(data);
                return Games.Snapshots.commitAndClose(mActivity.getGoogleApiClient(), snapshot, metadata).await();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Snapshots.CommitSnapshotResult result) {
            if (result != null) {
                // TODO: Report saving. End game?
                Toast.makeText(mActivity, getString(R.string.savegame_saved), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
