package ru.practicum.shareit.shared;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OffsetPageRequestTest {

    @Test
    void sizeAndOffsetReturnParametersShouldBeEqualToConstructorArguments() {
        Long offset = 1L;
        Integer size = 4;
        OffsetPageRequest offsetPageRequest = OffsetPageRequest.of(offset, size);

        assertThat(offsetPageRequest.getOffset(), is(offset));
        assertThat(offsetPageRequest.getPageSize(), is(size));
    }

    @Test
    void whenOffsetNull_ShouldThrowIllegalArgumentException() {
        Long offset = null;
        Integer size = 4;

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> OffsetPageRequest.of(offset, size));
        assertThat(e.getMessage(), is("Offset must be positive or zero!"));
    }

    @Test
    void whenSizeNull_ShouldThrowIllegalArgumentException() {
        Long offset = 1L;
        Integer size = null;

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> OffsetPageRequest.of(offset, size));
        assertThat(e.getMessage(), is("Page size must be positive!"));
    }

    @Test
    void defaultSortShouldBeUnsorted() {
        Long offset = 1L;
        Integer size = 4;
        OffsetPageRequest offsetPageRequest = OffsetPageRequest.of(offset, size);

        assertThat(offsetPageRequest.getSort(), is(Sort.unsorted()));
    }

    @Test
    void next_ShouldReturnOffsetToBeOffsetPlusSize() {
        Long offset = 1L;
        Integer size = 4;
        OffsetPageRequest offsetPageRequest = OffsetPageRequest.of(offset, size);
        OffsetPageRequest next = (OffsetPageRequest) offsetPageRequest.next();

        assertThat(next.getOffset(), is(offset + size));
        assertThat(next.getPageSize(), is( size));
    }

    @Test
    void hasPrevious_ShouldReturnTrueIfOffsetBiggerThanSize() {
        Long offset = 6L;
        Integer size = 4;
        OffsetPageRequest offsetPageRequest = OffsetPageRequest.of(offset, size);

        assertThat(offsetPageRequest.hasPrevious(),  is(true));
    }

    @Test
    void hasPrevious_ShouldReturnTrueIfOffsetLessThanSize() {
        Long offset = 1L;
        Integer size = 4;
        OffsetPageRequest offsetPageRequest = OffsetPageRequest.of(offset, size);

        assertThat(offsetPageRequest.hasPrevious(),  is(false));
    }

    @Test
    void withPage_ShouldReturnRequestWithPageOffset() {
        Long offset = 1L;
        Integer size = 4;
        OffsetPageRequest offsetPageRequest = OffsetPageRequest.of(offset, size);
        OffsetPageRequest withPage = (OffsetPageRequest) offsetPageRequest.withPage(4);

        assertThat(withPage.getPageNumber(), is(4));

        OffsetPageRequest first = (OffsetPageRequest) withPage.first();

        assertThat(first.getOffset(), is(offset));
        assertThat(first.getPageSize(), is(size));
    }

    @Test
    void previousOrFirst_WhenOffsetGreaterThanSize_ShouldReturnPrevious() {
        Long offset = 5L;
        Integer size = 4;
        OffsetPageRequest offsetPageRequest = OffsetPageRequest.of(offset, size);
        OffsetPageRequest previousOrFirst = (OffsetPageRequest) offsetPageRequest.previousOrFirst();

        assertThat(previousOrFirst.getOffset(), is(1L));
    }

    @Test
    void previousOrFirst_WhenOffsetLessThanSize_ShouldReturnFirst() {
        Long offset = 2L;
        Integer size = 4;
        OffsetPageRequest offsetPageRequest = OffsetPageRequest.of(offset, size);
        OffsetPageRequest previousOrFirst = (OffsetPageRequest) offsetPageRequest.previousOrFirst();

        assertThat(previousOrFirst.getOffset(), is(2L));
    }

}