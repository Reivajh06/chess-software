package reivajh06.database;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class Field {

	private String name;
	private FieldType type;
	private FieldConstraint[] constraints;
	private boolean autoincrement = false;

	public Field(String name) {
		this.name = name;
	}

	public Field ofInteger() {
		type = FieldType.INTEGER;
		return this;
	}

	public Field ofString() {
		type = FieldType.VARCHAR;
		return this;
	}

	public Field constraint(FieldConstraint... constraints) {
		this.constraints = constraints;
		return this;
	}

	public Field autoincrement() {
		this.autoincrement = true;
		return this;
	}

	@Override
	public String toString() {
		Optional<String> joinedConstraints = Arrays.stream(constraints)
				.map(f -> f.name().replace("_", " "))
				.reduce((s1, s2) -> String.join(" ", s1, s2));

		return "%s %s %s".formatted(
				name,
				type,
				autoincrement ?
						"AUTO INCREMENT " + joinedConstraints.orElse("")
						: joinedConstraints.orElse("")
		);
	}
}
