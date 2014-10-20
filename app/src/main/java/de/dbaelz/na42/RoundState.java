package de.dbaelz.na42;

public enum RoundState {
    NOT_PLAYED(0),
    WON(1),
    LOST(2);

    private int state;

    private RoundState(int state) {
        this.state = state;
    }

    public int getValue() {
        return state;
    }
}
