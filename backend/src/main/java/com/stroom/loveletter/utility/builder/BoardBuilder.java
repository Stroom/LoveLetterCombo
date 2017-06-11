package com.stroom.loveletter.utility.builder;

import com.stroom.loveletter.card.Card;
import com.stroom.loveletter.game.round.Board;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stroom on 31/05/2017.
 */
@Getter
public class BoardBuilder {
	private List<Card> deck;
	private List<Card> revealedCards;
	private Card removedCard;
	
	public BoardBuilder(List<Card> cards) {
		//TODO check deck count validity
		this.deck = cards;
		this.revealedCards = new ArrayList<Card>();
		this.removedCard = null;
	}
	
	//TODO must call this or set the removed card manually to build. TODO maybe better to move this under the game - adding a round.
	public BoardBuilder initiateGame(Integer playerCount) {
		if(playerCount == 2) {
			for (int i = 0; i < 3; i++) {
				revealedCards.add(this.deck.remove(0));//TODO catch IndexOutOfBoundsException
			}
		}
		this.removedCard = this.deck.remove(0);
		return this;
	}
	
	//TODO other builders for when need to load a game.
	
	public BoardBuilder removedCard(Card removedCard) {
		this.removedCard = removedCard;
		return this;
	}
	
	public Board build() {
		return new Board(this);
	}
	
	
}
