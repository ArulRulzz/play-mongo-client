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
import com.mongodb.DBRef;

import mongo.utils.JsonUtils;

/**
 * @author arul.g
 *
 */
public class DBRefDeserializer extends JsonDeserializer<DBRef> {

	@Override
	public DBRef deserialize(JsonParser parser, DeserializationContext ctx)
			throws IOException, JsonProcessingException {

		JsonNode node = parser.getCodec().readTree(parser);
		JsonNode refNode = JsonUtils.newJsonObject();
		JsonNode idNode = JsonUtils.newJsonObject();
		if(JsonUtils.isValidField(node, "$ref") && JsonUtils.isValidField(node, "$id")){
			refNode=node.get("$ref");
			idNode = node.get("$id");
		}
		if (node != null && idNode != null && refNode != null) {
			if (idNode.asText() != null && ObjectId.isValid(idNode.asText()) && refNode.asText() != null) {
				return new DBRef(refNode.asText(), idNode.asText());
			}
		}
		return null;
	}

}
