package ru.practicum.shareit.shared;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class OffsetPageRequest implements Pageable {

    private final Long offset;
    private final Integer size;
    private final Sort sort;

    private OffsetPageRequest(Long offset, Integer size, Sort sort) {
        validateOffsetAndSize(offset, size);
        this.offset = offset;
        this.size = size;
        this.sort = sort;
    }

    private OffsetPageRequest(Long offset, Integer size) {
        validateOffsetAndSize(offset, size);
        this.offset = offset;
        this.size = size;
        sort = Sort.unsorted();
    }

    public static OffsetPageRequest of(Long offset, Integer size, Sort sort) {
        return new OffsetPageRequest(offset, size, sort);
    }

    public static OffsetPageRequest of(Long offset, Integer size) {
        return new OffsetPageRequest(offset, size);
    }

    @Override
    public int getPageNumber() {
        return (int) (offset / size);
    }

    @Override
    public int getPageSize() {
        return size;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        return new OffsetPageRequest(offset + size, size);
    }

    @Override
    public Pageable previousOrFirst() {
        return hasPrevious() ? of(offset - size, size) : first();
    }

    @Override
    public Pageable first() {
        return new OffsetPageRequest(offset, size);
    }

    @Override
    public Pageable withPage(int pageNumber) {
        return null;
    }

    @Override
    public boolean hasPrevious() {
        return offset - size < 0;
    }

    private void validateOffsetAndSize(Long offset, Integer size) {
        if (offset == null || offset < 0) {
            throw new IllegalArgumentException("Offset must be positive!");
        }
        if (size == null || size < 0) {
            throw new IllegalArgumentException("Page size must be positive or zero!");
        }
    }
}
