package com.stroom.security.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by Stroom on 16/06/2017.
 */
@Data
public class JwtAuthRolesDTO {
	
	private final List<String> authorities;
	
	public JwtAuthRolesDTO(List<String> authorities) {
		this.authorities = authorities;
	}
	
}
