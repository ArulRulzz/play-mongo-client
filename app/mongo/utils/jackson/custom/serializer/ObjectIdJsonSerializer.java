/**
 * 
 */
package mongo.utils.jackson.custom.serializer;

import java.io.IOException;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * @author arul.g
 *
 */
public class ObjectIdJsonSerializer extends JsonSerializer<ObjectId> {

	@Override
	public void serialize(ObjectId id, JsonGenerator jsonGen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		jsonGen.writeString(id.toString());
	}

}
