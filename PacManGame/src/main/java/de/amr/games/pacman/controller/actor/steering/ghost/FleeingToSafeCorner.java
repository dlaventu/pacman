package de.amr.games.pacman.controller.actor.steering.ghost;

import static de.amr.datastruct.StreamUtils.permute;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.MazeMover;
import de.amr.games.pacman.controller.actor.steering.common.TakingPrecomputedPath;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.MazeGraph;
import de.amr.games.pacman.model.Tile;

/**
 * Lets a ghost escape to the "safest" maze corner depending on Pac-Man's
 * current position. The "safest" corner is defined by the maximum distance of
 * Pac-Man to any tile on the path from the actor's current position to the
 * corner. When the target corner is reached the next corner is computed.
 * 
 * @author Armin Reichert
 */
public class FleeingToSafeCorner extends TakingPrecomputedPath {

	final MazeGraph graph;
	final Tile[] corners;

	public FleeingToSafeCorner(Ghost refugee, MazeMover attacker, Tile... corners) {
		super(refugee, attacker::tile);
		graph = new MazeGraph(maze);
		this.corners = Arrays.copyOf(corners, corners.length);
	}

	@Override
	public boolean requiresGridAlignment() {
		return true;
	}

	@Override
	protected List<Tile> pathToTarget(MazeMover refugee, Tile targetTile) {
		Tile target = refugee.tile();
		while (target.equals(refugee.tile())) {
			target = safeCorner(refugee);
		}
		return graph.shortestPath(refugee.tile(), target);
	}

	private Tile safeCorner(MazeMover refugee) {
		Tile refugeeTile = refugee.tile();
		Tile chaserTile = fnTargetTile.get();
		//@formatter:off
		return permute(Arrays.stream(corners))
			.filter(corner -> !corner.equals(refugeeTile))
			.sorted(byDist(maze,refugeeTile, chaserTile).reversed())
			.findFirst().get();
		//@formatter:on
	}

	private Comparator<Tile> byDist(Maze maze, Tile refugeeTile, Tile chaserTile) {
		return (corner1, corner2) -> {
			double dist1 = minDistFromPath(maze, graph.shortestPath(refugeeTile, corner1), chaserTile);
			double dist2 = minDistFromPath(maze, graph.shortestPath(refugeeTile, corner2), chaserTile);
			return Double.compare(dist1, dist2);
		};
	}

	private int manhattanDist(Tile t1, Tile t2) {
		// Note: tiles may be outside of board so we cannot use graph.manhattan()!
		int dx = t2.col - t1.col, dy = t2.row - t1.row;
		return Math.abs(dx) + Math.abs(dy);
	}

	private int minDistFromPath(Maze maze, List<Tile> path, Tile tile) {
		int min = Integer.MAX_VALUE;
		for (Tile t : path) {
			int dist = manhattanDist(t, tile);
			if (dist < min) {
				min = dist;
			}
		}
		return min;
	}
}