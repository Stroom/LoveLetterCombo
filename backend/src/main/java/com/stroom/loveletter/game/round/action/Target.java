package com.stroom.loveletter.game.round.action;

import com.stroom.loveletter.card.action.effect.AbstractEffect;
import com.stroom.loveletter.game.round.RoundPlayer;
import com.stroom.loveletter.utility.exception.ActionImpossibleException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.javatuples.Pair;

import java.util.List;

/**
 * Created by Stroom on 31/05/2017.
 */
@Getter
@AllArgsConstructor(staticName = "of")
@ToString
public class Target {
	@NonNull
	private List<Pair<RoundPlayer, AbstractEffect>> targets;
	//Could be 1 target in base game (null element if no target), up to 2 targets in expansion.
	
	/**
	 * Expecting to get only one target player.
	 */
	public RoundPlayer getOneTarget() {
		if(this.targets.size() == 1) {
			return this.targets.get(0).getValue0();
		}
		throw new ActionImpossibleException();
	}
	
	/**
	 * Expecting to get only one target effect.
	 */
	public AbstractEffect getOneEffect() {
		if(this.targets.size() == 1) {
			return this.targets.get(0).getValue1();
		}
		throw new ActionImpossibleException();
	}
	
	@Override
	public boolean equals(Object other) {
		if(other != null && other.getClass().equals(this.getClass())) {
			Target that = (Target) other;
			if(this.targets.size() == that.targets.size()) {
				for (int i = 0; i < this.targets.size(); i++) {
					if(this.targets.get(i).getValue0() == null || that.targets.get(i).getValue0() == null) {
						if(!this.targets.get(i).getValue1().equals(that.targets.get(i).getValue1())) {
							return false;
						}
					}
					else if(!this.targets.get(i).getValue0().getPlayer().equals( that.targets.get(i).getValue0().getPlayer()) ||
							!this.targets.get(i).getValue1().equals(that.targets.get(i).getValue1())) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder().append("Target(targets=[");
		targets.stream().forEach(pair -> {
			if(pair.getValue0() == null) {
				builder.append("null, ");
			}
			else {
				builder.append(pair.getValue0().getPlayer().toString() + ", ");
			}
			
			builder.append(pair.getValue1());
		});
		builder.append("])");
		return builder.toString();
	}
}
