package com.stroom.loveletter.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * Created by Stroom on 01/06/2017.
 */
@Getter
@AllArgsConstructor(staticName = "of")
@ToString
public class GameOptions {
	@NonNull
	private Integer totalPoints;
	private Integer winningPoints;
	
	//TODO playerCount range?
	
	//In case when the round ends and some players have the same numbers in their hands, compare the sums of played/discarded cards in front of each player.
	//If the sums are also the same, all players win.
	//"Officially", the player who was last on a date, starts the next round.
	//TODO add options for these draw situations?
	//Starting player: 1) Random winner 2) Last winner in reverse turn order
	//TODO I think best (default) would be 2
	@NonNull
	private MultipleRoundWinnersNextRoundStartingPlayer multipleRoundWinnersNextRoundStartingPlayer;
	
	//TODO what if players get enough points to win at the end of the round?
	//1) Random winner 2) Another game round until the tie has been broken 3) Draw between the players
	//TODO I think the best default would be 3
	@NonNull
	private MultipleGameWinnersSolution multipleGameWinnersSolution;
	
	//TODO what if multiple players reach enough points to win but someone is ahead? Unsolved - currently just more points wins.
	
	//TODO can game end in the middle of a round? When someone gains affection token mid-round.
	
	public void setWinningPoints(Integer winningPoints) {
		this.winningPoints = winningPoints;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other != null && other.getClass().equals(this.getClass())) {
			GameOptions that = (GameOptions) other;
			return this.totalPoints.equals(that.totalPoints) &&
					this.winningPoints.equals(that.winningPoints) &&
					this.multipleRoundWinnersNextRoundStartingPlayer.equals(that.multipleRoundWinnersNextRoundStartingPlayer) &&
					this.multipleGameWinnersSolution.equals(that.multipleGameWinnersSolution);
		}
		return false;
	}
}
