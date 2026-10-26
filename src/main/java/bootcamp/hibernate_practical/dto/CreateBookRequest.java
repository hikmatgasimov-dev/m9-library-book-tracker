package bootcamp.hibernate_practical.dto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CreateBookRequest {
    @NotBlank(message = "Title is required")
    private String title;
    @NotBlank(message = "Author is required")
    private String author;
    private String genre;
    @Min(value = 1000, message = "Publication year must be 1000 or later")
    @Max(value = 2100, message = "Publication year must be 2100 or earlier")
    private int publicationYear;
}
