package se.uu.farmbio.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.json.simple.JSONObject;

import io.swagger.annotations.ApiModelProperty;




/**
 * BadRequestError
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-03-03T13:55:11.972+01:00")
public class BadRequestError extends Error  {

	public BadRequestError(int code, String message, List<String> fields) {
		super(code, message);
		this.fields = fields;
	}

	private List<String> fields = new ArrayList<String>();

	public BadRequestError fields(List<String> fields) {
		this.fields = fields;
		return this;
	}

	public BadRequestError addFieldsItem(String fieldsItem) {
		this.fields.add(fieldsItem);
		return this;
	}

	/**
	 * Relevant field(s)
	 * @return fields
	 **/
	@ApiModelProperty(required = true, value = "Relevant field(s)")
	public List<String> getFields() {
		return fields;
	}

	public void setFields(List<String> fields) {
		this.fields = fields;
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
		return Objects.hash(getCode(), getMessage(), fields);
	}

	@SuppressWarnings("unchecked")
	@Override
	public String toString() {
		JSONObject jsonResponse = new JSONObject();
		jsonResponse.put("code", getCode());
		jsonResponse.put("message", getMessage());
		jsonResponse.put("fields", fields);
		
		return jsonResponse.toJSONString();
	}
	

	
}

