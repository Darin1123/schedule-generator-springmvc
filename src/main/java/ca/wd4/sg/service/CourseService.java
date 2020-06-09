package ca.wd4.sg.service;

import ca.wd4.sg.data.CourseRepository;
import ca.wd4.sg.data.UserRepository;
import ca.wd4.sg.exception.CourseNameConflictException;
import ca.wd4.sg.exception.SectionConversionFailureException;
import ca.wd4.sg.model.Course;
import ca.wd4.sg.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CourseService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @Autowired
    public CourseService(UserRepository userRepository, CourseRepository courseRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    public void save(String name, User user, String term, int i, String lecs, String tuts, String labs) throws SectionConversionFailureException, CourseNameConflictException {

        List<String> lecList = convertToSections(lecs, "lecture");
        List<String> tutList = convertToSections(tuts, "tutorial");
        List<String> labList = convertToSections(labs, "lab");
        Course course = new Course(0, user.getId(), name, term, user.getName(), user.getId(), 0, lecList, tutList, labList);
        courseRepository.save(course);
    }

    private List<String> convertToSections(String sections, String category) throws SectionConversionFailureException {
        List<String> result = new ArrayList<>();
        String[] lines = sections.split("\n");
        log.info("**** number of lines: "+lines.length);
        for (String line: lines) {
            line = line.trim();
            log.info("*** line: "+line);
            if (!isValidSectionTime(line)) {
                throw new SectionConversionFailureException("Incorrect "+category+" format: "+line);
            }
            result.add(line);
        }
        return result;
    }

    private boolean isValidSectionTime(String line) {
        if ("".equals(line)) {
            return true;
        }
        if (line.matches("((Mo|Tu|We|Th|Fr)+ \\d{2}:\\d{2}-\\d{2}:\\d{2},\\s?)*((Mo|Tu|We|Th|Fr)+ \\d{2}:\\d{2}-\\d{2}:\\d{2})")) {
            return true;
        }
        return false;
    }

    public Course findById(long cid) {
        return courseRepository.findById(cid);
    }

    public void delete(long cid) {
        courseRepository.remove(cid);
    }

    public void update(long cid, long uid, String term, String lecs, String tuts, String labs) throws CourseNameConflictException, SectionConversionFailureException {
        Course course = new Course();
        course.setId(cid);
        course.setUid(uid);
        course.setTerm(term);
        course.setAuthorId(uid);
        course.setLecs(convertToSections(lecs, "lecture"));
        course.setTuts(convertToSections(tuts, "tutorial"));
        course.setLabs(convertToSections(labs, "lab"));
        courseRepository.update(course);
    }
}
