//package com.mybox.mybox.domain.user;
//
//import com.mybox.mybox.domain.user.entity.User;
//import java.net.URI;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//
//@Slf4j
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/users")
//public class UserController {
//
//    private final UserRepository userRepository;
//
//    @GetMapping
//    public Flux<User> getUsers() {
//        return userRepository.findAll();
//    }
//
//    @GetMapping("/{id}")
//    public Mono<ResponseEntity<?>> getUser(@PathVariable String id) {
//        return userRepository.findById(id)
//            .map(user -> ResponseEntity.ok().body(user));
//    }
//
//    @GetMapping
//    public Mono<ResponseEntity<?>> addUser(@RequestBody Mono<User> user) {
//        return user.flatMap(userRepository::save)
//            .map(savedUser -> ResponseEntity
//                .created(URI.create("/users/" + savedUser.getId()))
//                .body(savedUser)
//            );
//    }
//
////
////    @ResponseBody
////    public Mono<ResponseEntity<?>> oneImage(@PathVariable String filename) {
////        return imageService.getOneImage(filename)
////            .map(resource -> {
////                try {
////                    return ResponseEntity.ok()
////                        .contentLength(resource.contentLength())
////                        .body(new InputStreamResource(
////                            resource.getInputStream()));
////                } catch (IOException e) {
////                    return ResponseEntity.badRequest()
////                        .body("Couldn't find " + filename +
////                            " => " + e.getMessage());
////                }
////            });
////    }
//}