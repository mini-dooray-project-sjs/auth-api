package com.nhnacademy.authapi.repository.impl;

import com.nhnacademy.authapi.entity.User;
import com.nhnacademy.authapi.entity.UserRole;
import com.nhnacademy.authapi.entity.UserStatus;
import com.nhnacademy.authapi.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AuthRepositoryImpl implements AuthRepository {

    private final JdbcTemplate jdbcTemplate;

    // 아이디로 유저 조회 -> 필요한 필드만 조회
    @Override
    public User findByById(String userId) {
        String sql="select user_id, password, status, role from users where user_id=?";

        try {
            return jdbcTemplate.queryForObject(
                    sql,
                    new Object[] {userId},
                    (rs, rowNum) -> {
                        return User.builder()
                                .id(rs.getString("user_id"))
                                .password(rs.getString("password"))
                                .status(parseUserStatus(rs))
                                .role(parseUserRole(rs))
                                .build();
                    }
            );
        } catch (Exception e) {
            log.error("findById error", e);
        }
        return null;
    }

    private UserStatus parseUserStatus(ResultSet rs) throws SQLException {
        String statusStr = rs.getString("status");
        if (statusStr != null && !statusStr.isBlank()) {
            statusStr = statusStr.trim();
            // enum 이름으로 먼저 시도
            try {
                return UserStatus.valueOf(statusStr);
            } catch (IllegalArgumentException ignored) {
                // 이름 매칭 실패하면 다음 단계로
            }
        }

        // 숫자 코드로 시도 (DB에 정수로 저장된 경우)
        int statusInt = rs.getInt("status");
        if (!rs.wasNull()) {
            try {
                return UserStatus.fromCode(statusInt);
            } catch (IllegalArgumentException ex) {
                throw new SQLException("Unknown UserStatus for code: " + statusInt, ex);
            }
        }

        throw new SQLException("UserStatus is null or unknown (statusStr=" + statusStr + ")");
    }

    private UserRole parseUserRole(ResultSet rs) throws SQLException {
        String roleStr = rs.getString("role");
        if (roleStr != null && !roleStr.isBlank()) {
            roleStr = roleStr.trim();
            try {
                return UserRole.valueOf(roleStr);
            } catch (IllegalArgumentException ignored) {
                // continue to numeric parsing below
            }
        }

        // 숫자 코드로 저장된 경우(예시: 0=ADMIN, 1=USER). DB 스키마에 따라 매핑을 맞추세요.
        int roleInt = rs.getInt("role");
        if (!rs.wasNull()) {
            try {
                return UserRole.fromCode(roleInt);
            } catch (IllegalArgumentException ex) {
                throw new SQLException("Unknown UserRole for code: " + roleInt, ex);
            }
        }

        throw new SQLException("UserRole is null or unknown (roleStr=" + roleStr + ")");
    }
}
