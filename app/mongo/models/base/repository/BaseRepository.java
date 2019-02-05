/**
 * 
 */
package mongo.models.base.repository;

import java.util.List;
import java.util.Map;

import mongo.exceptions.ValidationException;
import mongo.models.base.BaseModel;

/**
 * @author arul.g
 *
 */
public interface BaseRepository<M extends BaseModel<String>> {

	public void insert(M obj) throws ValidationException;

	public boolean updateOne(M obj) throws ValidationException;

	public M findOneById(String objectId) throws ValidationException;
	
	public List<M> findAll(Map<String, List<String>> query) throws ValidationException;

	public boolean deleteOneById(String objectId) throws ValidationException;

	public boolean deleteMany(Map<String, List<String>> deleteQuery) throws ValidationException;

	public long count();

	public long count(Map<String, List<String>> query) throws ValidationException;

	boolean deleteFromArray(String parentId, String elementId, String arrayFieldName) throws ValidationException;

}
