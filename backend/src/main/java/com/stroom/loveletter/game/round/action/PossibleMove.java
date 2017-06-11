package com.stroom.loveletter.game.round.action;

import com.stroom.loveletter.card.Card;
import com.stroom.loveletter.card.action.effect.AbstractEffect;
import com.stroom.loveletter.game.round.RoundPlayer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.javatuples.Pair;

import java.util.List;

/**
 * Created by Stroom on 01/06/2017.
 */
@Getter
@AllArgsConstructor(staticName = "of")
@ToString
public class PossibleMove {
	private Card card;
	private List<RoundPlayer> targets;
	private List<AbstractEffect> effects;
	private Pair<Integer, Integer> rangeOfTargets;
	
	@Override
	public boolean equals(Object other) {
		if(other != null && other.getClass().equals(this.getClass())) {
			PossibleMove that = (PossibleMove) other;
			return this.card.equals(that.card) &&
					this.targets.equals(that.targets) &&
					this.effects.equals(that.effects) &&
					this.rangeOfTargets.equals(that.rangeOfTargets);
		}
		return false;
	}
}
