package com.stroom.loveletter.game.round;

import com.stroom.loveletter.card.Card;
import com.stroom.loveletter.utility.exception.ActionImpossibleException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stroom on 31/05/2017.
 */
@Getter
@AllArgsConstructor(staticName = "of")
@ToString
public class Hand {
	@NonNull
	private List<Card> cards;//0-2 cards. Non-null.
	
	public Hand() {
		this.cards = new ArrayList<Card>();
	}
	
	/**
	 * If the player holds only one card, returns it.
	 */
	public Card getCard() {
		if(this.cards.size() == 1) {
			return this.cards.get(0);
		}
		throw new ActionImpossibleException();
	}
	
	/**
	 * If the player holds only one card, swaps it with the given one.
	 */
	public Card setCard(Card card) {
		if(this.cards.size() == 1) {
			return this.cards.set(0, card);
		}
		throw new ActionImpossibleException();
	}
	
	/**
	 * If the player holds only one card, removes it from the hand and returns it.
	 */
	public Card removeCard() {
		if(this.cards.size() == 1) {
			return this.cards.remove(0);
		}
		throw new ActionImpossibleException();
	}
	
	/**
	 * Removes the given card type from currentPlayer hand if possible.
	 */
	public Card removeCard(Card card) {
		for (Card c : this.cards) {
			if(c.getClass().equals(card.getClass())) {
				this.cards.remove(c);
				return c;
			}
		}
		throw new ActionImpossibleException();
	}
	
	@Override
	public boolean equals(Object other) {
		if(other != null && other.getClass().equals(this.getClass())) {
			Hand that = (Hand) other;
			return this.cards.equals(that.cards);
		}
		return false;
	}
}
