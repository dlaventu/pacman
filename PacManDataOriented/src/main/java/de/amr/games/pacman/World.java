package de.amr.games.pacman;

import java.awt.Point;

public class World {

	public static final int TS = 8;
	public static final int HTS = TS / 2;
	public static final int WORLD_WIDTH_TILES = 28;
	public static final int WORLD_HEIGHT_TILES = 36;
	public static final int WORLD_WIDTH = WORLD_WIDTH_TILES * TS;
	public static final int WORLD_HEIGHT = WORLD_HEIGHT_TILES * TS;

	public static final Point CLYDE_CORNER = new Point(0, WORLD_HEIGHT_TILES - 1);
	public static final Point INKY_CORNER = new Point(WORLD_WIDTH_TILES - 1, WORLD_HEIGHT_TILES - 1);
	public static final Point PINKY_CORNER = new Point(2, 0);
	public static final Point BLINKY_CORNER = new Point(WORLD_WIDTH_TILES - 3, 0);
	public static final Point PORTAL_LEFT_ENTRY = new Point(-1, 17);
	public static final Point PORTAL_RIGHT_ENTRY = new Point(WORLD_WIDTH_TILES, 17);

	private final String[] map;

	public World() {
		map = new String[] {
			//@formatter:off
			"1111111111111111111111111111",
			"1111111111111111111111111111",
			"1111111111111111111111111111",
			"1111111111111111111111111111",
			"1222222222222112222222222221",
			"1211112111112112111112111121",
			"1211112111112112111112111121",
			"1211112111112112111112111121",
			"1222222222222222222222222221",
			"1211112112111111112112111121",
			"1211112112111111112112111121",
			"1222222112222112222112222221",
			"1111112111110110111112111111",
			"1111112111110110111112111111",
			"1111112110000000000112111111",
			"1111112110111001110112111111",
			"1111112110100000010112111111",
			"0000002000100000010002000000",
			"1111112110100000010112111111",
			"1111112110111111110112111111",
			"1111112110000000000112111111",
			"1111112110111111110112111111",
			"1111112110111111110112111111",
			"1222222222222112222222222221",
			"1211112111112112111112111121",
			"1211112111112112111112111121",
			"1222112222222002222222112221",
			"1112112112111111112112112111",
			"1112112112111111112112112111",
			"1222222112222112222112222221",
			"1211111111112112111111111121",
			"1211111111112112111111111121",
			"1222222222222222222222222221",
			"1111111111111111111111111111",
			"1111111111111111111111111111",
			"1111111111111111111111111111",
			//@formatter:on
		};
	}

	public char map(int x, int y) {
		return map[y].charAt(x);
	}

	public int index(int x, int y) {
		return y * WORLD_WIDTH_TILES + x;
	}

	public Point tile(V2 position) {
		return new Point(position.x_int() / TS, position.y_int() / TS);
	}

	public float offsetX(V2 position, int tileX, int tileY) {
		return position.x - tileX * TS;
	}

	public float offsetY(V2 position, int tileX, int tileY) {
		return position.y - tileY * TS;
	}

	public V2 position(Creature guy) {
		return new V2(guy.tile.x * TS + guy.offsetX, guy.tile.y * TS + guy.offsetY);
	}

	public boolean inMapRange(int x, int y) {
		return 0 <= x && x < WORLD_WIDTH_TILES && 0 <= y && y < WORLD_HEIGHT_TILES;
	}

	public boolean isGhostHouseDoor(int x, int y) {
		return y == 15 && (x == 13 || x == 14);
	}

	public boolean isInsideGhostHouse(int x, int y) {
		return x >= 10 && x <= 17 && y >= 15 && y <= 22;
	}

	public boolean isInsideTunnel(int x, int y) {
		return y == 17 && (x <= 5 || x >= 21);
	}

	public boolean isUpwardsBlocked(int x, int y) {
		//@formatter:off
		return x == 12 && y == 13
			|| x == 15 && y == 13
			|| x == 12 && y == 25
			|| x == 15 && y == 25;
		//@formatter:on
	}

	public boolean isFoodTile(int x, int y) {
		return inMapRange(x, y) && map(x, y) == '2';
	}

	public boolean isEnergizerTile(int x, int y) {
		//@formatter:off
		return x == 1  && y == 6
			|| x == 26 && y == 6
			|| x == 1  && y == 26
			|| x == 26 && y == 26;
		//@formatter:on
	}

	public boolean isIntersectionTile(int x, int y) {
		int accessibleNeighbors = 0;
		for (Direction dir : Direction.values()) {
			int neighborX = x + dir.vec.x;
			int neighborY = y + dir.vec.y;
			if (isAccessibleTile(neighborX, neighborY)) {
				++accessibleNeighbors;
			}
		}
		return accessibleNeighbors >= 3;
	}

	public boolean isAccessibleTile(int x, int y) {
		if (isPortalTile(x, y)) {
			return true;
		}
		if (x >= 0 && x < WORLD_WIDTH_TILES && y > 0 && y < WORLD_HEIGHT_TILES) {
			return map(x, y) != '1';
		}
		return false;
	}

	public boolean isPortalTile(int x, int y) {
		return x == PORTAL_RIGHT_ENTRY.x && y == PORTAL_RIGHT_ENTRY.x
				|| x == PORTAL_LEFT_ENTRY.x && y == PORTAL_LEFT_ENTRY.y;
	}

	public boolean isBonusTile(int x, int y) {
		return x == 13 && y == 20;
	}
}