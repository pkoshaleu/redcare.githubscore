package local.redcare.service.github;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import local.redcare.support.PermitHandler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class QuotaInterceptorTest {

    private QuotaGate gate;
    private ClientHttpRequestExecution execution;
    private ClientHttpResponse response;
    private PermitHandler.Permit permit;
    private HttpRequest request;
    private final byte[] body = new byte[0];

    private QuotaInterceptor interceptor;

    @BeforeEach
    void setUp() {
        gate = mock(QuotaGate.class);
        execution = mock(ClientHttpRequestExecution.class);
        response = mock(ClientHttpResponse.class);
        permit = mock(PermitHandler.Permit.class);
        request = mock(HttpRequest.class);
        interceptor = new QuotaInterceptor(gate);
    }

    @ParameterizedTest(name = "[{index}] limit={0}, used={1} is quota({2})")
    @CsvSource(nullValues = "None", value = {
            "1000, 750, 750",
            "1000, 1, 1",
            "1000, 1000, 1000",
            "3, 1, 333",
            "None, 750, 500",
            "1000, None, 500",
            "None, None, 500",
            "abc, 750, 500",
            "1000, def, 500",
    })
    void quota_computed_andUpdatesGate(String limit, String used, long expected) throws Exception {

        HttpHeaders headers = headersOf(limit, used);

        when(gate.enter()).thenReturn(permit);
        when(execution.execute(request, body)).thenReturn(response);
        when(response.getHeaders()).thenReturn(headers);

        interceptor.intercept(request, body, execution);

        verify(gate).updateQuota(expected);
    }

    private static HttpHeaders headersOf(String limit, String used) {
        HttpHeaders headers = new HttpHeaders();
        if (limit != null) {
            headers.add("x-ratelimit-limit", limit);
        }
        if (used != null) {
            headers.add("x-ratelimit-used", used);
        }

        return headers;
    }

}
