package bootcamp.hibernate_practical.exception;

public class BookStateException extends RuntimeException {
    public BookStateException(String message) {
        super(message);
    }
}
