package local.redcare.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import local.redcare.service.GitHubService;
import local.redcare.service.ScoreService;
import local.redcare.service.SearchHandler;
import local.redcare.service.handler.CachedSearchHandler;
import local.redcare.service.handler.DirectSearchHandler;


@Configuration
public class AppConfig {

    @Bean("handler.direct-search")
    public SearchHandler handlerDirect(GitHubService gitHubService, ScoreService scoreService) {
        return new DirectSearchHandler(gitHubService, scoreService);
    }

    @Primary
    @Bean("handler.cached-search")
    public SearchHandler handlerCached(
            @Qualifier("handler.direct-search") SearchHandler handlerDirect
    ) {
        return new CachedSearchHandler(handlerDirect);
    }

}
