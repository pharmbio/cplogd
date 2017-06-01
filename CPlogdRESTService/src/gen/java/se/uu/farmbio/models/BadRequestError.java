package se.uu.farmbio.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{\n");
		sb.append("    \"code\": ").append(FormattingUtils.toIndentedString(getCode())).append(",\n");
		sb.append("    \"message\": ").append(FormattingUtils.toIndentedString("\""+getMessage()+"\"")).append(",\n");
		sb.append("    \"fields\": ").append(FormattingUtils.toIndentedString(fieldsToString())).append('\n');
		sb.append("}\n");
		return sb.toString();
	}
	
	private String fieldsToString() {
		StringBuilder sb = new StringBuilder("[");
		for(int i = 0; i<fields.size(); i++) {
			sb.append("\""+fields.get(i)+"\"");
			if (i < fields.size()-1)
				sb.append(", ");
		}
		
		sb.append("]");
		return sb.toString();
	}
	
}

