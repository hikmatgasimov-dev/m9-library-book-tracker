package bootcamp.hibernate_practical.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// no role field: anyone who registers becomes a USER.
@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 30, message = "Username must be 3-30 characters")
    @Pattern(regexp = "[A-Za-z0-9._-]*", message = "Username may only contain letters, digits, '.', '_' and '-'")
    private String username;

    // BCrypt only uses the first 72 bytes, so longer passwords are not allowed
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 72, message = "Password must be 6-72 characters")
    private String password;

    @NotBlank(message = "Please repeat the password")
    private String confirmPassword;
}