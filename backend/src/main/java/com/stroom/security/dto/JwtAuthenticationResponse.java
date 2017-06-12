package com.stroom.security.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Stroom on 12/06/2017.
 */
@Data
public class JwtAuthenticationResponse implements Serializable {
	
	@JsonIgnore
	private final long serialVersionUID = 1250166508152483573L;
	
	private final String token;
	
	private final List<String> authorities;
	
	public JwtAuthenticationResponse(String token, List<String> authorities) {
		this.token = token;
		this.authorities = authorities;
	}
	
}
