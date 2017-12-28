package exceptions;

public class NotOwnerException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 973701347457583069L;

	public NotOwnerException(String message) {
		super(message);
	}
}
