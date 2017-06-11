package com.stroom.loveletter.utility.builder;

import com.stroom.loveletter.game.Game;
import com.stroom.loveletter.game.Player;
import com.stroom.loveletter.game.round.Board;
import com.stroom.loveletter.game.round.GameRound;
import com.stroom.loveletter.game.round.RoundPlayer;
import com.stroom.loveletter.game.round.RoundStatus;
import lombok.Getter;

import java.util.List;

/**
 * Created by Stroom on 31/05/2017.
 */
@Getter
public class RoundBuilder {
	
	private Game game;
	
	private Integer roundNumber;
	private Board board;
	private List<RoundPlayer> activePlayers;
	private List<RoundPlayer> eliminatedPlayers;
	private Integer currentPlayerId;
	private RoundStatus roundStatus;
	
	private List<Player> winners;
	
	public RoundBuilder(Game game, Integer roundNumber, Board board, List<RoundPlayer> activePlayers,
						List<RoundPlayer> eliminatedPlayers, Integer currentPlayerId, RoundStatus roundStatus,
						List<Player> winners) {
		this.game = game;
		this.roundNumber = roundNumber;
		this.board = board;
		this.activePlayers = activePlayers;
		this.eliminatedPlayers = eliminatedPlayers;
		this.currentPlayerId = currentPlayerId;
		this.roundStatus = roundStatus;
		this.winners = winners;
	}
	
	//All the info is required so for safety measure, all is needed in the builder.
	
	//TODO other builder without players - so you can add players with their info separately - for loading a game.
	
	public GameRound build() {
		return new GameRound(this);
	}
}
