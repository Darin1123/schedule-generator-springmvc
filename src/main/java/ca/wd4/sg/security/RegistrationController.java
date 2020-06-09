package ca.wd4.sg.security;

import ca.wd4.sg.data.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@Slf4j
@Controller
@RequestMapping("/register")
public class RegistrationController {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public RegistrationController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String registerForm(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "registration";
    }

    @PostMapping
    public String processRegistration(
            @Valid
            @ModelAttribute(name = "registrationForm") RegistrationForm form,
            Errors errors, Model model) {
        log.info("processing "+form);
        if (errors.hasErrors()) {
            return "registration";
        }

        try { //TODO need to be improved
            userRepository.findOneByEmail(form.getEmail());
        } catch (EmptyResultDataAccessException e) {
            userRepository.save(form.toUser(passwordEncoder));
            return "redirect:/login";
        }
        log.info("duplicated email address");
        model.addAttribute("error", "email address already exist.");
        return "registration";
    }
}
