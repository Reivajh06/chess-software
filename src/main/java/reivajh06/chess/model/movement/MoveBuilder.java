package reivajh06.chess.model.movement;

public class MoveBuilder {

	private static final int MAX_POSSIBLE_REPETITIONS = 8;

	private int x;
	private int y;
	private int nRepetitions = 1;
	private MoveCondition condition;
	private MoveEffect effect = MoveEffect.DIRECT_CAPTURE;

	public MoveBuilder forward() {
		y += 1;
		return this;
	}

	public MoveBuilder backward() {
		y -= 1;
		return this;
	}

	public MoveBuilder left() {
		x -= 1;
		return this;
	}

	public MoveBuilder right() {
		x += 1;
		return this;
	}

	public MoveBuilder forwardLeft() {
		forward();
		left();
		return this;
	}

	public MoveBuilder forwardRight() {
		forward();
		right();
		return this;
	}

	public MoveBuilder backwardLeft() {
		backward();
		left();
		return this;
	}

	public MoveBuilder backwardRight() {
		backward();
		right();
		return this;
	}

	public MoveBuilder repetitions(int nRepetitions) {
		this.nRepetitions = nRepetitions;
		return this;
	}

	public MoveBuilder allRepetitions() {
		return repetitions(MAX_POSSIBLE_REPETITIONS);
	}

	public MoveBuilder condition(MoveCondition condition) {
		this.condition = condition;
		return this;
	}

	public MoveBuilder effect(MoveEffect effect) {
		this.effect = effect;
		return this;
	}

	public Move build() {
		return new Move(x, y, nRepetitions, condition == null ? MoveCondition.NO_CONDITION : condition, effect);
	}
}
