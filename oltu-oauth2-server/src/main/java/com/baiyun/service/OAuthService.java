package com.baiyun.service;

public interface OAuthService {



    //添加 auth code
    void addAuthCode(String authCode, String username);

    void deleteAuthCode(String authCode);

    void cacheToken(String accessToken, String refreshToken, String username);

    Object getAccessToken(String refreshToken);

    /**
     * 验证auth code是否有效
     * @param authCode
     * @return
     */
    boolean checkAuthCode(String authCode);

    /**
     * 验证access token是否有效
     */
    boolean checkAccessToken(String accessToken);

    /**
     * 验证 refreshToken 是否有效
     * @param refreshToken
     * @return
     */
    boolean checkRefreshToken(String refreshToken);


    String getUsernameByAccessToken(String accessToken);

    String getUsernameByRefreshToken(String refreshToken);

    String getUsernameByAuthCode(String authCode);


    //auth code / access token 过期时间
    long getExpireIn();


    boolean checkClientId(String clientId);

    boolean checkClientSecret(String clientSecret);


}
