package com.stroom.loveletter.game;

import com.stroom.loveletter.card.*;
import com.stroom.loveletter.card.action.effect.AbstractEffect;
import com.stroom.loveletter.card.action.effect.GuardEffect;
import com.stroom.loveletter.card.action.result.*;
import com.stroom.loveletter.game.round.*;
import com.stroom.loveletter.game.round.action.*;
import com.stroom.loveletter.utility.builder.BoardBuilder;
import com.stroom.loveletter.utility.builder.GameBuilder;
import com.stroom.loveletter.utility.builder.RoundBuilder;
import com.stroom.loveletter.utility.exception.ActionImpossibleException;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Tests for playing each card. Some cases of error handling too.
 * Created by Stroom on 03/06/2017.
 */
public class PlayingCardsTests {
	
	private Game game;
	private List<Player> players;
	private List<Player> expectedPlayers;
	
	@Before
	public void initTest() {
		GameOptions options = GameOptions.of(
				13,
				0,
				MultipleRoundWinnersNextRoundStartingPlayer.LAST_TURN_WINNER,
				MultipleGameWinnersSolution.ANOTHER_ROUND_UNTIL_TIE_BROKEN);
		
		players = Arrays.asList(Player.of("PlayerA", 0), Player.of("PlayerB", 0), Player.of("PlayerC", 0));
		expectedPlayers = Arrays.asList(Player.of("PlayerA", 0), Player.of("PlayerB", 0), Player.of("PlayerC", 0));
		
		game = new GameBuilder(UUID.randomUUID().toString(), players, options).build();
		
	}
	
	//The cards are distributed in the order of (3 revealed), 1 removed, 1 card each to the players in list order, 1 card to starting player.
	
	@Test
	public void testPlayingGuardSuccess() {
		//Buildup for testing case:
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new Guard(), new Priest(), new Baron(), new Guard(), new Priest()));
		game.start(cards);
		
		Player currentPlayer = players.get(0);
		//findPossibleMoves() result is sent to the client so he can choose which move to perform.
		//Have to check if that info is also relevant.
		Pair<Player, List<PossibleMove>> moves = game.findPossibleMoves();
		Pair<Player, List<PossibleMove>> expectedMoves = new Pair<Player, List<PossibleMove>>(
				expectedPlayers.get(0),
				Arrays.asList(
						PossibleMove.of(
								new Guard(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2)
								),
								Collections.singletonList(GuardEffect.of(-1)),
								new Pair<Integer, Integer>(1, 1)),
						PossibleMove.of(
								new Guard(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2)
								),
								Collections.singletonList(GuardEffect.of(-1)),
								new Pair<Integer, Integer>(1, 1))
				));
		assertEquals(expectedMoves, moves);
		
		RoundPlayer target = game.getCurrentRound().getRoundPlayerByPlayer(expectedPlayers.get(1));
		
		//Performing the move.
		Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
				game.performMove(PlayerMove.of(currentPlayer, new Guard(),
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(target, GuardEffect.of(2))))));
		
		//Check that performing the move had correct response.
		assertEquals(MoveGameResult.GAME_CONTINUES, result.getValue0());
		assertEquals(MoveRoundResult.ROUND_CONTINUES, result.getValue1());
		assertEquals(GuardActionResult.class, result.getValue2().getClass());
		assertEquals("Success", ((GuardActionResult)result.getValue2()).getResult());
		
		//Create the expected board state with all players etc.
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
				Hand.of(Collections.singletonList(new Guard())),
				Collections.singletonList(PlayedCard.of(
						new Guard(),
						CardPlayType.PLAYED,
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(expectedTargetPlayer, GuardEffect.of(2)))))),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedCurrentPlayer);
		RoundPlayer expectedOtherPlayer = RoundPlayer.of(
				expectedPlayers.get(2),
				Hand.of(Arrays.asList(new Baron(), new Priest())),
				Collections.emptyList(),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedOtherPlayer);
		
		GameRound expectedRound = new RoundBuilder(
				null,
				1,
				expectedBoard,
				expectedActivePlayers,
				expectedEliminatedPlayers,
				1,
				RoundStatus.RUNNING,
				Collections.emptyList())
				.build();
		
		Game expectedGame = new GameBuilder(
						game.getId(),
						game.getPlayers(),
						game.getGameOptions())
				.gameStatus(GameStatus.RUNNING)
				.addRound(expectedRound)
				.build();
		expectedRound.setGame(expectedGame);
		
		//Check that the expected board state is the same as we got in the test.
		assertEquals(expectedGame, game);
		
		return;
	}
	
	@Test
	public void testPlayingGuardFail() {
		//Buildup for testing case:
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new Guard(), new Priest(), new Baron(), new Prince(), new Priest()));
		game.start(cards);
		
		Player currentPlayer = players.get(0);
		
		Pair<Player, List<PossibleMove>> moves = game.findPossibleMoves();
		Pair<Player, List<PossibleMove>> expectedMoves = new Pair<Player, List<PossibleMove>>(
				expectedPlayers.get(0),
				Arrays.asList(
						PossibleMove.of(
								new Guard(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2)
								),
								Collections.singletonList(GuardEffect.of(-1)),
								new Pair<Integer, Integer>(1, 1)),
						PossibleMove.of(
								new Prince(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2),
										game.getCurrentRound().getActivePlayers().get(0)
								),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(1, 1))
				));
		assertEquals(expectedMoves, moves);
		
		RoundPlayer target = game.getCurrentRound().getRoundPlayerByPlayer(expectedPlayers.get(1));
		//Performing the move.
		Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
				game.performMove(PlayerMove.of(currentPlayer, new Guard(),
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(target, GuardEffect.of(3))))));
		
		//Performing the move had correct response.
		assertEquals(MoveGameResult.GAME_CONTINUES, result.getValue0());
		assertEquals(MoveRoundResult.ROUND_CONTINUES, result.getValue1());
		assertEquals(GuardActionResult.class, result.getValue2().getClass());
		assertEquals("Fail", ((GuardActionResult)result.getValue2()).getResult());
		
		//Create the expected board state with all players etc.
		Board expectedBoard = new BoardBuilder(Collections.emptyList()).removedCard(new Guard()).build();
		
		List<RoundPlayer> expectedActivePlayers = new ArrayList<RoundPlayer>();
		List<RoundPlayer> expectedEliminatedPlayers = new ArrayList<RoundPlayer>();
		RoundPlayer expectedTargetPlayer  = RoundPlayer.of(
				expectedPlayers.get(1),
				Hand.of(Arrays.asList(new Priest(), new Priest())),
				Collections.emptyList(),
				PlayerStatus.ACTIVE);
		
		RoundPlayer expectedCurrentPlayer = RoundPlayer.of(
				expectedPlayers.get(0),
				Hand.of(Collections.singletonList(new Prince())),
				Collections.singletonList(PlayedCard.of(
						new Guard(),
						CardPlayType.PLAYED,
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(expectedTargetPlayer, GuardEffect.of(3)))))),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedCurrentPlayer);
		expectedActivePlayers.add(expectedTargetPlayer);
		RoundPlayer expectedOtherPlayer = RoundPlayer.of(
				expectedPlayers.get(2),
				Hand.of(Collections.singletonList(new Baron())),
				Collections.emptyList(),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedOtherPlayer);
		
		GameRound expectedRound = new RoundBuilder(
				null,
				1,
				expectedBoard,
				expectedActivePlayers,
				expectedEliminatedPlayers,
				1,
				RoundStatus.RUNNING,
				Collections.emptyList())
				.build();
		
		Game expectedGame = new GameBuilder(
				game.getId(),
				game.getPlayers(),
				game.getGameOptions())
				.gameStatus(GameStatus.RUNNING)
				.addRound(expectedRound)
				.build();
		expectedRound.setGame(expectedGame);
		
		//Check that the expected board state is the same as we got in the test.
		assertEquals(expectedGame, game);
		
		return;
	}
	
	@Test
	public void testPlayingGuardNoTarget() {
		//Buildup for testing case:
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new Guard(), new Priest(), new Baron(), new Guard(), new Priest()));
		game.start(cards);
		
		setHandmaids();
		
		Player currentPlayer = players.get(0);
		
		Pair<Player, List<PossibleMove>> moves = game.findPossibleMoves();
		Pair<Player, List<PossibleMove>> expectedMoves = new Pair<Player, List<PossibleMove>>(
				expectedPlayers.get(0),
				Arrays.asList(
						PossibleMove.of(
								new Guard(),
								Arrays.asList((RoundPlayer) null),
								Collections.singletonList(GuardEffect.of(-1)),
								new Pair<Integer, Integer>(1, 1)),
						PossibleMove.of(
								new Guard(),
								Arrays.asList((RoundPlayer) null),
								Collections.singletonList(GuardEffect.of(-1)),
								new Pair<Integer, Integer>(1, 1))
				));
		assertEquals(expectedMoves, moves);
		//Performing the move.
		Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
				game.performMove(PlayerMove.of(currentPlayer, new Guard(),
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(null, GuardEffect.of(2))))));
		
		//Performing the move had correct response.
		assertEquals(MoveGameResult.GAME_CONTINUES, result.getValue0());
		assertEquals(MoveRoundResult.ROUND_CONTINUES, result.getValue1());
		assertEquals(GuardActionResult.class, result.getValue2().getClass());
		assertEquals("NoTarget", ((GuardActionResult)result.getValue2()).getResult());
		
		//Create the expected board state with all players etc.
		Board expectedBoard = new BoardBuilder(Collections.emptyList()).removedCard(new Guard()).build();
		
		List<RoundPlayer> expectedActivePlayers = new ArrayList<RoundPlayer>();
		List<RoundPlayer> expectedEliminatedPlayers = new ArrayList<RoundPlayer>();
		RoundPlayer expectedTargetPlayer  = RoundPlayer.of(
				expectedPlayers.get(1),
				Hand.of(Arrays.asList(new Priest(), new Priest())),
				new ArrayList<PlayedCard>(),
				PlayerStatus.ACTIVE);
		expectedTargetPlayer.getPlayedCards().add(
				PlayedCard.of(
						new Handmaid(),
						CardPlayType.PLAYED,
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(expectedTargetPlayer, VoidEffect.of()))))
		);
		
		RoundPlayer expectedCurrentPlayer = RoundPlayer.of(
				expectedPlayers.get(0),
				Hand.of(Collections.singletonList(new Guard())),
				Collections.singletonList(PlayedCard.of(
						new Guard(),
						CardPlayType.PLAYED,
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(null, GuardEffect.of(2)))))),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedCurrentPlayer);
		expectedActivePlayers.add(expectedTargetPlayer);
		
		RoundPlayer expectedOtherPlayer = RoundPlayer.of(
				expectedPlayers.get(2),
				Hand.of(Collections.singletonList(new Baron())),
				new ArrayList<PlayedCard>(),
				PlayerStatus.ACTIVE);
		expectedOtherPlayer.getPlayedCards().add(
				PlayedCard.of(
						new Handmaid(),
						CardPlayType.PLAYED,
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(expectedOtherPlayer, VoidEffect.of()))))
		);
		expectedActivePlayers.add(expectedOtherPlayer);
		
		GameRound expectedRound = new RoundBuilder(
				null,
				1,
				expectedBoard,
				expectedActivePlayers,
				expectedEliminatedPlayers,
				1,
				RoundStatus.RUNNING,
				Collections.emptyList())
				.build();
		
		Game expectedGame = new GameBuilder(
				game.getId(),
				game.getPlayers(),
				game.getGameOptions())
				.gameStatus(GameStatus.RUNNING)
				.addRound(expectedRound)
				.build();
		expectedRound.setGame(expectedGame);
		
		//Check that the expected board state is the same as we got in the test.
		assertEquals(expectedGame, game);
		
		return;
	}
	
	@Test
	public void testPlayingGuardWithNoTargetFails() {
		//Buildup for testing case:
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new Guard(), new Priest(), new Baron(), new Guard(), new Priest()));
		game.start(cards);
		
		Player currentPlayer = players.get(0);
		
		Pair<Player, List<PossibleMove>> moves = game.findPossibleMoves();
		Pair<Player, List<PossibleMove>> expectedMoves = new Pair<Player, List<PossibleMove>>(
				expectedPlayers.get(0),
				Arrays.asList(
						PossibleMove.of(
								new Guard(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2)
								),
								Collections.singletonList(GuardEffect.of(-1)),
								new Pair<Integer, Integer>(1, 1)),
						PossibleMove.of(
								new Guard(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2)
								),
								Collections.singletonList(GuardEffect.of(-1)),
								new Pair<Integer, Integer>(1, 1))
				));
		assertEquals(expectedMoves, moves);
		//Performing the move.
		try {
			Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
					game.performMove(PlayerMove.of(currentPlayer, new Guard(),
							Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(null, GuardEffect.of(2))))));
			Assert.fail("Should not allow playing without a target.");
		}
		catch (ActionImpossibleException e) {
			Assert.assertTrue(true);
		}
		return;
	}
	
	@Test
	public void testPlayingPriestSuccess() {
		//Buildup for testing case:
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new Priest(), new Priest(), new Baron(), new Guard(), new Guard()));
		game.start(cards);
		
		Player currentPlayer = players.get(0);
		
		Pair<Player, List<PossibleMove>> moves = game.findPossibleMoves();
		Pair<Player, List<PossibleMove>> expectedMoves = new Pair<Player, List<PossibleMove>>(
				expectedPlayers.get(0),
				Arrays.asList(
						PossibleMove.of(
								new Priest(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2)
								),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(1, 1)),
						PossibleMove.of(
								new Guard(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2)
								),
								Collections.singletonList(GuardEffect.of(-1)),
								new Pair<Integer, Integer>(1, 1))
				));
		assertEquals(expectedMoves, moves);
		RoundPlayer target = game.getCurrentRound().getRoundPlayerByPlayer(expectedPlayers.get(1));
		//Performing the move.
		Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
				game.performMove(PlayerMove.of(currentPlayer, new Priest(),
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(target, VoidEffect.of())))));
		
		//Performing the move had correct response.
		assertEquals(MoveGameResult.GAME_CONTINUES, result.getValue0());
		assertEquals(MoveRoundResult.ROUND_CONTINUES, result.getValue1());
		assertEquals(PriestActionResult.class, result.getValue2().getClass());
		assertEquals("Success", ((PriestActionResult)result.getValue2()).getResult());
		assertEquals(Priest.class, ((PriestActionResult)result.getValue2()).getTargetsCard().getClass());
		
		//Create the expected board state with all players etc.
		Board expectedBoard = new BoardBuilder(Collections.emptyList()).removedCard(new Guard()).build();
		
		List<RoundPlayer> expectedActivePlayers = new ArrayList<RoundPlayer>();
		List<RoundPlayer> expectedEliminatedPlayers = new ArrayList<RoundPlayer>();
		RoundPlayer expectedTargetPlayer  = RoundPlayer.of(
				expectedPlayers.get(1),
				Hand.of(Arrays.asList(new Priest(), new Guard())),
				Collections.emptyList(),
				PlayerStatus.ACTIVE);
		
		RoundPlayer expectedCurrentPlayer = RoundPlayer.of(
				expectedPlayers.get(0),
				Hand.of(Collections.singletonList(new Guard())),
				Collections.singletonList(PlayedCard.of(
						new Priest(),
						CardPlayType.PLAYED,
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(expectedTargetPlayer, VoidEffect.of()))))),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedCurrentPlayer);
		expectedActivePlayers.add(expectedTargetPlayer);
		RoundPlayer expectedOtherPlayer = RoundPlayer.of(
				expectedPlayers.get(2),
				Hand.of(Collections.singletonList(new Baron())),
				Collections.emptyList(),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedOtherPlayer);
		
		GameRound expectedRound = new RoundBuilder(
				null,
				1,
				expectedBoard,
				expectedActivePlayers,
				expectedEliminatedPlayers,
				1,
				RoundStatus.RUNNING,
				Collections.emptyList())
				.build();
		
		Game expectedGame = new GameBuilder(
				game.getId(),
				game.getPlayers(),
				game.getGameOptions())
				.gameStatus(GameStatus.RUNNING)
				.addRound(expectedRound)
				.build();
		expectedRound.setGame(expectedGame);
		
		//Check that the expected board state is the same as we got in the test.
		assertEquals(expectedGame, game);
		
		return;
	}
	
	@Test
	public void testPlayingPriestNoTarget() {
		//Buildup for testing case:
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new Priest(), new Priest(), new Baron(), new Guard(), new Guard()));
		game.start(cards);
		
		setHandmaids();
		
		Player currentPlayer = players.get(0);
		
		Pair<Player, List<PossibleMove>> moves = game.findPossibleMoves();
		Pair<Player, List<PossibleMove>> expectedMoves = new Pair<Player, List<PossibleMove>>(
				expectedPlayers.get(0),
				Arrays.asList(
						PossibleMove.of(
								new Priest(),
								Arrays.asList((RoundPlayer) null),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(1, 1)),
						PossibleMove.of(
								new Guard(),
								Arrays.asList((RoundPlayer) null),
								Collections.singletonList(GuardEffect.of(-1)),
								new Pair<Integer, Integer>(1, 1))
				));
		assertEquals(expectedMoves, moves);
		//Performing the move.
		Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
				game.performMove(PlayerMove.of(currentPlayer, new Priest(),
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(null, VoidEffect.of())))));
		
		//Performing the move had correct response.
		assertEquals(MoveGameResult.GAME_CONTINUES, result.getValue0());
		assertEquals(MoveRoundResult.ROUND_CONTINUES, result.getValue1());
		assertEquals(PriestActionResult.class, result.getValue2().getClass());
		assertEquals("NoTarget", ((PriestActionResult)result.getValue2()).getResult());
		assertEquals(null, ((PriestActionResult)result.getValue2()).getTargetsCard());
		
		//Create the expected board state with all players etc.
		Board expectedBoard = new BoardBuilder(Collections.emptyList()).removedCard(new Guard()).build();
		
		List<RoundPlayer> expectedActivePlayers = new ArrayList<RoundPlayer>();
		List<RoundPlayer> expectedEliminatedPlayers = new ArrayList<RoundPlayer>();
		RoundPlayer expectedTargetPlayer  = RoundPlayer.of(
				expectedPlayers.get(1),
				Hand.of(Arrays.asList(new Priest(), new Guard())),
				new ArrayList<PlayedCard>(),
				PlayerStatus.ACTIVE);
		expectedTargetPlayer.getPlayedCards().add(
				PlayedCard.of(
						new Handmaid(),
						CardPlayType.PLAYED,
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(expectedTargetPlayer, VoidEffect.of()))))
		);
		
		RoundPlayer expectedCurrentPlayer = RoundPlayer.of(
				expectedPlayers.get(0),
				Hand.of(Collections.singletonList(new Guard())),
				Collections.singletonList(PlayedCard.of(
						new Priest(),
						CardPlayType.PLAYED,
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(null, VoidEffect.of()))))),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedCurrentPlayer);
		expectedActivePlayers.add(expectedTargetPlayer);
		RoundPlayer expectedOtherPlayer = RoundPlayer.of(
				expectedPlayers.get(2),
				Hand.of(Collections.singletonList(new Baron())),
				new ArrayList<PlayedCard>(),
				PlayerStatus.ACTIVE);
		expectedOtherPlayer.getPlayedCards().add(
				PlayedCard.of(
						new Handmaid(),
						CardPlayType.PLAYED,
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(expectedOtherPlayer, VoidEffect.of()))))
		);
		expectedActivePlayers.add(expectedOtherPlayer);
		
		GameRound expectedRound = new RoundBuilder(
				null,
				1,
				expectedBoard,
				expectedActivePlayers,
				expectedEliminatedPlayers,
				1,
				RoundStatus.RUNNING,
				Collections.emptyList())
				.build();
		
		Game expectedGame = new GameBuilder(
				game.getId(),
				game.getPlayers(),
				game.getGameOptions())
				.gameStatus(GameStatus.RUNNING)
				.addRound(expectedRound)
				.build();
		expectedRound.setGame(expectedGame);
		
		//Check that the expected board state is the same as we got in the test.
		assertEquals(expectedGame, game);
		
		return;
	}
	
	@Test
	public void testPlayingPriestWithNoTargetFails() {
		//Buildup for testing case:
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new Priest(), new Priest(), new Baron(), new Guard(), new Priest()));
		game.start(cards);
		
		Player currentPlayer = players.get(0);
		
		Pair<Player, List<PossibleMove>> moves = game.findPossibleMoves();
		Pair<Player, List<PossibleMove>> expectedMoves = new Pair<Player, List<PossibleMove>>(
				expectedPlayers.get(0),
				Arrays.asList(
						PossibleMove.of(
								new Priest(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2)
								),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(1, 1)),
						PossibleMove.of(
								new Guard(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2)
								),
								Collections.singletonList(GuardEffect.of(-1)),
								new Pair<Integer, Integer>(1, 1))
				));
		assertEquals(expectedMoves, moves);
		//Performing the move.
		try {
			Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
					game.performMove(PlayerMove.of(currentPlayer, new Priest(),
							Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(null, VoidEffect.of())))));
			Assert.fail("Should not allow playing without a target.");
		}
		catch (ActionImpossibleException e) {
			Assert.assertTrue(true);
		}
		return;
	}
	
	@Test
	public void testPlayingBaronSuccess() {
		//Buildup for testing case:
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new Baron(), new Priest(), new Baron(), new Prince(), new Guard()));
		game.start(cards);
		
		Player currentPlayer = players.get(0);
		
		Pair<Player, List<PossibleMove>> moves = game.findPossibleMoves();
		Pair<Player, List<PossibleMove>> expectedMoves = new Pair<Player, List<PossibleMove>>(
				expectedPlayers.get(0),
				Arrays.asList(
						PossibleMove.of(
								new Baron(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2)
								),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(1, 1)),
						PossibleMove.of(
								new Prince(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2),
										game.getCurrentRound().getActivePlayers().get(0)
								),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(1, 1))
				));
		assertEquals(expectedMoves, moves);
		RoundPlayer target = game.getCurrentRound().getRoundPlayerByPlayer(expectedPlayers.get(1));
		//Performing the move.
		Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
				game.performMove(PlayerMove.of(currentPlayer, new Baron(),
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(target, VoidEffect.of())))));
		
		//Performing the move had correct response.
		assertEquals(MoveGameResult.GAME_CONTINUES, result.getValue0());
		assertEquals(MoveRoundResult.ROUND_CONTINUES, result.getValue1());
		assertEquals(BaronActionResult.class, result.getValue2().getClass());
		assertEquals("Success", ((BaronActionResult)result.getValue2()).getResult());
		assertEquals(Prince.class, ((BaronActionResult)result.getValue2()).getYourCard().getClass());
		assertEquals(Priest.class, ((BaronActionResult)result.getValue2()).getTargetsCard().getClass());
		
		//Create the expected board state with all players etc.
		Board expectedBoard = new BoardBuilder(Collections.emptyList()).removedCard(new Guard()).build();
		
		List<RoundPlayer> expectedActivePlayers = new ArrayList<RoundPlayer>();
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
						new Baron(),
						CardPlayType.PLAYED,
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(expectedTargetPlayer, VoidEffect.of()))))),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedCurrentPlayer);
		RoundPlayer expectedOtherPlayer = RoundPlayer.of(
				expectedPlayers.get(2),
				Hand.of(Arrays.asList(new Baron(), new Guard())),
				Collections.emptyList(),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedOtherPlayer);
		
		GameRound expectedRound = new RoundBuilder(
				null,
				1,
				expectedBoard,
				expectedActivePlayers,
				expectedEliminatedPlayers,
				1,
				RoundStatus.RUNNING,
				Collections.emptyList())
				.build();
		
		Game expectedGame = new GameBuilder(
				game.getId(),
				game.getPlayers(),
				game.getGameOptions())
				.gameStatus(GameStatus.RUNNING)
				.addRound(expectedRound)
				.build();
		expectedRound.setGame(expectedGame);
		
		//Check that the expected board state is the same as we got in the test.
		assertEquals(expectedGame, game);
		
		return;
	}
	
	@Test
	public void testPlayingBaronFail() {
		//Buildup for testing case:
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new Baron(), new Priest(), new Baron(), new Guard(), new Guard()));
		game.start(cards);
		
		Player currentPlayer = players.get(0);
		
		Pair<Player, List<PossibleMove>> moves = game.findPossibleMoves();
		Pair<Player, List<PossibleMove>> expectedMoves = new Pair<Player, List<PossibleMove>>(
				expectedPlayers.get(0),
				Arrays.asList(
						PossibleMove.of(
								new Baron(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2)
								),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(1, 1)),
						PossibleMove.of(
								new Guard(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2)
								),
								Collections.singletonList(GuardEffect.of(-1)),
								new Pair<Integer, Integer>(1, 1))
				));
		assertEquals(expectedMoves, moves);
		RoundPlayer target = game.getCurrentRound().getRoundPlayerByPlayer(expectedPlayers.get(1));
		//Performing the move.
		Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
				game.performMove(PlayerMove.of(currentPlayer, new Baron(),
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(target, VoidEffect.of())))));
		
		//Performing the move had correct response.
		assertEquals(MoveGameResult.GAME_CONTINUES, result.getValue0());
		assertEquals(MoveRoundResult.ROUND_CONTINUES, result.getValue1());
		assertEquals(BaronActionResult.class, result.getValue2().getClass());
		assertEquals("Fail", ((BaronActionResult)result.getValue2()).getResult());
		assertEquals(Guard.class, ((BaronActionResult)result.getValue2()).getYourCard().getClass());
		assertEquals(Priest.class, ((BaronActionResult)result.getValue2()).getTargetsCard().getClass());
		
		//Create the expected board state with all players etc.
		Board expectedBoard = new BoardBuilder(Collections.emptyList()).removedCard(new Guard()).build();
		
		List<RoundPlayer> expectedActivePlayers = new ArrayList<RoundPlayer>();
		List<RoundPlayer> expectedEliminatedPlayers = new ArrayList<RoundPlayer>();
		RoundPlayer expectedTargetPlayer  = RoundPlayer.of(
				expectedPlayers.get(1),
				Hand.of(Arrays.asList(new Priest(), new Guard())),
				Collections.emptyList(),
				PlayerStatus.ACTIVE);
		
		RoundPlayer expectedCurrentPlayer = RoundPlayer.of(
				expectedPlayers.get(0),
				Hand.of(Collections.emptyList()),
				Arrays.asList(
						PlayedCard.of(
								new Baron(),
								CardPlayType.PLAYED,
								Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(expectedTargetPlayer, VoidEffect.of())))),
						PlayedCard.of(
								new Guard(),
								CardPlayType.DISCARDED,
								null)
						),
				PlayerStatus.ELIMINATED);
		expectedEliminatedPlayers.add(expectedCurrentPlayer);
		expectedActivePlayers.add(expectedTargetPlayer);
		
		RoundPlayer expectedOtherPlayer = RoundPlayer.of(
				expectedPlayers.get(2),
				Hand.of(Collections.singletonList(new Baron())),
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
				RoundStatus.RUNNING,
				Collections.emptyList())
				.build();
		
		Game expectedGame = new GameBuilder(
				game.getId(),
				game.getPlayers(),
				game.getGameOptions())
				.gameStatus(GameStatus.RUNNING)
				.addRound(expectedRound)
				.build();
		expectedRound.setGame(expectedGame);
		
		//Check that the expected board state is the same as we got in the test.
		assertEquals(expectedGame, game);
		
		return;
	}
	
	@Test
	public void testPlayingBaronDraw() {
		//Buildup for testing case:
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new Baron(), new Priest(), new Baron(), new Priest(), new Guard()));
		game.start(cards);
		
		Player currentPlayer = players.get(0);
		
		Pair<Player, List<PossibleMove>> moves = game.findPossibleMoves();
		Pair<Player, List<PossibleMove>> expectedMoves = new Pair<Player, List<PossibleMove>>(
				expectedPlayers.get(0),
				Arrays.asList(
						PossibleMove.of(
								new Baron(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2)
								),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(1, 1)),
						PossibleMove.of(
								new Priest(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2)
								),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(1, 1))
				));
		assertEquals(expectedMoves, moves);
		RoundPlayer target = game.getCurrentRound().getRoundPlayerByPlayer(expectedPlayers.get(1));
		//Performing the move.
		Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
				game.performMove(PlayerMove.of(currentPlayer, new Baron(),
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(target, VoidEffect.of())))));
		
		//Performing the move had correct response.
		assertEquals(MoveGameResult.GAME_CONTINUES, result.getValue0());
		assertEquals(MoveRoundResult.ROUND_CONTINUES, result.getValue1());
		assertEquals(BaronActionResult.class, result.getValue2().getClass());
		assertEquals("Draw", ((BaronActionResult)result.getValue2()).getResult());
		assertEquals(Priest.class, ((BaronActionResult)result.getValue2()).getYourCard().getClass());
		assertEquals(Priest.class, ((BaronActionResult)result.getValue2()).getTargetsCard().getClass());
		
		//Create the expected board state with all players etc.
		Board expectedBoard = new BoardBuilder(Collections.emptyList()).removedCard(new Guard()).build();
		
		List<RoundPlayer> expectedActivePlayers = new ArrayList<RoundPlayer>();
		List<RoundPlayer> expectedEliminatedPlayers = new ArrayList<RoundPlayer>();
		RoundPlayer expectedTargetPlayer  = RoundPlayer.of(
				expectedPlayers.get(1),
				Hand.of(Arrays.asList(new Priest(), new Guard())),
				Collections.emptyList(),
				PlayerStatus.ACTIVE);
		
		RoundPlayer expectedCurrentPlayer = RoundPlayer.of(
				expectedPlayers.get(0),
				Hand.of(Collections.singletonList(new Priest())),
				Collections.singletonList(PlayedCard.of(
						new Baron(),
						CardPlayType.PLAYED,
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(expectedTargetPlayer, VoidEffect.of()))))),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedCurrentPlayer);
		expectedActivePlayers.add(expectedTargetPlayer);
		RoundPlayer expectedOtherPlayer = RoundPlayer.of(
				expectedPlayers.get(2),
				Hand.of(Collections.singletonList(new Baron())),
				Collections.emptyList(),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedOtherPlayer);
		
		GameRound expectedRound = new RoundBuilder(
				null,
				1,
				expectedBoard,
				expectedActivePlayers,
				expectedEliminatedPlayers,
				1,
				RoundStatus.RUNNING,
				Collections.emptyList())
				.build();
		
		Game expectedGame = new GameBuilder(
				game.getId(),
				game.getPlayers(),
				game.getGameOptions())
				.gameStatus(GameStatus.RUNNING)
				.addRound(expectedRound)
				.build();
		expectedRound.setGame(expectedGame);
		
		//Check that the expected board state is the same as we got in the test.
		assertEquals(expectedGame, game);
		
		return;
	}
	
	@Test
	public void testPlayingBaronNoTarget() {
		//Buildup for testing case:
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new Baron(), new Priest(), new Baron(), new Priest(), new Guard()));
		game.start(cards);
		
		setHandmaids();
		
		Player currentPlayer = players.get(0);
		
		Pair<Player, List<PossibleMove>> moves = game.findPossibleMoves();
		Pair<Player, List<PossibleMove>> expectedMoves = new Pair<Player, List<PossibleMove>>(
				expectedPlayers.get(0),
				Arrays.asList(
						PossibleMove.of(
								new Baron(),
								Arrays.asList((RoundPlayer) null),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(1, 1)),
						PossibleMove.of(
								new Priest(),
								Arrays.asList((RoundPlayer) null),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(1, 1))
				));
		assertEquals(expectedMoves, moves);
		//Performing the move.
		Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
				game.performMove(PlayerMove.of(currentPlayer, new Baron(),
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(null, VoidEffect.of())))));
		
		//Performing the move had correct response.
		assertEquals(MoveGameResult.GAME_CONTINUES, result.getValue0());
		assertEquals(MoveRoundResult.ROUND_CONTINUES, result.getValue1());
		assertEquals(BaronActionResult.class, result.getValue2().getClass());
		assertEquals("NoTarget", ((BaronActionResult)result.getValue2()).getResult());
		assertEquals(null, ((BaronActionResult)result.getValue2()).getYourCard());
		assertEquals(null, ((BaronActionResult)result.getValue2()).getTargetsCard());
		
		//Create the expected board state with all players etc.
		Board expectedBoard = new BoardBuilder(Collections.emptyList()).removedCard(new Guard()).build();
		
		List<RoundPlayer> expectedActivePlayers = new ArrayList<RoundPlayer>();
		List<RoundPlayer> expectedEliminatedPlayers = new ArrayList<RoundPlayer>();
		RoundPlayer expectedTargetPlayer  = RoundPlayer.of(
				expectedPlayers.get(1),
				Hand.of(Arrays.asList(new Priest(), new Guard())),
				new ArrayList<PlayedCard>(),
				PlayerStatus.ACTIVE);
		expectedTargetPlayer.getPlayedCards().add(
				PlayedCard.of(
						new Handmaid(),
						CardPlayType.PLAYED,
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(expectedTargetPlayer, VoidEffect.of()))))
		);
		
		RoundPlayer expectedCurrentPlayer = RoundPlayer.of(
				expectedPlayers.get(0),
				Hand.of(Collections.singletonList(new Priest())),
				Collections.singletonList(PlayedCard.of(
						new Baron(),
						CardPlayType.PLAYED,
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(null, VoidEffect.of()))))),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedCurrentPlayer);
		expectedActivePlayers.add(expectedTargetPlayer);
		RoundPlayer expectedOtherPlayer = RoundPlayer.of(
				expectedPlayers.get(2),
				Hand.of(Collections.singletonList(new Baron())),
				new ArrayList<PlayedCard>(),
				PlayerStatus.ACTIVE);
		expectedOtherPlayer.getPlayedCards().add(
				PlayedCard.of(
						new Handmaid(),
						CardPlayType.PLAYED,
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(expectedOtherPlayer, VoidEffect.of()))))
		);
		expectedActivePlayers.add(expectedOtherPlayer);
		
		GameRound expectedRound = new RoundBuilder(
				null,
				1,
				expectedBoard,
				expectedActivePlayers,
				expectedEliminatedPlayers,
				1,
				RoundStatus.RUNNING,
				Collections.emptyList())
				.build();
		
		Game expectedGame = new GameBuilder(
				game.getId(),
				game.getPlayers(),
				game.getGameOptions())
				.gameStatus(GameStatus.RUNNING)
				.addRound(expectedRound)
				.build();
		expectedRound.setGame(expectedGame);
		
		//Check that the expected board state is the same as we got in the test.
		assertEquals(expectedGame, game);
		
		return;
	}
	
	@Test
	public void testPlayingBaronWithNoTargetFails() {
		//Buildup for testing case:
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new Baron(), new Priest(), new Baron(), new Guard(), new Priest()));
		game.start(cards);
		
		Player currentPlayer = players.get(0);
		
		Pair<Player, List<PossibleMove>> moves = game.findPossibleMoves();
		Pair<Player, List<PossibleMove>> expectedMoves = new Pair<Player, List<PossibleMove>>(
				expectedPlayers.get(0),
				Arrays.asList(
						PossibleMove.of(
								new Baron(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2)
								),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(1, 1)),
						PossibleMove.of(
								new Guard(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2)
								),
								Collections.singletonList(GuardEffect.of(-1)),
								new Pair<Integer, Integer>(1, 1))
				));
		assertEquals(expectedMoves, moves);
		//Performing the move.
		try {
			Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
					game.performMove(PlayerMove.of(currentPlayer, new Baron(),
							Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(null, VoidEffect.of())))));
			Assert.fail("Should not allow playing without a target.");
		}
		catch (ActionImpossibleException e) {
			Assert.assertTrue(true);
		}
		return;
	}
	
	@Test
	public void testPlayingHandmaidSuccess() {
		//Buildup for testing case:
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new Baron(), new Priest(), new Baron(), new Handmaid(), new Guard()));
		game.start(cards);
		
		Player currentPlayer = players.get(0);
		
		Pair<Player, List<PossibleMove>> moves = game.findPossibleMoves();
		Pair<Player, List<PossibleMove>> expectedMoves = new Pair<Player, List<PossibleMove>>(
				expectedPlayers.get(0),
				Arrays.asList(
						PossibleMove.of(
								new Baron(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2)
								),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(1, 1)),
						PossibleMove.of(
								new Handmaid(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(0)
								),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(1, 1))
				));
		assertEquals(expectedMoves, moves);
		RoundPlayer target = game.getCurrentRound().getRoundPlayerByPlayer(expectedPlayers.get(0));
		//Performing the move.
		Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
				game.performMove(PlayerMove.of(currentPlayer, new Handmaid(),
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(target, VoidEffect.of())))));
		
		//Performing the move had correct response.
		assertEquals(MoveGameResult.GAME_CONTINUES, result.getValue0());
		assertEquals(MoveRoundResult.ROUND_CONTINUES, result.getValue1());
		assertEquals(HandmaidActionResult.class, result.getValue2().getClass());
		assertEquals("Success", ((HandmaidActionResult)result.getValue2()).getResult());
		
		//Create the expected board state with all players etc.
		Board expectedBoard = new BoardBuilder(Collections.emptyList()).removedCard(new Guard()).build();
		
		List<RoundPlayer> expectedActivePlayers = new ArrayList<RoundPlayer>();
		List<RoundPlayer> expectedEliminatedPlayers = new ArrayList<RoundPlayer>();
		RoundPlayer expectedTargetPlayer  = RoundPlayer.of(
				expectedPlayers.get(1),
				Hand.of(Arrays.asList(new Priest(), new Guard())),
				Collections.emptyList(),
				PlayerStatus.ACTIVE);
		
		RoundPlayer expectedCurrentPlayer = RoundPlayer.of(
				expectedPlayers.get(0),
				Hand.of(Collections.singletonList(new Baron())),
				new ArrayList<PlayedCard>(),
				PlayerStatus.ACTIVE);
		expectedCurrentPlayer.getPlayedCards().add(
				PlayedCard.of(
						new Handmaid(),
						CardPlayType.PLAYED,
						Target.of(Collections.singletonList(
								new Pair<RoundPlayer, AbstractEffect>(expectedCurrentPlayer, VoidEffect.of()))))
		);
		expectedActivePlayers.add(expectedCurrentPlayer);
		expectedActivePlayers.add(expectedTargetPlayer);
		RoundPlayer expectedOtherPlayer = RoundPlayer.of(
				expectedPlayers.get(2),
				Hand.of(Collections.singletonList(new Baron())),
				Collections.emptyList(),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedOtherPlayer);
		
		GameRound expectedRound = new RoundBuilder(
				null,
				1,
				expectedBoard,
				expectedActivePlayers,
				expectedEliminatedPlayers,
				1,
				RoundStatus.RUNNING,
				Collections.emptyList())
				.build();
		
		Game expectedGame = new GameBuilder(
				game.getId(),
				game.getPlayers(),
				game.getGameOptions())
				.gameStatus(GameStatus.RUNNING)
				.addRound(expectedRound)
				.build();
		expectedRound.setGame(expectedGame);
		
		//Check that the expected board state is the same as we got in the test.
		assertEquals(expectedGame, game);
		
		return;
	}
	
	@Test
	public void testPlayingHandmaidWithNoTargetFails() {
		//Buildup for testing case:
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new Handmaid(), new Priest(), new Baron(), new Guard(), new Priest()));
		game.start(cards);
		
		Player currentPlayer = players.get(0);
		
		Pair<Player, List<PossibleMove>> moves = game.findPossibleMoves();
		Pair<Player, List<PossibleMove>> expectedMoves = new Pair<Player, List<PossibleMove>>(
				expectedPlayers.get(0),
				Arrays.asList(
						PossibleMove.of(
								new Handmaid(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(0)
								),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(1, 1)),
						PossibleMove.of(
								new Guard(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2)
								),
								Collections.singletonList(GuardEffect.of(-1)),
								new Pair<Integer, Integer>(1, 1))
				));
		assertEquals(expectedMoves, moves);
		//Performing the move.
		try {
			Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
					game.performMove(PlayerMove.of(currentPlayer, new Handmaid(),
							Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(null, VoidEffect.of())))));
			Assert.fail("Should not allow playing without a target.");
		}
		catch (ActionImpossibleException e) {
			Assert.assertTrue(true);
		}
		return;
	}
	
	@Test
	public void testIgnoringHandmaidFails() {
		//Buildup for testing case:
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new Baron(), new Priest(), new Baron(), new Priest(), new Guard()));
		game.start(cards);
		
		setHandmaids();
		
		Player currentPlayer = players.get(0);
		
		Pair<Player, List<PossibleMove>> moves = game.findPossibleMoves();
		Pair<Player, List<PossibleMove>> expectedMoves = new Pair<Player, List<PossibleMove>>(
				expectedPlayers.get(0),
				Arrays.asList(
						PossibleMove.of(
								new Baron(),
								Arrays.asList((RoundPlayer) null),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(1, 1)),
						PossibleMove.of(
								new Priest(),
								Arrays.asList((RoundPlayer) null),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(1, 1))
				));
		assertEquals(expectedMoves, moves);
		
		RoundPlayer target = game.getCurrentRound().getRoundPlayerByPlayer(expectedPlayers.get(1));
		//Performing the move.
		try {
			Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
					game.performMove(PlayerMove.of(currentPlayer, new Baron(),
							Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(target, VoidEffect.of())))));
			Assert.fail("Should not allow playing against Handmaid.");
		}
		catch (ActionImpossibleException e) {
			Assert.assertTrue(true);
		}
		return;
	}
	
	@Test
	public void testPlayingPrinceOtherNextPlayerGameContinues() {
		//Buildup for testing case:
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new Baron(), new Priest(), new Baron(), new Prince(), new Guard(), new King()));
		game.start(cards);
		
		Player currentPlayer = players.get(0);
		
		Pair<Player, List<PossibleMove>> moves = game.findPossibleMoves();
		Pair<Player, List<PossibleMove>> expectedMoves = new Pair<Player, List<PossibleMove>>(
				expectedPlayers.get(0),
				Arrays.asList(
						PossibleMove.of(
								new Baron(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2)
								),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(1, 1)),
						PossibleMove.of(
								new Prince(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2),
										game.getCurrentRound().getActivePlayers().get(0)
								),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(1, 1))
				));
		assertEquals(expectedMoves, moves);
		
		RoundPlayer target = game.getCurrentRound().getRoundPlayerByPlayer(expectedPlayers.get(1));
		//Performing the move.
		Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
				game.performMove(PlayerMove.of(currentPlayer, new Prince(),
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(target, VoidEffect.of())))));
		
		//Performing the move had correct response.
		assertEquals(MoveGameResult.GAME_CONTINUES, result.getValue0());
		assertEquals(MoveRoundResult.ROUND_CONTINUES, result.getValue1());
		assertEquals(PrinceActionResult.class, result.getValue2().getClass());
		assertEquals("Success", ((PrinceActionResult)result.getValue2()).getResult());
		assertEquals(Priest.class, ((PrinceActionResult)result.getValue2()).getDiscardedCard().getClass());
		
		//Create the expected board state with all players etc.
		Board expectedBoard = new BoardBuilder(Collections.emptyList()).removedCard(new Guard()).build();
		
		List<RoundPlayer> expectedActivePlayers = new ArrayList<RoundPlayer>();
		List<RoundPlayer> expectedEliminatedPlayers = new ArrayList<RoundPlayer>();
		RoundPlayer expectedTargetPlayer  = RoundPlayer.of(
				expectedPlayers.get(1),
				Hand.of(Arrays.asList(new Guard(), new King())),
				Arrays.asList(PlayedCard.of(
						new Priest(),
						CardPlayType.DISCARDED,
						null)),
				PlayerStatus.ACTIVE);
		
		RoundPlayer expectedCurrentPlayer = RoundPlayer.of(
				expectedPlayers.get(0),
				Hand.of(Collections.singletonList(new Baron())),
				Collections.singletonList(PlayedCard.of(
						new Prince(),
						CardPlayType.PLAYED,
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(expectedTargetPlayer, VoidEffect.of()))))),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedCurrentPlayer);
		expectedActivePlayers.add(expectedTargetPlayer);
		RoundPlayer expectedOtherPlayer = RoundPlayer.of(
				expectedPlayers.get(2),
				Hand.of(Collections.singletonList(new Baron())),
				Collections.emptyList(),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedOtherPlayer);
		
		GameRound expectedRound = new RoundBuilder(
				null,
				1,
				expectedBoard,
				expectedActivePlayers,
				expectedEliminatedPlayers,
				1,
				RoundStatus.RUNNING,
				Collections.emptyList())
				.build();
		
		Game expectedGame = new GameBuilder(
				game.getId(),
				game.getPlayers(),
				game.getGameOptions())
				.gameStatus(GameStatus.RUNNING)
				.addRound(expectedRound)
				.build();
		expectedRound.setGame(expectedGame);
		
		//Check that the expected board state is the same as we got in the test.
		assertEquals(expectedGame, game);
		
		return;
	}
	
	@Test
	public void testPlayingPrinceOtherNextPlayerRoundEnds() {
		//Buildup for testing case:
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new Baron(), new Priest(), new Baron(), new Prince(), new Guard()));
		game.start(cards);
		
		Player currentPlayer = players.get(0);
		
		Pair<Player, List<PossibleMove>> moves = game.findPossibleMoves();
		Pair<Player, List<PossibleMove>> expectedMoves = new Pair<Player, List<PossibleMove>>(
				expectedPlayers.get(0),
				Arrays.asList(
						PossibleMove.of(
								new Baron(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2)
								),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(1, 1)),
						PossibleMove.of(
								new Prince(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2),
										game.getCurrentRound().getActivePlayers().get(0)
								),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(1, 1))
				));
		assertEquals(expectedMoves, moves);
		
		RoundPlayer target = game.getCurrentRound().getRoundPlayerByPlayer(expectedPlayers.get(1));
		//Performing the move.
		Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
				game.performMove(PlayerMove.of(currentPlayer, new Prince(),
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(target, VoidEffect.of())))));
		
		//Performing the move had correct response.
		assertEquals(MoveGameResult.GAME_CONTINUES, result.getValue0());
		assertEquals(MoveRoundResult.ROUND_ENDS, result.getValue1());
		assertEquals(PrinceActionResult.class, result.getValue2().getClass());
		assertEquals("Success", ((PrinceActionResult)result.getValue2()).getResult());
		assertEquals(Priest.class, ((PrinceActionResult)result.getValue2()).getDiscardedCard().getClass());
		
		//Create the expected board state with all players etc.
		Board expectedBoard = new BoardBuilder(Collections.emptyList()).removedCard(new Guard()).build();
		
		//TODO Since we can't know what the next random round is, have to get it from existing game. No error/validity check...
		GameRound expectedNewRound = game.getCurrentRound();
		//We know who won the last round. Add a point to him.
		expectedPlayers.get(0).addPoint();
		
		List<RoundPlayer> expectedActivePlayers = new ArrayList<RoundPlayer>();
		List<RoundPlayer> expectedEliminatedPlayers = new ArrayList<RoundPlayer>();
		RoundPlayer expectedTargetPlayer  = RoundPlayer.of(
				expectedPlayers.get(1),
				Hand.of(Arrays.asList(new Guard())),
				Arrays.asList(PlayedCard.of(
						new Priest(),
						CardPlayType.DISCARDED,
						null)),
				PlayerStatus.ACTIVE);
		
		RoundPlayer expectedCurrentPlayer = RoundPlayer.of(
				expectedPlayers.get(0),
				Hand.of(Collections.singletonList(new Baron())),
				Collections.singletonList(PlayedCard.of(
						new Prince(),
						CardPlayType.PLAYED,
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(expectedTargetPlayer, VoidEffect.of()))))),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedCurrentPlayer);
		expectedActivePlayers.add(expectedTargetPlayer);
		RoundPlayer expectedOtherPlayer = RoundPlayer.of(
				expectedPlayers.get(2),
				Hand.of(Collections.singletonList(new Baron())),
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
				.gameStatus(GameStatus.RUNNING)
				.addRound(expectedRound)
				.addRound(expectedNewRound)
				.build();
		expectedRound.setGame(expectedGame);
		expectedNewRound.setGame(expectedGame);
		
		//Check that the expected board state is the same as we got in the test.
		assertEquals(expectedGame, game);
		
		return;
	}
	
	@Test
	public void testPlayingPrinceOtherGetsRemovedCardRoundEnds() {
		//Buildup for testing case:
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new Baron(), new Priest(), new Baron(), new Prince()));
		game.start(cards);
		
		Player currentPlayer = players.get(0);
		
		Pair<Player, List<PossibleMove>> moves = game.findPossibleMoves();
		Pair<Player, List<PossibleMove>> expectedMoves = new Pair<Player, List<PossibleMove>>(
				expectedPlayers.get(0),
				Arrays.asList(
						PossibleMove.of(
								new Baron(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2)
								),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(1, 1)),
						PossibleMove.of(
								new Prince(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2),
										game.getCurrentRound().getActivePlayers().get(0)
								),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(1, 1))
				));
		assertEquals(expectedMoves, moves);
		RoundPlayer target = game.getCurrentRound().getRoundPlayerByPlayer(expectedPlayers.get(1));
		//Performing the move.
		Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
				game.performMove(PlayerMove.of(currentPlayer, new Prince(),
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(target, VoidEffect.of())))));
		
		//Performing the move had correct response.
		assertEquals(MoveGameResult.GAME_CONTINUES, result.getValue0());
		assertEquals(MoveRoundResult.ROUND_ENDS, result.getValue1());
		assertEquals(PrinceActionResult.class, result.getValue2().getClass());
		assertEquals("Success", ((PrinceActionResult)result.getValue2()).getResult());
		assertEquals(Priest.class, ((PrinceActionResult)result.getValue2()).getDiscardedCard().getClass());
		
		//Create the expected board state with all players etc.
		Board expectedBoard = new BoardBuilder(Collections.emptyList()).removedCard(null).build();
		
		//TODO Since we can't know what the next random round is, have to get it from existing game. No error/validity check...
		GameRound expectedNewRound = game.getCurrentRound();
		//We know who won the last round. Add a point to him.
		expectedPlayers.get(0).addPoint();
		
		List<RoundPlayer> expectedActivePlayers = new ArrayList<RoundPlayer>();
		List<RoundPlayer> expectedEliminatedPlayers = new ArrayList<RoundPlayer>();
		RoundPlayer expectedTargetPlayer  = RoundPlayer.of(
				expectedPlayers.get(1),
				Hand.of(Arrays.asList(new Guard())),
				Arrays.asList(PlayedCard.of(
						new Priest(),
						CardPlayType.DISCARDED,
						null)),
				PlayerStatus.ACTIVE);
		
		RoundPlayer expectedCurrentPlayer = RoundPlayer.of(
				expectedPlayers.get(0),
				Hand.of(Collections.singletonList(new Baron())),
				Collections.singletonList(PlayedCard.of(
						new Prince(),
						CardPlayType.PLAYED,
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(expectedTargetPlayer, VoidEffect.of()))))),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedCurrentPlayer);
		expectedActivePlayers.add(expectedTargetPlayer);
		RoundPlayer expectedOtherPlayer = RoundPlayer.of(
				expectedPlayers.get(2),
				Hand.of(Collections.singletonList(new Baron())),
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
				.gameStatus(GameStatus.RUNNING)
				.addRound(expectedRound)
				.addRound(expectedNewRound)
				.build();
		expectedRound.setGame(expectedGame);
		expectedNewRound.setGame(expectedGame);
		
		//Check that the expected board state is the same as we got in the test.
		assertEquals(expectedGame, game);
		
		return;
	}
	
	@Test
	public void testPlayingPrinceOtherPrincess() {
		//Buildup for testing case:
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new Baron(), new Princess(), new Baron(), new Prince(), new Guard()));
		game.start(cards);
		
		Player currentPlayer = players.get(0);
		
		Pair<Player, List<PossibleMove>> moves = game.findPossibleMoves();
		Pair<Player, List<PossibleMove>> expectedMoves = new Pair<Player, List<PossibleMove>>(
				expectedPlayers.get(0),
				Arrays.asList(
						PossibleMove.of(
								new Baron(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2)
								),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(1, 1)),
						PossibleMove.of(
								new Prince(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2),
										game.getCurrentRound().getActivePlayers().get(0)
								),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(1, 1))
				));
		assertEquals(expectedMoves, moves);
		
		RoundPlayer target = game.getCurrentRound().getRoundPlayerByPlayer(expectedPlayers.get(1));
		//Performing the move.
		Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
				game.performMove(PlayerMove.of(currentPlayer, new Prince(),
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(target, VoidEffect.of())))));
		
		//Performing the move had correct response.
		assertEquals(MoveGameResult.GAME_CONTINUES, result.getValue0());
		assertEquals(MoveRoundResult.ROUND_CONTINUES, result.getValue1());
		assertEquals(PrinceActionResult.class, result.getValue2().getClass());
		assertEquals("Success", ((PrinceActionResult)result.getValue2()).getResult());
		assertEquals(Princess.class, ((PrinceActionResult)result.getValue2()).getDiscardedCard().getClass());
		
		//Create the expected board state with all players etc.
		Board expectedBoard = new BoardBuilder(Collections.emptyList()).removedCard(new Guard()).build();
		
		List<RoundPlayer> expectedActivePlayers = new ArrayList<RoundPlayer>();
		List<RoundPlayer> expectedEliminatedPlayers = new ArrayList<RoundPlayer>();
		RoundPlayer expectedTargetPlayer  = RoundPlayer.of(
				expectedPlayers.get(1),
				Hand.of(Collections.emptyList()),
				Arrays.asList(PlayedCard.of(
						new Princess(),
						CardPlayType.DISCARDED,
						null)),
				PlayerStatus.ELIMINATED);
		expectedEliminatedPlayers.add(expectedTargetPlayer);
		
		RoundPlayer expectedCurrentPlayer = RoundPlayer.of(
				expectedPlayers.get(0),
				Hand.of(Collections.singletonList(new Baron())),
				Collections.singletonList(PlayedCard.of(
						new Prince(),
						CardPlayType.PLAYED,
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(expectedTargetPlayer, VoidEffect.of()))))),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedCurrentPlayer);
		
		RoundPlayer expectedOtherPlayer = RoundPlayer.of(
				expectedPlayers.get(2),
				Hand.of(Arrays.asList(new Baron(), new Guard())),
				Collections.emptyList(),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedOtherPlayer);
		
		GameRound expectedRound = new RoundBuilder(
				null,
				1,
				expectedBoard,
				expectedActivePlayers,
				expectedEliminatedPlayers,
				1,
				RoundStatus.RUNNING,
				Collections.emptyList())
				.build();
		
		Game expectedGame = new GameBuilder(
				game.getId(),
				game.getPlayers(),
				game.getGameOptions())
				.gameStatus(GameStatus.RUNNING)
				.addRound(expectedRound)
				.build();
		expectedRound.setGame(expectedGame);
		
		//Check that the expected board state is the same as we got in the test.
		assertEquals(expectedGame, game);
		
		return;
	}
	
	@Test
	public void testPlayingPrinceSelf() {
		//Buildup for testing case:
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new Baron(), new Priest(), new Baron(), new Prince(), new Guard(), new King()));
		game.start(cards);
		
		Player currentPlayer = players.get(0);
		
		Pair<Player, List<PossibleMove>> moves = game.findPossibleMoves();
		Pair<Player, List<PossibleMove>> expectedMoves = new Pair<Player, List<PossibleMove>>(
				expectedPlayers.get(0),
				Arrays.asList(
						PossibleMove.of(
								new Baron(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2)
								),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(1, 1)),
						PossibleMove.of(
								new Prince(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2),
										game.getCurrentRound().getActivePlayers().get(0)
								),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(1, 1))
				));
		assertEquals(expectedMoves, moves);
		RoundPlayer target = game.getCurrentRound().getRoundPlayerByPlayer(expectedPlayers.get(0));
		//Performing the move.
		Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
				game.performMove(PlayerMove.of(currentPlayer, new Prince(),
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(target, VoidEffect.of())))));
		
		//Performing the move had correct response.
		assertEquals(MoveGameResult.GAME_CONTINUES, result.getValue0());
		assertEquals(MoveRoundResult.ROUND_CONTINUES, result.getValue1());
		assertEquals(PrinceActionResult.class, result.getValue2().getClass());
		assertEquals("Success", ((PrinceActionResult)result.getValue2()).getResult());
		assertEquals(Baron.class, ((PrinceActionResult)result.getValue2()).getDiscardedCard().getClass());
		
		//Create the expected board state with all players etc.
		Board expectedBoard = new BoardBuilder(Collections.emptyList()).removedCard(new Guard()).build();
		
		List<RoundPlayer> expectedActivePlayers = new ArrayList<RoundPlayer>();
		List<RoundPlayer> expectedEliminatedPlayers = new ArrayList<RoundPlayer>();
		RoundPlayer expectedTargetPlayer  = RoundPlayer.of(
				expectedPlayers.get(1),
				Hand.of(Arrays.asList(new Priest(), new King())),
				Collections.emptyList(),
				PlayerStatus.ACTIVE);
		
		RoundPlayer expectedCurrentPlayer = RoundPlayer.of(
				expectedPlayers.get(0),
				Hand.of(Collections.singletonList(new Guard())),
				new ArrayList<PlayedCard>(),
				PlayerStatus.ACTIVE);
		expectedCurrentPlayer.getPlayedCards().addAll(
				Arrays.asList(PlayedCard.of(
						new Prince(),
						CardPlayType.PLAYED,
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(expectedCurrentPlayer, VoidEffect.of())))),
						PlayedCard.of(
								new Baron(),
								CardPlayType.DISCARDED,
								null))
		);
		expectedActivePlayers.add(expectedCurrentPlayer);
		expectedActivePlayers.add(expectedTargetPlayer);
		RoundPlayer expectedOtherPlayer = RoundPlayer.of(
				expectedPlayers.get(2),
				Hand.of(Collections.singletonList(new Baron())),
				Collections.emptyList(),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedOtherPlayer);
		
		GameRound expectedRound = new RoundBuilder(
				null,
				1,
				expectedBoard,
				expectedActivePlayers,
				expectedEliminatedPlayers,
				1,
				RoundStatus.RUNNING,
				Collections.emptyList())
				.build();
		
		Game expectedGame = new GameBuilder(
				game.getId(),
				game.getPlayers(),
				game.getGameOptions())
				.gameStatus(GameStatus.RUNNING)
				.addRound(expectedRound)
				.build();
		expectedRound.setGame(expectedGame);
		
		//Check that the expected board state is the same as we got in the test.
		assertEquals(expectedGame, game);
		
		return;
	}
	
	@Test
	public void testPlayingPrinceSelfBecauseHandmaidsPrincess() {
		//Buildup for testing case:
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new Princess(), new Priest(), new Baron(), new Prince(), new Guard()));
		game.start(cards);
		
		setHandmaids();
		
		Player currentPlayer = players.get(0);
		
		Pair<Player, List<PossibleMove>> moves = game.findPossibleMoves();
		Pair<Player, List<PossibleMove>> expectedMoves = new Pair<Player, List<PossibleMove>>(
				expectedPlayers.get(0),
				Arrays.asList(
						PossibleMove.of(
								new Princess(),
								Arrays.asList((RoundPlayer) null),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(0, 0)),
						PossibleMove.of(
								new Prince(),
								Arrays.asList(game.getCurrentRound().getActivePlayers().get(0)),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(1, 1))
				));
		assertEquals(expectedMoves, moves);
		RoundPlayer target = game.getCurrentRound().getRoundPlayerByPlayer(expectedPlayers.get(0));
		//Performing the move.
		Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
				game.performMove(PlayerMove.of(currentPlayer, new Prince(),
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(target, VoidEffect.of())))));
		
		//Performing the move had correct response.
		assertEquals(MoveGameResult.GAME_CONTINUES, result.getValue0());
		assertEquals(MoveRoundResult.ROUND_CONTINUES, result.getValue1());
		assertEquals(PrinceActionResult.class, result.getValue2().getClass());
		assertEquals("Success", ((PrinceActionResult)result.getValue2()).getResult());
		assertEquals(Princess.class, ((PrinceActionResult)result.getValue2()).getDiscardedCard().getClass());
		
		//Create the expected board state with all players etc.
		Board expectedBoard = new BoardBuilder(Collections.emptyList()).removedCard(new Guard()).build();
		
		List<RoundPlayer> expectedActivePlayers = new ArrayList<RoundPlayer>();
		List<RoundPlayer> expectedEliminatedPlayers = new ArrayList<RoundPlayer>();
		RoundPlayer expectedTargetPlayer  = RoundPlayer.of(
				expectedPlayers.get(1),
				Hand.of(Arrays.asList(new Priest(), new Guard())),
				new ArrayList<PlayedCard>(),
				PlayerStatus.ACTIVE);
		expectedTargetPlayer.getPlayedCards().add(
				PlayedCard.of(
						new Handmaid(),
						CardPlayType.PLAYED,
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(expectedTargetPlayer, VoidEffect.of()))))
		);
		
		RoundPlayer expectedCurrentPlayer = RoundPlayer.of(
				expectedPlayers.get(0),
				Hand.of(Collections.emptyList()),
				new ArrayList<PlayedCard>(),
				PlayerStatus.ELIMINATED);
		expectedCurrentPlayer.getPlayedCards().addAll(
				Arrays.asList(PlayedCard.of(
								new Prince(),
								CardPlayType.PLAYED,
								Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(expectedCurrentPlayer, VoidEffect.of())))),
						PlayedCard.of(
								new Princess(),
								CardPlayType.DISCARDED,
								null)
						)
		);
		expectedEliminatedPlayers.add(expectedCurrentPlayer);
		
		expectedActivePlayers.add(expectedTargetPlayer);
		
		RoundPlayer expectedOtherPlayer = RoundPlayer.of(
				expectedPlayers.get(2),
				Hand.of(Arrays.asList(new Baron())),
				new ArrayList<PlayedCard>(),
				PlayerStatus.ACTIVE);
		expectedOtherPlayer.getPlayedCards().add(
				PlayedCard.of(
						new Handmaid(),
						CardPlayType.PLAYED,
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(expectedOtherPlayer, VoidEffect.of()))))
		);
		expectedActivePlayers.add(expectedOtherPlayer);
		
		GameRound expectedRound = new RoundBuilder(
				null,
				1,
				expectedBoard,
				expectedActivePlayers,
				expectedEliminatedPlayers,
				0,
				RoundStatus.RUNNING,
				Collections.emptyList())
				.build();
		
		Game expectedGame = new GameBuilder(
				game.getId(),
				game.getPlayers(),
				game.getGameOptions())
				.gameStatus(GameStatus.RUNNING)
				.addRound(expectedRound)
				.build();
		expectedRound.setGame(expectedGame);
		
		//Check that the expected board state is the same as we got in the test.
		assertEquals(expectedGame, game);
		
		return;
	}
	
	@Test
	public void testPlayingPrinceWithNoTargetFails() {
		//Buildup for testing case:
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new Prince(), new Priest(), new Baron(), new Guard(), new Priest()));
		game.start(cards);
		
		Player currentPlayer = players.get(0);
		
		Pair<Player, List<PossibleMove>> moves = game.findPossibleMoves();
		Pair<Player, List<PossibleMove>> expectedMoves = new Pair<Player, List<PossibleMove>>(
				expectedPlayers.get(0),
				Arrays.asList(
						PossibleMove.of(
								new Prince(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2),
										game.getCurrentRound().getActivePlayers().get(0)
								),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(1, 1)),
						PossibleMove.of(
								new Guard(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2)
								),
								Collections.singletonList(GuardEffect.of(-1)),
								new Pair<Integer, Integer>(1, 1))
				));
		assertEquals(expectedMoves, moves);
		//Performing the move.
		try {
			Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
					game.performMove(PlayerMove.of(currentPlayer, new Prince(),
							Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(null, VoidEffect.of())))));
			Assert.fail("Should not allow playing without a target.");
		}
		catch (ActionImpossibleException e) {
			Assert.assertTrue(true);
		}
		return;
	}
	
	@Test
	public void testPlayingKingSuccess() {
		//Buildup for testing case:
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new King(), new Priest(), new Baron(), new Prince(), new Guard()));
		game.start(cards);
		
		Player currentPlayer = players.get(0);
		
		Pair<Player, List<PossibleMove>> moves = game.findPossibleMoves();
		Pair<Player, List<PossibleMove>> expectedMoves = new Pair<Player, List<PossibleMove>>(
				expectedPlayers.get(0),
				Arrays.asList(
						PossibleMove.of(
								new King(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2)
								),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(1, 1)),
						PossibleMove.of(
								new Prince(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2),
										game.getCurrentRound().getActivePlayers().get(0)
								),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(1, 1))
				));
		assertEquals(expectedMoves, moves);
		RoundPlayer target = game.getCurrentRound().getRoundPlayerByPlayer(expectedPlayers.get(1));
		//Performing the move.
		Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
				game.performMove(PlayerMove.of(currentPlayer, new King(),
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(target, VoidEffect.of())))));
		
		//Performing the move had correct response.
		assertEquals(MoveGameResult.GAME_CONTINUES, result.getValue0());
		assertEquals(MoveRoundResult.ROUND_CONTINUES, result.getValue1());
		assertEquals(KingActionResult.class, result.getValue2().getClass());
		assertEquals("Success", ((KingActionResult)result.getValue2()).getResult());
		assertEquals(Prince.class, ((KingActionResult)result.getValue2()).getOldCard().getClass());
		assertEquals(Priest.class, ((KingActionResult)result.getValue2()).getNewCard().getClass());
		
		//Create the expected board state with all players etc.
		Board expectedBoard = new BoardBuilder(Collections.emptyList()).removedCard(new Guard()).build();
		
		List<RoundPlayer> expectedActivePlayers = new ArrayList<RoundPlayer>();
		List<RoundPlayer> expectedEliminatedPlayers = new ArrayList<RoundPlayer>();
		RoundPlayer expectedTargetPlayer  = RoundPlayer.of(
				expectedPlayers.get(1),
				Hand.of(Arrays.asList(new Prince(), new Guard())),
				Collections.emptyList(),
				PlayerStatus.ACTIVE);
		
		RoundPlayer expectedCurrentPlayer = RoundPlayer.of(
				expectedPlayers.get(0),
				Hand.of(Arrays.asList(new Priest())),
				Collections.singletonList(
						PlayedCard.of(
								new King(),
								CardPlayType.PLAYED,
								Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(expectedTargetPlayer, VoidEffect.of()))))),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedCurrentPlayer);
		expectedActivePlayers.add(expectedTargetPlayer);
		
		RoundPlayer expectedOtherPlayer = RoundPlayer.of(
				expectedPlayers.get(2),
				Hand.of(Collections.singletonList(new Baron())),
				Collections.emptyList(),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedOtherPlayer);
		
		GameRound expectedRound = new RoundBuilder(
				null,
				1,
				expectedBoard,
				expectedActivePlayers,
				expectedEliminatedPlayers,
				1,
				RoundStatus.RUNNING,
				Collections.emptyList())
				.build();
		
		Game expectedGame = new GameBuilder(
				game.getId(),
				game.getPlayers(),
				game.getGameOptions())
				.gameStatus(GameStatus.RUNNING)
				.addRound(expectedRound)
				.build();
		expectedRound.setGame(expectedGame);
		
		//Check that the expected board state is the same as we got in the test.
		assertEquals(expectedGame, game);
		
		return;
	}
	
	@Test
	public void testPlayingKingNoTarget() {
		//Buildup for testing case:
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new King(), new Priest(), new Baron(), new Prince(), new Guard()));
		game.start(cards);
		
		setHandmaids();
		
		Player currentPlayer = players.get(0);
		
		Pair<Player, List<PossibleMove>> moves = game.findPossibleMoves();
		Pair<Player, List<PossibleMove>> expectedMoves = new Pair<Player, List<PossibleMove>>(
				expectedPlayers.get(0),
				Arrays.asList(
						PossibleMove.of(
								new King(),
								Arrays.asList((RoundPlayer) null),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(1, 1)),
						PossibleMove.of(
								new Prince(),
								Arrays.asList(game.getCurrentRound().getActivePlayers().get(0)),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(1, 1))
				));
		assertEquals(expectedMoves, moves);
		//Performing the move.
		Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
				game.performMove(PlayerMove.of(currentPlayer, new King(),
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(null, VoidEffect.of())))));
		
		//Performing the move had correct response.
		assertEquals(MoveGameResult.GAME_CONTINUES, result.getValue0());
		assertEquals(MoveRoundResult.ROUND_CONTINUES, result.getValue1());
		assertEquals(KingActionResult.class, result.getValue2().getClass());
		assertEquals("NoTarget", ((KingActionResult)result.getValue2()).getResult());
		assertEquals(null, ((KingActionResult)result.getValue2()).getOldCard());
		assertEquals(null, ((KingActionResult)result.getValue2()).getNewCard());
		
		//Create the expected board state with all players etc.
		Board expectedBoard = new BoardBuilder(Collections.emptyList()).removedCard(new Guard()).build();
		
		List<RoundPlayer> expectedActivePlayers = new ArrayList<RoundPlayer>();
		List<RoundPlayer> expectedEliminatedPlayers = new ArrayList<RoundPlayer>();
		RoundPlayer expectedTargetPlayer  = RoundPlayer.of(
				expectedPlayers.get(1),
				Hand.of(Arrays.asList(new Priest(), new Guard())),
				new ArrayList<PlayedCard>(),
				PlayerStatus.ACTIVE);
		expectedTargetPlayer.getPlayedCards().add(
				PlayedCard.of(
						new Handmaid(),
						CardPlayType.PLAYED,
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(expectedTargetPlayer, VoidEffect.of()))))
		);
		
		RoundPlayer expectedCurrentPlayer = RoundPlayer.of(
				expectedPlayers.get(0),
				Hand.of(Collections.singletonList(new Prince())),
				Collections.singletonList(PlayedCard.of(
						new King(),
						CardPlayType.PLAYED,
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(null, VoidEffect.of()))))),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedCurrentPlayer);
		expectedActivePlayers.add(expectedTargetPlayer);
		RoundPlayer expectedOtherPlayer = RoundPlayer.of(
				expectedPlayers.get(2),
				Hand.of(Collections.singletonList(new Baron())),
				new ArrayList<PlayedCard>(),
				PlayerStatus.ACTIVE);
		expectedOtherPlayer.getPlayedCards().add(
				PlayedCard.of(
						new Handmaid(),
						CardPlayType.PLAYED,
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(expectedOtherPlayer, VoidEffect.of()))))
		);
		expectedActivePlayers.add(expectedOtherPlayer);
		
		GameRound expectedRound = new RoundBuilder(
				null,
				1,
				expectedBoard,
				expectedActivePlayers,
				expectedEliminatedPlayers,
				1,
				RoundStatus.RUNNING,
				Collections.emptyList())
				.build();
		
		Game expectedGame = new GameBuilder(
				game.getId(),
				game.getPlayers(),
				game.getGameOptions())
				.gameStatus(GameStatus.RUNNING)
				.addRound(expectedRound)
				.build();
		expectedRound.setGame(expectedGame);
		
		//Check that the expected board state is the same as we got in the test.
		assertEquals(expectedGame, game);
		
		return;
	}
	
	@Test
	public void testPlayingKingWithNoTargetFails() {
		//Buildup for testing case:
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new King(), new Priest(), new Baron(), new Guard(), new Priest()));
		game.start(cards);
		
		Player currentPlayer = players.get(0);
		
		Pair<Player, List<PossibleMove>> moves = game.findPossibleMoves();
		Pair<Player, List<PossibleMove>> expectedMoves = new Pair<Player, List<PossibleMove>>(
				expectedPlayers.get(0),
				Arrays.asList(
						PossibleMove.of(
								new King(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2)
								),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(1, 1)),
						PossibleMove.of(
								new Guard(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2)
								),
								Collections.singletonList(GuardEffect.of(-1)),
								new Pair<Integer, Integer>(1, 1))
				));
		assertEquals(expectedMoves, moves);
		//Performing the move.
		try {
			Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
					game.performMove(PlayerMove.of(currentPlayer, new King(),
							Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(null, VoidEffect.of())))));
			Assert.fail("Should not allow playing without a target.");
		}
		catch (ActionImpossibleException e) {
			Assert.assertTrue(true);
		}
		return;
	}
	
	@Test
	public void testPlayingCountessForcedByPrince() {
		//Buildup for testing case:
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new Countess(), new Priest(), new Baron(), new Prince(), new Guard()));
		game.start(cards);
		
		Player currentPlayer = players.get(0);
		
		Pair<Player, List<PossibleMove>> moves = game.findPossibleMoves();
		Pair<Player, List<PossibleMove>> expectedMoves = new Pair<Player, List<PossibleMove>>(
				expectedPlayers.get(0),
				Arrays.asList(
						PossibleMove.of(
								new Countess(),
								Arrays.asList((RoundPlayer) null),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(0, 0))
				));
		assertEquals(expectedMoves, moves);
		//Performing the move.
		Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
				game.performMove(PlayerMove.of(currentPlayer, new Countess(),
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(null, VoidEffect.of())))));
		
		//Performing the move had correct response.
		assertEquals(MoveGameResult.GAME_CONTINUES, result.getValue0());
		assertEquals(MoveRoundResult.ROUND_CONTINUES, result.getValue1());
		assertEquals(CountessActionResult.class, result.getValue2().getClass());
		assertEquals("Success", ((CountessActionResult)result.getValue2()).getResult());
		
		//Create the expected board state with all players etc.
		Board expectedBoard = new BoardBuilder(Collections.emptyList()).removedCard(new Guard()).build();
		
		List<RoundPlayer> expectedActivePlayers = new ArrayList<RoundPlayer>();
		List<RoundPlayer> expectedEliminatedPlayers = new ArrayList<RoundPlayer>();
		RoundPlayer expectedTargetPlayer  = RoundPlayer.of(
				expectedPlayers.get(1),
				Hand.of(Arrays.asList(new Priest(), new Guard())),
				Collections.emptyList(),
				PlayerStatus.ACTIVE);
		
		RoundPlayer expectedCurrentPlayer = RoundPlayer.of(
				expectedPlayers.get(0),
				Hand.of(Collections.singletonList(new Prince())),
				Collections.singletonList(PlayedCard.of(
						new Countess(),
						CardPlayType.PLAYED,
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(null, VoidEffect.of()))))),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedCurrentPlayer);
		expectedActivePlayers.add(expectedTargetPlayer);
		
		RoundPlayer expectedOtherPlayer = RoundPlayer.of(
				expectedPlayers.get(2),
				Hand.of(Collections.singletonList(new Baron())),
				Collections.emptyList(),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedOtherPlayer);
		
		GameRound expectedRound = new RoundBuilder(
				null,
				1,
				expectedBoard,
				expectedActivePlayers,
				expectedEliminatedPlayers,
				1,
				RoundStatus.RUNNING,
				Collections.emptyList())
				.build();
		
		Game expectedGame = new GameBuilder(
				game.getId(),
				game.getPlayers(),
				game.getGameOptions())
				.gameStatus(GameStatus.RUNNING)
				.addRound(expectedRound)
				.build();
		expectedRound.setGame(expectedGame);
		
		//Check that the expected board state is the same as we got in the test.
		assertEquals(expectedGame, game);
		
		return;
	}
	
	@Test
	public void testPlayingCountessForcedByKing() {
		//Buildup for testing case:
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new Countess(), new Priest(), new Baron(), new King(), new Guard()));
		game.start(cards);
		
		Player currentPlayer = players.get(0);
		
		Pair<Player, List<PossibleMove>> moves = game.findPossibleMoves();
		Pair<Player, List<PossibleMove>> expectedMoves = new Pair<Player, List<PossibleMove>>(
				expectedPlayers.get(0),
				Arrays.asList(
						PossibleMove.of(
								new Countess(),
								Arrays.asList((RoundPlayer) null),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(0, 0))
				));
		assertEquals(expectedMoves, moves);
		//Performing the move.
		Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
				game.performMove(PlayerMove.of(currentPlayer, new Countess(),
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(null, VoidEffect.of())))));
		
		//Performing the move had correct response.
		assertEquals(MoveGameResult.GAME_CONTINUES, result.getValue0());
		assertEquals(MoveRoundResult.ROUND_CONTINUES, result.getValue1());
		assertEquals(CountessActionResult.class, result.getValue2().getClass());
		assertEquals("Success", ((CountessActionResult)result.getValue2()).getResult());
		
		//Create the expected board state with all players etc.
		Board expectedBoard = new BoardBuilder(Collections.emptyList()).removedCard(new Guard()).build();
		
		List<RoundPlayer> expectedActivePlayers = new ArrayList<RoundPlayer>();
		List<RoundPlayer> expectedEliminatedPlayers = new ArrayList<RoundPlayer>();
		RoundPlayer expectedTargetPlayer  = RoundPlayer.of(
				expectedPlayers.get(1),
				Hand.of(Arrays.asList(new Priest(), new Guard())),
				Collections.emptyList(),
				PlayerStatus.ACTIVE);
		
		RoundPlayer expectedCurrentPlayer = RoundPlayer.of(
				expectedPlayers.get(0),
				Hand.of(Collections.singletonList(new King())),
				Collections.singletonList(PlayedCard.of(
						new Countess(),
						CardPlayType.PLAYED,
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(null, VoidEffect.of()))))),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedCurrentPlayer);
		expectedActivePlayers.add(expectedTargetPlayer);
		
		RoundPlayer expectedOtherPlayer = RoundPlayer.of(
				expectedPlayers.get(2),
				Hand.of(Collections.singletonList(new Baron())),
				Collections.emptyList(),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedOtherPlayer);
		
		GameRound expectedRound = new RoundBuilder(
				null,
				1,
				expectedBoard,
				expectedActivePlayers,
				expectedEliminatedPlayers,
				1,
				RoundStatus.RUNNING,
				Collections.emptyList())
				.build();
		
		Game expectedGame = new GameBuilder(
				game.getId(),
				game.getPlayers(),
				game.getGameOptions())
				.gameStatus(GameStatus.RUNNING)
				.addRound(expectedRound)
				.build();
		expectedRound.setGame(expectedGame);
		
		//Check that the expected board state is the same as we got in the test.
		assertEquals(expectedGame, game);
		
		return;
	}
	
	@Test
	public void testPlayingCountessNotForced() {
		//Buildup for testing case:
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new Countess(), new Priest(), new Baron(), new Guard(), new Guard()));
		game.start(cards);
		
		Player currentPlayer = players.get(0);
		
		Pair<Player, List<PossibleMove>> moves = game.findPossibleMoves();
		Pair<Player, List<PossibleMove>> expectedMoves = new Pair<Player, List<PossibleMove>>(
				expectedPlayers.get(0),
				Arrays.asList(
						PossibleMove.of(
								new Countess(),
								Arrays.asList((RoundPlayer) null),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(0, 0)),
						PossibleMove.of(
								new Guard(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2)
								),
								Collections.singletonList(GuardEffect.of(-1)),
								new Pair<Integer, Integer>(1, 1))
				));
		assertEquals(expectedMoves, moves);
		//Performing the move.
		Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
				game.performMove(PlayerMove.of(currentPlayer, new Countess(),
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(null, VoidEffect.of())))));
		
		//Performing the move had correct response.
		assertEquals(MoveGameResult.GAME_CONTINUES, result.getValue0());
		assertEquals(MoveRoundResult.ROUND_CONTINUES, result.getValue1());
		assertEquals(CountessActionResult.class, result.getValue2().getClass());
		assertEquals("Success", ((CountessActionResult)result.getValue2()).getResult());
		
		//Create the expected board state with all players etc.
		Board expectedBoard = new BoardBuilder(Collections.emptyList()).removedCard(new Guard()).build();
		
		List<RoundPlayer> expectedActivePlayers = new ArrayList<RoundPlayer>();
		List<RoundPlayer> expectedEliminatedPlayers = new ArrayList<RoundPlayer>();
		RoundPlayer expectedTargetPlayer  = RoundPlayer.of(
				expectedPlayers.get(1),
				Hand.of(Arrays.asList(new Priest(), new Guard())),
				Collections.emptyList(),
				PlayerStatus.ACTIVE);
		
		RoundPlayer expectedCurrentPlayer = RoundPlayer.of(
				expectedPlayers.get(0),
				Hand.of(Collections.singletonList(new Guard())),
				Collections.singletonList(PlayedCard.of(
						new Countess(),
						CardPlayType.PLAYED,
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(null, VoidEffect.of()))))),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedCurrentPlayer);
		expectedActivePlayers.add(expectedTargetPlayer);
		
		RoundPlayer expectedOtherPlayer = RoundPlayer.of(
				expectedPlayers.get(2),
				Hand.of(Collections.singletonList(new Baron())),
				Collections.emptyList(),
				PlayerStatus.ACTIVE);
		expectedActivePlayers.add(expectedOtherPlayer);
		
		GameRound expectedRound = new RoundBuilder(
				null,
				1,
				expectedBoard,
				expectedActivePlayers,
				expectedEliminatedPlayers,
				1,
				RoundStatus.RUNNING,
				Collections.emptyList())
				.build();
		
		Game expectedGame = new GameBuilder(
				game.getId(),
				game.getPlayers(),
				game.getGameOptions())
				.gameStatus(GameStatus.RUNNING)
				.addRound(expectedRound)
				.build();
		expectedRound.setGame(expectedGame);
		
		//Check that the expected board state is the same as we got in the test.
		assertEquals(expectedGame, game);
		
		return;
	}
	
	@Test
	public void testIgnoringForcedCountessFails() {
		//Buildup for testing case:
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new Countess(), new Priest(), new Baron(), new King(), new Guard()));
		game.start(cards);
		
		setHandmaids();
		
		Player currentPlayer = players.get(0);
		
		Pair<Player, List<PossibleMove>> moves = game.findPossibleMoves();
		Pair<Player, List<PossibleMove>> expectedMoves = new Pair<Player, List<PossibleMove>>(
				expectedPlayers.get(0),
				Arrays.asList(
						PossibleMove.of(
								new Countess(),
								Arrays.asList((RoundPlayer) null),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(0, 0))
				));
		assertEquals(expectedMoves, moves);
		
		RoundPlayer target = game.getCurrentRound().getRoundPlayerByPlayer(expectedPlayers.get(1));
		//Performing the move.
		try {
			Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
					game.performMove(PlayerMove.of(currentPlayer, new King(),
							Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(target, VoidEffect.of())))));
			Assert.fail("Should not allow playing when forced to play Countess.");
		}
		catch (ActionImpossibleException e) {
			Assert.assertTrue(true);
		}
		return;
	}
	
	@Test
	public void testPlayingPrincess() {
		//Buildup for testing case:
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new Guard(), new Priest(), new Baron(), new Princess(), new Priest()));
		game.start(cards);
		
		Player currentPlayer = players.get(0);
		
		Pair<Player, List<PossibleMove>> moves = game.findPossibleMoves();
		Pair<Player, List<PossibleMove>> expectedMoves = new Pair<Player, List<PossibleMove>>(
				expectedPlayers.get(0),
				Arrays.asList(
						PossibleMove.of(
								new Guard(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2)
								),
								Collections.singletonList(GuardEffect.of(-1)),
								new Pair<Integer, Integer>(1, 1)),
						PossibleMove.of(
								new Princess(),
								Arrays.asList((RoundPlayer) null),
								Collections.singletonList(VoidEffect.of()),
								new Pair<Integer, Integer>(0, 0))
				));
		assertEquals(expectedMoves, moves);
		//Performing the move.
		Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
				game.performMove(PlayerMove.of(currentPlayer, new Princess(),
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(null, VoidEffect.of())))));
		
		//Performing the move had correct response.
		assertEquals(MoveGameResult.GAME_CONTINUES, result.getValue0());
		assertEquals(MoveRoundResult.ROUND_CONTINUES, result.getValue1());
		assertEquals(PrincessActionResult.class, result.getValue2().getClass());
		assertEquals("Success", ((PrincessActionResult)result.getValue2()).getResult());
		
		//Create the expected board state with all players etc.
		Board expectedBoard = new BoardBuilder(Collections.emptyList()).removedCard(new Guard()).build();
		
		List<RoundPlayer> expectedActivePlayers = new ArrayList<RoundPlayer>();
		List<RoundPlayer> expectedEliminatedPlayers = new ArrayList<RoundPlayer>();
		RoundPlayer expectedTargetPlayer  = RoundPlayer.of(
				expectedPlayers.get(1),
				Hand.of(Arrays.asList(new Priest(), new Priest())),
				Collections.emptyList(),
				PlayerStatus.ACTIVE);
		
		RoundPlayer expectedCurrentPlayer = RoundPlayer.of(
				expectedPlayers.get(0),
				Hand.of(Collections.emptyList()),
				Arrays.asList(PlayedCard.of(
						new Princess(),
						CardPlayType.PLAYED,
						Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(null, VoidEffect.of())))),
						PlayedCard.of(
								new Guard(),
								CardPlayType.DISCARDED,
								null)),
				PlayerStatus.ELIMINATED);
		expectedEliminatedPlayers.add(expectedCurrentPlayer);
		expectedActivePlayers.add(expectedTargetPlayer);
		
		RoundPlayer expectedOtherPlayer = RoundPlayer.of(
				expectedPlayers.get(2),
				Hand.of(Collections.singletonList(new Baron())),
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
				RoundStatus.RUNNING,
				Collections.emptyList())
				.build();
		
		Game expectedGame = new GameBuilder(
				game.getId(),
				game.getPlayers(),
				game.getGameOptions())
				.gameStatus(GameStatus.RUNNING)
				.addRound(expectedRound)
				.build();
		expectedRound.setGame(expectedGame);
		
		//Check that the expected board state is the same as we got in the test.
		assertEquals(expectedGame, game);
		
		return;
	}
	
	@Test
	public void testPlayingCardWhichYouDoNotHaveFails() {
		//Buildup for testing case:
		List<Card> cards = new ArrayList<Card>(Arrays.asList(new Guard(), new Guard(), new Priest(), new Baron(), new Guard(), new Guard()));
		game.start(cards);
		
		Player currentPlayer = players.get(0);
		
		Pair<Player, List<PossibleMove>> moves = game.findPossibleMoves();
		Pair<Player, List<PossibleMove>> expectedMoves = new Pair<Player, List<PossibleMove>>(
				expectedPlayers.get(0),
				Arrays.asList(
						PossibleMove.of(
								new Guard(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2)
								),
								Collections.singletonList(GuardEffect.of(-1)),
								new Pair<Integer, Integer>(1, 1)),
						PossibleMove.of(
								new Guard(),
								Arrays.asList(
										game.getCurrentRound().getActivePlayers().get(1),
										game.getCurrentRound().getActivePlayers().get(2)
								),
								Collections.singletonList(GuardEffect.of(-1)),
								new Pair<Integer, Integer>(1, 1))
				));
		assertEquals(expectedMoves, moves);
		RoundPlayer target = game.getCurrentRound().getRoundPlayerByPlayer(expectedPlayers.get(1));
		//Performing the move.
		try {
			Triplet<MoveGameResult, MoveRoundResult, CardActionResult> result =
					game.performMove(PlayerMove.of(currentPlayer, new Priest(),
							Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(target, GuardEffect.of(3))))));
			Assert.fail("Should not allow playing a card you do not have.");
		}
		catch (ActionImpossibleException e) {
			Assert.assertTrue(true);
		}
		return;
	}
	
	private void setHandmaids() {
		List<RoundPlayer> opponents = game.getCurrentRound().getActivePlayers();
		opponents.get(1).getPlayedCards().add(PlayedCard.of(new Handmaid(), CardPlayType.PLAYED,
				Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(opponents.get(1), VoidEffect.of())))));
		opponents.get(2).getPlayedCards().add(PlayedCard.of(new Handmaid(), CardPlayType.PLAYED,
				Target.of(Collections.singletonList(new Pair<RoundPlayer, AbstractEffect>(opponents.get(2), VoidEffect.of())))));
	}
	
}
