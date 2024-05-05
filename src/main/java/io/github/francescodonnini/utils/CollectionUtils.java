package io.github.francescodonnini.utils;

import java.util.List;
import java.util.function.ToIntFunction;

public class CollectionUtils {
    private CollectionUtils() {}

    public static <T> T binarySearch(List<T> releases, ToIntFunction<T> comparator) {
        var low = 0;
        var mid = -1;
        var high = releases.size() - 1;
        while (low <= high) {
            mid = low + high >>> 1;
            var midVal = releases.get(mid);
            var cmp = comparator.applyAsInt(midVal);
            if (cmp < 0) {
                low = mid + 1;
            } else {
                if (cmp == 0) {
                    break;
                }
                high = mid - 1;
            }
        }
        return releases.get(mid);
    }
}
