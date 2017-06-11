package com.stroom.loveletter.game.round.action;

import com.stroom.loveletter.card.action.effect.AbstractEffect;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Used when there is no need to add additional info to the card effect.
 * Created by Stroom on 02/06/2017.
 */
@Getter
@AllArgsConstructor(staticName = "of")
@ToString
public class VoidEffect extends AbstractEffect {
	
	@Override
	public boolean equals(Object other) {
		if(other != null && other.getClass().equals(this.getClass())) {
			return true;
		}
		return false;
	}
}
