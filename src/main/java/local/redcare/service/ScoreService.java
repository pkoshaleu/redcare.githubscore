package local.redcare.service;

import local.redcare.domain.github.GitHubSearchEntry;

import java.math.BigDecimal;


public interface ScoreService {

    BigDecimal score(GitHubSearchEntry entry);

}
