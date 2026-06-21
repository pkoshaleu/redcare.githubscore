package local.redcare.support;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

public class MergingExecutor<K, V> {

    private final ConcurrentMap<K, CompletableFuture<V>> tasks = new ConcurrentHashMap<>();

    public V execute(K key, Supplier<V> loader) {
        CompletableFuture<V> task = new CompletableFuture<>();

        CompletableFuture<V> running = tasks.putIfAbsent(key, task);
        if (running != null) {
            return running.join();
        }

        try {
            V value = loader.get();
            task.complete(value);
            return value;
        } catch (RuntimeException | Error e) {
            task.completeExceptionally(e);
            throw e;
        } finally {
            tasks.remove(key, task);
        }
    }

}
