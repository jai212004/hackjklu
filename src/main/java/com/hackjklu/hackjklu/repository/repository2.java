package com.hackjklu.hackjklu.repository;

import com.hackjklu.hackjklu.entity.resetPassword;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface repository2 extends MongoRepository<resetPassword, String> {
}
