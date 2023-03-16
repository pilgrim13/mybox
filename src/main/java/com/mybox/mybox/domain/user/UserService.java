package com.mybox.mybox.domain.user;

import com.mybox.mybox.domain.user.entity.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {

    Mono<User> addUser(User user);

    Flux<User> getAllUsers();

    Mono<User> getUser(Mono<String> userId);

}
