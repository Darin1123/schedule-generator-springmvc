package ca.wd4.sg.web;

import ca.wd4.sg.core.CourseBasic;
import ca.wd4.sg.core.CourseInfo;
import ca.wd4.sg.data.CourseRepository;
import ca.wd4.sg.data.UserRepository;
import ca.wd4.sg.exception.CourseNameConflictException;
import ca.wd4.sg.exception.EmptyCourseException;
import ca.wd4.sg.exception.SectionConversionFailureException;
import ca.wd4.sg.model.Course;
import ca.wd4.sg.model.Response;
import ca.wd4.sg.model.Schedule;
import ca.wd4.sg.model.User;
import ca.wd4.sg.service.CourseService;
import ca.wd4.sg.service.ScheduleGenerateService;
import ca.wd4.sg.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/me")
public class MainController {

    private final UserService userService;
    private final CourseService courseService;
    private final ScheduleGenerateService scheduleGenerateService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MainController(CourseService courseService, UserRepository userRepository, CourseRepository courseRepository, UserService userService, ScheduleGenerateService scheduleGenerateService, PasswordEncoder encoder) {
        this.courseService = courseService;
        this.userService = userService;
        this.scheduleGenerateService = scheduleGenerateService;
        this.passwordEncoder = encoder;
    }

    @GetMapping
    public String mainPage(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("user", user);
        return "main";
    }

    @GetMapping("/user")
    public String user(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("user", user);
        return "user";
    }

    @PostMapping("/changeName")
    public RedirectView changeUserName(
            @AuthenticationPrincipal User user,
            @RequestParam(name = "newName", defaultValue = "") String newName,
            RedirectAttributes redir) {
        if (newName.length()==0) {
            redir.addFlashAttribute("errorNameMsg", "Name can not be empty.");
        } else if (newName.length()>50) {
            redir.addFlashAttribute("errorNameMsg", "Name can not be longer than 50 characters.");
        } else {
            userService.updateName(user, newName);
            user.setName(newName);
            redir.addFlashAttribute("resultNameMsg", "Updated successfully.");
        }

        return new RedirectView("/me/user");
    }

    @PostMapping("/changePassword")
    public RedirectView changePassword(@AuthenticationPrincipal User user,
                                       @RequestParam(name = "newPassword", defaultValue = "") String newPassword,
                                       @RequestParam(name = "confirm", defaultValue = "") String confirm,
                                       RedirectAttributes redir) {
        List<String> errorMsgs = new ArrayList<>();
        if (!newPassword.matches("^.*[a-z].*$")) {
            errorMsgs.add("Password must contain at least one lower case character.");
        }
        if (!newPassword.matches("^.*[A-Z].*$")) {
            errorMsgs.add("Password must contain at least one upper case character.");
        }
        if (!newPassword.matches("^.*[0-9].*$")) {
            errorMsgs.add("Password must contain at least digit.");
        }
        if (!newPassword.matches("^.*[\\*\\.\\!\\@\\#\\$\\%\\^\\&\\(\\)\\{\\}\\[\\]\\:\\;\\<\\>\\,\\?\\/\\~\\_\\+\\-\\=|]+.*$")) {
            errorMsgs.add("Password must contain at least one special character.");
        }
        if (newPassword.length()<8 || newPassword.length()>16) {
            errorMsgs.add("Password must contain 8 to 16 characters.");
        }
        if (!newPassword.equals(confirm)) {
            errorMsgs.add("Passwords don't match.");
        }

        if (errorMsgs.size()>0) {
            redir.addFlashAttribute("errorPwdMsg", errorMsgs);
        }
        else if (userService.usingOldPassword(user, newPassword)) {
            errorMsgs.add("Enter a different password.");
            redir.addFlashAttribute("errorPwdMsg", errorMsgs);
        }
        else {
            userService.updatePassword(user, newPassword);
            redir.addFlashAttribute("resultPwdMsg", "Updated successfully.");
        }
        return new RedirectView("/me/user");
    }

    @PostMapping("/delete")
    public RedirectView deleteAccount(@AuthenticationPrincipal User user,
                                      @RequestParam(name = "email", defaultValue = "") String email,
                                      RedirectAttributes redir) {
        if (!email.matches("^[a-zA-Z0-9]+\\@[a-zA-Z0-9]+\\.[a-zA-Z0-9]+$")) {
            redir.addFlashAttribute("errorDeleteMsg", "invalid email address format");
            return  new RedirectView("/me/user");
        }

        if (!user.getEmail().equals(email)) {
            redir.addFlashAttribute("errorDeleteMsg", "email addresses don't match");
            return  new RedirectView("/me/user");
        }

        userService.remove(user);
        return new RedirectView("/login");
    }

    @GetMapping("/search")
    public String searchCourse(@AuthenticationPrincipal User user,
                               Model model,
                               @RequestParam(name="keyWord", defaultValue = "") String keyWord,
                               @RequestParam(name="sort", defaultValue = "name") String sortOption) {
        model.addAttribute("user", user);

        //if keyword is empty directly return the view
        if ("".equals(keyWord))
            return "search";

        //keyword can be 1) course name and 2) term
        List<Course> courses = userService.find(user, keyWord, sortOption);

        model.addAttribute("courses", courses);
        model.addAttribute("keyWord", keyWord);
        model.addAttribute("sortOption", sortOption);
        return "search";
    }

    @GetMapping("/post")
    public String postForm(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("user", user);
        return "post";
    }

    @PostMapping("/post")
    public String postCourse(@RequestParam(name = "name") String name,
                            @RequestParam(name = "term") String term,
                            @RequestParam(name = "lecs") String lecs,
                            @RequestParam(name = "tuts") String tuts,
                            @RequestParam(name = "labs") String labs,
                            @AuthenticationPrincipal User user,
                             Model model) {
        name = name.toUpperCase();
        term = term.toLowerCase();
        model.addAttribute("user", user);
        model.addAttribute("name", name);
        model.addAttribute("term", term);
        model.addAttribute("lecs", lecs);
        model.addAttribute("tuts", tuts);
        model.addAttribute("labs", labs);
        List<String> errorMsgs = new ArrayList<>();
        if (!name.matches("^[a-zA-Z]{3,} \\d{1}[a-zA-Z][a-zA-Z0-9]\\d{1}$")) {
            errorMsgs.add("incorrect name, must be like 'SFWR 2AA4'.");
        }

        if (!term.matches("^(20|19)\\d{2} (spring|summer|fall|winter)$")) {
            errorMsgs.add("incorrect term, term must be like '2020 winter'");
        }

        if (!errorMsgs.isEmpty()) {
            model.addAttribute("errorMsgs", errorMsgs);
            return "post";
        }

        try {
            courseService.save(name, user, term, 0, lecs, tuts, labs);
        } catch (CourseNameConflictException e) {
            errorMsgs.add(e.getMessage());
            model.addAttribute("errorMsgs", errorMsgs);
            return "post";
        } catch (SectionConversionFailureException e) {
            errorMsgs.add(e.getMessage());
            model.addAttribute("errorMsgs", errorMsgs);
            return "post";
        }
        model.addAttribute("resultMsg", "saved "+name);
        return "post";
    }

    @GetMapping("/manage")
    public String manageCourses(@RequestParam(name = "keyword", defaultValue = "") String keyword,
                                @AuthenticationPrincipal User user,
                                Model model) {
        List<Course> allCourses = userService.findCourses(user);
        if (!"".equals(keyword)) {
            allCourses = allCourses
                            .stream()
                            .filter(c -> c.getName().contains(keyword.toUpperCase())||c.getTerm().contains(keyword.toLowerCase()))
                            .collect(Collectors.toList());
        }
        model.addAttribute("courses", allCourses);
        model.addAttribute("user", user);
        model.addAttribute("keyword", keyword);
        return "manage";
    }

    @GetMapping("/generate")
    public String selectCoursesByTerm(@RequestParam(name = "term", defaultValue = "") String term,
                                      @AuthenticationPrincipal User user,
                                      Model model) {
        model.addAttribute("term", term);
        model.addAttribute("user", user);
        log.info(term);
        //if keyword not empty, do search.
        //then if search result not empty, add model attribute
        if (!"".equals(term)) {
            List<Course> courses = userService.findCourses(user)
                    .stream()
                    .filter(c->c.getTerm().equals(term.toLowerCase()))
                    .collect(Collectors.toList());
            if (!courses.isEmpty()) {
                model.addAttribute("courses", courses);
            }
        }
        return "generate";
    }

    @GetMapping("/add")
    public @ResponseBody String addCourse(@RequestParam(name = "cid", defaultValue = "") long cid,
                                          @AuthenticationPrincipal User user) {
        try {
            Course course = courseService.findById(cid);
            log.info("authorId "+course.getAuthorId());
            userService.addCourse(user, course);
            return "Added " + course.getName();
        } catch (CourseNameConflictException e) {
            log.error(e.getMessage());
            return "Failed, because "+e.getMessage();
        }
    }

    @GetMapping("/delete")
    public @ResponseBody String deleteCourse(@RequestParam(name = "cid", defaultValue = "") long cid) {
            courseService.delete(cid);
            return "deleted";
    }

    @GetMapping("/modify")
    public String loadCourse(@RequestParam(name = "cid", defaultValue = "") long cid,
                               @AuthenticationPrincipal User user, Model model) {
        model.addAttribute("user", user);
        Course course = courseService.findById(cid);
        model.addAttribute("course", course);
        model.addAttribute("lecs", course.getLecs().stream().collect(Collectors.joining("\n")));
        model.addAttribute("tuts", course.getTuts().stream().collect(Collectors.joining("\n")));
        model.addAttribute("labs", course.getLabs().stream().collect(Collectors.joining("\n")));
        return "modify";
    }

    @PostMapping("/modify")
    public String modifyCourse(@RequestParam(name = "cid") long cid,
                               @RequestParam(name = "name") String name,
                               @RequestParam(name = "term") String term,
                               @RequestParam(name = "lecs") String lecs,
                               @RequestParam(name = "tuts") String tuts,
                               @RequestParam(name = "labs") String labs,
                               @AuthenticationPrincipal User user,
                               Model model) {
        log.info("** new lec section(s): "+lecs);
        term = term.toLowerCase();
        Course course = new Course();
        course.setName(name);
        course.setId(cid);
        course.setTerm(term);
        model.addAttribute("course", course);
        model.addAttribute("user", user);
        model.addAttribute("lecs", lecs);
        model.addAttribute("tuts", tuts);
        model.addAttribute("labs", labs);

        List<String> errorMsgs = new ArrayList<>();
        if (!term.matches("^(20|19)\\d{2} (spring|summer|fall|winter)$")) {
            errorMsgs.add("incorrect term, term must be like '2020 winter'");
        }

        if (!errorMsgs.isEmpty()) {
            model.addAttribute("errorMsgs", errorMsgs);
            return "modify";
        }

        try {
            courseService.update(cid, user.getId(), term, lecs, tuts, labs);
        } catch (Exception e) {
            errorMsgs.add(e.getMessage());
            model.addAttribute("errorMsgs", errorMsgs);
            return "modify";
        }
        model.addAttribute("resultMsg", "saved "+name+" in "+term);

        return "modify";
    }

    @GetMapping("/generateSchedules")
    public @ResponseBody
    Response<Schedule> generateSchedules(@RequestParam(name="cids") List<Long> cids) {
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
            if (result.isEmpty()) {
                return new Response("Oops, conflict exists.", new ArrayList());
            }
            return new Response("200", result);
        } catch (EmptyCourseException e) {
//            log.error("There is an empty course: "+e.getMessage());
            List<Schedule> result = new ArrayList<>();
            return new Response("Empty course exists", result);
        }
    }
}
