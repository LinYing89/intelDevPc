package com.bairock.intelDevPc.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bairock.iot.intelDev.user.User;

public interface UserRepository extends JpaRepository<User, Long> {

}
