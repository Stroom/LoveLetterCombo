package com.stroom.loveletter.game.round.action;

import com.stroom.loveletter.card.Card;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * Created by Stroom on 31/05/2017.
 */
@Getter
@AllArgsConstructor(staticName = "of")
@ToString
public class PlayedCard {
	@NonNull
	private Card card;
	private CardPlayType cardPlayType;
	private Target target;
	
	@Override
	public boolean equals(Object other) {
		if(other != null && other.getClass().equals(this.getClass())) {
			PlayedCard that = (PlayedCard) other;
			return this.card.equals(that.card) &&
					this.cardPlayType.equals(that.cardPlayType) &&
					((this.target == null && that.target == null) || this.target.equals(that.target));
		}
		return false;
	}
}
