package com.mybox.mybox.user.application;

import com.mybox.mybox.user.domain.dto.UserRequestDto;
import com.mybox.mybox.user.domain.entity.User;
import com.mybox.mybox.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class UserHandler {

    private final UserService userService;

    public Mono<ServerResponse> addUser(ServerRequest request) {
        return request.bodyToMono(UserRequestDto.class)
            .flatMap(userService::addUser)
            .flatMap(user -> ServerResponse.created(URI.create("/users/" + user.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(user)));
    }

    public Mono<ServerResponse> getAllUserList() {
        Flux<User> userList = this.userService.getAllUsers();
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(userList, User.class);
    }

    public Mono<ServerResponse> getUser(ServerRequest request) {
        return null;
    }

}