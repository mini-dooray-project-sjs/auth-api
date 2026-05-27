package com.nhnacademy.authapi.repository;

import com.nhnacademy.authapi.entity.User;

public interface AuthRepository {
    User findByById(String userId);
}
