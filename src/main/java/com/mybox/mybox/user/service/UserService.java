package com.mybox.mybox.user.service;

import com.mybox.mybox.user.domain.dto.UserRequestDto;
import com.mybox.mybox.user.domain.entity.User;
import com.mybox.mybox.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Mono<User> addUser(UserRequestDto requestDto) {
        User user = User.builder()
            .username(requestDto.getUsername())
            .password(passwordEncoder.encode(requestDto.getPassword()))
            .nickname(requestDto.getNickname())
            .build();
        return userRepository.save(user);
    }

    public Flux<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Mono<User> getUser(Mono<String> userId) {
        return userRepository.findById(userId);
    }
}