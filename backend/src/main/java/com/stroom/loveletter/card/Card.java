package com.stroom.loveletter.card;

import com.stroom.loveletter.card.action.result.CardActionResult;
import com.stroom.loveletter.game.round.GameRound;
import com.stroom.loveletter.game.round.RoundPlayer;
import com.stroom.loveletter.game.round.action.PlayerMove;
import com.stroom.loveletter.game.round.action.PossibleMove;
import com.stroom.loveletter.game.round.action.Target;
import lombok.Getter;
import lombok.NonNull;

/**
 * Created by Stroom on 31/05/2017.
 */
@Getter
public abstract class Card implements Comparable<Card> {
	@NonNull
	private Integer number;
	@NonNull
	private String name;
	@NonNull
	private TargetType targetType;
	@NonNull
	private EffectType effectType;
	@NonNull
	private PlayRestriction playRestriction;
	
	public Card(Integer number, String name, TargetType targetType, EffectType effectType, PlayRestriction playRestriction) {
		this.number = number;
		this.name = name;
		this.targetType = targetType;
		this.effectType = effectType;
		this.playRestriction = playRestriction;
	}
	
	@Override
	public int compareTo(Card c2) {
		return this.number.compareTo(c2.getNumber());
	}
	
	public String toString() {
		return "[" + number + " " + name + "]";
	}
	
	public abstract PossibleMove getPossibleMove(GameRound gameRound, RoundPlayer currentPlayer);
	
	public abstract CardActionResult performAction(GameRound gameRound, PlayerMove playerMove);
	//TODO reactionary cards in expansion
	
	public abstract boolean isValidMove(GameRound gameRound, Target target);
	
	@Override
	public abstract boolean equals(Object other);
	
}
