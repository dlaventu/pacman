package de.amr.games.pacman.navigation.impl;

import static de.amr.games.pacman.model.Maze.NESW;

import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.navigation.MazeRoute;
import de.amr.games.pacman.navigation.Navigation;

class Bounce implements Navigation {

	@Override
	public MazeRoute computeRoute(MazeMover bouncer) {
		MazeRoute route = new MazeRoute();
		route.dir = bouncer.canMove() ? bouncer.getCurrentDir() : NESW.inv(bouncer.getCurrentDir());
		return route;
	}
}