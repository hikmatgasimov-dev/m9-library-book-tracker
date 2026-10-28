package bootcamp.hibernate_practical.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Settings of the outbound book-info API. The key comes from the BOOK_API_KEY environment variable.
@ConfigurationProperties(prefix = "external-api")
public record ExternalApiProperties(String baseUrl, String key) {
}