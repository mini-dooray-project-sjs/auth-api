package com.nhnacademy.authapi.client;

import com.nhnacademy.authapi.dto.UserLoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserClient {

    private final RestTemplate restTemplate;

    @Value("${external.account-api.url}")
    private String accountUrl;

    public UserLoginResponse getUser(String userId) {
        String url=accountUrl+"/users/{user-id}/login-info";

        return restTemplate.getForObject(url, UserLoginResponse.class, userId);
    }
}
