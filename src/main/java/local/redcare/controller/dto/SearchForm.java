package local.redcare.controller.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import local.redcare.domain.github.SearchRequest;
import lombok.Data;

import java.time.LocalDate;


@Data
public class SearchForm {

    @NotBlank(message = "q is required")
    @Size(max=255, message = "q is too long, must be shorter than 255 characters long")
    @Pattern(regexp = "[^:]*", message = "q must not contain ':'")
    private String q;

    @PastOrPresent(message = "since must be in the past or today")
    private LocalDate since;

    @Size(min = 1, message = "lang must has at least 1 character")
    private String lang;

    @Positive(message = "page must be greater than zero")
    private int page = 1;

    @Min(value = 1, message = "limit must be at least 1")
    @Max(value = 30, message = "limit must be at most 30")
    private int limit = 7;


    public SearchRequest toRequest() {
        return new SearchRequest(q.strip(), since, lang == null ? lang : lang.strip(), page, limit);
    }
}
