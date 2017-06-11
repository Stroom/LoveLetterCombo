package com.stroom.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by Stroom on 10/06/2017.
 */
@Data
@AllArgsConstructor(staticName = "of")
public class TestMessageResponse {
	
	private String text;
	
}
