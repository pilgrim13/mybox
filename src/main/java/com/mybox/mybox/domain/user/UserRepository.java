package com.mybox.mybox.domain.user;

import com.mybox.mybox.domain.user.entity.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface UserRepository extends ReactiveMongoRepository<User, String> {

}