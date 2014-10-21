package de.dbaelz.na42.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.quest.Quest;
import com.google.android.gms.games.quest.QuestUpdateListener;
import com.google.android.gms.games.quest.Quests;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.Snapshots;

import org.json.JSONException;
import org.json.JSONObject;

import de.dbaelz.na42.Constants;
import de.dbaelz.na42.MainActivity;
import de.dbaelz.na42.R;
import de.dbaelz.na42.event.GoogleApiClientEvent;
import de.dbaelz.na42.model.SingleplayerSavegame;
import de.greenrobot.event.EventBus;

public class MenuFragment extends Fragment implements View.OnClickListener, QuestUpdateListener, ResultCallback<Quests.ClaimMilestoneResult> {
    private MainActivity mActivity;

    private Button mSingleplayer;
    private Button mLoadSavegame;
    private Button mMultiplayer;
    private Button mQuests;
    private Button mAchievement;
    private Button mLeaderboard;
    private SignInButton mSignInButton;
    private Button mSignOutButton;

    private ProgressDialog mProgressDialog;

    public MenuFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        mSingleplayer = (Button) view.findViewById(R.id.menu_singleplayer);
        mLoadSavegame = (Button) view.findViewById(R.id.menu_load_savegame);
        mMultiplayer = (Button) view.findViewById(R.id.menu_multiplayer);
        mQuests = (Button) view.findViewById(R.id.menu_quests);
        mAchievement = (Button) view.findViewById(R.id.menu_achievement);
        mLeaderboard = (Button) view.findViewById(R.id.menu_leaderboard);
        mSignInButton = (SignInButton) view.findViewById(R.id.menu_signin);
        mSignOutButton = (Button) view.findViewById(R.id.menu_signout);

        mSingleplayer.setOnClickListener(this);
        mLoadSavegame.setOnClickListener(this);
        mMultiplayer.setOnClickListener(this);
        mQuests.setOnClickListener(this);
        mAchievement.setOnClickListener(this);
        mLeaderboard.setOnClickListener(this);
        mSignInButton.setOnClickListener(this);
        mSignOutButton.setOnClickListener(this);

        mActivity = (MainActivity) getActivity();
        switchSignButton(mActivity.getGoogleApiClient().isConnected());

        mProgressDialog = new ProgressDialog(mActivity);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onClick(View view) {
        boolean isConnected = mActivity.getGoogleApiClient().isConnected();
        switch (view.getId()) {
            case R.id.menu_singleplayer:
                if (isConnected) {
                    mActivity.getSupportFragmentManager().beginTransaction().replace(R.id.container, new SingleplayerFragment()).commit();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                    builder.setMessage(mActivity.getString(R.string.menu_singleplayer_alert))
                            .setTitle(mActivity.getString(R.string.menu_singleplayer_alert_title));
                    builder.setPositiveButton(R.string.menu_singleplayer_without_signin_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mActivity.getSupportFragmentManager().beginTransaction().replace(R.id.container, new SingleplayerFragment()).commit();
                        }
                    }).setNegativeButton(R.string.menu_singleplayer_without_signin_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                break;
            case R.id.menu_load_savegame:
                if (isConnected) {
                    Intent savedGamesIntent = Games.Snapshots.getSelectSnapshotIntent(mActivity.getGoogleApiClient(), "Savegames", false, true, 10);
                    startActivityForResult(savedGamesIntent, Constants.REQUEST_CODE_SELECT_SAVEGAME);
                } else {
                    Toast.makeText(mActivity, getString(R.string.menu_need_signin), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_multiplayer:
                if (isConnected) {
                    mActivity.getSupportFragmentManager().beginTransaction().replace(R.id.container, new MultiplayerFragment()).commit();
                } else {
                    Toast.makeText(mActivity, getString(R.string.menu_need_signin), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_quests:
                if (isConnected) {
                    Intent questsIntent = Games.Quests.getQuestsIntent(mActivity.getGoogleApiClient(), Quest.QUEST_STATE_ALL);
                    startActivityForResult(questsIntent, 0);
                } else {
                    Toast.makeText(mActivity, getString(R.string.menu_need_signin), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_achievement:
                if (isConnected) {
                    startActivityForResult(Games.Achievements.getAchievementsIntent(mActivity.getGoogleApiClient()), 0);
                } else {
                    Toast.makeText(mActivity, getString(R.string.menu_need_signin), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_leaderboard:
                if (isConnected) {
                    startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(mActivity.getGoogleApiClient()), 0);
                } else {
                    Toast.makeText(mActivity, getString(R.string.menu_need_signin), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_signin:
                showProgressDialog();
                mActivity.getGoogleApiClient().connect();
                break;
            case R.id.menu_signout:
                EventBus.getDefault().postSticky(new GoogleApiClientEvent(false));
                mActivity.getGoogleApiClient().disconnect();
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_SELECT_SAVEGAME) {
            if (data != null) {
                if (data.hasExtra(Snapshots.EXTRA_SNAPSHOT_METADATA)) {
                    SnapshotMetadata snapshotMetadata = data.getParcelableExtra(Snapshots.EXTRA_SNAPSHOT_METADATA);
                    new LoadSavegameTask(snapshotMetadata).execute();
                }
            } else {

            }
        } else if (requestCode == Constants.REQUEST_CODE_LOAD_SAVEGAME) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null && data.hasExtra(Constants.SNAPSHOT_METADATA)) {

                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onQuestCompleted(Quest quest) {
        Toast.makeText(mActivity, String.format(mActivity.getString(R.string.menu_quest_completed), quest.getName()), Toast.LENGTH_SHORT).show();
        GoogleApiClient client = mActivity.getGoogleApiClient();
        if (client != null && client.isConnected()){
            Games.Quests.claim(client, quest.getQuestId(), quest.getCurrentMilestone().getMilestoneId())
                    .setResultCallback(this);
        }
    }

    @Override
    public void onResult(Quests.ClaimMilestoneResult result) {
        if (result.getStatus().isSuccess()) {
            String jsonString = new String(result.getQuest().getCurrentMilestone().getCompletionRewardData());
            try {
                JSONObject object = new JSONObject(jsonString);
                String reward = object.getString("item");
                Toast.makeText(mActivity, String.format(mActivity.getString(R.string.menu_quest_reward), reward), Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                Log.e(Constants.LOG_TAG, "JSON syntax error!");
            }
        }
    }

    public void onEvent(GoogleApiClientEvent event) {
        mProgressDialog.dismiss();
        switchSignButton(event.isConnected());
        if (event.isConnected() && mActivity.getGoogleApiClient() != null) {
            Games.Quests.registerQuestUpdateListener(mActivity.getGoogleApiClient(), this);
        }
    }

    private void showProgressDialog() {
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(getString(R.string.menu_progress));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
    }

    private void switchSignButton(boolean connected) {
        if (connected) {
            mSignInButton.setVisibility(View.GONE);
            mSignOutButton.setVisibility(View.VISIBLE);
        } else {
            mSignOutButton.setVisibility(View.GONE);
            mSignInButton.setVisibility(View.VISIBLE);
        }
    }

    private class LoadSavegameTask extends AsyncTask<Void, Void, SnapshotRequest> {
        private final SnapshotMetadata mMetadata;

        public LoadSavegameTask(SnapshotMetadata metadata) {
            this.mMetadata = metadata;
        }

        @Override
        protected SnapshotRequest doInBackground(Void... params) {
            Snapshots.OpenSnapshotResult result = null;
            if (mMetadata != null) {
                result = Games.Snapshots.open(mActivity.getGoogleApiClient(), mMetadata).await();
            }

            SnapshotRequest request = new SnapshotRequest();
            request.status = result.getStatus().getStatusCode();

            if (request.status == GamesStatusCodes.STATUS_OK) {
                request.snapshot = result.getSnapshot();
            }
            return request;
        }

        @Override
        protected void onPostExecute(SnapshotRequest snapshotRequest) {
            if (snapshotRequest.status == GamesStatusCodes.STATUS_OK && snapshotRequest.snapshot != null) {
                SingleplayerSavegame savegame = new SingleplayerSavegame(snapshotRequest.snapshot.readFully());

                SingleplayerFragment fragment = new SingleplayerFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable(Constants.SAVEGAME_PARCEL, savegame);
                fragment.setArguments(bundle);
                mActivity.getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
            } else {
                Toast.makeText(mActivity, getString(R.string.savegame_error_loading), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class SnapshotRequest {
        public int status;
        public Snapshot snapshot;
    }
}
