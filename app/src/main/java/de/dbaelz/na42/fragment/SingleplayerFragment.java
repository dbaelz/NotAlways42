package de.dbaelz.na42.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;

import java.util.Random;

import de.dbaelz.na42.MainActivity;
import de.dbaelz.na42.R;
import de.dbaelz.na42.event.GameFinishedEvent;
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

    private int mCurrentRound = 1;
    private int mWonRounds = 0;

    public SingleplayerFragment() {
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
        mTextViewRound.setText(String.format(getString(R.string.singleplayer_round), mCurrentRound));

        mEditText = (EditText) view.findViewById(R.id.guess_edittext);

        mGuessButton = (Button) view.findViewById(R.id.guess_button);
        mGuessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.hideSoftKeyboard();
                int inputNumber = 0;
                try {
                    inputNumber = Integer.parseInt(mEditText.getText().toString());
                } catch(NumberFormatException nfe) {
                }

                if (inputNumber >= MIN_NUMBER && inputNumber <= MAX_NUMBER) {
                    handleRound(inputNumber);
                } else {
                    Toast.makeText(mActivity, getString(R.string.singleplayer_invalid_number), Toast.LENGTH_SHORT).show();
                }
            }
        });

        mGameFinishedText = (TextView) view.findViewById(R.id.game_finished_text);
        mGameFinishedButton = (Button) view.findViewById(R.id.game_finished_button);
        mGameFinishedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new GameFinishedEvent());
            }
        });

        return view;
    }

    private void handleRound(int inputNumber) {
        Random r = new Random();
        int randomNumber = r.nextInt(MAX_NUMBER) + 1;

        // TODO: Make it more appealing!
        if (randomNumber == inputNumber) {
            Toast.makeText(mActivity, getString(R.string.singleplayer_correct_guess), Toast.LENGTH_SHORT).show();
            changeRoundIndicator(mCurrentRound, getResources().getColor(R.color.round_indicator_won));
            mWonRounds++;
        } else {
            Toast.makeText(mActivity, getString(R.string.singleplayer_wrong_guess), Toast.LENGTH_SHORT).show();
            changeRoundIndicator(mCurrentRound, getResources().getColor(R.color.round_indicator_lost));
        }

        mCurrentRound++;
        if (mCurrentRound > MAX_ROUND) {
            mGuess.setVisibility(View.GONE);
            mGameFinished.setVisibility(View.VISIBLE);
            boolean hasWon = mWonRounds >= 3;
            if (hasWon) {
                mGameFinishedText.setText(getString(R.string.game_finished_won));
            } else {
                mGameFinishedText.setText(getString(R.string.game_finished_lost));
            }
            processRewards(hasWon, mWonRounds);
        }
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

    private void processRewards(boolean hasWonTheGame, int wonRounds) {
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
            if (wonRounds > 0) {
                Games.Achievements.increment(client, getString(R.string.achievement_clairvoyant), wonRounds);
            }
            if ((MAX_ROUND - wonRounds) > 0) {
                Games.Achievements.increment(client, getString(R.string.achievement_so_unlucky), MAX_ROUND - wonRounds);
            }

            // Singleplayer Leaderboard
            int winBonus = hasWonTheGame ? 100 : 0;
            int scoreLeaderboard = wonRounds * 100 + winBonus;
            Games.Leaderboards.submitScore(client, getString(R.string.leaderboard_singleplayer), scoreLeaderboard);
        } else {
            // TODO: Alternative handling (e.g. save local until player is connected)
        }
    }
}
