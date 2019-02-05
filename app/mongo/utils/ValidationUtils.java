package mongo.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import play.data.validation.ValidationError;

public class ValidationUtils {

	public static final String EMAIL_REGEX = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}";

	public static final String UNDEFINED_LANG = "und";

	public static List<ValidationError> toListOfValidationErrors(Map<String, List<String>> errorMap) {
		if (errorMap == null || errorMap.isEmpty()) {
			return new ArrayList<ValidationError>();
		}
		return errorMap.entrySet().stream().map((e) -> {
			return new ValidationError(e.getKey(), e.getValue(), null);
		}).collect(Collectors.toList());
	}

	public static List<ValidationError> toValidationErrList(Map<String, List<ValidationError>> errorMap) {
		if (errorMap == null || errorMap.isEmpty()) {
			return new ArrayList<ValidationError>();
		}
		List<ValidationError> errList = new ArrayList<>();

		errorMap.entrySet().stream().forEach((e) -> {
			List<ValidationError> valErrList= e.getValue();
			if(!valErrList.isEmpty()){
				e.getValue().stream().forEach((validationError) -> {
					errList.add(validationError);
				});
			}
			
			

		});
		return errList;

	}

}
