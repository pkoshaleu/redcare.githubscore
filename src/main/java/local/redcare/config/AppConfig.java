package local.redcare.config;

import local.redcare.service.TimeService;
import local.redcare.service.github.LockingInterceptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class AppConfig {

    @Bean("github.mapper")
    public JsonMapper githubMapper() {
        return JsonMapper.builder()
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();
    }

    @Bean("github.lock")
    public LockingInterceptor githubLock(TimeService timeService) {
        return new LockingInterceptor(timeService);
    }

    @Bean("github.client")
    public RestClient githubClient(
            GitHubProps props,
            @Qualifier("github.mapper") JsonMapper githubMapper,
            @Qualifier("github.lock") LockingInterceptor lockInterceptor
    ) {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://api.github.com")
                .requestFactory(new JdkClientHttpRequestFactory())
                .defaultHeader("User-Agent", props.ua())
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2026-03-10")
                .requestInterceptor(lockInterceptor)
                .configureMessageConverters(converters ->
                        converters.withJsonConverter(new JacksonJsonHttpMessageConverter(githubMapper)));

        if (!props.token().isBlank()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.token());
        }

        return builder.build();
    }

}
