package de.dbaelz.na42.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import de.dbaelz.na42.R;

public class MenuFragment extends Fragment implements View.OnClickListener  {
    private Button mSignOutButton;

    public MenuFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_menu, container, false);

        mSignOutButton = (Button) view.findViewById(R.id.menu_signout);
        mSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return view;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.menu_singleplayer:
                break;
            case R.id.menu_multiplayer:
                break;
            case R.id.menu_achievement:
                break;
            case R.id.menu_leaderboard:
                break;
            case R.id.menu_signin:
                break;
            case R.id.menu_signout:
                break;
            default:
                break;
        }
    }
}
