/**
 * 
 */
package mongo.exceptions;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author arul.g
 *
 */
public class ValidationException extends RuntimeException {

	private static final long serialVersionUID = -2846066620260144168L;

	private final Map<String, List<String>> errors;

	public ValidationException(String msg) {
		super(msg);
		errors = new LinkedHashMap<>();
	}

	public ValidationException(Exception e) {
		super(e);
		errors = new LinkedHashMap<>();
	}

	public ValidationException(String msg, Map<String, List<String>> errors) {
		super(msg);
		this.errors = errors;
	}

	public Map<String, List<String>> getErrors() {
		return errors;
	}

}
