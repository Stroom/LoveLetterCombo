package com.stroom.loveletter.card.action.result;

import com.stroom.loveletter.card.Card;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by Stroom on 03/06/2017.
 */
@Data
@AllArgsConstructor(staticName = "of")
public class PriestActionResult extends CardActionResult {
	private String result;
	private Card targetsCard;
}
