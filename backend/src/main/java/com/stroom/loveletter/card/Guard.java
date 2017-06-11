package com.stroom.loveletter.card;

import com.stroom.loveletter.card.action.effect.AbstractEffect;
import com.stroom.loveletter.card.action.effect.GuardEffect;
import com.stroom.loveletter.card.action.result.CardActionResult;
import com.stroom.loveletter.card.action.result.GuardActionResult;
import com.stroom.loveletter.game.round.GameRound;
import com.stroom.loveletter.game.round.PlayerStatus;
import com.stroom.loveletter.game.round.RoundPlayer;
import com.stroom.loveletter.game.round.action.PlayerMove;
import com.stroom.loveletter.game.round.action.PossibleMove;
import com.stroom.loveletter.game.round.action.Target;
import com.stroom.loveletter.utility.exception.ActionImpossibleException;
import lombok.Getter;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stroom on 31/05/2017.
 */
@Getter
public class Guard extends Card {
	
	public Guard() {
		super(1, "Guard", TargetType.OTHER, EffectType.INSTANT, PlayRestriction.NONE);
	}
	
	@Override
	public PossibleMove getPossibleMove(GameRound gameRound, RoundPlayer currentPlayer) {
		List<RoundPlayer> players = new ArrayList<RoundPlayer>();
		List<AbstractEffect> effects = new ArrayList<AbstractEffect>();
		
		for(RoundPlayer player : gameRound.getActivePlayers()) {
			Card lastPlayedCard = player.getLastPlayedCard();
			if(player != currentPlayer && player.getStatus() != PlayerStatus.ELIMINATED &&
					(lastPlayedCard == null || !(lastPlayedCard.getClass().equals(Handmaid.class)))) {
				players.add(player);
			}
		}
		
		if(players.isEmpty()) {
			players.add(null);//Can play on no-one.
		}
		
		effects.add(GuardEffect.of(-1));
		
		return PossibleMove.of(this, players, effects, new Pair<Integer, Integer>(1,1));
	}
	
	@Override
	public CardActionResult performAction(GameRound gameRound, PlayerMove playerMove) {
		GuardActionResult actionResult = null;
		if(playerMove.isValid(gameRound)) {
			//MSG: Create message: CurrentPlayer is playing a Guard
			//Find who the affected player(s) are
			RoundPlayer currentPlayer = gameRound.getRoundPlayerByPlayer(playerMove.getPlayer());
			RoundPlayer targetPlayer = playerMove.getTarget().getOneTarget();
			//Remove the used card from the players hand
			currentPlayer.playCard(playerMove.getCard(), playerMove.getTarget());
			
			if(targetPlayer != null) {
				//Find what card(s) the player(s) have
				Card currentCard = currentPlayer.getHand().getCard();
				Card targetCard = targetPlayer.getHand().getCard();
				
				//Check if the guessed number is the same as the card number
				GuardEffect guess = (GuardEffect) playerMove.getTarget().getOneEffect();
				if(targetCard.getNumber().compareTo(guess.getNumberGuess()) == 0) {
					targetPlayer.discardHand();
					gameRound.eliminatePlayer(targetPlayer);
					//MSG: TargetPlayer guessed correctly.
					actionResult = GuardActionResult.of("Success");
				}
				else {
					//MSG: TargetPlayer did not guess correctly.
					actionResult = GuardActionResult.of("Fail");
				}
			}
			else {
				//MSG: The player could not target anyone.
				actionResult = GuardActionResult.of("NoTarget");
			}
			
			//MSG: Send useful information back to both players.
			
			//MSG: Send info about the action to other players
			return actionResult;
		}
		else {
			throw new ActionImpossibleException();
		}
	}
	
	@Override
	public boolean isValidMove(GameRound gameRound, Target target) {
		Pair<RoundPlayer, AbstractEffect> pair = target.getTargets().get(0);
		if(pair.getValue1().getClass().equals(GuardEffect.class)) {
			Integer guess = ((GuardEffect)pair.getValue1()).getNumberGuess();
			//TODO options for expansion
			if(guess > 1 && guess < 9) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other != null && other.getClass().equals(this.getClass())) {
			return true;
		}
		return false;
	}
	
}
