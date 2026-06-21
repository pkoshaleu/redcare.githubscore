package local.redcare.domain.github;

import lombok.Data;

import java.util.List;


@Data
public class GitHubPage<T> {

    private final List<T> items;

}