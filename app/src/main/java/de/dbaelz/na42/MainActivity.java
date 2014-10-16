package de.dbaelz.na42;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;

import de.dbaelz.na42.event.GameFinishedEvent;
import de.dbaelz.na42.event.GoogleApiClientEvent;
import de.dbaelz.na42.fragment.MenuFragment;
import de.greenrobot.event.EventBus;


public class MainActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String LOG_TAG = "NotAlways42";
    private static final int REQUEST_CODE_SIGNIN = 100;

    private GoogleApiClient mGoogleApiClient;

    private MenuFragment mMenuFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        mMenuFragment = new MenuFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, mMenuFragment).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(LOG_TAG, "onConnected");
        EventBus.getDefault().postSticky(new GoogleApiClientEvent(true));
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(LOG_TAG, "onConnectionSuspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(LOG_TAG, "onConnectionFailed");
        EventBus.getDefault().postSticky(new GoogleApiClientEvent(false));
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, REQUEST_CODE_SIGNIN);
            } catch (IntentSender.SendIntentException e) {
                mGoogleApiClient.connect();
            }
        } else {
            int errorCode = connectionResult.getErrorCode();
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(errorCode, this, REQUEST_CODE_SIGNIN);
            if (dialog != null) {
                dialog.show();
            } else {
                // TODO: Handle error;
                Log.d(LOG_TAG, "Error: Can't show error dialog");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SIGNIN) {
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                // TODO: Handle error;
                Log.d(LOG_TAG, "Error: resultCode != RESULT_OK");
            }
        }
    }

    public void onEvent(GameFinishedEvent event) {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, mMenuFragment).commit();
    }

    public void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }
}
