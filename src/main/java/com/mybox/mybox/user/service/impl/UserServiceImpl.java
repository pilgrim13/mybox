package com.mybox.mybox.user.service.impl;

import com.mybox.mybox.user.domain.dto.UserRequestDto;
import com.mybox.mybox.user.domain.entity.User;
import com.mybox.mybox.user.repository.UserRepository;
import com.mybox.mybox.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Mono<User> addUser(UserRequestDto requestDto) {
        User user = User.builder()
            .username(requestDto.getUsername())
            .password(passwordEncoder.encode(requestDto.getPassword()))
            .nickname(requestDto.getNickname())
            .build();
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