/**
 * 
 */
package mongo.utils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bson.types.ObjectId;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.DBRef;

import mongo.utils.jackson.custom.deserializer.DBRefDeserializer;
import mongo.utils.jackson.custom.deserializer.LocalDateTimeDeserializer;
import mongo.utils.jackson.custom.deserializer.ObjectIdDeserializer;
import mongo.utils.jackson.custom.serializer.DBRefJsonSerializer;
import mongo.utils.jackson.custom.serializer.LocalDateTimeJsonSerializer;
import mongo.utils.jackson.custom.serializer.ObjectIdJsonSerializer;
import play.data.validation.ValidationError;
import play.libs.Json;

/**
 * @author arul.g
 *
 */
public class JsonUtils {

	public static final String JSON_ARRAY_ITEMS_KEY = "items";

	// Error keys

	public static final String STATUS = "status";
	public static final String ERROR_CODE = "errorCode";
	public static final String ERROR = "error";
	public static final String ERROR_SOURCE = "source";
	public static final String ERROR_MESSAGE = "message";
	public static final String ERROR_CAUSE = "cause";
	public static final String ERROR_STATUS = "status";

	// client error status

	public static final String NOT_FOUND = "NOT_FOUND";
	public static final String BAD_REQUEST = "BAD_REQUEST";
	public static final String FORBIDDEN = "FORBIDDEN";

	// server error status

	public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
	public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
	public static final String ERROR_INVALID = "error.invalid";

	private static ObjectMapper objMapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
			.registerModule(new SimpleModule().addSerializer(LocalDateTime.class, new LocalDateTimeJsonSerializer()))
			.registerModule(new SimpleModule().addSerializer(ObjectId.class, new ObjectIdJsonSerializer()))
			.registerModule(new SimpleModule().addSerializer(DBRef.class, new DBRefJsonSerializer()))
			.registerModule(new SimpleModule().addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer()))
			.registerModule(new SimpleModule().addDeserializer(ObjectId.class, new ObjectIdDeserializer()))
			.registerModule(new SimpleModule().addDeserializer(DBRef.class, new DBRefDeserializer()));

	public static boolean isValidField(JsonNode jsonNode, String key) {
		return jsonNode != null && key != null && !key.isEmpty() && jsonNode.has(key) && jsonNode.hasNonNull(key);
	}

	public static boolean isValidIndex(JsonNode jsonNode, int index) {
		return jsonNode != null && index >= 0 && jsonNode.has(index) && jsonNode.hasNonNull(index);
	}
	
	public static boolean arrayContains(ArrayNode tagListArr, String testTag) {
		if (tagListArr != null && tagListArr.size() > 0) {
			List<String> tagList = new ArrayList<String>();
			tagListArr.elements().forEachRemaining(tag -> {
				tagList.add(tag.asText());
			});
			return tagList.stream().anyMatch(tag -> tag.equalsIgnoreCase(testTag));
		}
		return false;
	}
	
	public static JsonNode arrayContainsValue(ArrayNode outputParamListArr, String key, String value) {
		if (outputParamListArr != null && outputParamListArr.size() > 0) {
			List<JsonNode> outputParamList = new ArrayList<JsonNode>();
			outputParamListArr.elements().forEachRemaining(jsonObj -> {
				outputParamList.add(jsonObj);
			});
			
			for (JsonNode jsonNode : outputParamList) {
				if(jsonNode.get(key).asText().equals(value)) {
					return jsonNode.get("value");
				}
			}
		}
		return Json.toJson(null);
	}

	public static String stringify(JsonNode node) {
		try {
			if (node != null) {
				return objMapper.writeValueAsString(node);
			}
			return objMapper.writeValueAsString(newJsonObject());
		} catch (JsonProcessingException e) {
			return null;
		}
	}

	public static JsonNode errorDataNotFoundJson(String value) {
		ObjectNode jsonObject = JsonUtils.newJsonObject();
		jsonObject.put("errors", value);
		return jsonObject;

	}

	public static JsonNode getJsonResponseInternalServerError(Throwable throwable) {
		return getJsonResponseInternalServerError(throwable, false);
	}
	
	public static JsonNode getJsonResponseInternalServerError(Throwable throwable, boolean isOutsideResp) {
		ObjectNode exceptionJson = Json.newObject();
		ObjectNode errorJson = exceptionJson.putObject(ERROR);
		if(!isOutsideResp) {
			errorJson.put(ERROR_SOURCE, throwable.getClass().getName());
			errorJson.put(ERROR_CAUSE, ExceptionUtils.getStackTrace(throwable));
		}
		errorJson.put(ERROR_MESSAGE, throwable.getMessage());
		return exceptionJson;
	}
	
	public static JsonNode errorDataBadRequestJson(String value) {
		ObjectNode jsonObject = JsonUtils.newJsonObject();
		jsonObject.put("errors", value);
		return jsonObject;

	}

	public static JsonNode errorDataNotFoundJson(JsonNode value) {
		ObjectNode jsonObject = JsonUtils.newJsonObject();
		jsonObject.set("errors", value);
		return jsonObject;

	}

	public static JsonNode errorMsg() {
		ObjectNode error = Json.newObject();
		error.put("error", "Invalid data");
		return error;
	}

	public static JsonNode getJsonResponseForResourceNotFound() {
		ObjectNode responseJson = Json.newObject();
		responseJson.put(ERROR, RESOURCE_NOT_FOUND);
		return responseJson;
	}
	
	public static JsonNode getJsonResponseForResourceNotFound(String message) {
		ObjectNode responseJson = Json.newObject();
		responseJson.put(ERROR, RESOURCE_NOT_FOUND);
		responseJson.put(ERROR_MESSAGE, message);
		return responseJson;
	}
	
	/**
	 * Gets a pre-formatted response for <b>FORBIDDEN</b> scenarios.
	 *
	 * @param message the message
	 * @return the json response for <b>FORBIDDEN</b>
	 * @author madhusaran.a
	 * 
	 */
	public static JsonNode getJsonResponseForForbbiden(String message) {
		ObjectNode responseJson = Json.newObject();
		responseJson.put(ERROR, FORBIDDEN);
		responseJson.put(ERROR_MESSAGE, message);
		return responseJson;
	}
	
	

	public static JsonNode getJsonresponseForBadRequest(ValidationError validateError) {
		ObjectNode responseJson = Json.newObject();
		ObjectNode errorJson = responseJson.putObject(ERROR);
		String errorKey = validateError.key();
		ArrayNode errorMsgArr = errorJson.putArray(errorKey);

		validateError.messages().stream().forEach((message) -> {
			errorMsgArr.add(message);
		});
		return responseJson;
	}

	public static JsonNode getJsonresponseForBadRequest(List<ValidationError> validate) {
		Map<String, List<ValidationError>> validationErrorMap = convertToValidationErrorMap(validate);
		return getJsonresponseForBadRequest(validationErrorMap);
	}

	private static Map<String, List<ValidationError>> convertToValidationErrorMap(List<ValidationError> validate) {
		Map<String, List<ValidationError>> validationErrorMap = new HashMap<String, List<ValidationError>>();

		validate.stream().forEach((err) -> {
			String errorKey = err.key();
			if (!validationErrorMap.containsKey(errorKey)) {
				validationErrorMap.put(errorKey, new ArrayList<ValidationError>());
			}
			validationErrorMap.get(errorKey).add(err);
		});
		return validationErrorMap;
	}

	public static JsonNode getJsonresponseForBadRequest(Map<String, List<ValidationError>> errors) {

		ObjectNode responseJson = Json.newObject();
		ObjectNode errorJson = responseJson.putObject(ERROR);
		errors.entrySet().stream().forEach((err) -> {
			String errorField = err.getKey();
			ArrayNode errorMsgArr = errorJson.putArray(errorField);
			err.getValue().stream().forEach((validateErr) -> {
				validateErr.messages().stream().forEach((validationErrMsg) -> {
					errorMsgArr.add(validationErrMsg);
				});
			});
		});
		return responseJson;
	}

	public static JsonNode getJsonResponseForClientError(int status, String message, String cientError) {
		ObjectNode exceptionJson = Json.newObject();
		ObjectNode errorJson = exceptionJson.putObject(ERROR);
		errorJson.put(ERROR_STATUS, status);
		errorJson.put(ERROR_MESSAGE, message);
		exceptionJson.put(STATUS, cientError);
		return exceptionJson;
	}
	
	

	public static String stringify(Object obj) {
		return stringify(toJson(obj));
	}

	public static JsonNode toJson(String jsonString) {
		if (jsonString != null) {
			try {
				return objMapper.readTree(jsonString);
			} catch (IOException e) {
				return objMapper.createObjectNode();
			}
		}
		return objMapper.createObjectNode();
	}
	

	public static JsonNode toJson(Object obj) {
		if (obj != null) {

			try {

				return objMapper.readTree(objMapper.writeValueAsString(obj));
			} catch (Exception e) {
				return objMapper.createObjectNode();
			}
		}
		return objMapper.createObjectNode();
	}

	public static <T> T fromJson(JsonNode jsonNode, Class<T> clazz) {
		if (jsonNode != null && clazz != null && jsonNode.size() > 0) {
			try {
				return objMapper.readValue(jsonNode.toString(), clazz);
			} catch (IOException e) {
				return null;
			}
		}
		return null;
	}

	public static <T> List<T> jsonToList(JsonNode json, Class<T> clazz) {
		List<T> list = new ArrayList<>();
		if (json != null && json.size() > 0) {
			json.forEach(resJson -> {
				list.add(JsonUtils.fromJson(resJson, clazz));
			});
		}
		return list;
	}
	
	public static ObjectNode newJsonObject() {
		return objMapper.createObjectNode();
	}

	public static ArrayNode newJsonArray() {
		return objMapper.createArrayNode();
	}

	public static ArrayNode valueToTree(Object obj) {
		return objMapper.valueToTree(obj);
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> convertToMap(Object obj) {
		return (Map<String, Object>) objMapper.convertValue(obj, HashMap.class);
	}

	public static <T> T convertToObject(Map<String, Object> mapData, Class<T> clazz) {
		return objMapper.convertValue(mapData, clazz);
	}

	public static ArrayNode getArrayNodeFromString(String tag) {

		ArrayNode tagNodeArr = Json.newArray();
		if (tag != null && !tag.trim().isEmpty()) {
			List<String> tagList = Arrays.asList(tag.split(","));
			tagList.stream().forEach(tagElement -> {
				tagNodeArr.add(tagElement);
			});
		}
		return tagNodeArr;

	}
}
