/**
 * 
 */
package mongo.utils.jackson.custom.deserializer;

import java.io.IOException;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author arul.g
 *
 */
public class ObjectIdDeserializer extends JsonDeserializer<ObjectId> {

	@Override
	public ObjectId deserialize(JsonParser parser, DeserializationContext ctx)
			throws IOException, JsonProcessingException {
		JsonNode node = parser.getCodec().readTree(parser);
		if (node != null && node.asText() != null && ObjectId.isValid(node.asText())) {
			return new ObjectId(node.asText());
		}
		return null;
	}

}
