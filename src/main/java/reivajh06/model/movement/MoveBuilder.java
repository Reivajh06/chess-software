package reivajh06.model.movement;

public class MoveBuilder {

	private int x;
	private int y;
	private int nRepetitions;

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

	public Move build() {
		return new Move(x, y, nRepetitions);
	}
}
