package ca.wd4.sg.data;

import ca.wd4.sg.exception.CourseNameConflictException;
import ca.wd4.sg.model.Course;
import ca.wd4.sg.model.User;

import java.util.List;

public interface CourseRepository {
    List<Course> findByName(String keyWord, String sortOption);

    List<Course> findByTerm(String keyWord, String sortOption);

    Course save(Course course) throws CourseNameConflictException;

    List<Course> findByUId(long uid);

    Course findById(long id);

    void save(User user, Course course) throws CourseNameConflictException;

    void remove(long cid);

    void update(Course course) throws CourseNameConflictException;
}
