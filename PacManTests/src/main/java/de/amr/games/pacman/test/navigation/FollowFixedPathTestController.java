package de.amr.games.pacman.test.navigation;

import java.util.Arrays;
import java.util.List;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.play.PlayViewX;

public class FollowFixedPathTestController implements ViewController {

	private final PacManGame game;
	private final PlayViewX view;
	private final List<Tile> targets;
	private int targetIndex;

	public FollowFixedPathTestController() {
		game = new PacManGame();
		targets = Arrays.asList(game.getMaze().getBottomRightCorner(), game.getMaze().getBottomLeftCorner(),
				game.getMaze().getLeftTunnelEntry(), game.getMaze().getTopLeftCorner(),
				game.getMaze().getBlinkyHome(), game.getMaze().getTopRightCorner(),
				game.getMaze().getRightTunnelEntry(), game.getMaze().getPacManHome());
		view = new PlayViewX(game);
		view.setShowRoutes(true);
		view.setShowGrid(false);
		view.setShowStates(false);
		view.setScoresVisible(false);
	}

	@Override
	public void init() {
		targetIndex = 0;
		game.setLevel(1);
		game.getMaze().tiles().filter(game.getMaze()::isFood).forEach(game::eatFoodAtTile);
		game.setActive(game.pacMan, false);
		game.ghosts().filter(ghost -> ghost != game.blinky).forEach(ghost -> game.setActive(ghost, false));
		game.blinky.initGhost();
		game.blinky.setState(GhostState.CHASING);
		game.blinky.setBehavior(GhostState.CHASING, game.blinky.followFixedPath(() -> targets.get(targetIndex)));
		game.blinky.getBehavior().computePath(game.blinky);
	}

	private void nextTarget() {
		targetIndex += 1;
		if (targetIndex == targets.size()) {
			targetIndex = 0;
			game.setLevel(game.getLevel() + 1);
		}
	}

	@Override
	public void update() {
		game.blinky.update();
		if (game.blinky.getTile().equals(targets.get(targetIndex))) {
			nextTarget();
		}
		view.update();
	}

	@Override
	public View currentView() {
		return view;
	}
}