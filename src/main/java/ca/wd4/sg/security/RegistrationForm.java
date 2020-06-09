package ca.wd4.sg.security;

import ca.wd4.sg.model.User;
import lombok.Data;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class RegistrationForm {

    @NotNull(message = "can not be blank")
    @Size(min=1, max = 50, message = "name length should be between 1 and 50.")
    private String name;
    @Email(message = "incorrect email format.")
    @Size(max=60, message = "email address too long.")
    private String email;
    @Pattern(regexp = "^.*[0-9]+.*$", message = "password must contain at least one digit.")
    @Pattern(regexp = "^.*[a-z]+.*$", message = "password must contain at least one lower case letter")
    @Pattern(regexp = "^.*[A-Z]+.*$", message = "password must contain st least one upper case letter")
    @Pattern(regexp = "^.*[\\*\\.\\!\\@\\#\\$\\%\\^\\&\\(\\)\\{\\}\\[\\]\\:\\;\\<\\>\\,\\?\\/\\~\\_\\+\\-\\=|]+.*$", message = "password must contain at least one special character")
    @Size(min=8, max = 16, message = "password length should be between 8 and 16.")
    private String password;
    private String confirm;

    public User toUser(PasswordEncoder passwordEncoder) {
        return new User(name, email, passwordEncoder.encode(password));
    }
}
