package com.hackjklu.hackjklu.repository;

import com.hackjklu.hackjklu.entity.UserCred;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface repository extends MongoRepository<UserCred , String> {
}
