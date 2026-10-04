package com.guruge.hardware.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PaginationUtils {

    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_PAGE_SIZE = 20;

    private PaginationUtils() {
    }

    public static Pageable of(Integer page, Integer size) {
        return of(page, size, Sort.unsorted());
    }

    public static Pageable of(Integer page, Integer size, Sort sort) {
        int p = (page == null || page < 0) ? 0 : page;
        int s = (size == null || size <= 0) ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        return PageRequest.of(p, s, sort == null ? Sort.unsorted() : sort);
    }

    public static Pageable of(Integer page, Integer size, String sortBy, String direction) {
        Sort sort = Sort.unsorted();
        if (sortBy != null && !sortBy.isBlank()) {
            Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
            sort = Sort.by(dir, sortBy);
        }
        return of(page, size, sort);
    }
}
