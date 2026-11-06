package reivajh06.infrastructure.adapters.sqlite.queries;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultMapper<R> {

	R map(ResultSet resultPointer) throws SQLException;
}
