package local.redcare.service.github;

import local.redcare.service.TimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.assertj.core.api.Assertions.assertThat;


public class LockingInterceptorTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant REQUEST_AT = NOW.plusSeconds(60);
    private static final Instant RESET_AT = NOW.plusSeconds(360);

    private TimeService timeService;
    private ClientHttpRequestExecution execution;
    private ClientHttpResponse response;
    private HttpRequest request;
    private final byte[] body = new byte[0];

    private LockingInterceptor interceptor;

    @BeforeEach
    void setUp() {
        timeService = mock(TimeService.class);
        when(timeService.now()).thenReturn(NOW, REQUEST_AT);

        execution = mock(ClientHttpRequestExecution.class);
        response = mock(ClientHttpResponse.class);
        request = mock(HttpRequest.class);

        interceptor = new LockingInterceptor(timeService);
        interceptor.init();                         // seeds unblockAt = NOW
    }


    @ParameterizedTest(name = "[{index}] remaining={0} is blocked={1}")
    @CsvSource(nullValues = "None", value = {
            "None,false",
            "abc, false",
            "100, false",
            "2, false",
            "1, true",
            "0, true",
    })
    void quota_computed_andLocked(String remaining, boolean blocked) throws Exception {
        HttpHeaders headers = headersOf(remaining, "" + RESET_AT.toEpochMilli() / 1000);

        when(execution.execute(request, body)).thenReturn(response);
        when(response.getHeaders()).thenReturn(headers);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);

        interceptor.intercept(request, body, execution);

        Instant expected = blocked ? RESET_AT : NOW;
        assertThat(interceptor.getUnblockAt()).isEqualTo(expected);
    }

    private static HttpHeaders headersOf(String remaining, String resetAt) {
        HttpHeaders headers = new HttpHeaders();
        if (remaining != null) {
            headers.add("x-ratelimit-remaining", remaining);
        }
        if (resetAt != null) {
            headers.add("x-ratelimit-reset", resetAt);
        }

        return headers;
    }

}
