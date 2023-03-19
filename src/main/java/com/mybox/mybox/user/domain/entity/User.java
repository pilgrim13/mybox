package com.mybox.mybox.user.domain.entity;

import com.mybox.mybox.user.domain.constants.Role;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Id;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


@Data
@Document
@ToString
public class User implements UserDetails {

    @Id
    private String id;
    private String username;
    private String password;
    private String nickname;
    private Set<GrantedAuthority> roles = new HashSet<>();
    private boolean active = true;

    @Builder
    public User(String username, String password, String nickname) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        roles.add(new SimpleGrantedAuthority(Role.ROLE_USER.name()));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    public String getHomeFolder() {
        return "/" + this.username;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return active;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return active;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

}

