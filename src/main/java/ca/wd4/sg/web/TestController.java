package ca.wd4.sg.web;

import ca.wd4.sg.core.CourseBasic;
import ca.wd4.sg.core.CourseInfo;
import ca.wd4.sg.exception.EmptyCourseException;
import ca.wd4.sg.model.Schedule;
import ca.wd4.sg.service.CourseService;
import ca.wd4.sg.service.ScheduleGenerateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
public class TestController {

    private CourseService courseService;

    @Autowired
    public TestController(CourseService courseService, ScheduleGenerateService scheduleGenerateService) {
        this.courseService = courseService;
        this.scheduleGenerateService = scheduleGenerateService;
    }

    private ScheduleGenerateService scheduleGenerateService;

    @GetMapping("/generateSchedules")
    public @ResponseBody
    List<Schedule> generateSchedules(@RequestParam(name="cids") List<Long> cids) {
        log.info("cids: "+cids);
        List<CourseBasic> courses = new ArrayList<>();
        for (long cid: cids) {
            courses.add(courseService.findById(cid).getBasic());
        }
        try {
            List<List<CourseInfo>> allCourseInfos = scheduleGenerateService.generateSchedules(courses);
            List<Schedule> result = new ArrayList<>();
            for (List<CourseInfo> courseInfos : allCourseInfos) {
                result.add(new Schedule(courseInfos, scheduleGenerateService.getTable(courseInfos)));
            }
            return result;
        } catch (EmptyCourseException e) {
            log.error("There is an empty course: "+e.getMessage());
            return new ArrayList<>();
        }
    }
}
