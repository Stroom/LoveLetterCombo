package com.stroom.loveletter.card;

import com.stroom.loveletter.card.action.effect.AbstractEffect;
import com.stroom.loveletter.card.action.result.CardActionResult;
import com.stroom.loveletter.card.action.result.PrincessActionResult;
import com.stroom.loveletter.game.round.GameRound;
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
public class Princess extends Card {
	
	public Princess() {
		super(8, "Princess", TargetType.SELF, EffectType.INSTANT, PlayRestriction.LOSE_WHEN_DISCARDED);
	}
	
	@Override
	public PossibleMove getPossibleMove(GameRound gameRound, RoundPlayer currentPlayer) {
		List<RoundPlayer> players = new ArrayList<RoundPlayer>();
		List<AbstractEffect> effects = new ArrayList<AbstractEffect>();
		
		players.add(null);//Can play on no-one.
		
		effects.add(VoidEffect.of());
		
		return PossibleMove.of(this, players, effects, new Pair<Integer, Integer>(0,0));
	}
	
	@Override
	public CardActionResult performAction(GameRound gameRound, PlayerMove playerMove) {
		PrincessActionResult actionResult = null;
		if(playerMove.isValid(gameRound)) {
			//MSG: Create message: CurrentPlayer is playing a Princess
			//Find who the affected player(s) are
			RoundPlayer currentPlayer = gameRound.getRoundPlayerByPlayer(playerMove.getPlayer());
			//Remove the used card from the players hand
			currentPlayer.playCard(playerMove.getCard(), playerMove.getTarget());
			//Since the player discarded a princess, he loses.
			currentPlayer.discardHand();
			gameRound.eliminatePlayer(currentPlayer);
			actionResult = PrincessActionResult.of("Success");
			
			//MSG: Send result to currentPlayer.
			
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
