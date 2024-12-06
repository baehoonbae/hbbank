package com.hbbank.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hbbank.backend.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

}
