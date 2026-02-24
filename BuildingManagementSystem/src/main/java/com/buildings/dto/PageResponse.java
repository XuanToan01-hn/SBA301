package com.buildings.dto;

import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    @Builder.Default
    private List<T> content = Collections.emptyList();

    private int page;

    private int size;

    private long totalElements;

    private int totalPages;

    // Backward compatibility
    public List<T> getData() {
        return content;
    }

    public long getTotal() {
        return totalElements;
    }
}
