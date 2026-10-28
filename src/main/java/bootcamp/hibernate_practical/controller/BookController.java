package bootcamp.hibernate_practical.controller;

import bootcamp.hibernate_practical.dto.BookResponse;
import bootcamp.hibernate_practical.dto.CreateBookRequest;
import bootcamp.hibernate_practical.dto.UpdateBookRequest;
import bootcamp.hibernate_practical.service.BookService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import java.util.List;
import bootcamp.hibernate_practical.dto.BookInfoResponse;
import bootcamp.hibernate_practical.service.BookInfoService;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookService bookService;
    private final BookInfoService bookInfoService;

    public BookController(BookService bookService,BookInfoService bookInfoService) {
        this.bookService = bookService;
        this.bookInfoService = bookInfoService;
    }

    @PostMapping
    public BookResponse createBook(@Valid @RequestBody CreateBookRequest createBookRequest) {
        return bookService.createBook(createBookRequest);
    }

    @GetMapping
    public List<BookResponse> getAllBooks() {

        return bookService.getAllBooks();
    }

    @GetMapping("/page")
    public PagedModel<BookResponse> getBooksPage(@PageableDefault(size = 5, sort = "title") Pageable pageable) {
        return bookService.getBooksPage(pageable);
    }

    @GetMapping("/filter")
    public List<BookResponse> filterBooks(@RequestParam(required = false) String genre,
                                          @RequestParam(required = false) String author,
                                          @RequestParam(required = false) Boolean available) {
        return bookService.filterBooks(genre, author, available);
    }

    @GetMapping("/{id}")
    public BookResponse getBookById(@PathVariable Long id) {
        return bookService.getBookById(id);
    }

    @PutMapping("/{id}")
    public BookResponse updateBook(@PathVariable Long id, @Valid  @RequestBody UpdateBookRequest updateBookRequest) {
        return bookService.updateBook(id,updateBookRequest);
    }
    @GetMapping("/{id}/info")
    public BookInfoResponse getBookInfo(@PathVariable Long id) {
        return bookInfoService.getInfo(id);
    }

    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable Long id) {
       bookService.deleteBook(id);
    }
    @PostMapping("/{id}/borrow")
    public BookResponse borrowBook(@PathVariable Long id) {
        return bookService.borrowBook(id);
    }

    @PostMapping("/{id}/return")
    public BookResponse returnBook(@PathVariable Long id) {
        return bookService.returnBook(id);
    }
    @GetMapping("/author/{author}")
    public List<BookResponse> getBooksByAuthor(@PathVariable String author) {

        return bookService.findByAuthor(author);
    }

    @GetMapping("/available")
    public List<BookResponse> getAvailableBooks() {
        return bookService.findAvailableBooks();
    }
    @GetMapping("/search")
    public List<BookResponse> searchBookByTitle(@RequestParam String title) {
        return bookService.searchByTitle(title);
    }
    @GetMapping("/published-after/{year}")
    public List<BookResponse> getBooksPublishedAfter(@PathVariable int year) {
        return bookService.findPublishedAfter(year);
    }
    @GetMapping("/count")
    public long countBooks() {
        return bookService.countBooks();
    }
}
