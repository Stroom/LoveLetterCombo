package com.stroom.loveletter.card.action.result;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by Stroom on 03/06/2017.
 */
@Data
@AllArgsConstructor(staticName = "of")
public class CountessActionResult extends CardActionResult {
	private String result;
}
