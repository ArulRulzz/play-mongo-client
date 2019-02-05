package mongo.db.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import mongo.db.codec.LocalDateTimeCodec;
import mongo.db.service.MongoDBService;
import play.Configuration;
import play.inject.ApplicationLifecycle;

/**
 * @author arul.g
 *
 */

@Singleton
public class MongoDBServiceImpl implements MongoDBService {

	private final Configuration config;
	private final MongoClient mongoClient;

	@Inject
	public MongoDBServiceImpl(ApplicationLifecycle lifecycle, Configuration config) {

		// onStart
		this.config = config;
		mongoClient = createMongoClient();

		lifecycle.addStopHook(() -> {
			// onStop
			return CompletableFuture.supplyAsync(() -> {
				if (mongoClient != null) {
					mongoClient.close();
				}
				return null;
			});
		});
	}

	private MongoClient createMongoClient() {
		String user = config.getString("mongodb.user");
		char[] password = config.getString("mongodb.password").toCharArray();
		String database = config.getString("mongodb.db");
		String host = config.getString("mongodb.host");

		MongoCredential credential = MongoCredential.createCredential(user, database, password);

		CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
				CodecRegistries.fromCodecs(new LocalDateTimeCodec()), MongoClient.getDefaultCodecRegistry());

		MongoClientOptions options = MongoClientOptions.builder().connectionsPerHost(config.getInt("mongodb.conn.host"))
				.threadsAllowedToBlockForConnectionMultiplier(config.getInt("mongodb.conn.multiplexer"))
				.codecRegistry(codecRegistry).build();

		return new MongoClient(Arrays.asList(new ServerAddress(host, config.getInt("mongodb.port"))),
				Arrays.asList(credential), options);
	}

	@Override
	public MongoClient getMongoClient() {
		return mongoClient;
	}

	@Override
	public MongoDatabase getDB() {
		return getMongoClient() != null ? getMongoClient().getDatabase(config.getString("mongodb.db")) : null;
	}

	@Override
	public MongoCollection<Document> getCollection(String collectionName) {
		return getDB() != null ? getDB().getCollection(collectionName, Document.class) : null;
	}

	public List<String> getCollections() {
		return getDB() != null ? getDB().listCollectionNames().into(new ArrayList<>()) : new ArrayList<>();
	}
}
