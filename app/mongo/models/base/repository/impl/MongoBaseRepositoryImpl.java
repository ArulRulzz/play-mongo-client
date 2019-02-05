/**
 * 
 */
package mongo.models.base.repository.impl;

import java.lang.reflect.ParameterizedType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.google.inject.Inject;
import com.mongodb.DBRef;
import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.UpdateResult;

import mongo.builder.MongoQueryBuilder;
import mongo.db.service.MongoDBService;
import mongo.exceptions.ValidationException;
import mongo.models.base.BaseModel;
import mongo.models.base.repository.MongoBaseRepository;
import mongo.utils.CriteriaUtils;
import mongo.utils.JsonUtils;
import mongo.validator.CriteriaValidator;
import play.Application;

/**
 * @author arul.g
 *
 */
public abstract class MongoBaseRepositoryImpl<M extends BaseModel<String>> implements MongoBaseRepository<M> {
	
	protected final String COLLECTION_NAME;

	@Inject
	protected MongoDBService dbService;

	@Inject
	protected MongoQueryBuilder mongoQueryBuilder;

	@Inject
	protected CriteriaValidator criteriaValidator;

	public MongoBaseRepositoryImpl(String collectionName) {
		COLLECTION_NAME = collectionName;
	}

	@Override
	public void insert(M obj) throws ValidationException {

		Map<String, List<String>> errors = new LinkedHashMap<>();
		if (obj == null) {
			throw new ValidationException("Model Object can't be null", errors);
		}
		obj.setCreatedTime(LocalDateTime.now());
		Document doc = new Document(JsonUtils.convertToMap(obj));
		String _id = doc.getString(_ID);
		if (_id != null) {
			if (!ObjectId.isValid(_id)) {
				errors.put(_ID, Arrays.asList("Invalid format"));
				throw new ValidationException("Object Id validation falied", errors);
			}
			doc.put(_ID, new ObjectId(doc.getString(_ID)));
		} else {
			doc.remove(_ID);
		}

		try {
			dbService.getCollection(COLLECTION_NAME).insertOne(doc);
			setBasicFields(obj, doc);
		} catch (MongoWriteException mwe) {
			Map<String, List<String>> errorMap = new LinkedHashMap<>();
			if (mwe.getMessage() != null && mwe.getMessage().contains("E11000 duplicate key error")) {
				errorMap.put("error", Arrays.asList("Insert failed due to duplicate key"));
			} else {
				errorMap.put("error", Arrays.asList(mwe.getMessage()));
			}
			throw new ValidationException("Insert failed", errorMap);
		}

	}

	@Override
	public boolean updateOne(M obj) throws ValidationException {

		if (obj == null) {
			return false;
		}
		obj.setUpdatedTime(LocalDateTime.now());
		Document doc = new Document(JsonUtils.convertToMap(obj));
		String _id = doc.getString(_ID);
		if (_id == null || !ObjectId.isValid(_id)) {
			Map<String, List<String>> errors = new LinkedHashMap<>();
			errors.put(_ID, Arrays.asList("Invalid format"));
			throw new ValidationException("Object Id validation falied", errors);
		}
		ObjectId objectId = new ObjectId(doc.getString(_ID));
		Document objIdDoc = new Document(_ID, objectId);
		doc.remove(_ID);
		UpdateResult updateResult = dbService.getCollection(COLLECTION_NAME).updateOne(objIdDoc,
				new Document("$set", doc));
		return updateResult.getModifiedCount() > 0;

	}

	@Override
	public M findOneById(String objectId) throws ValidationException {

		Map<String, List<String>> errors = new LinkedHashMap<>();
		if (objectId == null || !ObjectId.isValid(objectId)) {
			errors.put(_ID, Arrays.asList("Invalid format"));
			throw new ValidationException("Object Id validation falied", errors);
		}
		Document firstDoc = dbService.getCollection(COLLECTION_NAME).find(new Document(_ID, new ObjectId(objectId)))
				.first();
		if (firstDoc != null && !firstDoc.isEmpty()) {
			try {
				Class<M> modelClass = getGenericModelClass();
				M obj = JsonUtils.convertToObject(firstDoc, modelClass);
				setBasicFields(obj, firstDoc);
				return obj;
			} catch (ClassNotFoundException e) {
				errors.put(JsonUtils.ERROR_MESSAGE, Arrays.asList(e.getMessage()));
				throw new ValidationException(JsonUtils.INTERNAL_SERVER_ERROR, errors);
			}
		}
		return null;

	}

	@Override
	public List<M> findAll(Map<String, List<String>> query) throws ValidationException {

		boolean distinct = false;
		if (query != null && query.get(CriteriaUtils.DISTINCT) != null)
			distinct = true;

		if (distinct) {
			return aggregate(query);
		} else {
			return find(query);
		}

	}

	@Override
	public boolean deleteOneById(String objectId) throws ValidationException {

		if (objectId == null || !ObjectId.isValid(objectId)) {
			Map<String, List<String>> errors = new LinkedHashMap<>();
			errors.put(_ID, Arrays.asList("Invalid format"));
			throw new ValidationException("Object Id validation falied", errors);
		}
		return dbService.getCollection(COLLECTION_NAME).deleteOne(new Document(_ID, new ObjectId(objectId)))
				.getDeletedCount() > 0;

	}

	@Override
	public boolean deleteMany(Map<String, List<String>> deleteQuery) throws ValidationException {
		boolean isRegex = false;
		if (deleteQuery != null && deleteQuery.get(CriteriaUtils.REGEX) != null)
			isRegex = deleteQuery.get(CriteriaUtils.REGEX) != null;
		try {
			Map<String, List<Object>> validFilters = criteriaValidator.getValidFilters(deleteQuery,
					getGenericModelClass());
			return dbService.getCollection(COLLECTION_NAME)
					.deleteMany(mongoQueryBuilder.buildQuery(validFilters, isRegex)).getDeletedCount() > 0;
		} catch (ClassNotFoundException |RuntimeException e) {
			Map<String, List<String>> errorMap = new LinkedHashMap<>();
			errorMap.put("error", Arrays.asList(e.getMessage()));
			throw new ValidationException("Can't delete", errorMap);
		}
	}

	@Override
	public boolean deleteFromArray(String parentId, String elementId, String arrayFieldName)
			throws ValidationException {

		if (parentId == null || !ObjectId.isValid(parentId) || elementId == null || !ObjectId.isValid(elementId)) {
			Map<String, List<String>> errors = new LinkedHashMap<>();
			errors.put("error", Arrays.asList("Invalid input"));
			throw new ValidationException("Object Id validation falied or array name missing.", errors);
		}

		Document parent = new Document(_ID, new ObjectId(parentId));
		Document update = new Document("$pull",
				new Document(arrayFieldName, new Document(_ID, new ObjectId(elementId))));

		UpdateResult updateResult = dbService.getCollection(COLLECTION_NAME).updateOne(parent, update);

		return updateResult.getModifiedCount() > 0;

	}

	@Override
	public long count() {
		return dbService.getCollection(COLLECTION_NAME).count();
	}

	@Override
	public long count(Map<String, List<String>> query) throws ValidationException {

		boolean distinct = false;
		if (query != null && query.get(CriteriaUtils.DISTINCT) != null)
			distinct = true;

		if (distinct) {
			return aggregateCount(query);
		} else {
			return findCount(query);
		}

	}

	@Override
	public List<String> getAllMongoCollections() {
		return dbService.getCollections();
	}
	
	@Override
	public boolean isCollectionPresent(String collectionname){
		return getMongoCollection(collectionname).count() == 0;
	}
	
	@Override
	public  MongoCollection<Document> getMongoCollection(String collectionName){
		return dbService.getDB().getCollection(collectionName);
	}
	
	@Override
	public  Long getMongoCollectionCount(String collectionName){
		return dbService.getDB().getCollection(collectionName).count();
	}
	
	@Override
	public void insertMany(String collectionName, List<Map<String, Object>> dataList) {
		if (collectionName != null && dataList != null) {
			List<Document> collect = dataList.parallelStream().map(data -> new Document(data)).collect(Collectors.toList());
			dbService.getCollection(collectionName).insertMany(collect);
		}
	}
	
	@Override
	public void deleteMany(String collectionName, Map<String, Object> query) {
		if (collectionName != null && query != null) {
			getMongoCollection(collectionName).deleteMany(new Document(query));
		}
	}
	

	@Override
	public void deleteCollection(String collectionName) {
		dbService.getDB().getCollection(collectionName).drop();
	}

	@Override
	public DBRef getDBRef(String restritionId) {
		if (restritionId != null) {
			return new DBRef(COLLECTION_NAME, restritionId);
		}
		return null;
	}

	@Override
	public <F> List<F> getDistinctValue(String field, Class<F> clazz) {
		return dbService.getCollection(COLLECTION_NAME).distinct(field, clazz).into(new ArrayList<>());
	}

	public void createNewCollection(String newCollectionName) {
		dbService.getDB().createCollection(newCollectionName);
	}

	protected List<M> find(Map<String, List<String>> query) throws ValidationException {

		boolean isRegex = false;
		if (query != null && query.get(CriteriaUtils.REGEX) != null)
			isRegex = query.get(CriteriaUtils.REGEX) != null;

		List<M> fetchedList = new ArrayList<>();

		try {
			Map<String, List<Object>> validFilters = criteriaValidator.getValidFilters(query, getGenericModelClass());
			FindIterable<Document> findItr = dbService.getCollection(COLLECTION_NAME).find(
					mongoQueryBuilder.buildQuery(validFilters, isRegex, CriteriaUtils.queryStringListToArray(query)));

			mongoQueryBuilder.fillFindCriteria(findItr, query);

			List<M> fetched = findItr.into(new ArrayList<>()).stream().map(doc -> {
				try {
					M obj = JsonUtils.convertToObject(doc, getGenericModelClass());
					setBasicFields(obj, doc);
					return obj;
				} catch (ClassNotFoundException e) {
					
				}
				return null;
			}).filter(obj -> obj != null).collect(Collectors.toList());

			fetchedList.addAll(fetched);
		} catch (ClassNotFoundException | RuntimeException e) {
			Map<String, List<String>> errorMap = new LinkedHashMap<>();
			errorMap.put("error", Arrays.asList(e.getMessage()));
			throw new ValidationException("Can't find", errorMap);
		}
		return fetchedList;
	}

	protected List<M> aggregate(Map<String, List<String>> query) throws ValidationException {

		boolean isRegex = false;
		if (query != null && query.get(CriteriaUtils.REGEX) != null)
			isRegex = query.get(CriteriaUtils.REGEX) != null;

		List<M> fetchedList = new ArrayList<>();

		try {
			Map<String, List<Object>> validFilters = criteriaValidator.getValidFilters(query, getGenericModelClass());
			List<Document> aggreList = new ArrayList<>();
			if (query != null && !query.isEmpty()) {
				aggreList.add(new Document("$match", mongoQueryBuilder.buildQuery(validFilters, isRegex)));
			}
			mongoQueryBuilder.fillAggregationCriteria(aggreList, query);

			List<M> fetched = dbService.getCollection(COLLECTION_NAME).aggregate(aggreList).allowDiskUse(true)
					.into(new ArrayList<Document>()).parallelStream().map(doc -> {
						if (doc.get(_ID) instanceof Document) {
							doc = (Document) doc.get(_ID);
						}
						try {
							M obj = JsonUtils.convertToObject(doc, getGenericModelClass());
							setBasicFields(obj, doc);
							return obj;
						} catch (ClassNotFoundException e) {
							//Intentionally Ignored
						}
						return null;
					}).filter(obj -> obj != null).collect(Collectors.toList());

			fetchedList.addAll(fetched);
		} catch (ClassNotFoundException | RuntimeException e) {
			Map<String, List<String>> errorMap = new LinkedHashMap<>();
			errorMap.put("error", Arrays.asList(e.getMessage()));
			throw new ValidationException("Can't aggregate", errorMap);
		}
		return fetchedList;
	}

	private long aggregateCount(Map<String, List<String>> query) throws ValidationException {

		boolean isRegex = false;
		if (query != null && query.get(CriteriaUtils.REGEX) != null)
			isRegex = query.get(CriteriaUtils.REGEX) != null;
		try {
			List<Document> aggreList = new ArrayList<>();
			if (query != null && !query.isEmpty()) {
				aggreList.add(new Document("$match", mongoQueryBuilder
						.buildQuery(criteriaValidator.getValidFilters(query, getGenericModelClass()), isRegex)));
				mongoQueryBuilder.fillAggregationCountCriteria(aggreList, query);
			}
			Document countDoc = dbService.getCollection(COLLECTION_NAME).aggregate(aggreList).allowDiskUse(true)
					.first();
			if (countDoc != null && countDoc.containsKey("count") && countDoc.getInteger("count") != null) {
				return countDoc.getInteger("count");
			}
		} catch (ClassNotFoundException | RuntimeException e) {
			Map<String, List<String>> errorMap = new LinkedHashMap<>();
			errorMap.put("error", Arrays.asList(e.getMessage()));
			throw new ValidationException("Can't aggregate Count", errorMap);
		}
		return 0;

	}

	private long findCount(Map<String, List<String>> query) throws ValidationException {
		boolean isRegex = false;
		if (query != null && query.get(CriteriaUtils.REGEX) != null)
			isRegex = query.get(CriteriaUtils.REGEX) != null;
		try {
			Map<String, List<Object>> validFilters = criteriaValidator.getValidFilters(query, getGenericModelClass());
			return dbService.getCollection(COLLECTION_NAME).count(mongoQueryBuilder
					.buildQuery(validFilters, isRegex));
		} catch (ClassNotFoundException | RuntimeException e) {
			Map<String, List<String>> errorMap = new LinkedHashMap<>();
			errorMap.put("error", Arrays.asList(e.getMessage()));
			throw new ValidationException("Can't find count", errorMap);
		}
	}

	protected void setBasicFields(M obj, Document doc) {
		if (doc.get(_ID) != null) {
			if (doc.get(_ID) instanceof ObjectId) {
				obj.setId(doc.getObjectId(_ID).toHexString());
			}
		}
		obj.setCreatedBy(doc.getLong("created_by"));
		obj.setUpdatedBy(doc.getLong("updated_by"));
		if (doc.getString("created_time") != null)
			obj.setCreatedTime(LocalDateTime.parse(doc.getString("created_time")));
		if (doc.getString("updated_time") != null)
			obj.setUpdatedTime(LocalDateTime.parse(doc.getString("updated_time")));
	}
	

	@SuppressWarnings("unchecked")
	protected Class<M> getGenericModelClass() throws ClassNotFoundException {
		try {
			return (Class<M>) Class.forName(((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0].getTypeName());
		} catch (ClassNotFoundException cnfe1) {

			try {
				return (Class<M>) Class.forName(((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0].getTypeName(), true,
						getClass().getClassLoader());
			} catch (ClassNotFoundException cnfe2) {

				return (Class<M>) Application.class.getClassLoader().loadClass(((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0].getTypeName());
			}

		}

	}


}
