package com.stroom.loveletter.game.round;

import com.stroom.loveletter.card.Card;
import com.stroom.loveletter.card.King;
import com.stroom.loveletter.card.PlayRestriction;
import com.stroom.loveletter.card.Prince;
import com.stroom.loveletter.card.action.result.CardActionResult;
import com.stroom.loveletter.game.Game;
import com.stroom.loveletter.game.Player;
import com.stroom.loveletter.game.round.action.MoveRoundResult;
import com.stroom.loveletter.game.round.action.PlayerMove;
import com.stroom.loveletter.game.round.action.PossibleMove;
import com.stroom.loveletter.utility.builder.RoundBuilder;
import com.stroom.loveletter.utility.exception.InvalidGameStateException;
import com.stroom.loveletter.utility.exception.PlayerNotFoundException;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Stroom on 31/05/2017.
 */
@Getter
@ToString(exclude = "game")
public class GameRound {
	@NonNull
	private Game game;//TODO maybe not needed.
	@NonNull
	private Integer roundNumber;
	@NonNull
	private Board board;
	@NonNull
	private List<RoundPlayer> activePlayers;
	@NonNull
	private List<RoundPlayer> eliminatedPlayers;
	@NonNull
	private Integer currentPlayerId;
	@NonNull
	private RoundStatus roundStatus;
	
	private List<Player> winners;
	
	public GameRound(RoundBuilder builder) {
		this.game = builder.getGame();
		this.roundNumber = builder.getRoundNumber();
		this.board = builder.getBoard();
		this.activePlayers = builder.getActivePlayers();
		this.eliminatedPlayers = builder.getEliminatedPlayers();
		this.currentPlayerId = builder.getCurrentPlayerId();
		this.roundStatus = builder.getRoundStatus();
		this.winners = builder.getWinners();
	}
	
	public void start() {//TODO check validity
		//TODO maybe give cards starting with currentPlayer?
		for(RoundPlayer player : this.activePlayers) {
			player.addCard(this.board.drawCard());
		}
		this.activePlayers.get(this.currentPlayerId).addCard(this.board.drawCard());
		this.roundStatus = RoundStatus.RUNNING;
	}
	
	/**
	 * Performs a move for the player.
	 */
	public Pair<MoveRoundResult, CardActionResult> performMove(PlayerMove playerMove) {
		CardActionResult result = playerMove.getCard().performAction(this, playerMove);
		
		if(isRoundOver()) {
			//If round is over, add points to all winners.
			this.winners.stream().forEach(p -> p.addPoint());
			//TODO maybe some extra checks based on card effects in expansion? Someone else might also get points.
			return new Pair<MoveRoundResult, CardActionResult>(MoveRoundResult.ROUND_ENDS, result);
		}
		else {
			//If round is not over, set next player
			setNextPlayer();
			return new Pair<MoveRoundResult, CardActionResult>(MoveRoundResult.ROUND_CONTINUES, result);
		}
	}
	
	public void eliminatePlayer(RoundPlayer targetPlayer) {
		Integer playerId = this.activePlayers.indexOf(targetPlayer);
		this.activePlayers.remove(targetPlayer);
		this.eliminatedPlayers.add(targetPlayer);
		targetPlayer.eliminate();
		//TODO check if the player has a card with some kind of ability (expansion)
		if(playerId <= this.currentPlayerId) {
			this.currentPlayerId--;//If currentPlayer is removed, temporarily makes the previous player the "current player".
		}
	}
	
	/**
	 * Moves the turn to the next player
	 */
	public void setNextPlayer() {
		//Find the next active player and set it.
		Integer nextPlayerId = this.currentPlayerId;
		nextPlayerId++;
		if(nextPlayerId >= activePlayers.size()) {
			nextPlayerId = 0;
		}
		if(nextPlayerId == this.currentPlayerId) {
			throw new InvalidGameStateException();
		}
		this.currentPlayerId = nextPlayerId;
		
		//Draw a card for that player.
		this.activePlayers.get(currentPlayerId).addCard(this.board.drawCard());
	}
	
	/**
	 * Checks if the round is over or not, writes down the winners.
	 */
	private boolean isRoundOver() {
		//TODO maybe check first if deck is not empty and at least players some are active? Wastes less time.
		//Find scores of all active players. Should have at least one.
		this.winners = new ArrayList<Player>();
		List<Triplet<RoundPlayer, Integer, Integer>> bestScores = new ArrayList<Triplet<RoundPlayer, Integer, Integer>>();
		for(RoundPlayer player : this.activePlayers) {
			bestScores.add(player.calculateScore());
		}
		if(bestScores.isEmpty()) {
			//TODO maybe there is a possibility that there are 0 winners?
			throw new InvalidGameStateException();
		}
		if(this.currentPlayerId < 0) {
			this.currentPlayerId = this.activePlayers.size()-1;
		}
		//Only one alive.
		if(bestScores.size() == 1){
			this.winners.add(bestScores.get(0).getValue0().getPlayer());
			this.roundStatus = RoundStatus.OVER;
			return true;
		}
		//If deck is empty, compare all alive players. Must have at least 2 at this point.
		if(this.board.getDeck().isEmpty()) {
			bestScores.sort(Collections.reverseOrder(RoundPlayer.scoreComparator()));
			//The first scoring player in the list is definitely a winner.
			this.winners.add(bestScores.get(0).getValue0().getPlayer());
			this.roundStatus = RoundStatus.OVER;
			//From the highest score, check whether the next score is smaller or not.
			//If scores are equal, add the player to winners list and continue checking from the next score.
			for (int i = 0; i < bestScores.size()-2 ; i++) {
				int cmp = RoundPlayer.scoreComparator().compare(bestScores.get(i), bestScores.get(i+1));
				if(cmp == 0) {
					this.winners.add(bestScores.get(i+1).getValue0().getPlayer());
				}
				else {
					break;
				}
			}
			//Set currentPlayerId to the last winner whose turn it was.
			if(this.currentPlayerId >= 0) {
				for(int i = this.currentPlayerId; i >= 0; i--) {
					if(this.winners.contains(activePlayers.get(i).getPlayer())) {
						this.currentPlayerId = i;
						return true;
					}
				}
				for(int i = this.activePlayers.size()-1; i > this.currentPlayerId; i--) {
					if(this.winners.contains(activePlayers.get(i).getPlayer())) {
						this.currentPlayerId = i;
						return true;
					}
				}
			}
			//Should never reach this place. If it does, fails silently.
			System.err.println("Unexpected did not find winner ID.");
			return true;
		}
		return false;
	}
	
	public Pair<Player, List<PossibleMove>> findPossibleMoves() {
		RoundPlayer current = this.activePlayers.get(this.currentPlayerId);
		List<PossibleMove> moves = new ArrayList<PossibleMove>();
		
		//TODO add expansion checks...
		
		//Find out who can be the possible targets to your cards
		Card card1 = current.getHand().getCards().get(0);
		PossibleMove possible1 = card1.getPossibleMove(this, current);
		Card card2 = current.getHand().getCards().get(1);
		PossibleMove possible2 = card2.getPossibleMove(this, current);
		
		if (possible1.getCard().getPlayRestriction() == PlayRestriction.PLAY_WITH_PRINCE_OR_KING &&
				(card2.getClass().equals(Prince.class) || card2.getClass().equals(King.class))) {
			moves.add(possible1);
		}
		else if (possible2.getCard().getPlayRestriction() == PlayRestriction.PLAY_WITH_PRINCE_OR_KING &&
				(card1.getClass().equals(Prince.class) || card1.getClass().equals(King.class))) {
			moves.add(possible2);
		}
		else {
			moves.add(possible1);
			moves.add(possible2);
		}
		return new Pair<Player, List<PossibleMove>>(current.getPlayer(), moves);
	}
	
	public RoundPlayer getRoundPlayerByPlayer(Player player) {
		for (RoundPlayer rp : this.activePlayers) {
			if(rp.getPlayer().equals(player)) {
				return rp;
			}
		}
		throw new PlayerNotFoundException();
	}
	
	public RoundPlayer getCurrentPlayer() {
		return this.activePlayers.get(currentPlayerId);
	}
	
	@Override
	public boolean equals(Object other) {
		if(other != null && other.getClass().equals(this.getClass())) {
			GameRound that = (GameRound) other;
			return that.game != null && this.game.getId().equals(that.game.getId()) && //No need for a deeper check. Otherwise you would get circular reference.
					this.roundNumber.equals(that.roundNumber) &&
					this.board.equals(that.board) &&
					this.activePlayers.equals(that.activePlayers) &&
					this.eliminatedPlayers.equals(that.eliminatedPlayers) &&
					this.currentPlayerId.equals(that.currentPlayerId) &&
					this.roundStatus.equals(that.roundStatus) &&
					this.winners.equals(that.winners);
		}
		return false;
	}
	
	/**
	 * This should only be used by tests.
	 * Have to make sure nothing else touches this method.
	 */
	public void setGame(Game expectedGame) {
		this.game = expectedGame;
	}
}
