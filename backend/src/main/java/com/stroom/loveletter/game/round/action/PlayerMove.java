package com.stroom.loveletter.game.round.action;

import com.stroom.loveletter.card.Card;
import com.stroom.loveletter.card.action.effect.AbstractEffect;
import com.stroom.loveletter.game.Player;
import com.stroom.loveletter.game.round.GameRound;
import com.stroom.loveletter.game.round.RoundPlayer;
import com.stroom.loveletter.utility.exception.PlayerNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.javatuples.Pair;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Stroom on 01/06/2017.
 */
@Getter
@AllArgsConstructor(staticName = "of")
@ToString
public class PlayerMove {
	private Player player;
	private Card card;
	private Target target;
	
	public boolean isValid(GameRound gameRound) {
		//Check if it is this players turn
		RoundPlayer currentRoundPlayer = gameRound.getActivePlayers().get(gameRound.getCurrentPlayerId());
		if(this.player != currentRoundPlayer.getPlayer()) {
			return false;
		}
		//Check if the player is holding the card mentioned
		boolean hasCard = false;
		for(Card c : currentRoundPlayer.getHand().getCards()) {
			if(this.card.getClass().equals(c.getClass())) {
				hasCard = true;
			}
		}
		if(!hasCard) {
			return false;
		}
		//Find out who can be targeted by this card and what card effect is associated with it.
		PossibleMove possibleMove = this.card.getPossibleMove(gameRound, currentRoundPlayer);
		List<RoundPlayer> possibleTargets = possibleMove.getTargets();
		//TODO might not need to check class if card validation also checks.
		List<Class> possibleEffects = possibleMove.getEffects().stream().map(e -> e.getClass()).collect(Collectors.toList());
		//Check if the amount of targets is within the allowed range. null player is allowed.
		if((possibleMove.getRangeOfTargets().getValue0() > this.target.getTargets().size() ||
				possibleMove.getRangeOfTargets().getValue1() < this.target.getTargets().size()) &&
				!(possibleMove.getRangeOfTargets().getValue0() == 0 &&
						possibleMove.getRangeOfTargets().getValue1() == 0 &&
						this.target.getTargets().size() == 1 && this.target.getTargets().get(0).getValue0() == null)) {
			return false;
		}
		//Check if all targets are allowed to be targeted and the Effect type is the same as expected.
		for(Pair<RoundPlayer, AbstractEffect> targetPlayer : this.target.getTargets()) {
			try {
				if(!possibleTargets.contains(targetPlayer.getValue0()) || !possibleEffects.contains(targetPlayer.getValue1().getClass())) {
					return false;
				}
			}
			catch (PlayerNotFoundException e) {
				return false;//Target player does not exist. Should not happen.
			}
		}
		//Check if the card validation also allows playing with the given targets (expansion stuff)
		if(!this.card.isValidMove(gameRound, this.target)) {
			return false;
		}
		return true;
	}
	
}
