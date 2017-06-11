package com.stroom.loveletter.game.round;

import com.stroom.loveletter.card.Card;
import com.stroom.loveletter.utility.builder.BoardBuilder;
import com.stroom.loveletter.utility.exception.ActionImpossibleException;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.List;

/**
 * Created by Stroom on 31/05/2017.
 */
@Getter
@ToString
public class Board {
	@NonNull
	private List<Card> deck;
	@NonNull
	private List<Card> revealedCards;
	@NonNull
	private Card removedCard;
	
	public Board(BoardBuilder builder) {
		this.deck = builder.getDeck();
		this.revealedCards = builder.getRevealedCards();
		this.removedCard = builder.getRemovedCard();
	}
	
	public Card drawCard() {
		if(!deck.isEmpty()) {
			return deck.remove(0);
		}
		else if(removedCard != null) {//TODO sometimes you can draw it, sometimes not. Hopefully outside checks make sure this is the case.
			//TODO maybe add a flag saying whether you can draw the removed card or not? For additional safety measure.
			Card card = removedCard;
			removedCard = null;
			return card;
		}
		else {
			throw new ActionImpossibleException();
		}
	}
	
	@Override
	public boolean equals(Object other) {
		if(other != null && other.getClass().equals(this.getClass())) {
			Board that = (Board) other;
			return this.deck.equals(that.deck) &&
					this.revealedCards.equals(that.revealedCards) &&
					(this.removedCard == null && that.removedCard == null || this.removedCard.equals(that.removedCard));
		}
		return false;
	}
}
