package bootcamp.hibernate_practical.controller;

import bootcamp.hibernate_practical.dto.CreateBookRequest;
import bootcamp.hibernate_practical.exception.BookNotFoundException;
import bootcamp.hibernate_practical.service.BookService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class BookPageController {
    private final BookService bookService;

    public BookPageController(BookService bookService) {
        this.bookService = bookService;
    }

    // CreateBookRequest has no setters, so the form is bound to its fields directly.
    @InitBinder
    void useFieldAccess(WebDataBinder binder) {
        binder.initDirectFieldAccess();
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/books";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/books")
    public String books(Model model, Authentication authentication) {
        model.addAttribute("books", bookService.getAllBooks());
        model.addAttribute("username", authentication.getName());
        model.addAttribute("isAdmin", authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
        return "books";
    }

    @PostMapping("/books")
    public String addBook(@Valid @ModelAttribute CreateBookRequest request, BindingResult result, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            redirect.addFlashAttribute("error", "Please fill in title, author and a valid year (1000-2100).");
        } else {
            bookService.createBook(request);
            redirect.addFlashAttribute("message", "Book added");
        }
        return "redirect:/books";
    }

    @PostMapping("/books/{id}/delete")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirect) {

        try {
            bookService.deleteBook(id);
            redirect.addFlashAttribute("message", "Book deleted.");
        } catch (BookNotFoundException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/books";
    }
}
