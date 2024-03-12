package ru.practicum.shareit.shared;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class OffsetPageRequest implements Pageable {

    private final long offset;
    private final int size;

    private final Sort sort;

    private OffsetPageRequest(long offset, int size, Sort sort) {
        validateOffsetAndSize(offset, size);
        this.offset = offset;
        this.size = size;
        this.sort = sort;
    }

    private OffsetPageRequest(long offset, int size) {
        this.offset = offset;
        this.size = size;
        sort = Sort.unsorted();
    }

    public static OffsetPageRequest of(long offset, int size, Sort sort) {
        return new OffsetPageRequest(offset, size, sort);
    }

    public static OffsetPageRequest of(long offset, int size) {
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

    private void validateOffsetAndSize(long offset, int size) {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset must can not be negative!");
        }
        if (size < 0) {
            throw new IllegalArgumentException("Page size must can not be negative!");
        }
    }
}
