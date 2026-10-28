package bootcamp.hibernate_practical.security;

import bootcamp.hibernate_practical.entity.AppUser;
import bootcamp.hibernate_practical.entity.Role;
import bootcamp.hibernate_practical.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class RegistrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Register page is open to anonymous visitors")
    void registerPageIsPublic() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    @DisplayName("Registering creates a USER with a BCrypt password and redirects to login")
    void registrationCreatesUserWithEncodedPassword() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                        .param("username", "sarah").param("password", "secret1").param("confirmPassword", "secret1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        AppUser saved = userRepository.findByUsername("sarah").orElseThrow();
        assertThat(saved.getRole()).isEqualTo(Role.USER);
        assertThat(saved.getPassword()).isNotEqualTo("secret1");
        assertThat(passwordEncoder.matches("secret1", saved.getPassword())).isTrue();
    }

    @Test
    @DisplayName("A role sent in the form is ignored: the new account is still a USER")
    void roleParameterCannotMakeAnAdmin() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                        .param("username", "sneaky").param("password", "secret1").param("confirmPassword", "secret1")
                        .param("role", "ADMIN"))
                .andExpect(status().is3xxRedirection());

        assertThat(userRepository.findByUsername("sneaky").orElseThrow().getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("A newly registered user can read but cannot write")
    void newUserHasUserPermissionsOnly() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                .param("username", "reader").param("password", "secret1").param("confirmPassword", "secret1"));

        mockMvc.perform(get("/api/books").with(httpBasic("reader", "secret1")))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/books").with(httpBasic("reader", "secret1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"X\",\"author\":\"Y\",\"genre\":\"Z\",\"publicationYear\":2000}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Existing username is rejected with a clear message")
    void duplicateUsernameIsRejected() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                        .param("username", "admin").param("password", "secret1").param("confirmPassword", "secret1"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrorCode("registerRequest", "username", "taken"));
    }

    @Test
    @DisplayName("Too short username/password and mismatching passwords are rejected")
    void invalidInputIsRejected() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                        .param("username", "ab").param("password", "123").param("confirmPassword", "123"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("registerRequest", "username", "password"));

        mockMvc.perform(post("/register").with(csrf())
                        .param("username", "mismatch").param("password", "secret1").param("confirmPassword", "other99"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode("registerRequest", "confirmPassword", "mismatch"));

        assertThat(userRepository.findByUsername("mismatch")).isEmpty();
    }

    @Test
    @DisplayName("Registration form without CSRF token -> 403")
    void registrationWithoutCsrfIsRejected() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "nocsrf").param("password", "secret1").param("confirmPassword", "secret1"))
                .andExpect(status().isForbidden());
        assertThat(userRepository.findByUsername("nocsrf")).isEmpty();
    }
}