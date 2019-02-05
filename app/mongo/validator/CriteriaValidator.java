package mongo.validator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;

import mongo.exceptions.ValidationException;

public class CriteriaValidator {

	public static final String DATA_ANNOTATION_METHOD = "value";

	public Map<String, List<Object>> getValidFilters(Map<String, List<String>> queryMap, Class<?> clazz) throws ValidationException {
		Map<String, List<Object>> validFilterFields = new HashMap<>();
		if (queryMap == null) {
			return validFilterFields;
		}

		/*
		try {
			
			Class<?> modelClazz = null;
			try {
				modelClazz = Class.forName(clazz.getTypeName());
			} catch (ClassNotFoundException e1) {
				System.out.println("1....CLASSNOTFOUND !!!!");
				e1.printStackTrace();
				try {
					modelClazz = Class.forName(clazz.getTypeName(), true, getClass().getClassLoader());
				} catch (ClassNotFoundException e2) {
					System.out.println("2....CLASSNOTFOUND !!!!");
					e2.printStackTrace();
					modelClazz = Application.class.getClassLoader().loadClass(clazz.getTypeName());
				}
			}
			if (null == modelClazz) {
				throw new ArkaRunTimeException("Unable to load Model Class");
			}

			Field[] declaredFields = modelClazz.getDeclaredFields();
			Field[] declaredParentFields = modelClazz.getSuperclass().getDeclaredFields();
			*/
			
			Field[] declaredFields = clazz.getDeclaredFields();
			Field[] declaredParentFields = clazz.getSuperclass().getDeclaredFields();
			
			List<Field> allFields = new ArrayList<>();
			allFields.addAll(Arrays.asList(declaredFields));
			allFields.addAll(Arrays.asList(declaredParentFields));
			allFields.stream().filter(field -> field != null).forEach(field -> {

				Arrays.asList(field.getAnnotations()).forEach(anno -> {
					try {
						Class<? extends Annotation> annotationType = anno.annotationType();
						Method valueMethod = annotationType.getDeclaredMethod(DATA_ANNOTATION_METHOD);
						String columnNameFromAnnotation = (String) valueMethod.invoke(anno);
						boolean foundInAnnotation = validateAndSet(field.getType().getName(), columnNameFromAnnotation, queryMap, validFilterFields);
						if (!foundInAnnotation) {
							validateAndSet(field.getType().getName(), field.getName(), queryMap, validFilterFields);
						}

					} catch (Exception e) {
						//throw new Exception(e);
					}

				});
			});
			/*
		} catch (ClassNotFoundException cnfe) {
			throw new ArkaRunTimeException(cnfe);
		} catch (ClassCastException cce) {
			throw new ArkaRunTimeException(cce);
		}
		*/

		return validFilterFields;
	}

	public Map<String, List<Object>> getValidFilters(Map<String, String[]> queryMap, Map<String, String> validFieldAndType) {

		Map<String, List<Object>> validFilterFields = new HashMap<>();
		if (queryMap == null) {
			return validFilterFields;
		}

		validFieldAndType.forEach((field, dataType) -> {
			Map<String, List<String>> queryMapList = new HashMap<>();
			queryMap.forEach((key, val) -> {
				queryMapList.put(key, Arrays.asList(val));
			});
			validateAndSet(dataType, field, queryMapList, validFilterFields);
		});

		return validFilterFields;
	}

	public boolean validateAndSet(String dataType, String fieldName, Map<String, List<String>> queryMap, Map<String, List<Object>> validFilterFields) {
		if (queryMap == null || validFilterFields == null) {
			return false;
		}
		Set<String> queryStringSet = queryMap.keySet();
		if (queryStringSet.contains(fieldName) && dataType != null) {
			switch (dataType.toLowerCase()) {
			case "java.lang.long":
			case "long":
				validFilterFields.put(fieldName, queryMap.get(fieldName).stream().map(val -> Long.parseLong(val)).collect(Collectors.toList()));
				break;
			case "java.lang.integer":
			case "integer":
				validFilterFields.put(fieldName, queryMap.get(fieldName).stream().map(val -> Integer.parseInt(val)).collect(Collectors.toList()));
				break;
			case "java.lang.double":
			case "double":
				validFilterFields.put(fieldName, queryMap.get(fieldName).stream().map(val -> Double.parseDouble(val)).collect(Collectors.toList()));
				break;
			case "java.lang.float":
			case "float":
				validFilterFields.put(fieldName, queryMap.get(fieldName).stream().map(val -> Float.parseFloat(val)).collect(Collectors.toList()));
				break;
			default:
				if (fieldName.equals("_id")) {
					validFilterFields.put(fieldName, queryMap.get(fieldName).stream().map(val -> new ObjectId(val)).collect(Collectors.toList()));
				} else {
					validFilterFields.put(fieldName, queryMap.get(fieldName).stream().map(val -> (Object) val).collect(Collectors.toList()));
				}
			}
			return true;
		} else

		{

			List<String> queryStringList = new ArrayList<String>(queryStringSet);

			queryStringList.stream().filter(queryString -> {
				if (queryString.contains(".") && Arrays.asList(queryString.split("\\.")).size() > 0) {
					return true;
				}
				return false;
			}).forEach(queryString -> {
				String queryFieldName = queryString.split("\\.")[0];
				if (queryFieldName.equals(fieldName) && dataType != null) {
					switch (dataType.toLowerCase()) {
					case "java.util.list":
					case "list":
						validFilterFields.put(queryString, queryMap.get(queryString).stream().map(val -> (Object) val).collect(Collectors.toList()));
						break;
					default:
					}
				}

			});

		}
		return false;
	}

}
