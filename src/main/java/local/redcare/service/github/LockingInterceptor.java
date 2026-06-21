package local.redcare.service.github;

import jakarta.annotation.PostConstruct;
import local.redcare.service.TimeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import static local.redcare.service.github.ParsingHelper.parseLong;


@Slf4j
@AllArgsConstructor
public class LockingInterceptor implements ClientHttpRequestInterceptor {

    private static final String HEADER_REMAINING = "x-ratelimit-remaining";
    private static final String HEADER_RESET_AT = "x-ratelimit-reset";
    private static final String HEADER_RESOURCE = "x-ratelimit-resource";
    private static final String HEADER_RETRY_AFTER = "retry-after";

    private final AtomicReference<Instant> unblockAt = new AtomicReference<>();

    private final TimeService timeService;

    @PostConstruct
    public void init() {
        unblockAt.set(timeService.now());
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request,
                                        byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        failIfBlocked();
        ClientHttpResponse response = execution.execute(request, body);

        HttpHeaders headers = response.getHeaders();
        handleApiExhaustion(headers);

        int code = response.getStatusCode().value();
        if (code == 403 || code == 429) {
            handleApiRateLimit(headers);
        }

        return response;
    }

    private void failIfBlocked() {
        Instant now = timeService.now();
        if (now.isBefore(unblockAt.get())) {
            //TODO: own exception here
            throw new RuntimeException("External API is blocked");
        }
    }

    private void handleApiExhaustion(HttpHeaders headers) {
        Long remaining = parseLong(headers.getFirst(HEADER_REMAINING));
        if (log.isDebugEnabled()) {
            String resource = headers.getFirst(HEADER_RESOURCE);
            log.debug("Remaining api usage; {}={}", resource, remaining);
        }

        if (remaining != null && remaining <= 1) {
            Long resetAt = parseLong(headers.getFirst(HEADER_RESET_AT));
            if (resetAt != null) {
                blockUntil(Instant.ofEpochSecond(resetAt));
            }
        }
    }

    private void handleApiRateLimit(HttpHeaders headers) {
        Long after = parseLong(headers.getFirst(HEADER_RETRY_AFTER));
        after = after == null ? 60 : after;
        blockUntil(timeService.now().plusSeconds(after));

        throw new RuntimeException("Block on github");
    }

    private void blockUntil(Instant moment) {
        unblockAt.updateAndGet(current ->
                current == null || current.isBefore(moment) ? moment : current
        );
    }

}
