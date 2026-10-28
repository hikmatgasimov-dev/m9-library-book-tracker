package bootcamp.hibernate_practical.security;

import bootcamp.hibernate_practical.client.BookInfoClient;
import bootcamp.hibernate_practical.exception.ExternalApiException;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Runs the whole application with the real SecurityConfig, the H2 database and the seeded
// user/user and admin/admin accounts. Only the outbound API client is mocked.
@SpringBootTest
@AutoConfigureMockMvc
class SecurityRulesTest {

    private static final String BOOK_JSON = """
            {"title": "Clean Code", "author": "Robert Martin", "genre": "Programming", "publicationYear": 2008}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookInfoClient bookInfoClient;

    @Test
    @DisplayName("Unauthenticated REST request -> 401")
    void unauthenticatedRestRequestIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Unauthenticated browser request -> redirect to the login page")
    void unauthenticatedBrowserRequestRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/books").accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/login")));
    }

    @Test
    @DisplayName("Wrong password -> 401")
    void wrongPasswordIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/books").with(httpBasic("user", "wrong")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("USER reading data -> 200")
    void userCanRead() throws Exception {
        mockMvc.perform(get("/api/books").with(httpBasic("user", "user")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("USER attempting a write -> 403")
    void userCannotWrite() throws Exception {
        mockMvc.perform(post("/api/books").with(httpBasic("user", "user"))
                        .contentType(MediaType.APPLICATION_JSON).content(BOOK_JSON))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/books/1").with(httpBasic("user", "user")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN writing data -> 200")
    void adminCanWrite() throws Exception {
        mockMvc.perform(post("/api/books").with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON).content(BOOK_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Clean Code"));
    }

    @Test
    @DisplayName("Browser form without CSRF token -> 403")
    void formWithoutCsrfTokenIsRejected() throws Exception {
        mockMvc.perform(post("/books").with(user("admin").roles("ADMIN"))
                        .param("title", "Clean Code").param("author", "Robert Martin")
                        .param("genre", "Programming").param("publicationYear", "2008"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Browser form with CSRF token as ADMIN -> redirect, book added")
    void formWithCsrfTokenIsAccepted() throws Exception {
        mockMvc.perform(post("/books").with(user("admin").roles("ADMIN")).with(csrf())
                        .param("title", "Refactoring").param("author", "Martin Fowler")
                        .param("genre", "Programming").param("publicationYear", "1999"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Browser form as USER, even with a CSRF token -> 403")
    void userCannotSubmitAddBookForm() throws Exception {
        mockMvc.perform(post("/books").with(user("user").roles("USER")).with(csrf())
                        .param("title", "Refactoring").param("author", "Martin Fowler")
                        .param("genre", "Programming").param("publicationYear", "1999"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("External API failure -> clear 502 JSON error")
    void externalApiFailureIsHandledClearly() throws Exception {
        String created = mockMvc.perform(post("/api/books").with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON).content(BOOK_JSON))
                .andReturn().getResponse().getContentAsString();
        int id = JsonPath.read(created, "$.id");

        when(bookInfoClient.findByTitle(anyString())).thenThrow(
                new ExternalApiException(HttpStatus.BAD_GATEWAY, "External book API is unreachable or timed out"));

        mockMvc.perform(get("/api/books/" + id + "/info").with(httpBasic("user", "user")))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.error").value("External book API is unreachable or timed out"));
    }
}