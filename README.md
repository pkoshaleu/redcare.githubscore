# Popularity Score Assignment

## Goal

Develop a scoring algorithm that assigns a popularity score to each GitHub repository.

The score should be based on data available directly from the GitHub repository search response. 
The main contributing factors are:

- number of stars;
- number of forks;
- recency of the latest code update.

## Analysis

GitHub contains 300+ millions of public repositories (https://gitcharts.com/). Because of GitHub API
primary and secondary rate limits, crawling even a small percentage of all public repositories
is not realistic within the assignment timeframe.

It means the scoring algorithm should rely only on repository data available immediately from 
the search response, without additional per-repository API calls.

Stars and forks are both popularity signals, but they are not equal.

Stars usually represent interest or act of bookmarking. Forks usually represent intension to contribute
or reuse. For that reason, stars should have a higher weight than forks.

Both stars and forks follow a heavy-tailed distribution (https://gitcharts.com/). A repository
with 100,000 stars is more popular than a repository with 10,000 stars, but it should not be 
considered exactly 10 times more popular. To compress large differences, the score uses log10(x).

### Baseline Score

Assuming 75:25 split the baseline popularity score is calculated as:

```
baseline = 0.75 * log10(stars + 1) + 0.25 * log10(forks + 1)
```

### Recency Score

GitHub provides two possible timestamps for estimating repository recency:

- `pushed_at`: time of the latest push operation;
- `updated_at`: time of the latest repository update.

The scoring algorithm uses `pushed_at`, because it is more specific to codebase freshness. 
The `updated_at` field can change because of more generic repository activity ie ticket
or wiki update.

Repositories pushed within the last 30 days are considered fresh and receive no freshness penalty.

Repositories older than 30 days receive a linear penalty. The multiplier decreases 
from 1.0 at 30 days to 0.2 at 365 days. Repositories older than 365 days keep 
the minimum multiplier of 0.2.

Penalty within 30 and 365 days can be calculated as:

```
multiplier = 1.0 - ((push_dt - 30) / 335.0) * 0.8
```

### Final Score

The final popularity score is calculated as:

```
score = baseline * multiplier
```

This approach gives higher scores to repositories that are popular and recently maintained, 
while still allowing stable but older repositories to retain part of their popularity score.

## Decisions
- Build a REST API to provide access to repository popularity scores.
- Calculate scores based on live responses from the GitHub API, without
long-term storage or preliminary crawling.
- Use in-memory response caching and pagination.
- Focus on GitHub API usage, including a flexible execution model depending on current API rate-limit allowances.
- Focus on preventing concurrent cache updates.

## Building and Execution

### Prerequisites
 - Java 25.x
 - Maven 3.9.x

### Start it 

With Maven:

```
mvn spring-boot:run
```
Alternatively, use the Jib plugin to build a Docker image.

### API Usage

Example request (with `httpie`):
```
http localhost:8080/api/repos/search?q=awesome&page=1&limit=10&since=2025-01-01&lang=python
```

The API searches GitHub repositories and returns repositories enriched with calculated popularity scores:

```
{
  "entries": [
    {
      "forks": 172,
      "repo_id": 1000784797,
      "score": 2.08,
      "stars": 789,
      "updated_at": "2026-02-10T23:20:36Z",
      "url": "https://github.com/rohitg00/awesome-ai-apps"
    },
      ...
  ]
}
```

## Known Issues
- Magic numbers are still present in `QuotaGate`.
- The task cache in `MergingExecutor` is currently unbounded.
- `GitHubSearchEntry` has some unnecessary dependencies in the graph.
- Cache invalidation strategy is still basic.
- Pagination strategy is intentionally limited.
- Only 100 candidates are used for scoring.

## What to Do Next
- Load a reasonable number of additional pages during scoring to improve result quality.
- Implement a more sophisticated cache with size limits, TTL, and eviction policy (or go with external cache).
- Search and destroy all magic numbers.
- Add metrics for GitHub API usage, cache hits, cache misses, and throttling events.
- Reasonable bound task cache.
- More integration tests.
