package com.mybox.mybox.user.service;

import com.mybox.mybox.user.domain.dto.UserRequestDto;
import com.mybox.mybox.user.domain.entity.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {

    Mono<User> addUser(UserRequestDto requestDto);

    Flux<User> getAllUsers();

    Mono<User> getUser(Mono<String> userId);

}
