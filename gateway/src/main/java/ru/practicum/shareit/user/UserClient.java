package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.List;

@Component
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

    public UserDto addUser(UserDto userDto) {
        return post("", userDto, UserDto.class);
    }

    public UserDto updateUser(long userId, UserUpdateDto userUpdateDto) {
        return patch("/" + userId, userUpdateDto, UserDto.class);
    }

    public UserDto findUserById(long userId) {
        return get("/" + userId, UserDto.class);
    }

    public List<UserDto> findAllUsers() {
        return get("", List.class);
    }

    public void deleteUserById(long userId) {
        delete("/" + userId);
    }
}
