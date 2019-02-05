/**
 * 
 */
package mongo.utils.jackson.custom.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.mongodb.DBRef;

import mongo.utils.JsonUtils;

/**
 * @author arul.g
 *
 */
public class DBRefJsonSerializer extends JsonSerializer<DBRef> {

	@Override
	public void serialize(DBRef dbRef, JsonGenerator jsonGen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		jsonGen.writeObject(JsonUtils.toJson(dbRef.toString()));
	}

}
