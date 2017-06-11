package com.stroom.loveletter.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.Comparator;

/**
 * Created by Stroom on 31/05/2017.
 */
@Getter
@AllArgsConstructor(staticName = "of")
@ToString
public class Player {
	@NonNull
	private String name;
	@NonNull
	private Integer points;
	
	public Player addPoint() {
		this.points++;
		return this;
	}
	
	public static Comparator<Player> playerScoreComparator() {
		return Comparator.comparing(Player::getPoints);
	}
	
	@Override
	public boolean equals(Object other) {
		if(other != null && other.getClass().equals(this.getClass())) {
			Player that = (Player) other;
			return this.name.equals(that.name) &&
					this.points.equals(that.points);
		}
		return false;
	}
}
