package de.amr.games.pacman.navigation.impl;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import de.amr.easy.game.Application;
import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.model.Content;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.navigation.MazeRoute;
import de.amr.games.pacman.navigation.Navigation;

/**
 * Attempt at implementing the original Ghost behavior as described
 * <a href="http://gameinternals.com/post/2072558330/understanding-pac-man-ghost-behavior">here</a>:
 *
 * <p>
 * The next step is understanding exactly how the ghosts attempt to reach their target tiles. The
 * ghosts’ AI is very simple and short-sighted, which makes the complex behavior of the ghosts even
 * more impressive. Ghosts only ever plan one step into the future as they move about the maze.
 * 
 * <p>
 * Whenever a ghost enters a new tile, it looks ahead to the next tile that it will reach, and makes
 * a decision about which direction it will turn when it gets there. These decisions have one very
 * important restriction, which is that ghosts may never choose to reverse their direction of
 * travel. That is, a ghost cannot enter a tile from the left side and then decide to reverse
 * direction and move back to the left. The implication of this restriction is that whenever a ghost
 * enters a tile with only two exits, it will always continue in the same direction.
 */
public class FollowTargetTile implements Navigation {

	private Supplier<Tile> targetTileSupplier;

	public FollowTargetTile(Supplier<Tile> targetTileSupplier) {
		this.targetTileSupplier = targetTileSupplier;
	}

	private Tile getTargetTile(Maze maze) {
		Tile targetTile = targetTileSupplier.get();
		if (maze.isTeleportSpace(targetTile)) {
			targetTile = new Tile(0, maze.getTunnelRow());
		}
		return targetTile;
	}

	@Override
	public MazeRoute computeRoute(MazeMover follower) {
		MazeRoute route = new MazeRoute();
		Maze maze = follower.getMaze();
		Tile currentTile = follower.getTile();
		Tile targetTile = getTargetTile(maze);

		// Special case: ghost leaves ghost house. Not sure what original game does in that case.
		if (maze.inGhostHouse(currentTile) || maze.getContent(currentTile) == Content.DOOR) {
			route.path = maze.findPath(currentTile, targetTile);
			route.dir = maze.alongPath(route.path).orElse(follower.getCurrentDir());
			return route;
		}

		if (follower.inTunnel() || follower.inTeleportSpace()) {
			route.dir = follower.getCurrentDir();
			return route;
		}

		// Find neighbor tile with least Euclidean distance to target tile
		/*@formatter:off*/
		List<Tile> neighbors = Maze.NESW.dirs()
			.filter(dir -> dir != Maze.NESW.inv(follower.getCurrentDir()))
			.mapToObj(dir -> maze.neighborTile(currentTile, dir))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.filter(tile -> maze.areAdjacentTiles(currentTile, tile))
			.filter(neighbor -> maze.getContent(neighbor) != Content.DOOR)
			.sorted((t1, t2) -> {
				return Integer.compare(maze.euclidean2(t1, targetTile), maze.euclidean2(t2, targetTile));
			})
			.collect(Collectors.toList());
		/*@formatter:on*/

		Application.LOGGER.info("Current:" + currentTile);
		neighbors.forEach(tile -> Application.LOGGER.info(tile.toString()));

		Tile best = neighbors.get(0);
		route.dir = maze.direction(follower.getTile(), best).getAsInt();
		return route;
	}
}
