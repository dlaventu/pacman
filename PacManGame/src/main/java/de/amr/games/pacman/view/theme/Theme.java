package de.amr.games.pacman.view.theme;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Sound;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Symbol;

/**
 * Interface for accessing Pac-Man game specific UI resources.
 * 
 * @author Armin Reichert
 */
public interface Theme {

	static final int RED_GHOST = 0, PINK_GHOST = 1, CYAN_GHOST = 2, ORANGE_GHOST = 3;

	BufferedImage img_logo();

	Color color_mazeBackground();

	Sprite spr_emptyMaze();

	Sprite spr_fullMaze();

	Sprite spr_flashingMaze();

	Sprite spr_bonusSymbol(Symbol symbol);

	Sprite spr_pacManFull();

	Sprite spr_pacManWalking(Direction dir);

	Sprite spr_pacManDying();

	Sprite spr_ghostColored(int color, Direction dir);

	Sprite spr_ghostFrightened();

	Sprite spr_ghostFlashing();

	Sprite spr_ghostEyes(Direction dir);

	Sprite spr_number(int number);

	Font fnt_text();

	Sound music_playing();

	Sound music_gameover();

	Sound snd_die();

	Sound snd_eatFruit();

	Sound snd_eatGhost();

	Sound snd_eatPill();

	Sound snd_extraLife();

	Sound snd_insertCoin();

	Sound snd_ready();

	Sound snd_ghost_chase();

	Sound snd_ghost_dead();

	Sound snd_waza();

	Stream<Sound> clips_all();
}