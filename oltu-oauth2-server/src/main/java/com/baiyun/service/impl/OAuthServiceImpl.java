package com.baiyun.service.impl;

import com.baiyun.service.ClientService;
import com.baiyun.service.OAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service("oAuthService")
public class OAuthServiceImpl implements OAuthService {

    @Autowired
    private ClientService clientService;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 缓存授权码，10分钟过期
     * @param authCode
     * @param username
     */
    @Override
    public void addAuthCode(String authCode, String username) {
        redisTemplate.opsForValue().set(authCode, username, 10 * 60L, TimeUnit.SECONDS);
    }

    @Override
    public void deleteAuthCode(String authCode) {
        redisTemplate.delete(authCode);
    }

    @Override
    public void cacheToken(String accessToken, String refreshToken, String username) {

        // 用户第三方调用资源资源时校验权限使用（2小时过期）
        addAccessToken(accessToken, username);
        // 刷新accessToken时判断是否过期使用（2小时过期）
        addRefreshToken(refreshToken, accessToken);
        /**
         *  TODO 设置refreshToken的30天过期，使用数据库保存（字段如下： access_token、refresh_token、expires_in、application_id）
         *  以及Token和用户的对应关系信息。。。。。。
         */
        addRefreshToken30Day(refreshToken + "_30", username);

    }


    private void addAccessToken(String accessToken, String userName) {
        redisTemplate.opsForValue().set(accessToken, userName, 2, TimeUnit.HOURS);
    }


    private void addRefreshToken(String refreshToken, String accessToken) {
        redisTemplate.opsForValue().set(refreshToken, accessToken, 2, TimeUnit.HOURS);
    }

    private void addRefreshToken30Day(String refreshToken, String userName) {
        redisTemplate.opsForValue().set(refreshToken, userName, 30, TimeUnit.DAYS);
    }

    @Override
    public Object getAccessToken(String refreshToken) {
        return redisTemplate.opsForValue().get(refreshToken);
    }

    @Override
    public String getUsernameByAccessToken(String accessToken) {
        return (String)redisTemplate.opsForValue().get(accessToken);
    }

    @Override
    public String getUsernameByRefreshToken(String refreshToken) {
        return (String)redisTemplate.opsForValue().get(refreshToken + "_30");
    }

    @Override
    public String getUsernameByAuthCode(String authCode) {
        return (String)redisTemplate.opsForValue().get(authCode);
    }

    @Override
    public boolean checkAuthCode(String authCode) {
        return redisTemplate.hasKey(authCode);
    }

    @Override
    public boolean checkAccessToken(String accessToken) {
        return redisTemplate.hasKey(accessToken);
    }

    @Override
    public boolean checkRefreshToken(String refreshToken) {
        return redisTemplate.hasKey(refreshToken + "_30");
    }

    @Override
    public boolean checkClientId(String clientId) {
        return clientService.findByClientId(clientId) != null;
    }

    @Override
    public boolean checkClientSecret(String clientSecret) {
        return clientService.findByClientSecret(clientSecret) != null;
    }

    @Override
    public long getExpireIn() {
        return 3600L;
    }
}
