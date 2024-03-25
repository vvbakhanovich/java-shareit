package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

@Service
public class UserClient extends BaseClient {

    private static final String API_PREFIX = "/users";

    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> addUser(UserDto userDto) {
        return post("", userDto);
    }

    public ResponseEntity<Object> updateUser(long userId, UserUpdateDto userUpdateDto) {
        return patch("/" + userId, userUpdateDto);
    }

    public ResponseEntity<Object> findUserById(long userId) {
        return get("/" + userId);
    }

    public ResponseEntity<Object> findAllUsers() {
        return get("");
    }

    public ResponseEntity<Object> deleteUserById(long userId) {
        return delete("/" + userId);
    }
}