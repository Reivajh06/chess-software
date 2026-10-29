package reivajh06.database.queries;

import reivajh06.database.Field;

import java.util.List;

public class CreateTableQuery implements Query {

	private String name;
	private List<Field> fields;

	public CreateTableQuery name(String name) {
		this.name = name;
		return this;
	}

	public CreateTableQuery content(List<Field> fields) {
		this.fields = fields;
		return this;
	}

	@Override
	public String body() {
		return "CREATE TABLE IF NOT EXISTS %s(%s);".formatted(
				name,
				fields.stream()
						.map(Field::toString)
						.reduce((s1, s2) -> String.join(", ", s1, s2)).orElse(" ")
		);
	}
}
