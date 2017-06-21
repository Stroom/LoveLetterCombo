package com.stroom.website.websocket.service;

import com.stroom.website.websocket.dto.TestMessageResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Created by Stroom on 11/06/2017.
 */
@Service
public class TestSocketService {
	
	private final MessageSendingOperations<String> messagingTemplate;
	
	private final SimpMessagingTemplate simpMessagingTemplate;
	
	@Autowired
	public TestSocketService(MessageSendingOperations<String> messagingTemplate, SimpMessagingTemplate simpMessagingTemplate) {
		this.messagingTemplate = messagingTemplate;
		this.simpMessagingTemplate = simpMessagingTemplate;
	}
	
	@Scheduled(fixedDelay = 2000)
	public void sendPeriodicTestMessage() {
		this.messagingTemplate.convertAndSend("/topic/greetings", TestMessageResponseDTO.of("trololo"));
		this.simpMessagingTemplate.convertAndSendToUser("admin", "/queue/reply", TestMessageResponseDTO.of("Admin: trololo"));
	}
	
}
