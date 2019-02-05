/**
 * 
 */
package mongo.db.codec;

import java.time.LocalDateTime;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

/**
 * @author arul.g
 *
 */
public class LocalDateTimeCodec implements Codec<LocalDateTime> {

	@Override
	public void encode(BsonWriter writer, LocalDateTime t, EncoderContext ec) {
		writer.writeString(t.toString());
	}

	@Override
	public Class<LocalDateTime> getEncoderClass() {
		return LocalDateTime.class;
	}

	@Override
	public LocalDateTime decode(BsonReader reader, DecoderContext dc) {
		String date = reader.readString();
		return LocalDateTime.parse(date);
	}
}