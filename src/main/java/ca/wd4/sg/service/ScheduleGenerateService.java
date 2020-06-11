package ca.wd4.sg.service;


import ca.wd4.sg.core.*;
import ca.wd4.sg.exception.EmptyCourseException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleGenerateService {
    private Map<Long, List<CourseInfo>> courseInfoMap;
    private Map<Long, CourseBasic> courseMap;

    /**
     * List<CourseInfo> is treated as a schedule.
     * @param courses
     * @return
     */
    public List<List<CourseInfo>> generateSchedules(List<CourseBasic> courses) throws EmptyCourseException {
        //initialize data
        init(courses);

        //prepare all possible schedules
        List<List<CourseInfo>> allSchedules = new ArrayList<>();
        Set<Long> keys = courseInfoMap.keySet();
        for (Long id : keys) {
//            System.out.println("should be once");
//            System.out.println("### id: "+id);
            List<CourseInfo> courseInfos = courseInfoMap.get(id);
//            System.out.println("### courseInfos: "+courseInfos);
            List<List<CourseInfo>> temp = new ArrayList<>();
            if (allSchedules.isEmpty()) {
                for (CourseInfo courseInfo: courseInfos) {
//                    System.out.println("should also be once");
                    List<CourseInfo> schedule = new ArrayList<>();
                    schedule.add(courseInfo);
                    allSchedules.add(schedule);
                }
            } else {
                for (List<CourseInfo> schedule : allSchedules) {
                    for (CourseInfo courseInfo : courseInfos) {
                        List<CourseInfo> scheduleCopy = copy(schedule);
                        scheduleCopy.add(courseInfo);
                        temp.add(scheduleCopy);
                    }
                }
                allSchedules = temp;
            }
        }//fill in allSchedules;

        //filter out conflict ones
        return allSchedules.stream().filter(schedule->!hasConflict(schedule)).collect(Collectors.toList());
    }

    public List<ClassDetail> getTable(List<CourseInfo> schedule) {
        Counter counter = Counter.getCounter();
        counter.init(0);
        List<ClassDetail> result = new ArrayList<>();
        for (CourseInfo courseInfo: schedule) {
            CourseBasic course = courseMap.get(courseInfo.getId());
            String name = course.getName();
            int lecNum = courseInfo.getLecNum();
            int tutNum = courseInfo.getTutNum();
            int labNum = courseInfo.getLabNum();
            String lecTimeDetail = lecNum==-1?"":course.getLecs().get(lecNum);
            String tutTimeDetail = tutNum==-1?"":course.getTuts().get(tutNum);
            String labTimeDetail = labNum==-1?"":course.getLabs().get(labNum);
            result.addAll(getTimeDetails(name, lecTimeDetail, counter, "lec"+(lecNum+1)));
            result.addAll(getTimeDetails(name, tutTimeDetail, counter, "tut"+(tutNum+1)));
            result.addAll(getTimeDetails(name, labTimeDetail, counter, "lab"+(labNum+1)));
        }
        return result;
    }

    private List<ClassDetail> getTimeDetails(String name, String secTimeDetail, Counter counter, String section) {
        List<ClassDetail> result = new ArrayList<>();
        if (secTimeDetail.equals("")) {
            return result;
        }
        String[] lines = secTimeDetail.split(",");
        List<String> timeInfos = new ArrayList<>();
        for (String line : lines) { timeInfos.add(line.trim()); }
        for (String timeInfo : timeInfos) {
            String daysStr = timeInfo.split(" ")[0];
            String time = timeInfo.split(" ")[1];
            List<Integer> days = getDays(daysStr);
            for (int day : days) {
                result.add(new ClassDetail(counter.getValue(), name, day, time, section));
                counter.increment();
            }
        }
        return result;
    }

    private List<Integer> getDays(String daysStr) {
        List<Integer> result = new ArrayList<>();
        for (int i=0; i<daysStr.length(); i+=2) {
            result.add(Time.day2Int(daysStr.substring(i, i+2)));
        }
        return result;
    }

    private boolean hasConflict(List<CourseInfo> schedule) {
        for (int i=0; i<schedule.size(); i++) {
            for (int j=i+1; j<schedule.size(); j++) {
                if (hasConflict(schedule.get(i), schedule.get(j))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasConflict(CourseInfo courseInfo1, CourseInfo courseInfo2) {
        CourseBasic course1 = courseMap.get(courseInfo1.getId());
        CourseBasic course2 = courseMap.get(courseInfo2.getId());
        List<String> course1Sections = Arrays.asList(
                courseInfo1.getLecNum()==-1?"":course1.getLecs().get(courseInfo1.getLecNum()),
                courseInfo1.getTutNum()==-1?"":course1.getTuts().get(courseInfo1.getTutNum()),
                courseInfo1.getLabNum()==-1?"":course1.getLabs().get(courseInfo1.getLabNum()));
        List<String> course2Sections = Arrays.asList(
                courseInfo2.getLecNum()==-1?"":course2.getLecs().get(courseInfo2.getLecNum()),
                courseInfo2.getTutNum()==-1?"":course2.getTuts().get(courseInfo2.getTutNum()),
                courseInfo2.getLabNum()==-1?"":course2.getLabs().get(courseInfo2.getLabNum()));
        for (String section1 : course1Sections) {
            for (String section2 : course2Sections) {
                if (hasConflict(section1, section2)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<CourseInfo> copy(List<CourseInfo> schedule) {
        List<CourseInfo> copy = new ArrayList<>();
        schedule.forEach(courseInfo -> copy.add(courseInfo));
        return copy;
    }

    private void init(List<CourseBasic> courses) throws EmptyCourseException {
        this.courseMap = new HashMap<>();
        this.courseInfoMap = new HashMap<>();
        for (CourseBasic course : courses) {
            this.courseMap.put(course.getId(), course);
            List<CourseInfo> courseInfos = generateCourseInfos(course);
            // filter out the conflict combinations
//            courseInfos = filterOutInvalid(courseInfos);
            courseInfos = courseInfos.stream().filter(courseInfo -> !hasConflict(courseInfo)).collect(Collectors.toList());
//            System.out.println("### courseInfos: "+courseInfos);
            if (isEmpty(courseInfos)) {
                throw new EmptyCourseException("course "+course.getId()+" "+course.getName()+" contains no sections."); //there is a course with no class ==> invalid input
            }
            courseInfoMap.put(course.getId(), courseInfos);
        }
    }

    private boolean hasConflict(CourseInfo courseInfo) {
        CourseBasic course = courseMap.get(courseInfo.getId());
        int lecNum = courseInfo.getLecNum();
        int tutNum = courseInfo.getTutNum();
        int labNum = courseInfo.getLabNum();
        String lec = lecNum==-1 ? "":course.getLecs().get(lecNum);
        String tut = tutNum==-1 ? "":course.getTuts().get(tutNum);
        String lab = labNum==-1 ? "":course.getLabs().get(labNum);
        if (hasConflict(lec, tut, lab)) {
            return true;
        }
        return false;
    }

    private boolean hasConflict(String lec, String tut, String lab) {
        if (hasConflict(lec, tut)) { return true; }
        if (hasConflict(lec, lab)) { return true; }
        if (hasConflict(tut, lab)) { return true; }
        return false;
    }

    private boolean hasConflict(String sec1, String sec2) {
        if ("".equals(sec1) || "".equals(sec2)) {
            return false;
        }
        List<Time> sec1Times = getTimes(sec1);
        List<Time> sec2Times = getTimes(sec2);
        for (Time time1 : sec1Times) {
            for (Time time2 : sec2Times) {
                if (time1.hasConflict(time2)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * input sample: "MoTu 12:30-13:20, Fr 08:30-09:20"
     * output sample: ["Mo 12:30-13:20", "Tu 12:30-13:20", "Fr 08:30-09:20"]
     * @param sec
     * @return
     */
    private List<Time> getTimes(String sec) {
        String[] lines = sec.split(",");
        List<String> splitComma = new ArrayList<>();
        for (String line : lines) {
            splitComma.add(line.trim());
        }

        List<Time> result = new ArrayList<>();
        for (String data : splitComma) {
            String days = data.split(" ")[0];
            String time = data.split(" ")[1];
            for (int i=0; i<days.length(); i+=2) {
                String day = days.substring(i, i+2);
                result.add(new Time(day, time));
            }
        }
        return result;
    }

    private List<CourseInfo> filterOutInvalid(List<CourseInfo> courseInfos) {
        List<CourseInfo> result = new ArrayList<>();
        courseInfos.forEach(courseInfo -> {
            if (!hasConflict(courseInfo)) {
                result.add(courseInfo);
            }
        });
        return result;
    }

    private boolean isEmpty(List<CourseInfo> courseInfos) {
        if (courseInfos.size()==1) {
            CourseInfo courseInfo = courseInfos.get(0);
            if (courseInfo.getLecNum()==-1 &&
                    courseInfo.getTutNum()==-1 &&
                    courseInfo.getLabNum()==-1) {
                return true;
            }
        }
        return false;
    }

    private List<CourseInfo> generateCourseInfos(CourseBasic course) {
        List<CourseInfo> result = new ArrayList<>();
        long id = course.getId();
        List<Integer> lecNums = range(course.getLecs().size());
        List<Integer> tutNums = range(course.getTuts().size());
        List<Integer> labNums = range(course.getLabs().size());
        if (lecNums.isEmpty()) {  lecNums.add(-1); }
        if (tutNums.isEmpty()) {  tutNums.add(-1); }
        if (labNums.isEmpty()) {  labNums.add(-1); }
        for (int lec : lecNums) {
            for (int tut : tutNums) {
                for (int lab : labNums) {
                    result.add(new CourseInfo(id, course.getName(), lec, tut, lab));
                }
            }
        }
        return result;
    }

    private List<Integer> range(int endInclusive) {
        List<Integer> result = new ArrayList<>();
        for (int i=0; i<endInclusive; i++) {
            result.add(i);
        }
        return result;
    }

    public static void main(String[] args) throws EmptyCourseException {
        CourseBasic course1 = new CourseBasic(1, "a",
                Arrays.asList("MoTu 08:30-09:20", "We 12:30-13:20"),
                Arrays.asList("Tu 08:30-09:20"),
                new ArrayList<>());
        ScheduleGenerateService service = new ScheduleGenerateService();
        List<List<CourseInfo>> schedules =  service.generateSchedules(Arrays.asList(course1));
        System.out.println("** result schedules: " + schedules);
        List<ClassDetail> table = service.getTable(schedules.get(0));
        System.out.println("** table1: "+table);
    }
}
