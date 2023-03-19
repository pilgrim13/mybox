package com.mybox.mybox.user.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserRequestDto {

    private String username;
    private String password;
    private String nickname;

}