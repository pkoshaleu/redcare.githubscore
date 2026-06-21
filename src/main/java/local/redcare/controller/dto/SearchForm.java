package local.redcare.controller.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import local.redcare.domain.SearchRequest;
import local.redcare.domain.github.GitHubQuery;

import java.time.LocalDate;


@Data
public class SearchForm {

    @NotBlank(message = "q is required")
    @Pattern(regexp = "[^:]*", message = "q must not contain ':'")
    private String q;

    @PastOrPresent(message = "since must be in the past or today")
    private LocalDate since;

    @Size(min = 1, message = "lang must has at least 1 character")
    private String lang;

    @Min(value = 1, message = "page must be at least 1")
    @Max(value = 10, message = "page must be at most 10")
    private int page = 1;

    @Min(value = 1, message = "limit must be at least 1")
    @Max(value = 30, message = "limit must be at most 30")
    private int limit = 7;

    @AssertTrue(message = "q is too long, must be shorter than 250 characters with lang and since applied")
    public boolean isItWithinLimit() {
        if (q == null) {
            return true;
        }
        return GitHubQuery.of(toRequest()).length() <= GitHubQuery.MAX_LENGTH;
    }

    public SearchRequest toRequest() {
        return new SearchRequest(q.strip(), since, lang == null ? null : lang.strip());
    }

}
