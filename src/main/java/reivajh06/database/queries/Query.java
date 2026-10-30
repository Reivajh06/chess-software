package reivajh06.database.queries;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class Query<R> {

	private final String sql;
	private final ResultMapper<R> resultMapper;

	public Query(String sql, ResultMapper<R> resultMapper) {
		this.sql = sql;
		this.resultMapper = resultMapper;
	}

	public Query(String sql) {
		this(sql, _ -> null);
	}

	public String sql() {
		return sql;
	}

	public R map(ResultSet resultPointer) throws SQLException {
		return resultMapper.map(resultPointer);
	}

	@Override
	public String toString() {
		return "Query{" +
				"sql='" + sql + '\'' +
				'}';
	}
}
