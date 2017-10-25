package common;

import java.io.Serializable;

public class Seat implements Serializable {

	private static final long serialVersionUID = -7976704381597516668L;

	/** Seat types available */
    public enum SeatType {FREE, OCCUPIED, RESERVED, SELF}

    /** Client id which owns this seat */
    private int clientId = -1;

    /** Current seat type */
    private SeatType seat;


    public Seat() {
        seat = SeatType.FREE;
    }

    public boolean isFree() {
        return seat == SeatType.FREE;
    }

    public boolean isReserved() {
        return seat == SeatType.RESERVED;
    }

	public boolean isSelf() {
		return seat == SeatType.SELF;
	}

    public int getClientId() {
        return clientId;
    }

    public void freeSeat() {
        seat = SeatType.FREE;
        clientId = -1;
    }

    public void reserveSeat(int clientId) {
        seat = SeatType.RESERVED;
        this.clientId = clientId;
    }

    public void setOccupied() {
        seat = SeatType.OCCUPIED;
    }

    public void setSelf() {
    	seat = SeatType.SELF;
    }

}
