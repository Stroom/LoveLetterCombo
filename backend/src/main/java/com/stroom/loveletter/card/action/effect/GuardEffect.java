package com.stroom.loveletter.card.action.effect;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * Created by Stroom on 02/06/2017.
 */
@Getter
@AllArgsConstructor(staticName = "of")
@ToString
public class GuardEffect extends AbstractEffect {
	@NonNull
	Integer numberGuess;
	
	@Override
	public boolean equals(Object other) {
		if(other != null && other.getClass().equals(this.getClass())) {
			GuardEffect that = (GuardEffect) other;
			return this.numberGuess.equals(that.numberGuess);
		}
		return false;
	}
}
