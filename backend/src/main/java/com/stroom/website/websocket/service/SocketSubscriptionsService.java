package com.stroom.website.websocket.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

/**
 * Created by Stroom on 21/06/2017.
 */
@Service
public class SocketSubscriptionsService {
	//https://stackoverflow.com/questions/37340271/how-to-send-message-to-user-when-he-connects-to-spring-websocket
	
	//Seems like existing subscription stuff is not what I need, have to implement it myself.
	
	//Needs to use a Map<String, Subscription> - (channel name - lobby, game_ID), (subscription object - List<User>, methods for sending info)
	//Might have to use another map to map subscription ID to its name.
	
	@Autowired
	private SimpMessagingTemplate template;
	
	@EventListener
	public void handleSubscribeEvent(SessionSubscribeEvent event) {
		System.out.println(event.toString() + " " + event.getUser().getName());
	}
	
	@EventListener
	public void handleUnsubscribeEvent(SessionUnsubscribeEvent event) {
		System.out.println(event.toString() + " " + event.getUser().getName());
	}
	
	@EventListener
	public void handleDisconnectEvent(SessionDisconnectEvent event) {
		System.out.println(event.toString() + " " + event.getUser().getName());
	}
	
}
