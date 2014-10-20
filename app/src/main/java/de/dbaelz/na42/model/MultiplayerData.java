package de.dbaelz.na42.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.dbaelz.na42.RoundState;

public class MultiplayerData {
    private final String player1ID;
    private final String player2ID;
    private int[] resultPlayer1;
    private int[] resultPlayer2;
    private int currentChallenge;
    private int currentRound;


    public MultiplayerData(int numberRounds, String player1ID, String player2ID) {
        this.player1ID = player1ID;
        this.player2ID = player2ID;
        this.resultPlayer1 = new int[numberRounds];
        this.resultPlayer2 = new int[numberRounds];
        currentChallenge = -1;
        currentRound = 1;
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
            currentChallenge = object.getInt("current-challenge");
            currentRound = object.getInt("current-round");
            player1ID = object.getString("player1ID");
            player2ID = object.getString("player2ID");
        } catch (JSONException e) {
            throw new RuntimeException("Can't convert JSON to data types!");
        } catch (NumberFormatException e) {
            throw new RuntimeException("Can't convert JSON to data types!");
        }
    }

    public int[] getResultPlayer1() {
        return resultPlayer1;
    }

    public void setResultPlayer1(int[] resultPlayer1) {
        this.resultPlayer1 = resultPlayer1;
    }

    public void setResultPlayer1(int round, RoundState state) {
        resultPlayer1[round - 1] = state.getValue();
    }

    public int[] getResultPlayer2() {
        return resultPlayer2;
    }

    public void setResultPlayer2(int[] resultPlayer2) {
        this.resultPlayer2 = resultPlayer2;
    }

    public void setResultPlayer2(int round, RoundState state) {
        resultPlayer2[round - 1] = state.getValue();
    }

    public int getCurrentChallenge() {
        return currentChallenge;
    }

    public void setCurrentChallenge(int currentChallenge) {
        this.currentChallenge = currentChallenge;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }

    public void incrementCurrentRound() {
        this.currentRound++;
    }

    public String getPlayer1ID() {
        return player1ID;
    }

    public String getPlayer2ID() {
        return player2ID;
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
            object.put("current-challenge", currentChallenge);
            object.put("current-round", currentRound);
            object.put("player1ID", player1ID);
            object.put("player2ID", player2ID);
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
