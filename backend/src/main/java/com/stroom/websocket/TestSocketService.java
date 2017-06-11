package com.stroom.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Created by Stroom on 11/06/2017.
 */
@Service
public class TestSocketService {
	
	private final MessageSendingOperations<String> messagingTemplate;
	
	@Autowired
	public TestSocketService(MessageSendingOperations<String> messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}
	
	@Scheduled(fixedDelay = 2000)
	public void sendPeriodicTestMessage() {
		this.messagingTemplate.convertAndSend("/topic/greetings", TestMessageResponse.of("trololo"));
	}
	
}
