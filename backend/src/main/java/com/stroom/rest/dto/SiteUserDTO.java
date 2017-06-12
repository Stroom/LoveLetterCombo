package com.stroom.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by Stroom on 12/06/2017.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class SiteUserDTO {
	
	private String _id;
	
	private String username;
	
	private List<String> authorities;
	
}
