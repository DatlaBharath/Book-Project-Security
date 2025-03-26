package com.example.booksecurity.Service;

import com.example.booksecurity.Entity.Security;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collection;


public class CustomUserDetails implements UserDetails {

    private String username;
    private String password;

    public CustomUserDetails(Security security){
        this.username = security.getUsername();
        this.password = security.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return null;
    }

    @Override
    public String getPassword(){
        return password;
    }

    public String getUsername(){
        return username;
    }
}
