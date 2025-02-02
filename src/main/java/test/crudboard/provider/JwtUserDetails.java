package test.crudboard.provider;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import test.crudboard.entity.enumtype.Roles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class JwtUserDetails implements UserDetails {
    @Getter
    private final Long id;
    private final Collection<? extends GrantedAuthority> authorities;

    public JwtUserDetails(Long id, Roles roles){
        this.id = id;
        this.authorities = Collections.singleton(new SimpleGrantedAuthority(roles.name()));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return "";
    }

}
