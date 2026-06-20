package local.redcare.service;

import local.redcare.github.GitHubSearchEntry;

import java.math.BigDecimal;


public interface ScoreService {

    BigDecimal score(GitHubSearchEntry entry);

}
