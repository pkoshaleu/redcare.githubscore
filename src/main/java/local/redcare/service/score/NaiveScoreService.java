package local.redcare.service.score;

import local.redcare.domain.github.GitHubSearchEntry;
import local.redcare.service.ScoreService;
import local.redcare.service.TimeService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;


@Component
@AllArgsConstructor
public class NaiveScoreService implements ScoreService {

    private static final double STARS_WEIGHT = 0.75;
    private static final double FORKS_WEIGHT = 0.25;

    private static final double MIN_SCORE = 0.2;
    private static final double MAX_SCORE = 1.0;
    private static final long PUSH_GRACE_PERIOD = 30;
    private static final long STALE_PERIOD = 365;

    private final TimeService timeService;

    @Override
    public BigDecimal score(GitHubSearchEntry entry) {
        return BigDecimal.valueOf(
                calculateBaseline(entry) * calculateMultiplier(entry)
        ).setScale(2, RoundingMode.HALF_UP) ;
    }

    private double calculateBaseline(GitHubSearchEntry entry) {
        int stars = getValue(entry.getStargazersCount(), 0);
        int forks = getValue(entry.getForksCount(), 0);

        return STARS_WEIGHT * Math.log10(stars + 1)
                + FORKS_WEIGHT * Math.log10(forks + 1);
    }

    private double calculateMultiplier(GitHubSearchEntry entry) {
        if (entry.getPushedAt() == null) {
            return MIN_SCORE;
        }

        long days = Duration.between(
                entry.getPushedAt().toInstant(),
                timeService.now()
        ).toDays();

        if (PUSH_GRACE_PERIOD > days) {
            return MAX_SCORE;
        } else if (days > STALE_PERIOD) {
            return MIN_SCORE;
        }

        days = Math.max(0, days - PUSH_GRACE_PERIOD);

        return 1.0 - (days / 335.0) * (1 - MIN_SCORE);
    }

    private static <T> T getValue(T value, T replace) {
        return value == null ? replace : value;
    }

}
