package com.stroom.website.websocket.controller;

import com.stroom.website.websocket.dto.TestMessageRequestDTO;
import com.stroom.website.websocket.dto.TestMessageResponseDTO;
import org.apache.log4j.Logger;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/**
 * Created by Stroom on 10/06/2017.
 */
@Controller
public class TestSocketController {
	
	private final Logger log = Logger.getLogger(TestSocketController.class.getName());
	
	@MessageMapping("/hello")
	@SendTo("/topic/greetings")
	public TestMessageResponseDTO testMessage(TestMessageRequestDTO message) throws Exception {
		log.info("Message: " + message.getText());
		return TestMessageResponseDTO.of("Reply to: " + message.getText());
	}
	
}
