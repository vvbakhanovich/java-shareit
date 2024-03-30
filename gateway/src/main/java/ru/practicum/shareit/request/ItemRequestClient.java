package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.AddItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;
import java.util.Map;

@Service
public class ItemRequestClient extends BaseClient {

    private static final String API_PREFIX = "/requests";

    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ItemRequestDto addNewItemRequest(Long userId, AddItemRequestDto addItemRequestDto) {
        return post("", userId, addItemRequestDto, ItemRequestDto.class);
    }

    public List<ItemRequestDto> getAllItemRequestsFromUser(Long userId) {
        return get("", userId, List.class);
    }

    public List<ItemRequestDto> getAvailableItemRequests(Long userId, Long from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("/all?from={from}&size={size}", userId, parameters, List.class);
    }

    public ItemRequestDto getItemRequestById(Long userId, Long requestId) {
        return get("/" + requestId, userId, ItemRequestDto.class);
    }
}
