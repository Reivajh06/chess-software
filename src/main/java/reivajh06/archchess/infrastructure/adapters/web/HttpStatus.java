package reivajh06.archchess.infrastructure.adapters.web;

public enum HttpStatus {

	OK(200),
	BAD_REQUEST(400),
	FORBIDDEN(403),
	NOT_FOUND(404),
	CONFLICT(409),
	SERVICE_UNAVAILABLE(503);

	public final int code;

	HttpStatus(int code) {
		this.code = code;
	}
}
