package database;

import common.TimeoutListener;

public interface SeatTimeoutListener extends TimeoutListener {

    void onSeatTimeout(String seatId);
}
