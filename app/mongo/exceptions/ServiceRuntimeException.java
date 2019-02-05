package mongo.exceptions;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * A custom runtime exception class that will be thrown by implementing
 * classes of WsServiceUtils.java
 * <br>
 * This exception will hold the status code that's obtained from a webservice execution.
 * <br><br>
 * Extension of <b>RuntimeException</b>
 * 
 * @author arul.g
 */
public class ServiceRuntimeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2817739493012056458L;

	private final int statusCode;
	private final JsonNode errorResponse;
	
	public ServiceRuntimeException(int statusCode, JsonNode errorResponse) {
		super();
		
		this.statusCode = statusCode;
		this.errorResponse = errorResponse;
	}

	public ServiceRuntimeException(String message, int statusCode, JsonNode errorResponse) {
		super(message);
		
		this.statusCode = statusCode;
		this.errorResponse = errorResponse;
	}

	public ServiceRuntimeException(Throwable cause, int statusCode, JsonNode errorResponse) {
		super(cause);
		
		this.statusCode = statusCode;
		this.errorResponse = errorResponse;
	}

	public ServiceRuntimeException(String message, Throwable cause, int statusCode, JsonNode errorResponse) {
		super(message, cause);
		
		this.statusCode = statusCode;
		this.errorResponse = errorResponse;
	}

	public ServiceRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, int statusCode, JsonNode errorResponse) {
		super(message, cause, enableSuppression, writableStackTrace);
		
		this.statusCode = statusCode;
		this.errorResponse = errorResponse;
	}

	public int getStatusCode() {
		return this.statusCode;
	}
	
	public JsonNode getErrorJsonResponse() {
		return this.errorResponse;
	}
}
