package com.stroom.loveletter.game.round;

import com.stroom.loveletter.card.Card;
import com.stroom.loveletter.game.Player;
import com.stroom.loveletter.game.round.action.CardPlayType;
import com.stroom.loveletter.game.round.action.PlayedCard;
import com.stroom.loveletter.game.round.action.Target;
import com.stroom.loveletter.utility.exception.InvalidGameStateException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.javatuples.Triplet;

import java.util.Comparator;
import java.util.List;

/**
 * Created by Stroom on 31/05/2017.
 */
@Getter
@AllArgsConstructor(staticName = "of")
@ToString
public class RoundPlayer {
	@NonNull
	private Player player;
	@NonNull
	private Hand hand;
	@NonNull
	private List<PlayedCard> playedCards;//TODO instead of this or additionally, track active effects?
	@NonNull
	private PlayerStatus status;
	//TODO Jester token
	//TODO discarded cards effects
	
	public RoundPlayer addCard(Card card) {
		this.hand.getCards().add(card);
		return this;
	}
	
	public RoundPlayer discardHand() {
		this.playedCards.add(PlayedCard.of(this.hand.removeCard(), CardPlayType.DISCARDED, null));
		return this;
	}
	
	public RoundPlayer playCard(Card card, Target target) {
		this.playedCards.add(PlayedCard.of(hand.removeCard(card), CardPlayType.PLAYED, target));
		return this;
	}
	
	public RoundPlayer setCard(Card targetCard) {
		this.hand.setCard(targetCard);
		return this;
	}
	
	public Triplet<RoundPlayer,Integer,Integer> calculateScore() {
		if(hand.getCards().size() == 1) {
			//TODO in cases of end-game score, expansion might increase Card score.
			return new Triplet<RoundPlayer, Integer, Integer>(this, this.hand.getCards().get(0).getNumber(), this.calculateSumOfPlayedCards());
		}
		else {
			throw new InvalidGameStateException();
		}
	}
	
	/**
	 * Used only when the round is over and you have to compare who of the alive players wins.
	 */
	public static Comparator<Triplet<RoundPlayer, Integer, Integer>> scoreComparator() {
		return (o1, o2) -> {
			//Compare the card-in-hand value
			//Princess is stronger than bishop although 9 > 8 !!!
			/*if(o1.getValue1() == 8 && o2.getValue1() == 9) {
				return 1;
			}
			else if(o1.getValue1() == 9 && o2.getValue1() == 8) {
				return -1;
			}
			else TODO this is expansion part*/
			if(o1.getValue1() > o2.getValue1()) {
				return 1;
			}
			else if(o1.getValue1() < o2.getValue1()) {
				return -1;
			}
			//If these are tied, compare the played cards sum value.
			else if(o1.getValue2() > o2.getValue2()) {
				return 1;
			}
			else if(o1.getValue2() < o2.getValue2()) {
				return -1;
			}
			//If these are tied too, draw.
			else {
				return 0;
			}
		};
	}
	
	public Card getLastPlayedCard() {
		if(!playedCards.isEmpty()) {
			return playedCards.stream().filter(card -> card.getCardPlayType() == CardPlayType.PLAYED).reduce((a, b) -> b).get().getCard();
		}
		return null;
	}
	
	public Integer calculateSumOfPlayedCards() {
		return playedCards.stream().mapToInt(card -> card.getCard().getNumber()).sum();
	}
	
	public void eliminate() {
		this.status = PlayerStatus.ELIMINATED;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other != null && other.getClass().equals(this.getClass())) {
			RoundPlayer that = (RoundPlayer) other;
			return this.player.equals(that.player) &&
					this.hand.equals(that.hand) &&
					this.playedCards.equals(that.playedCards) &&
					this.status.equals(that.status);
		}
		return false;
	}
}
