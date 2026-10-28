package bootcamp.hibernate_practical.dto;

import java.util.List;

// Extra information about a book, fetched from the external API.
public record BookInfoResponse(String title,
                               List<String> authors,
                               String publishedDate,
                               Integer pageCount,
                               String description) {}