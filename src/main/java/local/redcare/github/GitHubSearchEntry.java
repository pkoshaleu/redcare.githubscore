package local.redcare.github;

import lombok.Data;

import java.time.ZonedDateTime;


@Data
public class GitHubSearchEntry {

    private Long id;
    private String name;
    private String htmlUrl;
    private ZonedDateTime pushedAt;
    private Integer forksCount;
    private Integer stargazersCount;

}
