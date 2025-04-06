package test.crudboard.security.provider;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import test.crudboard.domain.entity.enumtype.Roles;

import java.util.Collection;
import java.util.Collections;

public class JwtUserDetails implements UserDetails {
    private final String name;
    private final Collection<? extends GrantedAuthority> authorities;

    public JwtUserDetails(String name, Roles roles){
        this.name = name;
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
        return name;
    }

}
