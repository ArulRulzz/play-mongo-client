package mongo.models.base.constants;

public enum BaseModelConstant {

	_ID("_id"),

	CREATED_TIME("created_time"),

	UPDATED_TIME("updated_time"),

	CREATED_BY("created_by"),

	UPDATED_BY("updated_by"),

	VERSION("version");

	private String value;

	private BaseModelConstant(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}

}
