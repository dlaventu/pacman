package de.amr.games.pacman.model;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.controller.actor.GhostState.CHASING;
import static de.amr.games.pacman.controller.actor.GhostState.DEAD;
import static de.amr.games.pacman.controller.actor.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.controller.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.actor.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.controller.actor.GhostState.LOCKED;
import static de.amr.games.pacman.controller.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.model.Symbol.APPLE;
import static de.amr.games.pacman.model.Symbol.BELL;
import static de.amr.games.pacman.model.Symbol.CHERRIES;
import static de.amr.games.pacman.model.Symbol.GALAXIAN;
import static de.amr.games.pacman.model.Symbol.GRAPES;
import static de.amr.games.pacman.model.Symbol.KEY;
import static de.amr.games.pacman.model.Symbol.PEACH;
import static de.amr.games.pacman.model.Symbol.STRAWBERRY;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.actor.Bonus;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.GhostState;
import de.amr.games.pacman.controller.actor.MovingActor;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.controller.actor.PacManState;

/**
 * The "model" (in MVC speak) of the Pac-Man game.
 * 
 * @author Armin Reichert
 * 
 * @see <a href= "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php">Pac-Man
 *      dossier</a>
 * @see <a href= "http://www.gamasutra.com/db_area/images/feature/3938/tablea1.png">Pac-Man level
 *      specifications</a>
 */
public class Game {

	/*
	 * I am still not sure about the correct base speed. <p> In Shaun Williams' Pac-Man remake
	 * (https://github.com/masonicGIT/pacman/blob/master/src/Actor.js) there is a speed table giving the
	 * number of steps (=pixels?) Pac-Man is moving in 16 frames. In level 5 this gives 4*2 + 12 = 20
	 * steps in 16 frames, which gives 1.25 pixels / frame. <p> The table from Gamasutra ({@link
	 * Game#LEVELS}) states that this corresponds to 100% base speed for Pac-Man at level 5. Therefore I
	 * use 1.25 pixel/frame.
	 * 
	 */
	static final float BASE_SPEED = 1.25f;

	/**
	 * @param fraction fraction of seconds
	 * @return ticks corresponding to given fraction of seconds
	 */
	public static int sec(float fraction) {
		return Math.round(60 * fraction);
	}

	/**
	 * @param fraction fraction of base speed
	 * @return speed (pixels/tick) corresponding to given fraction of base speed
	 */
	public static float speed(float fraction) {
		return fraction * BASE_SPEED;
	}

	public static final int POINTS_PELLET = 10;
	public static final int POINTS_ENERGIZER = 50;
	public static final int POINTS_EXTRA_LIFE = 10_000;
	public static final int POINTS_KILLED_ALL_GHOSTS = 12_000;
	public static final int POINTS_BONUS[] = { 100, 300, 500, 700, 1000, 2000, 3000, 5000 };
	public static final int POINTS_GHOST[] = { 200, 400, 800, 1600 };
	public static final int DIGEST_PELLET_TICKS = 1;
	public static final int DIGEST_ENERGIZER_TICKS = 3;

	final GameLevel[] levels = {
		/*@formatter:off*/
		null, // level numbering starts at 1
		new GameLevel(CHERRIES,   100,  .80f, .71f, .75f, .40f,  20, .8f,  10,  .85f, .90f, .79f, .50f,   6, 5 ),
		new GameLevel(STRAWBERRY, 300,  .90f, .79f, .85f, .45f,  30, .9f,  15,  .95f, .95f, .83f, .55f,   5, 5 ),
		new GameLevel(PEACH,      500,  .90f, .79f, .85f, .45f,  40, .9f,  20,  .95f, .95f, .83f, .55f,   4, 5 ),
		new GameLevel(PEACH,      500,  .90f, .79f, .85f, .50f,  40,  1f,  20,  .95f, .95f, .83f, .55f,   3, 5 ),
		new GameLevel(APPLE,      700,    1f, .87f, .95f, .50f,  40,  1f,  20, 1.05f,   1f, .87f, .60f,   2, 5 ),
		new GameLevel(APPLE,      700,    1f, .87f, .95f, .50f,  50,  1f,  25, 1.05f,   1f, .87f, .60f,   5, 5 ),
		new GameLevel(GRAPES,    1000,    1f, .87f, .95f, .50f,  50,  1f,  25, 1.05f,   1f, .87f, .60f,   2, 5 ),
		new GameLevel(GRAPES,    1000,    1f, .87f, .95f, .50f,  50,  1f,  25, 1.05f,   1f, .87f, .60f,   2, 5 ),
		new GameLevel(GALAXIAN,  2000,    1f, .87f, .95f, .50f,  60,  1f,  30, 1.05f,   1f, .87f, .60f,   1, 3 ),
		new GameLevel(GALAXIAN,  2000,    1f, .87f, .95f, .50f,  60,  1f,  30, 1.05f,   1f, .87f, .60f,   5, 5 ),
		new GameLevel(BELL,      3000,    1f, .87f, .95f, .50f,  60,  1f,  30, 1.05f,   1f, .87f, .60f,   2, 5 ),
		new GameLevel(BELL,      3000,    1f, .87f, .95f, .50f,  80,  1f,  40, 1.05f,   1f, .87f, .60f,   1, 3 ),
		new GameLevel(KEY,       5000,    1f, .87f, .95f, .50f,  80,  1f,  40, 1.05f,   1f, .87f, .60f,   1, 3 ),
		new GameLevel(KEY,       5000,    1f, .87f, .95f, .50f,  80,  1f,  40, 1.05f,   1f, .87f, .60f,   3, 5 ),
		new GameLevel(KEY,       5000,    1f, .87f, .95f, .50f, 100,  1f,  50, 1.05f,   1f, .87f, .60f,   1, 3 ),
		new GameLevel(KEY,       5000,    1f, .87f, .95f, .50f, 100,  1f,  50, 1.05f,   0f,   0f,   0f,   0, 0 ),
		new GameLevel(KEY,       5000,    1f, .87f, .95f, .50f, 100,  1f,  50, 1.05f,   1f, .87f, .60f,   1, 3 ),
		new GameLevel(KEY,       5000,    1f, .87f, .95f, .50f, 100,  1f,  50, 1.05f,   0f,   0f,   0f,   0, 0 ),
		new GameLevel(KEY,       5000,    1f, .87f, .95f, .50f, 120,  1f,  60, 1.05f,   0f,   0f,   0f,   0, 0 ),
		new GameLevel(KEY,       5000,    1f, .87f, .95f, .50f, 120,  1f,  60, 1.05f,   0f,   0f,   0f,   0, 0 ),
		new GameLevel(KEY,       5000,  .90f, .79f, .95f, .50f, 120,  1f,  60, 1.05f,   0f,   0f,   0f,   0, 0 ),
		/*@formatter:on*/
	};

	public PacMan pacMan;
	public Ghost blinky, pinky, inky, clyde;
	public Bonus bonus;
	public Maze maze;
	public Stage stage;
	public List<Symbol> levelCounter;
	public Hiscore hiscore;
	public GameLevel level;
	public int lives;
	public int score;

	public Game(int startLevel) {
		lives = 3;
		score = 0;
		levelCounter = new ArrayList<>();
		hiscore = new Hiscore(new File(new File(System.getProperty("user.home")), "pacman.hiscore.xml"));
		maze = new Maze();
		stage = new Stage();
		createActors();
		enterLevel(startLevel);
	}

	public Game() {
		this(1);
	}

	public void enterLevel(int n) {
		loginfo("Enter level %d", n);
		level = levels[Math.min(n, levels.length - 1)];
		level.number = n;
		level.eatenFoodCount = 0;
		level.ghostsKilledByEnergizer = 0;
		level.ghostsKilled = 0;
		levelCounter.add(level.bonusSymbol);
		maze.restoreFood();
		hiscore.save();
	}

	private void createActors() {
		pacMan = new PacMan(this);
		blinky = new Ghost(this, "Blinky");
		inky = new Ghost(this, "Inky");
		pinky = new Ghost(this, "Pinky");
		clyde = new Ghost(this, "Clyde");
		bonus = new Bonus(this);

		pacMan.fnSpeed = this::pacManSpeed;
		ghosts().forEach(ghost -> ghost.fnSpeed = this::ghostSpeed);

		pacMan.behavior(pacMan.isFollowingKeys(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT));

		blinky.seat = 0;
		blinky.behavior(LOCKED, blinky.isHeadingFor(blinky::tile));
		blinky.behavior(ENTERING_HOUSE, blinky.isTakingSeat(maze.seatPosition(2)));
		blinky.behavior(LEAVING_HOUSE, blinky.isLeavingGhostHouse());
		blinky.behavior(FRIGHTENED, blinky.isMovingRandomlyWithoutTurningBack());
		blinky.behavior(SCATTERING, blinky.isHeadingFor(maze.horizonNE));
		blinky.behavior(CHASING, blinky.isHeadingFor(pacMan::tile));
		blinky.behavior(DEAD, blinky.isHeadingFor(() -> maze.ghostHouseEntry));

		inky.seat = 1;
		inky.behavior(LOCKED, inky.isJumpingUpAndDown(maze.seatPosition(1)));
		inky.behavior(ENTERING_HOUSE, inky.isTakingSeat(maze.seatPosition(1)));
		inky.behavior(LEAVING_HOUSE, inky.isLeavingGhostHouse());
		inky.behavior(FRIGHTENED, inky.isMovingRandomlyWithoutTurningBack());
		inky.behavior(SCATTERING, inky.isHeadingFor(maze.horizonSE));
		inky.behavior(CHASING, inky.isHeadingFor(() -> {
			Tile b = blinky.tile(), p = pacMan.tilesAhead(2);
			return new Tile(2 * p.col - b.col, 2 * p.row - b.row);
		}));
		inky.behavior(DEAD, inky.isHeadingFor(() -> maze.ghostHouseEntry));

		pinky.seat = 2;
		pinky.behavior(LOCKED, pinky.isJumpingUpAndDown(maze.seatPosition(2)));
		pinky.behavior(ENTERING_HOUSE, pinky.isTakingSeat(maze.seatPosition(2)));
		pinky.behavior(LEAVING_HOUSE, pinky.isLeavingGhostHouse());
		pinky.behavior(FRIGHTENED, pinky.isMovingRandomlyWithoutTurningBack());
		pinky.behavior(SCATTERING, pinky.isHeadingFor(maze.horizonNW));
		pinky.behavior(CHASING, pinky.isHeadingFor(() -> pacMan.tilesAhead(4)));
		pinky.behavior(DEAD, pinky.isHeadingFor(() -> maze.ghostHouseEntry));

		clyde.seat = 3;
		clyde.behavior(LOCKED, clyde.isJumpingUpAndDown(maze.seatPosition(3)));
		clyde.behavior(ENTERING_HOUSE, clyde.isTakingSeat(maze.seatPosition(3)));
		clyde.behavior(LEAVING_HOUSE, clyde.isLeavingGhostHouse());
		clyde.behavior(FRIGHTENED, clyde.isMovingRandomlyWithoutTurningBack());
		clyde.behavior(SCATTERING, clyde.isHeadingFor(maze.horizonSW));
		clyde.behavior(CHASING,
				clyde.isHeadingFor(() -> clyde.tile().distSq(pacMan.tile()) > 8 * 8 ? pacMan.tile() : maze.horizonSW));
		clyde.behavior(DEAD, clyde.isHeadingFor(() -> maze.ghostHouseEntry));
	}

	public Stream<Ghost> ghosts() {
		return Stream.of(blinky, pinky, inky, clyde);
	}

	public Stream<Ghost> ghostsOnStage() {
		return ghosts().filter(stage::contains);
	}

	public Stream<MovingActor<?>> movingActors() {
		return Stream.of(pacMan, blinky, pinky, inky, clyde);
	}

	public Stream<MovingActor<?>> movingActorsOnStage() {
		return movingActors().filter(stage::contains);
	}

	public int remainingFoodCount() {
		return maze.totalFoodCount - level.eatenFoodCount;
	}

	/**
	 * @param tile tile containing food
	 * @return points scored
	 */
	public int eatFood(Tile tile, boolean energizer) {
		if (!maze.isSimplePellet(tile) && !maze.isEnergizer(tile)) {
			loginfo("Tile %s does not contain food");
			return 0;
		}
		maze.removeFood(tile);
		level.eatenFoodCount += 1;
		if (energizer) {
			level.ghostsKilledByEnergizer = 0;
			return POINTS_ENERGIZER;
		} else {
			return POINTS_PELLET;
		}
	}

	public boolean isBonusScoreReached() {
		return level.eatenFoodCount == 70 || level.eatenFoodCount == 170;
	}

	public void score(int points) {
		int oldScore = score;
		score += points;
		if (score > hiscore.points) {
			hiscore.points = score;
			hiscore.levelNumber = level.number;
		}
		if (oldScore < POINTS_EXTRA_LIFE && POINTS_EXTRA_LIFE <= score) {
			lives += 1;
		}
	}

	public void scoreKilledGhost(String ghostName) {
		int points = POINTS_GHOST[level.ghostsKilledByEnergizer];
		level.ghostsKilledByEnergizer += 1;
		level.ghostsKilled += 1;
		score(points);
		if (level.ghostsKilled == 16) {
			score(POINTS_KILLED_ALL_GHOSTS);
		}
		loginfo("Scored %d points for killing %s (%s ghost in sequence)", points, ghostName,
				new String[] { "", "first", "2nd", "3rd", "4th" }[level.ghostsKilledByEnergizer]);
	}

	float pacManSpeed(Tile tile, PacManState state) {
		float fraction = 0;
		switch (state) {
		case SLEEPING:
		case DEAD:
			break;
		case EATING:
			if (pacMan.powerTicks > 0) {
				if (remainingFoodCount() < level.elroy2DotsLeft) {
					fraction = level.elroy2Speed;
				} else if (remainingFoodCount() < level.elroy1DotsLeft) {
					fraction = level.elroy1Speed;
				} else {
					fraction = level.pacManPowerSpeed;
				}
			} else {
				fraction = level.pacManSpeed;
			}
			break;
		default:
			throw new IllegalStateException("Illegal Pac-Man state: " + state);
		}
		return speed(fraction);
	}

	float ghostSpeed(Tile tile, GhostState state) {
		float fraction = 0;
		switch (state) {
		case LOCKED:
			if (maze.insideGhostHouse(tile)) {
				fraction = level.ghostSpeed / 2;
			}
			break;
		case LEAVING_HOUSE:
			fraction = level.ghostSpeed / 2;
			break;
		case ENTERING_HOUSE:
			fraction = level.ghostSpeed;
			break;
		case CHASING:
		case SCATTERING:
			if (maze.isTunnel(tile)) {
				fraction = level.ghostTunnelSpeed;
			} else {
				fraction = level.ghostSpeed;
			}
			break;
		case FRIGHTENED:
			if (maze.isTunnel(tile)) {
				fraction = level.ghostTunnelSpeed;
			} else {
				fraction = level.ghostFrightenedSpeed;
			}
			break;
		case DEAD:
			fraction = 2 * level.ghostSpeed;
			break;
		default:
			throw new IllegalStateException(String.format("Illegal ghost state %s", state));
		}
		return speed(fraction);
	}
}