package com.tcc.dtos.cursors;

import java.util.List;

public record CursorPageResponse<T>(
        List<T> items,
        String nextCursor,
        String prevCursor,
        boolean hasNext,
        boolean hasPrev
) {
}
