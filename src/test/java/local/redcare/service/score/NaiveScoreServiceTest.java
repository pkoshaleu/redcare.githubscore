package local.redcare.service.score;

import local.redcare.domain.github.GitHubSearchEntry;
import local.redcare.service.TimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class NaiveScoreServiceTest {

    private static final Instant NOW = Instant.parse("2026-02-01T00:00:00Z");
    private static final ZonedDateTime PUSH_30D = ZonedDateTime.parse("2026-01-15T00:00:00Z");

    private TimeService timeService;
    private NaiveScoreService service;

    @BeforeEach
    void setUp() {
        timeService = mock(TimeService.class);
        when(timeService.now()).thenReturn(NOW);
        service = new NaiveScoreService(timeService);
    }

    @ParameterizedTest(name = "[{index}] stars={0}, forks={1} -> {2}")
    @CsvSource(nullValues = "None", value = {
            "0, 0, 0.00",
            "999, 0, 2.25",
            "0, 999, 0.75",
            "999, 99, 2.75",
            "9, 9, 1.00",
            "99, 99, 2.00",
            "None, 99, 0.50",
            "999, None, 2.25",
            "None, None, 0.00",
    })
    void score_givenStarsAndForks_returnScore(Integer stars, Integer forks, BigDecimal expected) {
        BigDecimal result = service.score(entry(stars, forks, PUSH_30D));

        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest(name = "[{index}] days={0}")
    @CsvSource(nullValues = "None", value = {
            "0, 2.75",
            "30, 2.75",
            "60, 2.55",
            "335, 0.75",
            "365, 0.55",
            "730, 0.55"
    })
    void score_givenPush_returnScore(Integer days, BigDecimal expected) {
        ZonedDateTime dt = NOW.minus(days, ChronoUnit.DAYS).atZone(ZoneOffset.UTC);
        BigDecimal result = service.score(entry(999, 99, dt));

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void score_givenNoPushDt_returnScore() {
        BigDecimal result = service.score(entry(999, 99, null));
        assertThat(result).isEqualTo(BigDecimal.valueOf(55, 2));
    }

    private static GitHubSearchEntry entry(Integer stars, Integer forks, ZonedDateTime pushedAt) {
        GitHubSearchEntry entry = new GitHubSearchEntry();
        entry.setStargazersCount(stars);
        entry.setForksCount(forks);
        entry.setPushedAt(pushedAt);
        return entry;
    }

}
