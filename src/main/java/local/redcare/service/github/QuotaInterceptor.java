package local.redcare.service.github;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import local.redcare.support.PermitHandler;

import java.io.IOException;

import static local.redcare.support.ParsingHelper.parseLong;


@Slf4j
@AllArgsConstructor
public class QuotaInterceptor implements ClientHttpRequestInterceptor {

    private static final String HEADER_QUOTA_LIMIT = "x-ratelimit-limit";
    private static final String HEADER_QUOTA_USED = "x-ratelimit-used";

    private final QuotaGate gate;

    @Override
    public ClientHttpResponse intercept(HttpRequest request,
                                        byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        try (PermitHandler.Permit _ = gate.enter()) {
            ClientHttpResponse response = execution.execute(request, body);
            updateQuota(response.getHeaders());

            return response;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new QuotaGateException("Interruption at quota gate", ex);
        }
    }

    private void updateQuota(HttpHeaders headers) {
        Long used = parseLong(headers.getFirst(HEADER_QUOTA_USED));
        Long limit = parseLong(headers.getFirst(HEADER_QUOTA_LIMIT));

        gate.observe(used, limit);
    }

}
