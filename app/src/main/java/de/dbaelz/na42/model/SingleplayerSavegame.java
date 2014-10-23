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

package de.dbaelz.na42.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import de.dbaelz.na42.Constants;
import de.dbaelz.na42.RoundState;

public class SingleplayerSavegame implements Parcelable {
    private final String mUUID;
    private int mCurrentRound;
    private int[] mRounds;
    private int mWonRounds;

    public SingleplayerSavegame(int[] rounds, int currentRound, int wonRounds) {
        this.mRounds = rounds;
        this.mCurrentRound = currentRound;
        this.mWonRounds = wonRounds;
        mUUID = UUID.randomUUID().toString();
    }

    public SingleplayerSavegame(byte[] data) {
        mUUID = UUID.randomUUID().toString();

        String jsonString = new String(data);
        try {
            JSONObject object = new JSONObject(jsonString);

            JSONArray rounds = object.getJSONArray("rounds");
            mRounds = new int[rounds.length()];
            for (int i = 0; i < rounds.length(); i++) {
                mRounds[i] = rounds.getInt(i);
            }

            mCurrentRound = object.getInt("current");
            mWonRounds = object.getInt("won");
        } catch (JSONException e) {
            Log.e(Constants.LOG_TAG, "JSON syntax error!");
        } catch (NumberFormatException e) {
            throw new RuntimeException("Can't convert JSON to data types!");
        }
    }

    public SingleplayerSavegame(Parcel in) {
        mRounds = in.createIntArray();
        mCurrentRound = in.readInt();
        mWonRounds = in.readInt();
        mUUID = in.readString();
    }

    public int[] getRounds() {
        return mRounds;
    }

    public void setRounds(int[] mRounds) {
        this.mRounds = mRounds;
    }

    public int getRound(int round) {
        if (round > 0 && round <= mRounds.length) {
            return mRounds[round - 1];
        }
        return -1;
    }

    public void setRound(int round, RoundState state) {
        if (round > 0 && round <= mRounds.length) {
            mRounds[round - 1] = state.getValue();
        }
    }

    public int getCurrentRound() {
        return mCurrentRound;
    }

    public void setCurrentRound(int mCurrentRound) {
        this.mCurrentRound = mCurrentRound;
    }

    public void incrementCurrentRound() {
        this.mCurrentRound++;
    }

    public int getWonRounds() {
        return mWonRounds;
    }

    public void setWonRounds(int wonRounds) {
        this.mWonRounds = wonRounds;
    }

    public void incrementWonRounds() {
        this.mWonRounds++;
    }

    public String getUUID() {
        return this.mUUID;
    }

    @Override
    public String toString() {
        try {
            JSONArray rounds = new JSONArray();
            for (int i = 0; i < mRounds.length; i++) {
                rounds.put(mRounds[i]);
            }

            JSONObject object = new JSONObject();
            object.put("rounds", rounds);
            object.put("current", mCurrentRound);
            object.put("won", mWonRounds);
            return object.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RuntimeException("Error converting to JSON!", e);
        }
    }

    public byte[] toBytes() {
        return toString().getBytes();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeIntArray(mRounds);
        out.writeInt(mCurrentRound);
        out.writeInt(mWonRounds);
        out.writeString(mUUID);
    }
}
