package com.mybox.mybox.user.application;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class UserRouter {

    @Bean
    public RouterFunction<ServerResponse> userList(UserHandler userHandler) {
        return RouterFunctions
            .route(RequestPredicates
                    .GET("/users")
                    .and(RequestPredicates.accept(APPLICATION_JSON)),
                request -> userHandler.getAllUserList());
    }

    @Bean
    public RouterFunction<ServerResponse> user(UserHandler userHandler) {
        return RouterFunctions
            .route(RequestPredicates.GET("/users/{userId}").and(RequestPredicates.accept(APPLICATION_JSON)), userHandler::getUser)
            .andRoute(RequestPredicates.POST("/users").and(RequestPredicates.accept(APPLICATION_JSON)), userHandler::addUser);
    }

//    @Bean
//    public RouterFunction<ServerResponse> getUser(UserHandler userHandler){
//        return RouterFunctions.route(
//            RequestPredicates
//                .POST("/users/{req}")
//                .and(RequestPredicates.accept(APPLICATION_JSON)),
//            movieHandler::getMovieList);
//    }
//
//    @Bean
//    public RouterFunction<ServerResponse> index(MovieHandler movieHandler){
//        return RouterFunctions.route(
//                RequestPredicates.GET("/").and(RequestPredicates.accept(TEXT_HTML)), movieHandler::movie)
//            .andRoute(RequestPredicates.GET("/movie").and(RequestPredicates.accept(TEXT_HTML)), movieHandler::movie);
//    }
//
//    @Bean
//    public RouterFunction<ServerResponse> getMoiveList(MovieHandler movieHandler){
//        return RouterFunctions.route(
//            RequestPredicates
//                .POST("/getMovieList")
//                .and(RequestPredicates.accept(APPLICATION_JSON)),
//            movieHandler::getMovieList);
//    }
//
//    @Bean
//    public RouterFunction<ServerResponse> getMoiveListSearch(MovieHandler movieHandler){
//        return RouterFunctions.route(
//            RequestPredicates
//                .POST("/getMovieList/{req}")
//                .and(RequestPredicates.accept(APPLICATION_JSON)),
//            movieHandler::getMovieList);
//    }
}