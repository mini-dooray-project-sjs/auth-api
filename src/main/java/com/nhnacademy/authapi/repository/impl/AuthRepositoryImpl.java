package com.nhnacademy.authapi.repository.impl;

import com.nhnacademy.authapi.entity.User;
import com.nhnacademy.authapi.entity.UserRole;
import com.nhnacademy.authapi.entity.UserStatus;
import com.nhnacademy.authapi.repository.AuthRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
@Repository
public class AuthRepositoryImpl implements AuthRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public AuthRepositoryImpl(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // 아이디로 유저 조회 -> 필요한 필드만 조회
    @Override
    public User findByById(String userId) {
        String sql="select user_id, password, status, role from users where user_id=?";

        try(PreparedStatement pstmt=jdbcTemplate.getDataSource().getConnection().prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs=pstmt.executeQuery();

            if(rs.next()) {
                return User.builder()
                        .id(rs.getString("user_id"))
                        .password(rs.getString("password"))
                        .status(UserStatus.fromCode(rs.getInt("status")))
                        .role(UserRole.fromCode(rs.getInt("role")))
                        .build();
            }
        } catch (SQLException e) {
            log.warn("SQL 예외 발생: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        return null;
    }
}
