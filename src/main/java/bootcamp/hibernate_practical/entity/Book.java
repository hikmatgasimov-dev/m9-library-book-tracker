package bootcamp.hibernate_practical.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String author;
    private String genre;
    private int publicationYear;
    private boolean available;

    @Enumerated(EnumType.STRING)
    private BorrowedStatus  borrowedStatus= BorrowedStatus.AVAILABLE;
    public Book(String title, String author, String genre, int publicationYear, boolean available) {
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.publicationYear = publicationYear;
        this.available = available;
        this.borrowedStatus = available ? BorrowedStatus.AVAILABLE : BorrowedStatus.BORROWED;
    }
    public void markBorrowed() {
        this.available = false;
        this.borrowedStatus = BorrowedStatus.BORROWED;
    }

    public void markReturned() {
        this.available = true;
        this.borrowedStatus = BorrowedStatus.AVAILABLE;
    }
}
