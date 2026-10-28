package bootcamp.hibernate_practical.service;

import bootcamp.hibernate_practical.client.BookInfoClient;
import bootcamp.hibernate_practical.dto.BookInfoResponse;
import org.springframework.stereotype.Service;

@Service
public class BookInfoService {

    private final BookService bookService;
    private final BookInfoClient bookInfoClient;

    public BookInfoService(BookService bookService, BookInfoClient bookInfoClient) {
        this.bookService = bookService;
        this.bookInfoClient = bookInfoClient;
    }

    // Finds our book first (404 if it does not exist), then asks the external API about its title.
    public BookInfoResponse getInfo(Long id) {
        return bookInfoClient.findByTitle(bookService.getBookById(id).title);
    }
}