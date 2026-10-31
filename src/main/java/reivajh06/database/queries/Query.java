package reivajh06.database.queries;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class Query<R> {

	protected final String sql;
	protected final List<String> params;
	protected final ResultMapper<R> resultMapper;

	public Query(String sql, ResultMapper<R> resultMapper) {
		this.sql = sql;
		this.resultMapper = resultMapper;
		params = new ArrayList<>();
	}

	public Query(String sql) {
		this(sql, _ -> null);
	}

	public List<String> params() {
		return params;
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
