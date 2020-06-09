package ca.wd4.sg.service;

import ca.wd4.sg.model.Course;
import ca.wd4.sg.exception.CourseNameConflictException;
import ca.wd4.sg.model.User;
import ca.wd4.sg.data.CourseRepository;
import ca.wd4.sg.data.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, CourseRepository courseRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void updateName(User user, String newName) {
        userRepository.updateName(user, newName);
    }

    public void updatePassword(User user, String newPassword) {
        userRepository.updatePassword(user, newPassword);
    }

    public boolean usingOldPassword(User user, String newPassword) {
        return passwordEncoder.matches(newPassword, userRepository.findOneByEmail(user.getEmail()).getPassword());
    }

    public void remove(User user) {
        userRepository.remove(user);
    }

    public List<Course> find(User user, String keyWord, String sortOption) {
        List<Course> courses = courseRepository.findByName(keyWord, sortOption);
        courses.addAll(courseRepository.findByTerm(keyWord, sortOption));

        //filter out those the user already has
        //filter out the courses with the same uid as the user
        courses = courses.stream().filter(c->c.getUid()!=user.getId()).collect(Collectors.toList());
        //filter out the courses that have the same name and same term
        List<Course> userCourses = courseRepository.findByUId(user.getId());
        courses = courses.stream().filter(c->!isRepeat(c, userCourses)).collect(Collectors.toList());
        return courses;
    }

    private boolean isRepeat(Course c, List<Course> userCourses) {
        for (Course course: userCourses) {
            if (c.getTerm().equals(course.getTerm()) && c.getName().equals(course.getName())) {
                return true;
            }
        }
        return false;
    }

    public List<Course> findCourses(User user) {
        List<Course> courses = courseRepository.findByUId(user.getId());
        return courses;
    }

    public void addCourse(User user, Course course) throws CourseNameConflictException {
        courseRepository.save(user, course);
    }
}
