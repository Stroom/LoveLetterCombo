package com.stroom.loveletter.card;

import com.stroom.loveletter.card.action.effect.AbstractEffect;
import com.stroom.loveletter.card.action.result.BaronActionResult;
import com.stroom.loveletter.card.action.result.CardActionResult;
import com.stroom.loveletter.game.round.GameRound;
import com.stroom.loveletter.game.round.PlayerStatus;
import com.stroom.loveletter.game.round.RoundPlayer;
import com.stroom.loveletter.game.round.action.PlayerMove;
import com.stroom.loveletter.game.round.action.PossibleMove;
import com.stroom.loveletter.game.round.action.Target;
import com.stroom.loveletter.game.round.action.VoidEffect;
import com.stroom.loveletter.utility.exception.ActionImpossibleException;
import lombok.Getter;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stroom on 31/05/2017.
 */
@Getter
public class Baron extends Card {
	
	public Baron() {
		super(3, "Baron", TargetType.OTHER, EffectType.INSTANT, PlayRestriction.NONE);
	}
	
	@Override
	public PossibleMove getPossibleMove(GameRound gameRound, RoundPlayer currentPlayer) {
		List<RoundPlayer> players = new ArrayList<RoundPlayer>();
		List<AbstractEffect> effects = new ArrayList<AbstractEffect>();
		
		for(RoundPlayer player : gameRound.getActivePlayers()) {
			Card lastPlayedCard = player.getLastPlayedCard();
			if(player != currentPlayer && player.getStatus() != PlayerStatus.ELIMINATED && (lastPlayedCard == null || !(lastPlayedCard.getClass().equals(Handmaid.class)))) {
				players.add(player);
			}
		}
		
		if(players.isEmpty()) {
			players.add(null);//Can play on no-one.
		}
		
		effects.add(VoidEffect.of());
		
		return PossibleMove.of(this, players, effects, new Pair<Integer, Integer>(1,1));
	}
	
	@Override
	public CardActionResult performAction(GameRound gameRound, PlayerMove playerMove) {
		BaronActionResult actionResult = null;
		if(playerMove.isValid(gameRound)) {
			//MSG: Create message: CurrentPlayer is playing a Baron
			//Find who the affected player(s) are
			RoundPlayer currentPlayer = gameRound.getRoundPlayerByPlayer(playerMove.getPlayer());
			RoundPlayer targetPlayer = playerMove.getTarget().getOneTarget();
			//Remove the used card from the players hand
			currentPlayer.playCard(playerMove.getCard(), playerMove.getTarget());
			
			if(targetPlayer != null) {
				//Find what card(s) the player(s) have
				Card currentCard = currentPlayer.getHand().getCard();
				Card targetCard = targetPlayer.getHand().getCard();
				//Perform the move based on the card and target(s)
				int cmp = currentCard.compareTo(targetCard);
				if(cmp > 0) {
					targetPlayer.discardHand();
					gameRound.eliminatePlayer(targetPlayer);
					//MSG: CurrentPlayer won. TargetPlayer had targetCard.
					actionResult = BaronActionResult.of("Success", currentCard, targetCard);
				}
				else if(cmp < 0) {
					currentPlayer.discardHand();
					gameRound.eliminatePlayer(currentPlayer);
					//MSG: TargetPlayer won. CurrentPlayer had currentCard.
					actionResult = BaronActionResult.of("Fail", currentCard, targetCard);
				}
				else {
					//MSG: There was a draw.
					actionResult = BaronActionResult.of("Draw", currentCard, targetCard);
				}
			}
			else {
				//MSG: The player could not target anyone.
				actionResult = BaronActionResult.of("NoTarget", null, null);
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
		if(pair.getValue1().getClass().equals(VoidEffect.class)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	@Override
	public boolean equals(Object other) {
		if(other != null && other.getClass().equals(this.getClass())) {
			return true;
		}
		return false;
	}
	
}
