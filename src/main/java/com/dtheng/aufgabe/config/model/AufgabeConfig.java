package com.dtheng.aufgabe.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Optional;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Getter
@Setter
@ToString
public class AufgabeConfig {

    @JsonProperty("http_port") private int httpPort;

    @JsonProperty("db_user") private String databaseUser;
    @JsonProperty("db_password") private String databasePassword;
    @JsonProperty("db_port") private int databasePort;
    @JsonProperty("db_name") private String databaseName;

    @JsonProperty("time_zone") private String timeZone;

    @JsonProperty("device_type") private AufgabeDeviceType deviceType;

    @JsonProperty("sync_remote_ip") private Optional<String> syncRemoteIp = Optional.empty();

    @JsonProperty("bonusly_enabled") private boolean bonuslyEnabled;
    @JsonProperty("bonusly_access_token") private Optional<String> bonuslyAccessToken = Optional.empty();
    @JsonProperty("bonusly_api_endpoint") private Optional<String> bonuslyApiEndpoint = Optional.empty();

    @JsonProperty("security_public_key") private String publicKey;
    @JsonProperty("security_private_key") private String privateKey;
}
