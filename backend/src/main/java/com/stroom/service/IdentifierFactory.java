package com.stroom.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Created by Stroom on 12/06/2017.
 */
@Service
public class IdentifierFactory {
	
	public String nextID() {
		return UUID.randomUUID().toString();
	}
}
