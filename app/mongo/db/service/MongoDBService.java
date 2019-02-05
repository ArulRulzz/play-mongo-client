
package mongo.db.service;

import java.util.List;

import org.bson.Document;

import com.google.inject.ImplementedBy;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import mongo.db.service.impl.MongoDBServiceImpl;

/**
 * @author arul.g
 *
 */

@ImplementedBy(MongoDBServiceImpl.class)
public interface MongoDBService {

	public MongoClient getMongoClient();

	public MongoDatabase getDB();

	public MongoCollection<Document> getCollection(String collectionName);

	public List<String> getCollections();
}
