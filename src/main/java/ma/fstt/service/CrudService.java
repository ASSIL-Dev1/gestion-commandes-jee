package ma.fstt.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
public interface CrudService<T> {
    List<T> findAll() throws SQLException;
    Optional<T> findById(long id) throws SQLException;
    void save(T entity) throws SQLException;
    void delete(long id) throws SQLException;
}
