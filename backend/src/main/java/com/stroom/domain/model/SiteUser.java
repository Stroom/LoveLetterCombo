package com.stroom.domain.model;

import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

/**
 * Created by Stroom on 11/06/2017.
 */
@Entity
@Getter
public class SiteUser {
	@Id @GeneratedValue
	String id;
	
	@NotNull
	private String username;
	@NotNull
	private String password; //TODO use hashes...
	
}
