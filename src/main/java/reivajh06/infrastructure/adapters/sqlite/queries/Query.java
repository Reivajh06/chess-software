package reivajh06.infrastructure.adapters.sqlite.queries;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface Query<R> {

    List<String> params();

    String body();

    R map(ResultSet resultPointer) throws SQLException;
}
