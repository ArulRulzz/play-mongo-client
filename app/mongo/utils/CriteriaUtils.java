/**
 * 
 */
package mongo.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

/**
 * @author arul.g
 *
 */
public class CriteriaUtils {

	public static final String DISTINCT = "distinct";
	public static final String PFIELD = "pfield";
	public static final String RCOUNT = "rcount";
	public static final String TOTAL_RECORD_COUNT = "total_record_count";
	public static final String FILTERED_RECORD_COUNT = "filtered_record_count";
	public static final String REGEX = "regex";
	public static final String TYPE = "type";
	public static final String PARENT_ID = "parent_id";
	public static final String ALL = "all";
	public static final String ARRAY = "arr";
	public static final String SORT_ASC = "asc";
	public static final String SORT_DESC = "desc";
	public static final String STARTS_WITH = "starts_with";
	public static final Object PAGE_NO = "pno";
	public static final Object NO_OF_RECORDS = "nor";


	public static Map<String, List<String>> queryStringArrayToList(Map<String, String[]> queryStringArray) {
		Map<String, List<String>> queryStringMap = new HashMap<>();
		if (queryStringArray != null && !queryStringArray.isEmpty()) {
			queryStringArray.forEach((k, v) -> {
				List<String> valList = Arrays.asList(v).stream().map(map -> {
					
					return map;
				}).collect(Collectors.toList());
				queryStringMap.put(k, valList);
			});
		}

		return queryStringMap;
	}

	public static Map<String, String[]> queryStringListToArray(Map<String, List<String>> criteriaMap) {
		Map<String, String[]> queryStringMapArray = new HashMap<>();
		if (criteriaMap != null && !criteriaMap.isEmpty()) {
			criteriaMap.forEach((k, v) -> {
				String arr[] = new String[v.size()];
				queryStringMapArray.put(k, v.toArray(arr));
			});
		}
		return queryStringMapArray;
	}

		public static List<String> getQueryStringNameList(List<String> qryStringNameLst) {
		return qryStringNameLst.parallelStream().filter(nameValue -> {
			return StringUtils.isNotEmpty(nameValue) ? true : false;
		}).collect(Collectors.toList());
	}
}