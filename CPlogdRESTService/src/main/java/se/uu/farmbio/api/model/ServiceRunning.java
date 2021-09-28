package se.uu.farmbio.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ServiceRunning {
	
	public static final String OK_MSG = "Service running";
	
	@JsonProperty
	public final String message = OK_MSG;
	
	public String toString() {
		return message;
	}
	
}
