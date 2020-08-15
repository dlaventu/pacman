package de.amr.games.pacman.model.game;

import static de.amr.easy.game.Application.loginfo;

import java.util.ArrayList;
import java.util.List;

import de.amr.games.pacman.model.world.api.Symbol;

/**
 * Data structure storing the level-specific values.
 * 
 * @author Armin Reichert
 */
public class GameLevel {

	private static float percentage(Object value) {
		return ((int) value) / 100f;
	}

	private static int integer(Object value) {
		return (int) value;
	}

	// constant values from table
	public final Symbol bonusSymbol;
	public final int bonusValue;
	public final float pacManSpeed;
	public final float pacManDotsSpeed;
	public final float ghostSpeed;
	public final float ghostTunnelSpeed;
	public final int elroy1DotsLeft;
	public final float elroy1Speed;
	public final int elroy2DotsLeft;
	public final float elroy2Speed;
	public final float pacManPowerSpeed;
	public final float pacManPowerDotsSpeed;
	public final float ghostFrightenedSpeed;
	public final int pacManPowerSeconds;
	public final int numFlashes;

	public final int number;
	public final int foodCount;

	public int eatenFoodCount;
	public int ghostsKilledByEnergizer;
	public int ghostsKilled;
	public int lives;
	public int score;
	public Hiscore hiscore;
	public List<Symbol> counter;

	public GameLevel(int number, int lives, int score, int foodCount) {
		this(number, foodCount, Game.levelData(number));
		this.lives = lives;
		this.score = score;
		counter = new ArrayList<>();
		counter.add(bonusSymbol);
		hiscore = new Hiscore();
		hiscore.load();
	}

	public GameLevel(int number, GameLevel previous) {
		this(number, previous.foodCount, Game.levelData(number));
		lives = previous.lives;
		score = previous.score;
		counter = previous.counter;
		counter.add(bonusSymbol);
		hiscore = previous.hiscore;
	}

	private GameLevel(int number, int foodCount, List<Object> data) {
		this.number = number;
		this.foodCount = foodCount;
		int i = 0;
		bonusSymbol = Symbol.valueOf((String) data.get(i++));
		bonusValue = integer(data.get(i++));
		pacManSpeed = percentage(data.get(i++));
		pacManDotsSpeed = percentage(data.get(i++));
		ghostSpeed = percentage(data.get(i++));
		ghostTunnelSpeed = percentage(data.get(i++));
		elroy1DotsLeft = integer(data.get(i++));
		elroy1Speed = percentage(data.get(i++));
		elroy2DotsLeft = integer(data.get(i++));
		elroy2Speed = percentage(data.get(i++));
		pacManPowerSpeed = percentage(data.get(i++));
		pacManPowerDotsSpeed = percentage(data.get(i++));
		ghostFrightenedSpeed = percentage(data.get(i++));
		pacManPowerSeconds = integer(data.get(i++));
		numFlashes = integer(data.get(i++));
	}

	public int remainingFoodCount() {
		return foodCount - eatenFoodCount;
	}

	/**
	 * Score the given number of points and handles high score, extra life etc.
	 * 
	 * @param points points to score
	 * @return points scored
	 */
	public int score(int points) {
		int oldScore = score;
		score += points;
		if (oldScore < Game.POINTS_EXTRA_LIFE && Game.POINTS_EXTRA_LIFE <= score) {
			lives += 1;
		}
		hiscore.checkNewHiscore(this, score);
		return points;
	}

	/**
	 * Score points for eating an energizer.
	 * 
	 * @return points scored
	 */
	public int scoreEnergizerEaten() {
		eatenFoodCount += 1;
		ghostsKilledByEnergizer = 0;
		return score(Game.POINTS_ENERGIZER);
	}

	/**
	 * Score points for eating a simple pellet
	 * 
	 * @return points scored
	 */
	public int scoreSimplePelletEaten() {
		eatenFoodCount += 1;
		return score(Game.POINTS_SIMPLE_PELLET);
	}

	/**
	 * Scores for killing a ghost. Value of a killed ghost doubles if killed in series using the same
	 * energizer.
	 */
	public int scoreGhostKilled() {
		ghostsKilledByEnergizer += 1;
		ghostsKilled += 1;
		if (ghostsKilled == 16) {
			score(Game.POINTS_ALL_GHOSTS);
		}
		int points = killedGhostPoints();
		loginfo("Scored %d points for killing %s ghost", points,
				new String[] { "", "first", "2nd", "3rd", "4th" }[ghostsKilledByEnergizer]);
		return score(points);
	}

	/**
	 * @return current value of a killed ghost. Value doubles for each ghost killed by the same
	 *         energizer.
	 */
	public int killedGhostPoints() {
		return Game.GHOST_BOUNTIES[ghostsKilledByEnergizer > 0 ? ghostsKilledByEnergizer - 1 : 0];
	}

	/**
	 * @return {@code true} if the number of eaten pellets causes the bonus to get active
	 */
	public boolean isBonusDue() {
		return eatenFoodCount == Game.BONUS_ACTIVATION_1 || eatenFoodCount == Game.BONUS_ACTIVATION_2;
	}
}