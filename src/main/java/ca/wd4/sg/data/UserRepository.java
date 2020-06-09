package ca.wd4.sg.data;

import ca.wd4.sg.model.User;
import org.springframework.dao.EmptyResultDataAccessException;

public interface UserRepository {
    User findOneByEmail(String email) throws EmptyResultDataAccessException;
    User findOneById(long id);
    long save(User user);
    void updateName(User user, String newName);
    void updatePassword(User user, String newPwd);
    void remove(User user);
}
