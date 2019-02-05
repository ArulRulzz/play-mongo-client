package mongo.builder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.client.FindIterable;

import mongo.models.base.repository.MongoBaseRepository;
import mongo.utils.CriteriaUtils;
import mongo.utils.JsonUtils;

public class MongoQueryBuilder {

	public static final int TOTAL_RECORDS = 10;

	public Document buildSimpleQuery(Map<String, Object> criteriaMap) {
		Document doc = new Document();
		doc.putAll(criteriaMap);
		return doc;
	}

	public Document buildQuery(Map<String, List<Object>> criteriaMap, boolean isRegex,
			Map<String, String[]> queryStringWithArray) {
		Document doc = new Document();
		JsonNode qsJson = JsonUtils.toJson(queryStringWithArray);
		if (queryStringWithArray != null) {
			qsJson = JsonUtils.toJson(queryStringWithArray);
			if (qsJson != null && qsJson.hasNonNull(CriteriaUtils.ARRAY)
					&& qsJson.get(CriteriaUtils.ARRAY).hasNonNull(0)) {
				String key = qsJson.get(CriteriaUtils.ARRAY).get(0).asText();
				if (qsJson.has(key) && qsJson.get(key).hasNonNull(0)) {
					doc.put(key, new Document("$in", Arrays.asList(qsJson.get(key).get(0).asText())));
				}
			}
		}

		criteriaMap.forEach((key, val) -> {
			Document queryDoc = new Document();
			boolean starts_with = JsonUtils.isValidField(JsonUtils.toJson(queryStringWithArray),
					CriteriaUtils.STARTS_WITH);
			if ((isRegex || starts_with) && val != null && val.get(0) != null && val.get(0) instanceof String) {
				if (starts_with) {
					queryDoc.put(key, new Document("$regex", "^" + val.get(0)).append("$options", "i"));
				} else {
					queryDoc.put(key, new Document("$regex", val.get(0)).append("$options", "i"));
				}
			} else {

				if (val.size() == 1) {
					// single value
					Object value = val.get(0);

					if (value.equals("true") || value.equals("false")) {
						queryDoc.put(key, Boolean.valueOf(value.toString()));
					} else {
						queryDoc.put(key, new Document("$eq", val.get(0)));
					}

				} else {
					// multiple values
					queryDoc.put(key, new Document("$in", val));

				}

			}
			doc.putAll(queryDoc);
		});
		return doc;

	}

	public Document buildQuery(Map<String, List<Object>> criteriaMap, boolean isRegex) {
		return buildQuery(criteriaMap, isRegex, null);
	}

	public Document buildQuery(Map<String, List<Object>> criteriaMap) {
		return buildQuery(criteriaMap, false);
	}

	public Document buildProjection(List<String> fieldList) {
		Document projectFields = new Document();
		if (fieldList != null) {
			fieldList.forEach((field) -> {
				projectFields.putAll(new Document(field, 1));
			});
		}
		return projectFields;
	}

	public Document buildGroupBy(List<String> groupByFields) {
		Document groupByDoc = new Document();
		if (groupByFields != null) {
			groupByFields.forEach(field -> {
				groupByDoc.append(field, "$" + field);
			});
		}
		return groupByDoc;

	}

	public Document buildSortQuery(Map<String, List<String>> queryString) {

		Document sortByDoc = new Document();

		if (queryString != null && queryString.get(CriteriaUtils.SORT_ASC) != null
				&& !queryString.get(CriteriaUtils.SORT_ASC).isEmpty()) {
			queryString.get(CriteriaUtils.SORT_ASC).forEach(sortField -> {
				sortByDoc.put(sortField, 1);
			});
		}

		if (queryString != null && queryString.get(CriteriaUtils.SORT_DESC) != null
				&& !queryString.get(CriteriaUtils.SORT_DESC).isEmpty()) {
			queryString.get(CriteriaUtils.SORT_DESC).forEach(sortField -> {
				sortByDoc.put(sortField, -1);
			});
		}

		return sortByDoc;
	}

	public int getPaginationSkipRecordCount(Map<String, List<String>> queryString) {

		try {
			if (queryString != null && queryString.get(CriteriaUtils.PAGE_NO) != null
					&& !queryString.get(CriteriaUtils.PAGE_NO).isEmpty()
					&& queryString.get(CriteriaUtils.NO_OF_RECORDS) != null
					&& !queryString.get(CriteriaUtils.NO_OF_RECORDS).isEmpty()) {

				int skip = 0;
				int pageNo = Integer.parseInt(queryString.get(CriteriaUtils.PAGE_NO).get(0));
				if (pageNo > 1) {
					skip = (pageNo - 1) * Integer.parseInt(queryString.get(CriteriaUtils.NO_OF_RECORDS).get(0));
				}
				return skip;
			}
		} catch (NumberFormatException skippingExce) {
			return 0;
		}
		return 0;
	}

	public int getPaginationTotalRecordCount(Map<String, List<String>> queryString) {
		try {
			if (queryString != null && queryString.get(CriteriaUtils.NO_OF_RECORDS) != null
					&& !queryString.get(CriteriaUtils.NO_OF_RECORDS).isEmpty()) {
				return Integer.parseInt(queryString.get(CriteriaUtils.NO_OF_RECORDS).get(0));
			}
		} catch (NumberFormatException skippingExce) {
			return TOTAL_RECORDS;
		}
		return TOTAL_RECORDS;

	}

	public void setPaginationFieldsForAggregation(List<Document> aggreList, Map<String, List<String>> queryString) {

		aggreList.add(new Document("$skip", getPaginationSkipRecordCount(queryString)));
		int limitVal = getPaginationTotalRecordCount(queryString);
		if (limitVal > 0) {
			aggreList.add(new Document("$limit", limitVal));
		}

	}

	public void setPaginationFieldsForFind(FindIterable<Document> findItr, Map<String, List<String>> queryString) {

		findItr.skip(getPaginationSkipRecordCount(queryString));
		int limitVal = getPaginationTotalRecordCount(queryString);
		if (limitVal > 0) {
			findItr.limit(limitVal);
		}
	}

	public void fillFindCriteria(final FindIterable<Document> findItr, Map<String, List<String>> queryString) {
		if (findItr != null && queryString != null) {
			List<String> projection = queryString.get(CriteriaUtils.PFIELD);

			if (projection != null && !projection.isEmpty()) {
				findItr.projection(buildProjection(projection));
			}
			Document sortQuery = buildSortQuery(queryString);
			if (sortQuery != null && !sortQuery.isEmpty()) {
				findItr.sort(sortQuery);
			}
			int recordCount = getPaginationTotalRecordCount(queryString);
			if (recordCount > 0)
				findItr.limit(recordCount);

			int skipRecordCount = getPaginationSkipRecordCount(queryString);
			if (skipRecordCount > 0)
				findItr.skip(skipRecordCount);
		}
	}

	public void fillAggregationCriteria(final List<Document> aggreList, Map<String, List<String>> queryString) {

		if (aggreList != null && queryString != null) {
			List<String> projection = queryString.get(CriteriaUtils.PFIELD);
			if (projection != null && !projection.isEmpty()) {
				aggreList.add(new Document("$project", buildProjection(projection)));
			}

			Document sortBy = buildSortQuery(queryString);
			if (sortBy != null && !sortBy.isEmpty()) {
				aggreList.add(new Document("$sort", sortBy));
			}

			Document groupBy = buildGroupBy(projection);

			if (groupBy != null && !groupBy.isEmpty()) {
				aggreList.add(new Document("$group", new Document(MongoBaseRepository._ID, groupBy)));
			}

			int recordCount = getPaginationTotalRecordCount(queryString);
			if (recordCount > 0)
				aggreList.add(new Document("$limit", recordCount));

			int skipRecordCount = getPaginationSkipRecordCount(queryString);
			if (skipRecordCount > 0)
				aggreList.add(new Document("$skip", skipRecordCount));
		}

	}

	public void fillAggregationCountCriteria(List<Document> aggreList, Map<String, List<String>> queryString) {

		List<String> groupBy = queryString.get(CriteriaUtils.PFIELD);
		if (groupBy != null && !groupBy.isEmpty()) {
			aggreList.add(new Document("$group", new Document(MongoBaseRepository._ID, buildGroupBy(groupBy))));
		}
		Document countDocGroupBy = new Document(MongoBaseRepository._ID, null);
		countDocGroupBy.put("count", new Document("$sum", 1));
		aggreList.add(new Document("$group", countDocGroupBy));

	}
}
