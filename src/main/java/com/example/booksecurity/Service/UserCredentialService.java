package com.example.booksecurity.Service;

import com.example.booksecurity.Entity.Security;
import com.example.booksecurity.Repository.SecurityRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserCredentialService {
    @Autowired
    private JwtService jwtService;

    @Autowired
    private SecurityRepo securityRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Security register(Security security){
        security.setPassword(passwordEncoder.encode(security.getPassword()));
        return securityRepo.save(security);
    }

    public String generateToken(String username){
        return jwtService.generateToken(username);
    }

    public boolean validateToken(String token){
        jwtService.validateToken(token);
        return true;
    }



}
