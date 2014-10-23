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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.dbaelz.na42.RoundState;

public class MultiplayerData {
    private final String player1ID;
    private final String player2ID;
    private final String player1Name;
    private final String player2Name;
    private int[] resultPlayer1;
    private int[] resultPlayer2;
    private int currentRoundPlayer1;
    private int currentRoundPlayer2;


    public MultiplayerData(int numberRounds, String player1ID, String player2ID, String player1Name, String player2Name) {
        this.player1ID = player1ID;
        this.player2ID = player2ID;
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.resultPlayer1 = new int[numberRounds];
        this.resultPlayer2 = new int[numberRounds];
        this.currentRoundPlayer1 = 1;
        this.currentRoundPlayer2 = 1;
    }

    public MultiplayerData(byte[] data) {
        String jsonString = new String(data);
        try {
            JSONObject object = new JSONObject(jsonString);

            JSONArray jsonRoundsPlayer1 = object.getJSONArray("result-player1");
            resultPlayer1 = new int[jsonRoundsPlayer1.length()];
            for (int i = 0; i < jsonRoundsPlayer1.length(); i++) {
                resultPlayer1[i] = jsonRoundsPlayer1.getInt(i);
            }

            JSONArray jsonRoundsPlayer2 = object.getJSONArray("result-player2");
            resultPlayer2 = new int[jsonRoundsPlayer2.length()];
            for (int i = 0; i < jsonRoundsPlayer2.length(); i++) {
                resultPlayer2[i] = jsonRoundsPlayer2.getInt(i);
            }

            currentRoundPlayer1 = object.getInt("current-round-player1");
            currentRoundPlayer2 = object.getInt("current-round-player2");
            player1ID = object.getString("player1ID");
            player2ID = object.getString("player2ID");
            player1Name = object.getString("player1Name");
            player2Name = object.getString("player2Name");
        } catch (JSONException e) {
            throw new RuntimeException("Can't convert JSON to data types!");
        } catch (NumberFormatException e) {
            throw new RuntimeException("Can't convert JSON to data types!");
        }
    }

    public int[] getResultPlayer1() {
        return resultPlayer1;
    }

    public void setResultPlayer1(int round, RoundState state) {
        resultPlayer1[round - 1] = state.getValue();
    }

    public int[] getResultPlayer2() {
        return resultPlayer2;
    }

    public void setResultPlayer2(int round, RoundState state) {
        resultPlayer2[round - 1] = state.getValue();
    }

    public int getCurrentRoundPlayer1() {
        return currentRoundPlayer1;
    }

    public void incrementRoundPlayer1() {
        this.currentRoundPlayer1++;
    }

    public int getCurrentRoundPlayer2() {
        return currentRoundPlayer2;
    }

    public void incrementRoundPlayer2() {
        this.currentRoundPlayer2 ++;
    }

    public String getPlayer1ID() {
        return player1ID;
    }

    public String getPlayer2ID() {
        return player2ID;
    }

    public String getPlayer1Name() {
        return player1Name;
    }

    public String getPlayer2Name() {
        return player2Name;
    }

    public String getWinnerName() {
        int wonPlayer1 = 0;
        for (int game : resultPlayer1) {
            if (game == 1) {
                wonPlayer1++;
            }
        }

        int wonPlayer2 = 0;
        for (int game : resultPlayer2) {
            if (game == 1) {
                wonPlayer2++;
            }
        }

        if (wonPlayer1 == wonPlayer2) {
            return "Draw";
        }
        return (wonPlayer1 > wonPlayer2) ? getPlayer1Name() : getPlayer2Name();
    }

    @Override
    public String toString() {
        try {
            JSONArray jsonResultPlayer1 = new JSONArray();
            for (int i = 0; i < resultPlayer1.length; i++) {
                jsonResultPlayer1.put(resultPlayer1[i]);
            }
            JSONArray jsonResultPlayer2 = new JSONArray();
            for (int i = 0; i < resultPlayer2.length; i++) {
                jsonResultPlayer2.put(resultPlayer2[i]);
            }
            JSONObject object = new JSONObject();
            object.put("result-player1", jsonResultPlayer1);
            object.put("result-player2", jsonResultPlayer2);
            object.put("current-round-player1", currentRoundPlayer1);
            object.put("current-round-player2", currentRoundPlayer2);
            object.put("player1ID", player1ID);
            object.put("player2ID", player2ID);
            object.put("player1Name", player1Name);
            object.put("player2Name", player2Name);
            return object.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RuntimeException("Error converting to JSON!", e);
        }
    }

    public byte[] toBytes() {
        return toString().getBytes();
    }
}
