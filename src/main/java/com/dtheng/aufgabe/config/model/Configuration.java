package com.dtheng.aufgabe.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Getter
@Setter
@ToString
public class Configuration {

	@JsonProperty("http_port") private int httpPort;

	@JsonProperty("db_user") private String databaseUser;
	@JsonProperty("db_password") private String databasePassword;
	@JsonProperty("db_port") private int databasePort;
	@JsonProperty("db_name") private String databaseName;

}
