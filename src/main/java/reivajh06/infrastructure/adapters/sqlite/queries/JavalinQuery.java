package reivajh06.infrastructure.adapters.sqlite.queries;

import reivajh06.infrastructure.adapters.sqlite.queries.Query;
import reivajh06.infrastructure.adapters.sqlite.queries.ResultMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class JavalinQuery<R> implements Query<R> {

	protected final String sql;
	protected final List<String> params;
	protected final ResultMapper<R> resultMapper;

	public JavalinQuery(String sql, ResultMapper<R> resultMapper) {
		this.sql = sql;
		this.resultMapper = resultMapper;
		params = new ArrayList<>();
	}

	public JavalinQuery(String sql) {
		this(sql, _ -> null);
	}

	@Override
	public List<String> params() {
		return params;
	}

	@Override
	public String body() {
		return sql;
	}

	@Override
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
