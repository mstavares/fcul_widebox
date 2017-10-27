package common;

public interface TimeoutListener {

    interface Timeout extends TimeoutListener {
        void timeout();
    }
}
