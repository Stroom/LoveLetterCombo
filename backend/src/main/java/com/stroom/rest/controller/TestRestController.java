package com.stroom.rest.controller;

import org.apache.log4j.Logger;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * To see if Angular connects to RestControllers.
 * Created by Stroom on 10/06/2017.
 */
@RestController
@RequestMapping("/api/test")
@CrossOrigin
public class TestRestController {
	
	private final Logger log = Logger.getLogger(TestRestController.class.getName());
	
	@GetMapping
	@PreAuthorize("hasRole('USER')")
	public String test() {
		log.info("TestRestController");
		return "TestResponse";
	}
	
}
