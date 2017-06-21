package com.stroom.website.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Stroom on 12/06/2017.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class RegistrationDTO {
	
	private String username;
	private String password;
	
}
