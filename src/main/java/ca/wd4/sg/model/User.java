package ca.wd4.sg.model;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import java.util.Arrays;
import java.util.Collection;

@Data
public class User implements UserDetails {

    private long id;
    @Size(min=6, max=20, message = "name should be 6 to 20 characters.")
    private String name;
    @Email(message = "wrong email format")
    private final String email;
    @Size(min=8, max = 16, message = "password length should be between 8 and 16.")
    private final String password;

    public User(long id, @Size(min = 6, max = 20, message = "name should be 6 to 20 characters.") String name, @Email(message = "wrong email format") String email, @Size(min = 8, max = 16, message = "password length should be between 8 and 16.") String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public User(@Size(min = 6, max = 20, message = "name should be 6 to 20 characters.") String name, @Email(message = "wrong email format") String email, @Size(min = 8, max = 16, message = "password length should be between 8 and 16.") String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getUsername() {
        return name;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
