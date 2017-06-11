package com.stroom.loveletter.card;

import com.stroom.loveletter.card.action.effect.AbstractEffect;
import com.stroom.loveletter.card.action.result.CardActionResult;
import com.stroom.loveletter.card.action.result.PrinceActionResult;
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
public class Prince extends Card {
	
	public Prince() {
		super(5, "Prince", TargetType.EITHER, EffectType.INSTANT, PlayRestriction.NONE);
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
		
		players.add(currentPlayer);
		
		effects.add(VoidEffect.of());
		
		return PossibleMove.of(this, players, effects, new Pair<Integer, Integer>(1,1));
	}
	
	@Override
	public CardActionResult performAction(GameRound gameRound, PlayerMove playerMove) {
		PrinceActionResult actionResult = null;
		if(playerMove.isValid(gameRound)) {
			//MSG: Create message: CurrentPlayer is playing a Prince
			//Find who the affected player(s) are
			RoundPlayer currentPlayer = gameRound.getRoundPlayerByPlayer(playerMove.getPlayer());
			RoundPlayer targetPlayer = playerMove.getTarget().getOneTarget();
			//Remove the used card from the players hand
			currentPlayer.playCard(playerMove.getCard(), playerMove.getTarget());
			
			if(targetPlayer != null) {
				//Find what card(s) the player(s) have
				Card targetCard = targetPlayer.getHand().getCard();
				//Perform the move based on the card and target(s)
				targetPlayer.discardHand();
				//MSG: TargetPlayer discarded targetCard.
				if(targetCard.getPlayRestriction() == PlayRestriction.LOSE_WHEN_DISCARDED) {
					gameRound.eliminatePlayer(targetPlayer);
				}
				else {
					targetPlayer.addCard(gameRound.getBoard().drawCard());
				}
				actionResult = PrinceActionResult.of("Success", targetCard);
			}
			else {
				throw new ActionImpossibleException();
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
