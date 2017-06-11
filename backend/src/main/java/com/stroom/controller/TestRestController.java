package com.stroom.controller;

import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * To see if Angular connects to RestControllers.
 * Created by Stroom on 10/06/2017.
 */
@RestController
@RequestMapping("test")
@CrossOrigin
public class TestRestController {
	
	static Logger log = Logger.getLogger(TestRestController.class.getName());
	
	@GetMapping
	public String test() {
		log.info("TestRestController");
		return "TestResponse";
	}
}
