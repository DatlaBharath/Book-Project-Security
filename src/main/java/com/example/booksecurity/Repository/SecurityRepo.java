package com.example.booksecurity.Repository;

import com.example.booksecurity.Entity.Security;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SecurityRepo extends MongoRepository<Security,String> {
    public Optional<Security> findByUsername(String username);

}
