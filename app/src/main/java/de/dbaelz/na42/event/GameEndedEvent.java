package de.dbaelz.na42.event;

public class GameEndedEvent {
    private final boolean canceled;

    public GameEndedEvent(boolean canceled) {
        this.canceled = canceled;
    }

    public boolean isCanceled() {
        return canceled;
    }
}
