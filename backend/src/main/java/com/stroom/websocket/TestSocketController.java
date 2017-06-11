package com.stroom.websocket;

import org.apache.log4j.Logger;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/**
 * Created by Stroom on 10/06/2017.
 */
@Controller
public class TestSocketController {
	
	static Logger log = Logger.getLogger(TestSocketController.class.getName());
	
	@MessageMapping("/hello")
	@SendTo("/topic/greetings")
	public TestMessageResponse testMessage(TestMessageRequest message) throws Exception {
		log.info("Message: " + message.getText());
		return TestMessageResponse.of("Reply to: " + message.getText());
	}
	
}
