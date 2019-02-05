/**
 * 
 */
package mongo.utils.jackson.custom.serializer;

import java.io.IOException;
import java.time.LocalDateTime;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * @author arul.g
 *
 */
public class LocalDateTimeJsonSerializer extends JsonSerializer<LocalDateTime> {

	@Override
	public void serialize(LocalDateTime dateTime, JsonGenerator jsonGen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		jsonGen.writeString(dateTime.toString());
	}

}
