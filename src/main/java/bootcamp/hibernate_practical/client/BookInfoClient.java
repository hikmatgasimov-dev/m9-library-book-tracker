package bootcamp.hibernate_practical.client;

import bootcamp.hibernate_practical.config.ExternalApiProperties;
import bootcamp.hibernate_practical.dto.BookInfoResponse;
import bootcamp.hibernate_practical.exception.ExternalApiException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

// Secured outbound call: every request carries the API key, which is read from configuration.
@Component
public class BookInfoClient {

    private static final Logger log = LoggerFactory.getLogger(BookInfoClient.class);
    private static final String API_KEY_HEADER = "X-Goog-Api-Key";

    private final RestClient restClient;
    private final String apiKey;

    public BookInfoClient(RestClient.Builder builder, ExternalApiProperties properties) {
        this.restClient = builder.baseUrl(properties.baseUrl()).build();
        this.apiKey = properties.key();
    }

    public BookInfoResponse findByTitle(String title) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ExternalApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "External book API key is not configured (set the BOOK_API_KEY environment variable)");
        }
        SearchResponse response = callExternalApi(title);
        if (response == null || response.items() == null || response.items().isEmpty()) {
            throw new ExternalApiException(HttpStatus.NOT_FOUND, "No external information found for '" + title + "'");
        }
        Volume volume = response.items().get(0).volumeInfo();
        return new BookInfoResponse(volume.title(), volume.authors(), volume.publishedDate(),
                volume.pageCount(), volume.description());
    }

    private SearchResponse callExternalApi(String title) {
        try {
            return restClient.get()
                    .uri("/volumes?q={query}&maxResults=1", "intitle:" + title)
                    .header(API_KEY_HEADER, apiKey)
                    .retrieve()
                    .body(SearchResponse.class);
        } catch (HttpClientErrorException e) {
            log.error("External book API answered {} for '{}'", e.getStatusCode(), title);
            if (e.getStatusCode().value() == 401 || e.getStatusCode().value() == 403) {
                throw new ExternalApiException(HttpStatus.BAD_GATEWAY,
                        "External book API rejected our API key (" + e.getStatusCode() + ")", e);
            }
            throw new ExternalApiException(HttpStatus.BAD_GATEWAY,
                    "External book API rejected the request (" + e.getStatusCode() + ")", e);
        } catch (HttpServerErrorException e) {
            log.error("External book API failed with {} for '{}'", e.getStatusCode(), title);
            throw new ExternalApiException(HttpStatus.BAD_GATEWAY,
                    "External book API failed (" + e.getStatusCode() + ")", e);
        } catch (ResourceAccessException e) {
            log.error("External book API is unreachable or timed out: {}", e.getMessage());
            throw new ExternalApiException(HttpStatus.BAD_GATEWAY,
                    "External book API is unreachable or timed out", e);
        } catch (RestClientException e) {
            log.error("External book API sent an unreadable response: {}", e.getMessage());
            throw new ExternalApiException(HttpStatus.BAD_GATEWAY,
                    "External book API sent an unexpected response", e);
        }
    }

    // Only the parts of the external JSON that we need
    private record SearchResponse(List<Item> items) {
    }

    private record Item(Volume volumeInfo) {
    }

    private record Volume(String title, List<String> authors, String publishedDate,
                          Integer pageCount, String description) {
    }
}