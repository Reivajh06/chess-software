package reivajh06.api;

public class ApiException extends RuntimeException {

	private final int errorCode;

	public ApiException(int errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public int errorCode() {
		return errorCode;
	}
}
