package common;

import java.io.Serializable;

public class Theater implements Serializable{
	
	private static final long serialVersionUID = -3282175681599158763L;
	
	private Boolean[][] seats;
	
	private int clientId;
	
	private int reservationRow;
	
	private int reservationColumn;
	
	//TODO maybe a better way para guardar os dados aqui? ou nomes diferentes
	
	
	public Theater(Boolean[][] seats, int clientId, int reservationRow, int reservationColumn) {
		super();
		this.seats = seats;
		this.clientId = clientId;
		this.reservationRow = reservationRow;
		this.reservationColumn = reservationColumn;
	}
	
	
	public Boolean[][] getSeats() {
		return seats;
	}
	
	
	public int getClientId() {
		return clientId;
	}
	
	
	public int getReservationRow() {
		return reservationRow;
	}
	
	
	public int getReservationColumn() {
		return reservationColumn;
	}
	
}
