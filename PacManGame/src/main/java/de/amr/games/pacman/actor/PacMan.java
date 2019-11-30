package de.amr.games.pacman.actor;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.actor.PacManState.DEAD;
import static de.amr.games.pacman.actor.PacManState.DYING;
import static de.amr.games.pacman.actor.PacManState.HOME;
import static de.amr.games.pacman.actor.PacManState.HUNGRY;
import static de.amr.games.pacman.actor.behavior.pacman.PacManSteerings.steeredByKeys;
import static de.amr.games.pacman.model.Maze.NESW;
import static de.amr.games.pacman.model.PacManGame.TS;
import static de.amr.games.pacman.model.PacManGame.sec;
import static de.amr.games.pacman.model.PacManGame.LevelData.PACMAN_POWER_SECONDS;
import static de.amr.games.pacman.model.PacManGame.LevelData.PACMAN_POWER_SPEED;
import static de.amr.games.pacman.model.PacManGame.LevelData.PACMAN_SPEED;

import java.awt.event.KeyEvent;
import java.util.Optional;
import java.util.logging.Logger;

import de.amr.easy.game.assets.Sound;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.actor.behavior.Steering;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGettingWeakerEvent;
import de.amr.games.pacman.controller.event.PacManGhostCollisionEvent;
import de.amr.games.pacman.controller.event.PacManKilledEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.graph.grid.impl.Top4;
import de.amr.statemachine.State;
import de.amr.statemachine.StateMachine;

/**
 * The one and only.
 * 
 * @author Armin Reichert
 */
public class PacMan extends Actor<PacManState> {

	public int ticksSinceLastMeal;
	public Steering<PacMan> steering;
	private StateMachine<PacManState, PacManGameEvent> fsm;

	public PacMan(PacManGame game) {
		super("Pac-Man", game, game.maze);
		NESW.dirs().forEach(dir -> sprites.set("walking-" + dir, game.theme.spr_pacManWalking(dir)));
		sprites.set("dying", game.theme.spr_pacManDying());
		sprites.set("full", game.theme.spr_pacManFull());
		sprites.select("full");
		steering = steeredByKeys(KeyEvent.VK_UP, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT);
		buildStateMachine();
	}

	@Override
	protected StateMachine<PacManState, PacManGameEvent> fsm() {
		return fsm;
	}

	// Movement

	@Override
	public float maxSpeed() {
		switch (getState()) {
		case HUNGRY:
			return hasPower() ? game.speed(PACMAN_POWER_SPEED) : game.speed(PACMAN_SPEED);
		default:
			return 0;
		}
	}

	@Override
	protected void move() {
		super.move();
		sprites.select("walking-" + moveDir);
		sprites.current().ifPresent(sprite -> sprite.enableAnimation(!isStuck()));
	}

	@Override
	public void steer() {
		steering.steer(this);
	}

	@Override
	public boolean canCrossBorder(Tile current, Tile tile) {
		if (maze.isDoor(tile)) {
			return false;
		}
		return super.canCrossBorder(current, tile);
	}

	// State machine

	public int getPacManPowerTime() {
		return sec(PACMAN_POWER_SECONDS.$int(game.level));
	}

	public int getPacManDyingTime() {
		return sec(2);
	}

	public boolean isLosingPower() {
		return hasPower() && state().getTicksRemaining() <= state().getDuration() * 33 / 100;
	}

	private boolean startsLosingPower() {
		return hasPower() && state().getTicksRemaining() == state().getDuration() * 33 / 100;
	}

	public boolean hasPower() {
		return getState() == HUNGRY && state().getTicksRemaining() > 0;
	}

	public boolean isDead() {
		return getState() == DEAD;
	}

	@Override
	public void init() {
		super.init();
		ticksSinceLastMeal = 0;
		moveDir = nextDir = Top4.E;
		sprites.forEach(Sprite::resetAnimation);
		sprites.select("full");
		placeAtTile(maze.pacManHome, TS / 2, 0);
	}

	private void buildStateMachine() {
		fsm = StateMachine.
		/* @formatter:off */
		beginStateMachine(PacManState.class, PacManGameEvent.class)
				
			.description("[Pac-Man]")
			.initialState(HOME)

			.states()

				.state(HOME)
	
				.state(HUNGRY)
					.impl(new HungryState())
					
				.state(DYING)
					.timeoutAfter(() -> sec(4f))
					.onEntry(() -> {
						sprites.select("full");
						game.theme.snd_clips_all().forEach(Sound::stop);
					})
					.onTick(() -> {
						if (state().getTicksRemaining() == sec(2.5f)) {
							sprites.select("dying");
							game.theme.snd_die().play();
							game.activeGhosts().forEach(Ghost::hide);
						}
					})

			.transitions()

				.when(HOME).then(HUNGRY)
				
				.stay(HUNGRY)
					.on(PacManGainsPowerEvent.class)
					.act(() -> {
						state().setTimerFunction(this::getPacManPowerTime);
						state().resetTimer();
						LOGGER.info(() -> String.format("Pac-Man got power for %d ticks (%d sec)", 
								state().getDuration(), state().getDuration() / 60));
						game.theme.snd_waza().loop();
					})
					
				.when(HUNGRY).then(DYING)
					.on(PacManKilledEvent.class)
	
				.when(DYING).then(DEAD)
					.onTimeout()

		.endStateMachine();
		/* @formatter:on */
		fsm.traceTo(Logger.getLogger("StateMachineLogger"), app().clock::getFrequency);
	}

	private class HungryState extends State<PacManState, PacManGameEvent> {

		private int digestionTicks;

		@Override
		public void onEntry() {
			digestionTicks = 0;
		}

		@Override
		public void onTick() {
			if (startsLosingPower()) {
				publishEvent(new PacManGettingWeakerEvent());
			} else if (getTicksRemaining() == 1) {
				setTimerFunction(() -> 0);
				game.theme.snd_waza().stop();
				publishEvent(new PacManLostPowerEvent());
			} else if (mustDigest()) {
				digest();
			} else {
				steer();
				move();
				findSomethingInteresting().ifPresent(PacMan.this::publishEvent);
			}
		}

		private boolean mustDigest() {
			return digestionTicks > 0;
		}

		private void digest() {
			digestionTicks -= 1;
		}

		private Optional<PacManGameEvent> findSomethingInteresting() {
			Tile pacManTile = currentTile();

			if (!maze.insideBoard(pacManTile) || !visible) {
				return Optional.empty(); // when teleporting no events are triggered
			}

			/*@formatter:off*/
			Optional<PacManGameEvent> ghostCollision = game.activeGhosts()
				.filter(Ghost::visible)
				.filter(ghost -> ghost.currentTile().equals(pacManTile))
				.filter(ghost -> ghost.getState() == GhostState.CHASING
					|| ghost.getState() == GhostState.SCATTERING
					|| ghost.getState() == GhostState.FRIGHTENED)
				.findFirst()
				.map(PacManGhostCollisionEvent::new);
			/*@formatter:on*/

			if (ghostCollision.isPresent()) {
				return ghostCollision;
			}

			/*@formatter:off*/
			Optional<PacManGameEvent> bonusEaten = game.getBonus()
				.filter(bonus -> pacManTile == maze.bonusTile)
				.filter(bonus -> !bonus.consumed())
				.map(bonus -> new BonusFoundEvent(bonus.symbol(), bonus.value()));
			/*@formatter:on*/

			if (bonusEaten.isPresent()) {
				return bonusEaten;
			}

			if (maze.containsFood(pacManTile)) {
				ticksSinceLastMeal = 0;
				boolean energizer = maze.containsEnergizer(pacManTile);
				digestionTicks = game.getDigestionTicks(energizer);
				return Optional.of(new FoodFoundEvent(pacManTile, energizer));
			} else {
				ticksSinceLastMeal += 1;
			}

			return Optional.empty();
		}
	}
}