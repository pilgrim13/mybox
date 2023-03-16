package com.mybox.mybox.domain.user;

import com.mybox.mybox.domain.user.entity.User;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserHandler {

    private final UserService userService;

//    final static MediaType TEXT_HTML = MediaType.TEXT_HTML;

    public Mono<ServerResponse> addUser(ServerRequest request) {
        return request.bodyToMono(User.class)
            .flatMap(userService::addUser)
            .flatMap(user -> ServerResponse.created(URI.create("/users/" + user.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(user)));
//            .flatMap(body -> ServerResponse.ok().body(BodyInserters.fromValue(userService.addUser(body))));
    }

//    return request.bodyToMono(Book.class)
//        .flatMap(book -> Mono.just(bookService.createBook(book)))
//        .flatMap(book -> ServerResponse.created(URI.create("/books/" + book.getId()))
//        .contentType(MediaType.APPLICATION_JSON)
//					.body(BodyInserters.fromValue(book)));

    public Mono<ServerResponse> getAllUserList() {
        Flux<User> userList = this.userService.getAllUsers();
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(userList, User.class);
    }

    public Mono<ServerResponse> getUser(ServerRequest request) {
        return null;
    }

}