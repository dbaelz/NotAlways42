package de.dbaelz.na42.event;

public class GoogleApiClientEvent {
    private final boolean isConnected;

    public GoogleApiClientEvent(boolean status) {
        this.isConnected = status;
    }

    public boolean isConnected() {
        return isConnected;
    }
}
