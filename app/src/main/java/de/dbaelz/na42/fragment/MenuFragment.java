package de.dbaelz.na42.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.Snapshots;

import de.dbaelz.na42.Constants;
import de.dbaelz.na42.MainActivity;
import de.dbaelz.na42.R;
import de.dbaelz.na42.event.GoogleApiClientEvent;
import de.dbaelz.na42.model.SingleplayerSavegame;
import de.greenrobot.event.EventBus;

public class MenuFragment extends Fragment implements View.OnClickListener {
    private MainActivity mActivity;

    private Button mSingleplayer;
    private Button mLoadSavegame;
    private Button mMultiplayer;
    private Button mAchievement;
    private Button mLeaderboard;
    private SignInButton mSignInButton;
    private Button mSignOutButton;

    public MenuFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        mSingleplayer = (Button) view.findViewById(R.id.menu_singleplayer);
        mLoadSavegame = (Button) view.findViewById(R.id.menu_load_savegame);
        mMultiplayer = (Button) view.findViewById(R.id.menu_multiplayer);
        mAchievement = (Button) view.findViewById(R.id.menu_achievement);
        mLeaderboard = (Button) view.findViewById(R.id.menu_leaderboard);
        mSignInButton = (SignInButton) view.findViewById(R.id.menu_signin);
        mSignOutButton = (Button) view.findViewById(R.id.menu_signout);

        mSingleplayer.setOnClickListener(this);
        mLoadSavegame.setOnClickListener(this);
        mMultiplayer.setOnClickListener(this);
        mAchievement.setOnClickListener(this);
        mLeaderboard.setOnClickListener(this);
        mSignInButton.setOnClickListener(this);
        mSignOutButton.setOnClickListener(this);

        mActivity = (MainActivity) getActivity();
        switchSignButton(mActivity.getGoogleApiClient().isConnected());

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
                mActivity.getSupportFragmentManager().beginTransaction().replace(R.id.container, new SingleplayerFragment()).commit();
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
                    // TODO: Start multiplayer game
                    //mActivity.getSupportFragmentManager().beginTransaction().replace(R.id.container, FRAGMENT).commit();
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

    public void onEvent(GoogleApiClientEvent event) {
        switchSignButton(event.isConnected());
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
