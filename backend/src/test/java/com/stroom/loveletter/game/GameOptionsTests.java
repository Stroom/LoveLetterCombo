package com.stroom.loveletter.game;

import com.stroom.loveletter.card.*;
import com.stroom.loveletter.card.action.effect.AbstractEffect;
import com.stroom.loveletter.card.action.effect.GuardEffect;
import com.stroom.loveletter.card.action.result.CardActionResult;
import com.stroom.loveletter.card.action.result.GuardActionResult;
import com.stroom.loveletter.game.round.*;
import com.stroom.loveletter.game.round.action.*;
import com.stroom.loveletter.utility.builder.BoardBuilder;
import com.stroom.loveletter.utility.builder.GameBuilder;
import com.stroom.loveletter.utility.builder.RoundBuilder;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Created by Stroom on 04/06/2017.
 */
public class GameOptionsTests {
	
	private Game game;
	private List<Player> players;
	private List<Player> expectedPlayers;
	
	@Before
	public void initTest() {
		players = Arrays.asList(Player.of("PlayerA", 0), Player.of("PlayerB", 0), Player.of("PlayerC", 0));
		expectedPlayers = Arrays.asList(Player.of("PlayerA", 0), Player.of("PlayerB", 0), Player.of("PlayerC", 0));
	}
	
	@Test
	public void testWinningAtXPoints() {
		GameOptions options = GameOptions.of(
				13,
				0,
				MultipleRoundWinnersNextRoundStartingPlayer.LAST_TURN_WINNER,
				MultipleGameWinnersSolution.ANOTHER_ROUND_UNTIL_TIE_BROKEN);
		game = new GameBuilder(UUID.randomUUID().toString(), players, options).build();
		
		players.get(0).addPoint().addPoint().addPoint().addPoint();
		
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new Guard(), new Priest(), new Baron(), new Prince()));
		game.start(cards);
		
		Player currentPlayer = players.get(0);
		
		RoundPlayer target = game.getCurrentRound().getRoundPlayerByPlayer(expectedPlayers.get(1));
		
		//Performing the move.
		Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
				game.performMove(PlayerMove.of(currentPlayer, new Guard(),
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(target, GuardEffect.of(2))))));
		
		//Check that performing the move had correct response.
		assertEquals(MoveGameResult.GAME_ENDS, result.getValue0());
		assertEquals(MoveRoundResult.ROUND_ENDS, result.getValue1());
		assertEquals(GuardActionResult.class, result.getValue2().getClass());
		assertEquals("Success", ((GuardActionResult)result.getValue2()).getResult());
		
		//Create the expected board state with all players etc.
		expectedPlayers.get(0).addPoint().addPoint().addPoint().addPoint().addPoint();
		Board expectedBoard = new BoardBuilder(Collections.emptyList()).removedCard(new Guard()).build();
		
		List<RoundPlayer> expectedActivePlayers = new ArrayList<>();
		List<RoundPlayer> expectedEliminatedPlayers = new ArrayList<RoundPlayer>();
		RoundPlayer expectedTargetPlayer  = RoundPlayer.of(
				expectedPlayers.get(1),
				Hand.of(Collections.emptyList()),
				Collections.singletonList(PlayedCard.of(new Priest(), CardPlayType.DISCARDED, null)),
				PlayerStatus.ELIMINATED);
		expectedEliminatedPlayers.add(expectedTargetPlayer);
		
		RoundPlayer expectedCurrentPlayer = RoundPlayer.of(
				expectedPlayers.get(0),
				Hand.of(Collections.singletonList(new Prince())),
				Collections.singletonList(PlayedCard.of(
						new Guard(),
						CardPlayType.PLAYED,
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(expectedTargetPlayer, GuardEffect.of(2)))))),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedCurrentPlayer);
		RoundPlayer expectedOtherPlayer = RoundPlayer.of(
				expectedPlayers.get(2),
				Hand.of(Arrays.asList(new Baron())),
				Collections.emptyList(),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedOtherPlayer);
		
		GameRound expectedRound = new RoundBuilder(
				null,
				1,
				expectedBoard,
				expectedActivePlayers,
				expectedEliminatedPlayers,
				0,
				RoundStatus.OVER,
				Arrays.asList(expectedPlayers.get(0)))
				.build();
		
		Game expectedGame = new GameBuilder(
				game.getId(),
				game.getPlayers(),
				game.getGameOptions())
				.gameStatus(GameStatus.OVER)
				.addRound(expectedRound)
				.winners(Arrays.asList(expectedPlayers.get(0)))
				.build();
		expectedGame.getCurrentRound().setGame(expectedGame);
		
		//Check that the expected board state is the same as we got in the test.
		assertEquals(expectedGame, game);
	}
	
	@Test
	public void testLastTurnWinnerTwoTiedWinnersNotLastPlayer() {
		GameOptions options = GameOptions.of(
				13,
				0,
				MultipleRoundWinnersNextRoundStartingPlayer.LAST_TURN_WINNER,
				MultipleGameWinnersSolution.ANOTHER_ROUND_UNTIL_TIE_BROKEN);
		game = new GameBuilder(UUID.randomUUID().toString(), players, options).build();
		
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new Guard(), new Guard(), new Guard(), new Guard()));
		game.start(cards);
		
		List<RoundPlayer> opponents = game.getCurrentRound().getActivePlayers();
		opponents.get(1).getPlayedCards().add(PlayedCard.of(new Prince(), CardPlayType.PLAYED,
				Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(opponents.get(1), VoidEffect.of())))));
		opponents.get(2).getPlayedCards().add(PlayedCard.of(new Prince(), CardPlayType.PLAYED,
				Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(opponents.get(2), VoidEffect.of())))));
		
		Player currentPlayer = players.get(0);
		
		RoundPlayer target = game.getCurrentRound().getRoundPlayerByPlayer(expectedPlayers.get(1));
		
		//Performing the move.
		Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
				game.performMove(PlayerMove.of(currentPlayer, new Guard(),
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(target, GuardEffect.of(2))))));
		
		//Check that performing the move had correct response.
		assertEquals(MoveGameResult.GAME_CONTINUES, result.getValue0());
		assertEquals(MoveRoundResult.ROUND_ENDS, result.getValue1());
		assertEquals(GuardActionResult.class, result.getValue2().getClass());
		assertEquals("Fail", ((GuardActionResult)result.getValue2()).getResult());
		
		//Create the expected board state with all players etc.
		expectedPlayers.get(1).addPoint();
		expectedPlayers.get(2).addPoint();
		Board expectedBoard = new BoardBuilder(Collections.emptyList()).removedCard(new Guard()).build();
		
		List<RoundPlayer> expectedActivePlayers = new ArrayList<>();
		List<RoundPlayer> expectedEliminatedPlayers = new ArrayList<RoundPlayer>();
		RoundPlayer expectedTargetPlayer  = RoundPlayer.of(
				expectedPlayers.get(1),
				Hand.of(Arrays.asList(new Guard())),
				Collections.singletonList(PlayedCard.of(new Prince(), CardPlayType.PLAYED, Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(opponents.get(1), VoidEffect.of()))))),
				PlayerStatus.ACTIVE);
		
		RoundPlayer expectedCurrentPlayer = RoundPlayer.of(
				expectedPlayers.get(0),
				Hand.of(Collections.singletonList(new Guard())),
				Collections.singletonList(PlayedCard.of(
						new Guard(),
						CardPlayType.PLAYED,
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(expectedTargetPlayer, GuardEffect.of(2)))))),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedCurrentPlayer);
		expectedActivePlayers.add(expectedTargetPlayer);
		
		RoundPlayer expectedOtherPlayer = RoundPlayer.of(
				expectedPlayers.get(2),
				Hand.of(Arrays.asList(new Guard())),
				Collections.singletonList(PlayedCard.of(new Prince(), CardPlayType.PLAYED, Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(opponents.get(2), VoidEffect.of()))))),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedOtherPlayer);
		
		GameRound expectedRound = new RoundBuilder(
				null,
				1,
				expectedBoard,
				expectedActivePlayers,
				expectedEliminatedPlayers,
				2,
				RoundStatus.OVER,
				Arrays.asList(expectedPlayers.get(1), expectedPlayers.get(2)))
				.build();
		
		GameRound expectedNewRound = game.getCurrentRound();
		
		Game expectedGame = new GameBuilder(
				game.getId(),
				game.getPlayers(),
				game.getGameOptions())
				.gameStatus(GameStatus.RUNNING)
				.addRound(expectedRound)
				.addRound(expectedNewRound)
				.build();
		expectedRound.setGame(expectedGame);
		expectedNewRound.setGame(expectedGame);
		
		//Check that the expected board state is the same as we got in the test.
		assertEquals(expectedGame, game);
		//Check that the new round currentPlayer is what we expect it to be.
		assertEquals(Integer.valueOf(2), expectedNewRound.getCurrentPlayerId());
	}
	
	@Test
	public void testLastTurnWinnerTwoTiedWinnersLastPlayer() {
		GameOptions options = GameOptions.of(
				13,
				0,
				MultipleRoundWinnersNextRoundStartingPlayer.LAST_TURN_WINNER,
				MultipleGameWinnersSolution.ANOTHER_ROUND_UNTIL_TIE_BROKEN);
		game = new GameBuilder(UUID.randomUUID().toString(), players, options).build();
		
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new Guard(), new Guard(), new Guard(), new Guard()));
		game.start(cards);
		
		List<RoundPlayer> opponents = game.getCurrentRound().getActivePlayers();
		opponents.get(0).getPlayedCards().add(PlayedCard.of(new Handmaid(), CardPlayType.PLAYED,
				Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(opponents.get(0), VoidEffect.of())))));
		opponents.get(1).getPlayedCards().add(PlayedCard.of(new Prince(), CardPlayType.PLAYED,
				Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(opponents.get(1), VoidEffect.of())))));
		opponents.get(2).getPlayedCards().add(PlayedCard.of(new Handmaid(), CardPlayType.PLAYED,
				Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(opponents.get(2), VoidEffect.of())))));
		
		Player currentPlayer = players.get(0);
		
		RoundPlayer target = game.getCurrentRound().getRoundPlayerByPlayer(expectedPlayers.get(1));
		
		//Performing the move.
		Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
				game.performMove(PlayerMove.of(currentPlayer, new Guard(),
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(target, GuardEffect.of(2))))));
		
		//Check that performing the move had correct response.
		assertEquals(MoveGameResult.GAME_CONTINUES, result.getValue0());
		assertEquals(MoveRoundResult.ROUND_ENDS, result.getValue1());
		assertEquals(GuardActionResult.class, result.getValue2().getClass());
		assertEquals("Fail", ((GuardActionResult)result.getValue2()).getResult());
		
		//Create the expected board state with all players etc.
		expectedPlayers.get(0).addPoint();
		expectedPlayers.get(1).addPoint();
		Board expectedBoard = new BoardBuilder(Collections.emptyList()).removedCard(new Guard()).build();
		
		List<RoundPlayer> expectedActivePlayers = new ArrayList<>();
		List<RoundPlayer> expectedEliminatedPlayers = new ArrayList<RoundPlayer>();
		RoundPlayer expectedTargetPlayer  = RoundPlayer.of(
				expectedPlayers.get(1),
				Hand.of(Arrays.asList(new Guard())),
				Collections.singletonList(PlayedCard.of(new Prince(), CardPlayType.PLAYED, Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(opponents.get(1), VoidEffect.of()))))),
				PlayerStatus.ACTIVE);
		
		RoundPlayer expectedCurrentPlayer = RoundPlayer.of(
				expectedPlayers.get(0),
				Hand.of(Collections.singletonList(new Guard())),
				new ArrayList<PlayedCard>(),
				PlayerStatus.ACTIVE);
		expectedCurrentPlayer.getPlayedCards().addAll(
				Arrays.asList(PlayedCard.of(
						new Handmaid(),
						CardPlayType.PLAYED,
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(expectedCurrentPlayer, VoidEffect.of())))),
						PlayedCard.of(
								new Guard(),
								CardPlayType.PLAYED,
								Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(expectedTargetPlayer, GuardEffect.of(2))))))
		);
		expectedActivePlayers.add(expectedCurrentPlayer);
		expectedActivePlayers.add(expectedTargetPlayer);
		
		RoundPlayer expectedOtherPlayer = RoundPlayer.of(
				expectedPlayers.get(2),
				Hand.of(Arrays.asList(new Guard())),
				Collections.singletonList(PlayedCard.of(new Handmaid(), CardPlayType.PLAYED, Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(opponents.get(2), VoidEffect.of()))))),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedOtherPlayer);
		
		GameRound expectedRound = new RoundBuilder(
				null,
				1,
				expectedBoard,
				expectedActivePlayers,
				expectedEliminatedPlayers,
				0,
				RoundStatus.OVER,
				Arrays.asList(expectedPlayers.get(0), expectedPlayers.get(1)))
				.build();
		
		GameRound expectedNewRound = game.getCurrentRound();
		
		Game expectedGame = new GameBuilder(
				game.getId(),
				game.getPlayers(),
				game.getGameOptions())
				.gameStatus(GameStatus.RUNNING)
				.addRound(expectedRound)
				.addRound(expectedNewRound)
				.build();
		expectedRound.setGame(expectedGame);
		expectedNewRound.setGame(expectedGame);
		
		//Check that the expected board state is the same as we got in the test.
		assertEquals(expectedGame, game);
		//Check that the new round currentPlayer is what we expect it to be.
		assertEquals(Integer.valueOf(0), expectedNewRound.getCurrentPlayerId());
	}
	
	//TODO how to test selection of random player? 2 tests missing due to that.
	
	@Test
	public void testMultipleWinnersAnotherRoundUntilTieBroken() {
		GameOptions options = GameOptions.of(
				13,
				0,
				MultipleRoundWinnersNextRoundStartingPlayer.LAST_TURN_WINNER,
				MultipleGameWinnersSolution.ANOTHER_ROUND_UNTIL_TIE_BROKEN);
		game = new GameBuilder(UUID.randomUUID().toString(), players, options).build();
		
		players.get(0).addPoint().addPoint().addPoint().addPoint();
		players.get(1).addPoint().addPoint().addPoint().addPoint();
		
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new Guard(), new Guard(), new Guard(), new Guard()));
		game.start(cards);
		
		List<RoundPlayer> opponents = game.getCurrentRound().getActivePlayers();
		opponents.get(0).getPlayedCards().add(PlayedCard.of(new Handmaid(), CardPlayType.PLAYED,
				Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(opponents.get(0), VoidEffect.of())))));
		opponents.get(1).getPlayedCards().add(PlayedCard.of(new Prince(), CardPlayType.PLAYED,
				Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(opponents.get(1), VoidEffect.of())))));
		opponents.get(2).getPlayedCards().add(PlayedCard.of(new Handmaid(), CardPlayType.PLAYED,
				Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(opponents.get(2), VoidEffect.of())))));
		
		Player currentPlayer = players.get(0);
		
		RoundPlayer target = game.getCurrentRound().getRoundPlayerByPlayer(players.get(1));
		
		//Performing the move.
		Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
				game.performMove(PlayerMove.of(currentPlayer, new Guard(),
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(target, GuardEffect.of(2))))));
		
		//Check that performing the move had correct response.
		assertEquals(MoveGameResult.GAME_CONTINUES, result.getValue0());
		assertEquals(MoveRoundResult.ROUND_ENDS, result.getValue1());
		assertEquals(GuardActionResult.class, result.getValue2().getClass());
		assertEquals("Fail", ((GuardActionResult)result.getValue2()).getResult());
		
		//Create the expected board state with all players etc.
		expectedPlayers.get(0).addPoint().addPoint().addPoint().addPoint().addPoint();
		expectedPlayers.get(1).addPoint().addPoint().addPoint().addPoint().addPoint();
		Board expectedBoard = new BoardBuilder(Collections.emptyList()).removedCard(new Guard()).build();
		
		List<RoundPlayer> expectedActivePlayers = new ArrayList<>();
		List<RoundPlayer> expectedEliminatedPlayers = new ArrayList<RoundPlayer>();
		RoundPlayer expectedTargetPlayer  = RoundPlayer.of(
				expectedPlayers.get(1),
				Hand.of(Arrays.asList(new Guard())),
				Collections.singletonList(PlayedCard.of(new Prince(), CardPlayType.PLAYED, Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(opponents.get(1), VoidEffect.of()))))),
				PlayerStatus.ACTIVE);
		
		RoundPlayer expectedCurrentPlayer = RoundPlayer.of(
				expectedPlayers.get(0),
				Hand.of(Collections.singletonList(new Guard())),
				new ArrayList<PlayedCard>(),
				PlayerStatus.ACTIVE);
		expectedCurrentPlayer.getPlayedCards().addAll(
				Arrays.asList(PlayedCard.of(
						new Handmaid(),
						CardPlayType.PLAYED,
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(expectedCurrentPlayer, VoidEffect.of())))),
						PlayedCard.of(
								new Guard(),
								CardPlayType.PLAYED,
								Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(expectedTargetPlayer, GuardEffect.of(2))))))
		);
		expectedActivePlayers.add(expectedCurrentPlayer);
		expectedActivePlayers.add(expectedTargetPlayer);
		
		RoundPlayer expectedOtherPlayer = RoundPlayer.of(
				expectedPlayers.get(2),
				Hand.of(Arrays.asList(new Guard())),
				Collections.singletonList(PlayedCard.of(new Handmaid(), CardPlayType.PLAYED, Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(opponents.get(2), VoidEffect.of()))))),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedOtherPlayer);
		
		GameRound expectedRound = new RoundBuilder(
				null,
				1,
				expectedBoard,
				expectedActivePlayers,
				expectedEliminatedPlayers,
				0,
				RoundStatus.OVER,
				Arrays.asList(expectedPlayers.get(0), expectedPlayers.get(1)))
				.build();
		
		GameRound expectedNewRound = game.getCurrentRound();
		
		Game expectedGame = new GameBuilder(
				game.getId(),
				game.getPlayers(),
				game.getGameOptions())
				.gameStatus(GameStatus.RUNNING)
				.addRound(expectedRound)
				.addRound(expectedNewRound)
				.build();
		expectedRound.setGame(expectedGame);
		expectedNewRound.setGame(expectedGame);
		
		//Check that the expected board state is the same as we got in the test.
		assertEquals(expectedGame, game);
		//Check that the new round currentPlayer is what we expect it to be.
		assertEquals(Integer.valueOf(0), expectedNewRound.getCurrentPlayerId());
	}
	
	@Test
	public void testMultipleWinnersDrawAmongWinners() {
		GameOptions options = GameOptions.of(
				13,
				0,
				MultipleRoundWinnersNextRoundStartingPlayer.LAST_TURN_WINNER,
				MultipleGameWinnersSolution.DRAW_AMONG_WINNERS);
		game = new GameBuilder(UUID.randomUUID().toString(), players, options).build();
		
		players.get(0).addPoint().addPoint().addPoint().addPoint();
		players.get(1).addPoint().addPoint().addPoint().addPoint();
		
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new Guard(), new Guard(), new Guard(), new Guard()));
		game.start(cards);
		
		List<RoundPlayer> opponents = game.getCurrentRound().getActivePlayers();
		opponents.get(0).getPlayedCards().add(PlayedCard.of(new Handmaid(), CardPlayType.PLAYED,
				Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(opponents.get(0), VoidEffect.of())))));
		opponents.get(1).getPlayedCards().add(PlayedCard.of(new Prince(), CardPlayType.PLAYED,
				Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(opponents.get(1), VoidEffect.of())))));
		opponents.get(2).getPlayedCards().add(PlayedCard.of(new Handmaid(), CardPlayType.PLAYED,
				Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(opponents.get(2), VoidEffect.of())))));
		
		Player currentPlayer = players.get(0);
		
		RoundPlayer target = game.getCurrentRound().getRoundPlayerByPlayer(players.get(1));
		
		//Performing the move.
		Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
				game.performMove(PlayerMove.of(currentPlayer, new Guard(),
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(target, GuardEffect.of(2))))));
		
		//Check that performing the move had correct response.
		assertEquals(MoveGameResult.GAME_ENDS, result.getValue0());
		assertEquals(MoveRoundResult.ROUND_ENDS, result.getValue1());
		assertEquals(GuardActionResult.class, result.getValue2().getClass());
		assertEquals("Fail", ((GuardActionResult)result.getValue2()).getResult());
		
		//Create the expected board state with all players etc.
		expectedPlayers.get(0).addPoint().addPoint().addPoint().addPoint().addPoint();
		expectedPlayers.get(1).addPoint().addPoint().addPoint().addPoint().addPoint();
		Board expectedBoard = new BoardBuilder(Collections.emptyList()).removedCard(new Guard()).build();
		
		List<RoundPlayer> expectedActivePlayers = new ArrayList<>();
		List<RoundPlayer> expectedEliminatedPlayers = new ArrayList<RoundPlayer>();
		RoundPlayer expectedTargetPlayer  = RoundPlayer.of(
				expectedPlayers.get(1),
				Hand.of(Arrays.asList(new Guard())),
				Collections.singletonList(PlayedCard.of(new Prince(), CardPlayType.PLAYED, Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(opponents.get(1), VoidEffect.of()))))),
				PlayerStatus.ACTIVE);
		
		RoundPlayer expectedCurrentPlayer = RoundPlayer.of(
				expectedPlayers.get(0),
				Hand.of(Collections.singletonList(new Guard())),
				new ArrayList<PlayedCard>(),
				PlayerStatus.ACTIVE);
		expectedCurrentPlayer.getPlayedCards().addAll(
				Arrays.asList(PlayedCard.of(
						new Handmaid(),
						CardPlayType.PLAYED,
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(expectedCurrentPlayer, VoidEffect.of())))),
						PlayedCard.of(
								new Guard(),
								CardPlayType.PLAYED,
								Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(expectedTargetPlayer, GuardEffect.of(2))))))
		);
		expectedActivePlayers.add(expectedCurrentPlayer);
		expectedActivePlayers.add(expectedTargetPlayer);
		
		RoundPlayer expectedOtherPlayer = RoundPlayer.of(
				expectedPlayers.get(2),
				Hand.of(Arrays.asList(new Guard())),
				Collections.singletonList(PlayedCard.of(new Handmaid(), CardPlayType.PLAYED, Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(opponents.get(2), VoidEffect.of()))))),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedOtherPlayer);
		
		GameRound expectedRound = new RoundBuilder(
				null,
				1,
				expectedBoard,
				expectedActivePlayers,
				expectedEliminatedPlayers,
				0,
				RoundStatus.OVER,
				Arrays.asList(expectedPlayers.get(0), expectedPlayers.get(1)))
				.build();
		
		Game expectedGame = new GameBuilder(
				game.getId(),
				game.getPlayers(),
				game.getGameOptions())
				.gameStatus(GameStatus.OVER)
				.addRound(expectedRound)
				.winners(Arrays.asList(expectedPlayers.get(0), expectedPlayers.get(1)))
				.build();
		expectedRound.setGame(expectedGame);
		
		//Check that the expected board state is the same as we got in the test.
		assertEquals(expectedGame, game);
	}
}
