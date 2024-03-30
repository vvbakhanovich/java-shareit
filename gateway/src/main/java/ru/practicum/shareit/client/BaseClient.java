package ru.practicum.shareit.client;

import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

public class BaseClient {
    protected final RestTemplate rest;

    public BaseClient(RestTemplate rest) {
        this.rest = rest;
    }

    protected <S> S get(String path, Class<S> returnType) {
        return get(path, null, null, returnType);
    }

    protected <S> S get(String path, long userId, Class<S> returnType) {
        return get(path, userId, null, returnType);
    }

    protected <S> S get(String path, Long userId, @Nullable Map<String, Object> parameters,
                        Class<S> returnType) {
        return makeAndSendRequest(HttpMethod.GET, path, userId, parameters, null, returnType);
    }

    protected <T, S> S post(String path, T body, Class<S> returnType) {
        return post(path, null, null, body, returnType);
    }

    protected <T, S> S post(String path, long userId, T body, Class<S> returnType) {
        return post(path, userId, null, body, returnType);
    }

    protected <T, S> S post(String path, Long userId, @Nullable Map<String, Object> parameters, T body,
                            Class<S> returnType) {
        return makeAndSendRequest(HttpMethod.POST, path, userId, parameters, body, returnType);
    }

    protected <T, S> S put(String path, long userId, T body, Class<S> returnType) {
        return put(path, userId, null, body, returnType);
    }

    protected <T, S> S put(String path, long userId, @Nullable Map<String, Object> parameters, T body,
                           Class<S> returnType) {
        return makeAndSendRequest(HttpMethod.PUT, path, userId, parameters, body, returnType);
    }

    protected <T, S> S patch(String path, T body, Class<S> returnType) {
        return patch(path, null, null, body, returnType);
    }

    protected <S> S patch(String path, long userId, Class<S> returnType) {
        return patch(path, userId, null, null, returnType);
    }

    protected <T, S> S patch(String path, long userId, T body, Class<S> returnType) {
        return patch(path, userId, null, body, returnType);
    }

    protected <T, S> S patch(String path, Long userId, @Nullable Map<String, Object> parameters, T body,
                             Class<S> returnType) {
        return makeAndSendRequest(HttpMethod.PATCH, path, userId, parameters, body, returnType);
    }

    protected void delete(String path) {
        delete(path, null, null);
    }

    protected void delete(String path, long userId) {
        delete(path, userId, null);
    }

    protected void delete(String path, Long userId, @Nullable Map<String, Object> parameters) {
        makeAndSendRequest(HttpMethod.DELETE, path, userId, parameters, null, Void.class);
    }

    private <T, S> S makeAndSendRequest(HttpMethod method, String path, Long userId,
                                        @Nullable Map<String, Object> parameters, @Nullable T body, Class<S> returnType) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body, defaultHeaders(userId));

        ResponseEntity<S> shareitServerResponse;

        if (parameters != null) {
            shareitServerResponse = rest.exchange(path, method, requestEntity, returnType, parameters);
        } else {
            shareitServerResponse = rest.exchange(path, method, requestEntity, returnType);
        }
        return shareitServerResponse.getBody();
    }

    private HttpHeaders defaultHeaders(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (userId != null) {
            headers.set("X-Sharer-User-Id", String.valueOf(userId));
        }
        return headers;
    }
}
