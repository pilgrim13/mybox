package com.mybox.mybox.user.repository;

import com.mybox.mybox.user.domain.entity.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<User, String> {

//    Mono<User> findByUsername(String username);

    Mono<UserDetails> findByUsername(String username);

}