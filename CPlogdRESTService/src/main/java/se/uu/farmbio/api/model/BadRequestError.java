package se.uu.farmbio.api.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.ws.rs.core.Response.Status;

import org.json.simple.JSONObject;

import com.fasterxml.jackson.annotation.JsonProperty;




/**
 * BadRequestError
 */
public class BadRequestError extends ErrorResponse  {
	
	public BadRequestError(Status status, String message, List<String> fields) {
		super(status, message);
		this.fields = fields;
	}
	
	public BadRequestError(int code, String message, List<String> fields) {
		super(code, message);
		this.fields = fields;
	}

	@JsonProperty("fields")
	private List<String> fields = new ArrayList<String>();

	public BadRequestError fields(List<String> fields) {
		this.fields = fields;
		return this;
	}

	public BadRequestError addFieldsItem(String fieldsItem) {
		this.fields.add(fieldsItem);
		return this;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		BadRequestError badRequestError = (BadRequestError) o;
		return Objects.equals(this.fields, badRequestError.fields) &&
				super.equals(o);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fields);
	}

	@SuppressWarnings("unchecked")
	@Override
	public String toString() {
		JSONObject jsonResponse = super.toJSON();
		jsonResponse.put("fields", fields);

		return jsonResponse.toJSONString();
	}



}

