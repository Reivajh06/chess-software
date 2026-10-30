package reivajh06.database.queries;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultMapper<R> {

	R map(ResultSet resultPointer) throws SQLException;
}
