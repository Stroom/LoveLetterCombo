package com.stroom.loveletter.game;

import com.stroom.loveletter.card.*;
import com.stroom.loveletter.card.action.result.CardActionResult;
import com.stroom.loveletter.game.round.*;
import com.stroom.loveletter.game.round.action.*;
import com.stroom.loveletter.utility.builder.BoardBuilder;
import com.stroom.loveletter.utility.builder.GameBuilder;
import com.stroom.loveletter.utility.builder.RoundBuilder;
import com.stroom.loveletter.utility.exception.ActionImpossibleException;
import com.stroom.loveletter.utility.exception.GameIsOverException;
import com.stroom.loveletter.utility.exception.InvalidGameStateException;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Created by Stroom on 31/05/2017.
 */
@Getter
@ToString
public class Game {
	@NonNull
	private String id;
	@NonNull
	private List<Player> players;
	@NonNull
	private List<GameRound> rounds;
	private GameRound currentRound;//TODO maybe use currentRoundID instead? Easier on the toString.
	@NonNull
	private GameStatus gameStatus;
	private List<Player> winners;//TODO maybe use Set instead of List when order is not important?
	@NonNull
	private GameOptions gameOptions;
	
	public Game(GameBuilder builder) {
		this.id = builder.getId();
		this.players = builder.getPlayers();
		this.rounds = builder.getRounds();
		this.currentRound = builder.getCurrentRound();
		this.gameStatus = builder.getGameStatus();
		this.winners = builder.getWinners();
		this.gameOptions = builder.getGameOptions();
	}
	
	public void start(List<Card> cards) {
		this.gameStatus = GameStatus.RUNNING;
		//TODO maybe shuffle players list, pick random starting player... Needs more game options.
		newRound(cards, 1, 0);
	}
	
	public void newRound(GameRound previousRound) {
		if(previousRound.getRoundStatus() != RoundStatus.OVER) {
			throw new InvalidGameStateException();
		}
		Integer roundNumber = previousRound.getRoundNumber()+1;
		//Find out who should start the next round.
		Integer startingPlayerId = null;
		List<Player> winners = previousRound.getWinners();
		Player starter = null;
		switch(gameOptions.getMultipleRoundWinnersNextRoundStartingPlayer()) {
			case RANDOM_WINNER:
				starter = winners.get(ThreadLocalRandom.current().nextInt(winners.size()));
				startingPlayerId = this.players.indexOf(starter);
				break;
			case LAST_TURN_WINNER:
				starter = previousRound.getActivePlayers().get(previousRound.getCurrentPlayerId()).getPlayer();
				startingPlayerId = this.players.indexOf(starter);
				break;
		}
		newRound(initRandomDeck(), roundNumber, startingPlayerId);
	}
	
	public void newRound(List<Card> cards, Integer roundNumber, Integer startingPlayerId) {
		Board board = new BoardBuilder(cards).initiateGame(this.players.size()).build();
		List<RoundPlayer> activePlayers = this.players.stream().map(player -> RoundPlayer.of(
				player, new Hand(), new ArrayList<PlayedCard>(), PlayerStatus.ACTIVE)).collect(Collectors.toList());
		GameRound round = new RoundBuilder(this, roundNumber, board, activePlayers, new ArrayList<>(),
				startingPlayerId, RoundStatus.CREATED, new ArrayList<>()).build();
		this.rounds.add(round);
		this.currentRound = round;
		this.currentRound.start();
	}
	
	/**
	 * Performs the move in the game.
	 * Returns change of game state, change of round state and card action result
	 */
	public Triplet<MoveGameResult, MoveRoundResult, CardActionResult> performMove(PlayerMove playerMove) {
		synchronized (this) {
			//Make the current round perform the move
			Pair<MoveRoundResult, CardActionResult> moveResult = this.currentRound.performMove(playerMove);
			
			//If round ended, check whether the game ends too or to start a new round
			if(moveResult.getValue0() == MoveRoundResult.ROUND_ENDS) {
				if(isGameOver()) {
					//MSG: Send result of previous move and game over message
					return new Triplet<MoveGameResult, MoveRoundResult, CardActionResult>
							(MoveGameResult.GAME_ENDS, moveResult.getValue0(), moveResult.getValue1());
				}
				else {
					newRound(this.currentRound);
					//MSG: Send result of previous move and new round message
					return new Triplet<MoveGameResult, MoveRoundResult, CardActionResult>
							(MoveGameResult.GAME_CONTINUES, moveResult.getValue0(), moveResult.getValue1());
				}
			}
			else {
				//MSG: Send result of previous move
				return new Triplet<MoveGameResult, MoveRoundResult, CardActionResult>
						(MoveGameResult.GAME_CONTINUES, moveResult.getValue0(), moveResult.getValue1());
			}
		}
	}
	
	public Pair<Player, List<PossibleMove>> findPossibleMoves() {
		if(!isGameOver()) {
			return this.currentRound.findPossibleMoves();
		}
		else {
			throw new GameIsOverException();
		}
	}
	
	public GameRound getPreviousRound() {
		if(this.rounds.size() > 1) {
			return this.rounds.get(this.rounds.size()-2);
		}
		throw new ActionImpossibleException();
	}
	
	public boolean isGameOver() {
		//Get a list of players who have enough points to win.
		List<Player> winners = this.players.stream().filter(p -> p.getPoints() >= this.gameOptions.getWinningPoints())
				.collect(Collectors.toList());
		
		if(winners.size() == 0) {
			return false;
		}
		if(winners.size() == 1) {
			this.winners.add(winners.get(0));
			this.gameStatus = GameStatus.OVER;
			return true;
		}
		//Order the list based on points.
		winners.sort(Collections.reverseOrder(Player.playerScoreComparator()));
		
		//If 2 or more players have enough points to win, determine result by game option.
		//Find out how many players have the most points.
		Integer maxPoints = winners.get(0).getPoints();
		winners = winners.stream().filter(n -> n.getPoints() == maxPoints).collect(Collectors.toList());
		if(winners.size() == 1) {
			this.winners.add(winners.get(0));
			this.gameStatus = GameStatus.OVER;
			return true;
		}
		else {
			switch(gameOptions.getMultipleGameWinnersSolution()) {
				case RANDOM_WINNER:
					this.winners.add(winners.get(ThreadLocalRandom.current().nextInt(winners.size())));
					this.gameStatus = GameStatus.OVER;
					return true;
				case ANOTHER_ROUND_UNTIL_TIE_BROKEN:
					return false;
				case DRAW_AMONG_WINNERS:
					this.winners.addAll(winners);
					this.gameStatus = GameStatus.OVER;
					return true;
			}
		}
		
		return false;
	}
	
	public static List<Card> initRandomDeck() {
		//TODO based on expansions etc.
		ArrayList<Card> cards = new ArrayList<Card>();
		for (int i = 0; i < 5; i++) {
			cards.add(new Guard());
		}
		for (int i = 0; i < 2; i++) {
			cards.add(new Priest());
			cards.add(new Baron());
			cards.add(new Handmaid());
			cards.add(new Prince());
		}
		cards.add(new King());
		cards.add(new Countess());
		cards.add(new Princess());
		Collections.shuffle(cards);
		return cards;
	}
	
	public static List<Card> initStaticDeck() {
		ArrayList<Card> cards = new ArrayList<Card>();
		
		cards.add(new Guard());
		cards.add(new Guard());
		
		cards.add(new Priest());
		cards.add(new Baron());
		cards.add(new Handmaid());
		cards.add(new Prince());
		
		cards.add(new King());
		cards.add(new Countess());
		cards.add(new Princess());
		
		for (int i = 0; i < 3; i++) {
			cards.add(new Guard());
		}
		
		cards.add(new Priest());
		cards.add(new Baron());
		cards.add(new Handmaid());
		cards.add(new Prince());
		
		return cards;
	}
	
	
	@Override
	public boolean equals(Object other) {
		if(other != null && this.getClass().equals(other.getClass())) {
			Game that = (Game) other;
			return this.id.equals(that.id) &&
					this.players.equals(that.players) &&
					this.rounds.equals(that.rounds) &&
					this.currentRound.equals(that.currentRound) &&
					this.gameStatus.equals(that.gameStatus) &&
					this.winners.equals(that.winners) &&
					this.gameOptions.equals(that.gameOptions);
		}
		return false;
	}
	
}
