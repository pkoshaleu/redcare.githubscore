package local.redcare.service.handler;

import lombok.AllArgsConstructor;

import local.redcare.domain.ScoredEntry;
import local.redcare.domain.SearchRequest;
import local.redcare.service.SearchHandler;
import local.redcare.support.Cache;
import local.redcare.support.MergingExecutor;

import java.util.List;
import java.util.Optional;


@AllArgsConstructor
public class CachedSearchHandler implements SearchHandler {

    private final Cache<String, List<ScoredEntry>> cache = new Cache<>(100);
    private final MergingExecutor<String, List<ScoredEntry>> executor = new MergingExecutor<>();

    private final SearchHandler delegate;

    @Override
    public List<ScoredEntry> invoke(SearchRequest request) {
        String key = request.getKey();
        Optional<List<ScoredEntry>> cached = cache.lookUp(key);

        return cached.orElseGet(
                () -> executor.execute(key, () -> {
                    Optional<List<ScoredEntry>> innerCached = cache.lookUp(key);
                    return innerCached.orElseGet(
                            () -> {
                                List<ScoredEntry> entries = delegate.invoke(request);
                                cache.put(key, entries);
                                return entries;
                            }
                    );
                })
        );
    }
}
