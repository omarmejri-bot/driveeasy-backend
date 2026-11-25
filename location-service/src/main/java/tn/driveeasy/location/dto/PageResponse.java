package tn.driveeasy.location.dto;

import java.util.List;

/** Enveloppe générique de pagination pour GET /locations. */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {
}
