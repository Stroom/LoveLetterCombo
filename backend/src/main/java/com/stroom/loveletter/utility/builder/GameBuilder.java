package com.stroom.loveletter.utility.builder;

import com.stroom.loveletter.game.Game;
import com.stroom.loveletter.game.GameOptions;
import com.stroom.loveletter.game.GameStatus;
import com.stroom.loveletter.game.Player;
import com.stroom.loveletter.game.round.GameRound;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stroom on 31/05/2017.
 */
@Getter
public class GameBuilder {
	private String id;
	private List<Player> players;
	private List<GameRound> rounds;
	private GameRound currentRound;
	private GameStatus gameStatus;
	private List<Player> winners;
	
	private GameOptions gameOptions;
	
	public GameBuilder(String id, List<Player> players, GameOptions gameOptions) {
		this.id = id;
		this.players = players;
		this.gameOptions = gameOptions;
		this.rounds = new ArrayList<GameRound>();
		this.currentRound = null;
		calculatePoints();
		this.gameStatus = GameStatus.CREATED;
		this.winners = new ArrayList<Player>();
	}
	
	private void calculatePoints() {
		if(this.players.size() > 1) {//TODO maybe some other options
			this.gameOptions.setWinningPoints(this.gameOptions.getTotalPoints()/this.players.size()+1);
		}
		else {
			this.gameOptions.setWinningPoints(null);
		}
	}
	
	public GameBuilder gameStatus(GameStatus gameStatus) {
		this.gameStatus = gameStatus;
		return this;
	}
	
	public GameBuilder addRound(GameRound round) {
		this.rounds.add(round);
		this.currentRound = round;
		return this;
	}
	
	public GameBuilder winners(List<Player> winners) {
		this.winners = winners;
		return this;
	}
	
	public Game build() {
		return new Game(this);
	}
	
}
