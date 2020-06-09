package ca.wd4.sg.data;

import ca.wd4.sg.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@Slf4j
@Repository
public class JdbcUserRepository implements UserRepository {

    private final JdbcTemplate jdbc;
    private final SimpleJdbcInsert insert;
    private final ObjectMapper mapper;
    private final PasswordEncoder encoder;


    @Autowired
    public JdbcUserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.insert = new SimpleJdbcInsert(jdbc)
                .withTableName("user")
                .usingColumns("name", "email", "password")
                .usingGeneratedKeyColumns("id");
        this.mapper = new ObjectMapper();
        this.encoder = new BCryptPasswordEncoder(); // to avoid bean creation cycle.
    }

    @Override
    public User findOneByEmail(String email) throws EmptyResultDataAccessException {
        return jdbc.queryForObject("select id, name, email, password from user where email = ?",
                    this::mapToUser, email);
    }

    private User mapToUser(ResultSet resultSet, int i) throws SQLException {
        return new User(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("email"),
                resultSet.getString("password"));
    }

    @Override
    public User findOneById(long id) {
        return null;
    }

    @Override
    public long save(User user) {
        Map<String, Object> values = mapper.convertValue(user, Map.class);
        log.info(values.toString());
        return insert.executeAndReturnKey(values).longValue();
    }

    @Override
    public void updateName(User user, String newName) {
        jdbc.update("update user set name=? where email=?", newName, user.getEmail());
    }

    @Override
    public void updatePassword(User user, String newPwd) {
        jdbc.update("update user set password=? where email=?", encoder.encode(newPwd), user.getEmail());
    }

    @Override
    public void remove(User user) {
        jdbc.update("delete from user where email=?", user.getEmail());
    }
}
