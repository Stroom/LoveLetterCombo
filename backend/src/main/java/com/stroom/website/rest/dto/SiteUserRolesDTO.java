package com.stroom.website.rest.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by Stroom on 16/06/2017.
 */
@Data
public class SiteUserRolesDTO {
	
	private final List<String> authorities;
	
	public SiteUserRolesDTO(List<String> authorities) {
		this.authorities = authorities;
	}
	
}
