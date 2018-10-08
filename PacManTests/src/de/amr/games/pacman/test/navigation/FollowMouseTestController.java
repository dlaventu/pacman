package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.input.Mouse;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.play.PlayViewX;

public class FollowMouseTestController implements ViewController {

	private final PacManGame game;
	private final PacMan pacMan;
	private final Ghost blinky;
	private final PlayViewX view;
	private Tile mouseTile;

	public FollowMouseTestController() {
		Maze maze = new Maze(Assets.text("maze.txt"));
		game = new PacManGame(maze);
		pacMan = game.getActors().getPacMan();
		blinky = game.getActors().getBlinky();
		view = new PlayViewX(game);
		view.setShowRoutes(true);
		view.setShowGrid(false);
		view.setShowStates(false);
		view.setScoresVisible(false);
	}

	@Override
	public void init() {
		mouseTile = pacMan.getHomeTile();
		pacMan.placeAtTile(mouseTile, 0, 0);
		game.setLevel(1);
		game.getMaze().tiles().filter(game.getMaze()::isFood).forEach(game::eatFoodAtTile);
		game.getActors().getGhosts().forEach(ghost -> game.getActors().setActive(ghost, false));
		game.getActors().setActive(blinky, true);
		game.getActors().setActive(pacMan, true);
		blinky.init();
		blinky.setState(GhostState.CHASING);
		blinky.setMoveBehavior(GhostState.CHASING, blinky.followRoute(() -> mouseTile));
	}

	@Override
	public void update() {
		updateMouseTile();
		blinky.update();
		view.update();
	}

	private void updateMouseTile() {
		if (Mouse.moved()) {
			int x = Mouse.getX(), y = Mouse.getY();
			mouseTile = new Tile(x / PacManGame.TS, y / PacManGame.TS);
			pacMan.placeAtTile(mouseTile, 0, 0);
			Application.LOGGER.info(mouseTile.toString());
		}
	}

	@Override
	public View currentView() {
		return view;
	}
}