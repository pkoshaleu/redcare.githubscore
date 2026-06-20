package local.redcare.service;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;


@Component
public class TimeService {

    private final Clock clock = Clock.systemUTC();

    public Instant now() {
        return clock.instant();
    }

}