/**
 * 
 */
package mongo.models.base.repository;

import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.DBRef;
import com.mongodb.client.MongoCollection;

import mongo.models.base.BaseModel;

/**
 * 
 * @author arul.g
 */
public interface MongoBaseRepository<M extends BaseModel<String>> extends BaseRepository<M> {

	String _ID = "_id";

	DBRef getDBRef(String id);

	default String generateNewId() {
		return new ObjectId().toHexString();
	}
	
	public boolean isCollectionPresent(String collectionname);
	
	public MongoCollection<Document> getMongoCollection(String collectionname);
	
	public  Long getMongoCollectionCount(String collectionName);
	
	public void insertMany(String collectionName, List<Map<String, Object>> dataList);
	
	public void deleteMany(String collectionName, Map<String, Object> query);

	List<String> getAllMongoCollections();

	public <F> List<F> getDistinctValue(String field, Class<F> calzz);

	void createNewCollection(String newCollectionName);

	void deleteCollection(String collectionName);
	
}