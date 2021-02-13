package ca.wd4.sg.data;


import ca.wd4.sg.exception.CourseNameConflictException;
import ca.wd4.sg.model.Course;
import ca.wd4.sg.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class JdbcCourseRepository implements CourseRepository {

    private enum KEYWORD {NAME, TERM}
    private final JdbcTemplate jdbc;
    private final SimpleJdbcInsert insert;
    private final ObjectMapper mapper;

    /**
     * Constructor
     * @param jdbc
     */
    @Autowired
    public JdbcCourseRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.insert = new SimpleJdbcInsert(jdbc)
                .withTableName("course")
                .usingGeneratedKeyColumns("id")
                .usingColumns("uid", "name", "author_id", "term", "star");
        mapper = new ObjectMapper();
    }

    /**
     * find all courses contains input name
     * @param courseName
     * @param sortOption
     * @return all courses contains input name
     */
    @Override
    public List<Course> findByName(String courseName, String sortOption) {
        return findByKeyword(KEYWORD.NAME, courseName, sortOption);
    }

    /**
     * find all courses which term names contains input term name
     * @param term
     * @param sortOption
     * @return
     */
    @Override
    public List<Course> findByTerm(String term, String sortOption) {
        return findByKeyword(KEYWORD.TERM, term, sortOption);
    }

    /**
     * Save a course
     * @param course
     * @return
     * @throws CourseNameConflictException
     */
    @Override
    public Course save(Course course) throws CourseNameConflictException {
        // check if the course already exists (uid, name, term)
        if (!jdbc.query("select name from course where uid = ? and name = ? and term = ?", //query
                (rs, i)->rs.getString("name"), //object mapper
                course.getUid(), course.getName(), course.getTerm()).isEmpty() /*parameters*/) {
            throw new CourseNameConflictException("course with name "+course.getName()+" already exist");
        }

        //create map object from course object
        Map<String, Object> values = mapper.convertValue(course, Map.class);
        values.put("author_id", course.getAuthorId());
        //save into table course
        long cid = insert.executeAndReturnKey(values).longValue();

        //save section information into course_section table
        saveSections(cid, course.getLecs(), 1);
        saveSections(cid, course.getTuts(), 2);
        saveSections(cid, course.getLabs(), 3);

        return course;
    }

    /**
     * find all courses owned by a specific user
     * @param uid
     * @return
     */
    @Override
    public List<Course> findByUId(long uid) {
        //find all related cids
        List<Long> cids = jdbc.query("select id from course where uid = ?", (rs, i)->rs.getLong("id"), uid);
        //initialize result list
        List<Course> courses = new ArrayList<>();
        //find all course and save them into the result list
        for (long cid: cids) {
            courses.add(findById(cid));
        }
        return  courses;
    }

    /**
     * find a course by course id
     * @param cid
     * @return
     */
    @Override
    public Course findById(long cid) {
        //get basic info
        Map<String, Object> info = jdbc.queryForMap("select uid, name, author_id, term, star from course where id = ?", cid);
        Long uid = (Long) info.get("uid");
        String name = (String) info.get("name");
        Long authorId = (Long) info.get("author_id");
        //find author name from user table
        String authorName = jdbc.queryForObject("select name from users where id = ?", (rs, i)->rs.getString("name"), authorId);
        String term = (String) info.get("term");
        int star = (int) info.get("star");

        //get section info
        //get section information from course
        List<String> lecs = getSection(cid, 1);
        List<String> tuts = getSection(cid, 2);
        List<String> labs = getSection(cid, 3);

        //return the course
        return new Course(cid, uid, name, term, authorName, authorId, star, lecs, tuts, labs);
    }

    @Override
    public void save(User user, Course course) throws CourseNameConflictException {
        //check if user has this course
        List<Object> checkIfOwn = jdbc.query("select name from course where uid = ? and name = ? and term = ?"
                , (rs, i)->rs.getString("name"),
                user.getId(), course.getName(), course.getTerm());
        if (!checkIfOwn.isEmpty()) {
            throw new CourseNameConflictException(course.getName()+" at "+course.getTerm()+" is already owned by "+user.getName());
        }

        //the user doesn't have this course, add it
        course.setUid(user.getId());
        try {
            this.save(course);
            jdbc.update("update course set star = star+1 where id = ?", course.getId());
        } catch (CourseNameConflictException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void remove(long cid) {
        jdbc.update("delete from course where id = ?", cid);
    }

    @Override
    public void update(Course course) throws CourseNameConflictException {
        //check if conflict
        String name = findById(course.getId()).getName();
        String term = course.getTerm();
        long uid = course.getUid();
        long cid = course.getId();

        //there are two scenarios:
        //1) the input term is the same as the original term
        //    update content (no conflict)
        //2) the input term is the different from the original term
        //    check if there exists conflict
        //    if so, throw exception
        //    otherwise, update content
        if (!term.equals(findById(cid).getTerm())) {
            Boolean hasConflict = !jdbc.query("select term from course where name=? and uid = ?", (rs, i)->rs.getString("term"), name, uid).stream().filter(t->t.equals(term)).collect(Collectors.toList()).isEmpty();
            if (hasConflict) {
                throw new CourseNameConflictException(name+" in "+term+" already exist");
            }
        }

        //update basic information
        jdbc.update("update course set term = ?, author_id = ?, star = 0 where id = ?", term, uid, cid);
        //update section
        jdbc.update("delete from course_section where cid = ?", cid); //delete first
        //add new
        saveSections(cid, course.getLecs(), 1);
        saveSections(cid, course.getTuts(), 2);
        saveSections(cid, course.getLabs(), 3);
    }

    /**
     * Given keyword type (Name, Term) and value, with sort option,
     * return the related courses
     * @param keywordType
     * @param keyword
     * @param sortOption
     * @return all related courses
     */
    private List<Course> findByKeyword(KEYWORD keywordType, String keyword, String sortOption) {
        //init result list
        List<Course> courses = new ArrayList<>();

        //create query based on keyword
        String format = "select id, uid, name, author_id, term, star from course where %s like '%%"+keyword+"%%'";
        String query = "";
        if (keywordType.equals(KEYWORD.NAME)) {
            query = String.format(format, "name");
        } else if (keywordType.equals(KEYWORD.TERM)) {
            query = String.format(format, "term");
        }

        //add sorting constraint
        if ("term".equals(sortOption)) {
            query = query + "order by term desc";
        } else if ("star".equals(sortOption)) {
            query = query + "order by star desc";
        }

        log.info(query);

        // first, get information from table course
        List<Map<String, Object>> maps = jdbc.queryForList(query);


        for (Map<String, Object> map: maps) {
            long id = (long) map.get("id");
            long authorId = (long) map.get("author_id");
            String realCourseName = (String) map.get("name");
            String term = (String) map.get("term");
            long uid = (long) map.get("uid");
            int star = (int) map.get("star");

            //get author name from user table
            String authorName = jdbc.queryForObject("select name from users where id = ?", (rs, i)->rs.getString("name"), authorId);

            //get section information from course
            List<String> lecs = getSection(id, 1);
            List<String> tuts = getSection(id, 2);
            List<String> labs = getSection(id, 3);

            //add to result list
            courses.add(new Course(id, uid, realCourseName, term, authorName, authorId, star, lecs, tuts, labs));
        }

        //return result list
        return courses;

    }

    /**
     * find all section times, given a cid and a category
     * @param cid
     * @param category
     * @return
     */
    private List<String> getSection(long cid, int category) {
        return jdbc.query("select sec_time from course_section where cid = ? and category = ?", (rs, i)->rs.getString("sec_time"), cid, category);
    }

    /**
     * save section information of a specific category to a designated course
     * save information into course_section table
     * @param cid
     * @param sections
     * @param category
     */
    private void saveSections(long cid, List<String> sections, int category) {
        for (int i=0; i<sections.size(); i++) {
            if (!"".equals(sections.get(i)))
                jdbc.update("insert into course_section (cid, category, sec_num, sec_time) values (?, ?, ?, ?)",
                        cid, category, i+1, sections.get(i));
        }
    }

}
