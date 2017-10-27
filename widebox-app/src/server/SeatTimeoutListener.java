package server;

import common.TimeoutListener;

public interface SeatTimeoutListener extends TimeoutListener {

    void onSeatTimeout(int clientId);
}
