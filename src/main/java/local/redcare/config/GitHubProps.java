package local.redcare.config;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "github")
public record GitHubProps(
        String token,
        String ua
) {
}
