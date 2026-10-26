package bootcamp.hibernate_practical.service;

import bootcamp.hibernate_practical.dto.BookResponse;
import bootcamp.hibernate_practical.dto.CreateBookRequest;
import bootcamp.hibernate_practical.dto.UpdateBookRequest;
import bootcamp.hibernate_practical.entity.Book;
import bootcamp.hibernate_practical.repository.BookRepository;
import org.springframework.stereotype.Service;
import bootcamp.hibernate_practical.exception.BookNotFoundException;
import bootcamp.hibernate_practical.entity.BorrowedStatus;
import bootcamp.hibernate_practical.exception.BookStateException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import java.util.List;

@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public BookResponse createBook(CreateBookRequest request) {
        Book book = new Book(
                request.getTitle(),
                request.getAuthor(),
                request.getGenre(),
                request.getPublicationYear(),
                true
        );
        Book savedBook = bookRepository.save(book);
        return mapToResponse(savedBook);
    }

    public List<BookResponse> getAllBooks() {
               return bookRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
     }

    public BookResponse getBookById(Long id) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
        return mapToResponse(book);

    }

    public BookResponse updateBook(Long id, UpdateBookRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new  BookNotFoundException(id));
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setGenre(request.getGenre());
        book.setPublicationYear(request.getPublicationYear());
        if (request.isAvailable()) {
            book.markReturned();
        } else {
            book.markBorrowed();
        }
        Book savedBook = bookRepository.save(book);
        return mapToResponse(savedBook);
    }

    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new  BookNotFoundException(id);
        }
        bookRepository.deleteById(id);
    }

    public List<BookResponse> findByAuthor(String author) {

        return bookRepository.findByAuthor(author).stream()
                .map(this::mapToResponse)
                .toList();
    }


    public PagedModel<BookResponse> getBooksPage(Pageable pageable) {
        return new PagedModel<>(bookRepository.findAll(pageable).map(this::mapToResponse));
    }

    public List<BookResponse> filterBooks(String genre, String author, Boolean available) {
        return bookRepository.filter(blankToNull(genre), blankToNull(author), available).stream()
                .map(this::mapToResponse)
                .toList();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
    public List<BookResponse> searchByTitle(String text) {
        return bookRepository.findByTitleContainingIgnoreCase(text).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<BookResponse> findPublishedAfter(int year) {
        return bookRepository.findByPublicationYearGreaterThan(year).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public long countBooks() {
        return bookRepository.count();
    }

    public List<BookResponse> findAvailableBooks(){
      return bookRepository.findByAvailableTrue().stream()
              .map(this::mapToResponse).toList();

    }
    @Transactional
    public BookResponse borrowBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
        if (book.getBorrowedStatus() == BorrowedStatus.BORROWED) {
            throw new BookStateException("Book " + id + " is already borrowed");
        }
        book.markBorrowed();
        return mapToResponse(bookRepository.save(book));
    }

    @Transactional
    public BookResponse returnBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
        if (book.getBorrowedStatus() == BorrowedStatus.AVAILABLE) {
            throw new BookStateException("Book " + id + " is not borrowed");
        }
        book.markReturned();
        return mapToResponse(bookRepository.save(book));
    }
    private BookResponse mapToResponse(Book book) {

        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getGenre(),
                book.getPublicationYear(),
                book.isAvailable(),
                book.getBorrowedStatus()
        );
    }
}
