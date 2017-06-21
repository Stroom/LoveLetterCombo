package com.stroom.website.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by Stroom on 10/06/2017.
 */
@Data
@AllArgsConstructor(staticName = "of")
public class TestMessageResponseDTO {
	
	private String text;
	
}
