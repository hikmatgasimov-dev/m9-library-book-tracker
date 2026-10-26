package bootcamp.hibernate_practical.service;

import bootcamp.hibernate_practical.dto.BookResponse;
import bootcamp.hibernate_practical.dto.CreateBookRequest;
import bootcamp.hibernate_practical.dto.UpdateBookRequest;
import bootcamp.hibernate_practical.entity.Book;
import bootcamp.hibernate_practical.entity.BorrowedStatus;
import bootcamp.hibernate_practical.exception.BookNotFoundException;
import bootcamp.hibernate_practical.exception.BookStateException;
import bootcamp.hibernate_practical.repository.BookRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    private Book book(Long id, boolean available) {
        Book book = new Book("1984", "George Orwell", "Dystopian", 1949, available);
        book.setId(id);
        return book;
    }

    private <T> T request(Class<T> type, Object... fieldsAndValues) {
        T request = BeanUtils.instantiateClass(type);
        for (int i = 0; i < fieldsAndValues.length; i += 2) {
            ReflectionTestUtils.setField(request, (String) fieldsAndValues[i], fieldsAndValues[i + 1]);
        }
        return request;
    }

    @Test
    @DisplayName("createBook saves a new available book and returns it as a response")
    void createBookSavesAvailableBook() {
        CreateBookRequest request = request(CreateBookRequest.class,
                "title", "1984", "author", "George Orwell", "genre", "Dystopian", "publicationYear", 1949);
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        BookResponse response = bookService.createBook(request);

        assertThat(response.id).isEqualTo(1L);
        assertThat(response.title).isEqualTo("1984");
        assertThat(response.available).isTrue();
        assertThat(response.borrowedStatus).isEqualTo(BorrowedStatus.AVAILABLE);
    }

    @Test
    @DisplayName("getAllBooks maps every entity to a response")
    void getAllBooksMapsEntities() {
        when(bookRepository.findAll()).thenReturn(List.of(book(1L, true), book(2L, false)));

        List<BookResponse> responses = bookService.getAllBooks();

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(response -> response.id).containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("getBookById returns the book when it exists")
    void getBookByIdFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book(1L, true)));

        assertThat(bookService.getBookById(1L).title).isEqualTo("1984");
    }

    @Test
    @DisplayName("getBookById throws BookNotFoundException when the id is unknown")
    void getBookByIdNotFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById(99L))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessage("Book not found with id 99");
    }

    @Test
    @DisplayName("updateBook copies the request fields and marks the book borrowed when available is false")
    void updateBookUpdatesFields() {
        Book existing = book(1L, true);
        UpdateBookRequest request = request(UpdateBookRequest.class,
                "title", "Animal Farm", "author", "George Orwell", "genre", "Satire",
                "publicationYear", 1945, "available", false);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(bookRepository.save(existing)).thenReturn(existing);

        BookResponse response = bookService.updateBook(1L, request);

        assertThat(response.title).isEqualTo("Animal Farm");
        assertThat(response.publicationYear).isEqualTo(1945);
        assertThat(response.available).isFalse();
        assertThat(response.borrowedStatus).isEqualTo(BorrowedStatus.BORROWED);
    }

    @Test
    @DisplayName("deleteBook deletes an existing book")
    void deleteBookDeletes() {
        when(bookRepository.existsById(1L)).thenReturn(true);

        bookService.deleteBook(1L);

        verify(bookRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteBook throws BookNotFoundException and deletes nothing for an unknown id")
    void deleteBookNotFound() {
        when(bookRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> bookService.deleteBook(99L))
                .isInstanceOf(BookNotFoundException.class);
        verify(bookRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("borrowBook marks an available book as borrowed")
    void borrowBookSucceeds() {
        Book existing = book(1L, true);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(bookRepository.save(existing)).thenReturn(existing);

        BookResponse response = bookService.borrowBook(1L);

        assertThat(response.available).isFalse();
        assertThat(response.borrowedStatus).isEqualTo(BorrowedStatus.BORROWED);
    }

    @Test
    @DisplayName("borrowBook throws BookStateException when the book is already borrowed")
    void borrowBookAlreadyBorrowed() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book(1L, false)));

        assertThatThrownBy(() -> bookService.borrowBook(1L))
                .isInstanceOf(BookStateException.class)
                .hasMessage("Book 1 is already borrowed");
        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("borrowBook throws BookNotFoundException for an unknown id")
    void borrowBookNotFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.borrowBook(99L))
                .isInstanceOf(BookNotFoundException.class);
    }

    @Test
    @DisplayName("returnBook marks a borrowed book as available")
    void returnBookSucceeds() {
        Book existing = book(1L, false);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(bookRepository.save(existing)).thenReturn(existing);

        BookResponse response = bookService.returnBook(1L);

        assertThat(response.available).isTrue();
        assertThat(response.borrowedStatus).isEqualTo(BorrowedStatus.AVAILABLE);
    }

    @Test
    @DisplayName("returnBook throws BookStateException when the book is not borrowed")
    void returnBookNotBorrowed() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book(1L, true)));

        assertThatThrownBy(() -> bookService.returnBook(1L))
                .isInstanceOf(BookStateException.class)
                .hasMessage("Book 1 is not borrowed");
    }

    @Test
    @DisplayName("countBooks returns the repository count")
    void countBooksReturnsCount() {
        when(bookRepository.count()).thenReturn(3L);

        assertThat(bookService.countBooks()).isEqualTo(3L);
    }

    @Test
    @DisplayName("searchByTitle delegates to the case-insensitive repository search")
    void searchByTitleDelegates() {
        when(bookRepository.findByTitleContainingIgnoreCase("anim")).thenReturn(List.of(book(2L, true)));

        assertThat(bookService.searchByTitle("anim")).hasSize(1);
    }

    @Test
    @DisplayName("filterBooks turns blank text filters into null so they are ignored")
    void filterBooksBlankBecomesNull() {
        when(bookRepository.filter(null, null, null)).thenReturn(List.of(book(1L, true)));

        List<BookResponse> responses = bookService.filterBooks("", "  ", null);

        assertThat(responses).hasSize(1);
        verify(bookRepository).filter(null, null, null);
    }

    @Test
    @DisplayName("filterBooks passes real filter values through unchanged")
    void filterBooksPassesValues() {
        when(bookRepository.filter("Sci-Fi", "Frank Herbert", true)).thenReturn(List.of(book(3L, true)));

        assertThat(bookService.filterBooks("Sci-Fi", "Frank Herbert", true)).hasSize(1);
    }
}