package de.dbaelz.na42.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.games.Games;

import de.dbaelz.na42.MainActivity;
import de.dbaelz.na42.R;
import de.dbaelz.na42.event.GoogleApiClientEvent;
import de.greenrobot.event.EventBus;

public class MenuFragment extends Fragment implements View.OnClickListener {
    private MainActivity mActivity;

    private Button mSingleplayer;
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
        mMultiplayer = (Button) view.findViewById(R.id.menu_multiplayer);
        mAchievement = (Button) view.findViewById(R.id.menu_achievement);
        mLeaderboard = (Button) view.findViewById(R.id.menu_leaderboard);
        mSignInButton = (SignInButton) view.findViewById(R.id.menu_signin);
        mSignOutButton = (Button) view.findViewById(R.id.menu_signout);

        mSingleplayer.setOnClickListener(this);
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


}
