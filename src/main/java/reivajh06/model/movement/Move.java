package reivajh06.model.movement;

import reivajh06.model.Tile;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public record Move(int x, int y, int repetitions, BiPredicate<Tile, Tile> condition) {

	public Move(int x, int y, int repetitions) {
		this(x, y, repetitions, (_, _) -> true);
	}
}
