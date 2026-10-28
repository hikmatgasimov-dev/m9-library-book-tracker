package bootcamp.hibernate_practical.controller;

import bootcamp.hibernate_practical.dto.RegisterRequest;
import bootcamp.hibernate_practical.exception.UsernameTakenException;
import bootcamp.hibernate_practical.service.UserRegistrationService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

// Public sign-up page. SecurityConfig lets everyone reach /register; CSRF protection stays on.
@Controller
public class RegisterController {

    private final UserRegistrationService registrationService;

    public RegisterController(UserRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/register")
    public String showForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterRequest request, BindingResult result) {
        if (!result.hasFieldErrors("password") && !result.hasFieldErrors("confirmPassword")
                && !request.getPassword().equals(request.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "Passwords do not match");
        }
        if (result.hasErrors()) {
            return "register";
        }
        try {
            registrationService.register(request.getUsername(), request.getPassword());
        } catch (UsernameTakenException e) {
            result.rejectValue("username", "taken", e.getMessage());
            return "register";
        }
        return "redirect:/login?registered";
    }
}