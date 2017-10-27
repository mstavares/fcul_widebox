package common;


import server.SeatTimeoutListener;

import java.util.Timer;
import java.util.TimerTask;


public class TimeoutManager {

    private TimeoutListener timeoutListener;
    private int clientId;

    private Timer timer;

    private int time;

    public TimeoutManager(TimeoutListener timeoutListener, int time) {
        this.timeoutListener = timeoutListener;
        this.time = time;
    }

    public TimeoutManager(TimeoutListener timeoutListener, int time, int clientId) {
        this(timeoutListener, time);
        this.clientId = clientId;

    }

    public void runOnlyOnce() {
        timer = new Timer();
        timer.schedule(new TimeoutTask(), time);
    }

    public void runRepeatly() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimeoutTask(), time, time);
    }

    public void stop() {
        timer.cancel();
        timer.purge();
    }


    private class TimeoutTask extends TimerTask {

        @Override
        public void run() {
            if (timeoutListener instanceof SeatTimeoutListener)
                ((SeatTimeoutListener) timeoutListener).onSeatTimeout(clientId);
            else
                ((TimeoutListener.Timeout) timeoutListener).timeout();
        }
    }

}
