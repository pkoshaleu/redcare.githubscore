package local.redcare.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import local.redcare.domain.SearchRequest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;


public class GitHubServiceTest {

    @ParameterizedTest(name = "[{index}] lang={1}, since={2} is {3}")
    @CsvSource(nullValues = "None", value = {
            "awesome, None, None,       awesome",
            "awesome, java, None,       awesome lang:java",
            "awesome, None, 2026-01-01, awesome created:>2026-01-01",
            "awesome, java, 2026-01-01, awesome lang:java created:>2026-01-01",
    })
    void toQuery_givenRequest_buildsQuery(String q, String lang, LocalDate since, String expected) {
        SearchRequest request = new SearchRequest(q, since, lang);

        String result = GitHubService.toQuery(request);

        assertThat(result).isEqualTo(expected);
    }


}
