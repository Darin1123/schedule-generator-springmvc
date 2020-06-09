package ca.wd4.sg.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/login")
public class LoginController {

    @RequestMapping("/error")
    public String loginError(Model model) {
        log.info("authentication failure");
        model.addAttribute("errors", "error");
        return "loginForm";
    }
}
