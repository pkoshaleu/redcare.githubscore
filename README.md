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
