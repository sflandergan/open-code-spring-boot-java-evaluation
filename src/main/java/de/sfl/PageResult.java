package de.sfl;

import java.util.List;

public record PageResult<T>(List<T> items, boolean hasMore) {
}
