package com.mybox.mybox.domain.user.impl;

import com.mybox.mybox.domain.user.UserRepository;
import com.mybox.mybox.domain.user.UserService;
import com.mybox.mybox.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public Mono<User> addUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public Flux<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Mono<User> getUser(Mono<String> userId) {
        return userRepository.findById(userId);
    }
}