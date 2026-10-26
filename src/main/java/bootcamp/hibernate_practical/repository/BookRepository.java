package bootcamp.hibernate_practical.repository;

import bootcamp.hibernate_practical.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    @Query("""
            select b from Book b
            where (:genre is null or lower(b.genre) = lower(:genre))
              and (:author is null or lower(b.author) = lower(:author))
              and (:available is null or b.available = :available)
            """)
    List<Book> filter(@Param("genre") String genre,
                      @Param("author") String author,
                      @Param("available") Boolean available);
    List<Book>findByAuthor(String author);
    List<Book> findByAvailableTrue();
    List<Book> findByTitleContainingIgnoreCase(String text);

    List<Book> findByPublicationYearGreaterThan(int year);
}
