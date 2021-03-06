package com.stroom.website.rest.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by Stroom on 12/06/2017.
 */
@Data
public class LoginRequestDTO implements Serializable {
	
	private static final long serialVersionUID = -8445943548965154778L;
	
	private String username;
	private String password;
	
	public LoginRequestDTO() {
		super();
	}
	
	public LoginRequestDTO(String username, String password) {
		this.setUsername(username);
		this.setPassword(password);
	}
	
}
