package com.baiyun;

import com.alibaba.fastjson.JSONObject;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthClientResponseFactory;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.message.types.ResponseType;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TestOltuClient {



    public static final String CLIENT_ID = "6b758a22-e580-47b0-92cb-aff6e1afa219"; // 应用id CLIENT_ID

    public static final String CLIENT_SECRET = "bba2f454-0b0e-4de1-94de-55d6b08fdecd"; // 应用secret CLIENT_SECRET

    public static final String USERNAME = "admin"; // 用户名

    public static final String PASSWORD = "admin"; // 密码

    public static final String OAUTH_SERVER_URL = "http://localhost:8080/authorize"; // 授权地址

    public static final String OAUTH_SERVER_TOKEN_URL = "http://localhost:8080/accessToken"; // ACCESS_TOKEN换取地址

    public static final String OAUTH_SERVER_REDIRECT_URI = "http://www.codegot.com"; // 回调地址

    public static final String OAUTH_SERVER_TOKEN_VALIDATE = "http://localhost:8080/validateToken"; // 回调地址

    public static final String OAUTH_SERVICE_API = "http://localhost:8080/v1/openapi/userInfo"; // 测试开放数据api（GET请求）
    public static final String OAUTH_POST_SERVICE_API = "http://localhost:8080/v1/openapi/postRequest"; // 测试开放数据api（POST请求）


    public static void main(String[] args) throws Exception {
        String authCode = getAuthCode();
        String accessToken = getAccessToken(authCode);
        getResult(accessToken);
        getPostResult(accessToken);
    }

    /**
     * 获取服务结果, 服务端从Header请求头中获取参数
     * @return
     */
    private static void getPostResult(String accessToken) throws Exception {
        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        OAuthClientRequest serviceRequest =
                new OAuthBearerClientRequest(OAUTH_POST_SERVICE_API)
                        .setAccessToken(accessToken)
                        .buildHeaderMessage();
        serviceRequest.setHeader("Content-Type", "application/json");
        serviceRequest.setHeader("key", "value");
        serviceRequest.setBody("{\"username\":\"zhangsan\"}");
        OAuthResourceResponse validateResponse = oAuthClient.resource(
                serviceRequest, OAuth.HttpMethod.POST, OAuthResourceResponse.class);
        String validateResponseBody = validateResponse.getBody();

        System.out.println("POST请求调用服务返回结果：" + validateResponseBody);
    }

    /**
     * 获取服务结果, 服务端从Header请求头中获取参数
     * @return
     */
    private static void getResult(String accessToken) throws Exception {
        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        OAuthClientRequest serviceRequest =
                new OAuthBearerClientRequest(OAUTH_SERVICE_API)
                        .setAccessToken(accessToken)
                        .buildHeaderMessage();
        serviceRequest.setHeader("key", "value");
        serviceRequest.setBody("");
        OAuthResourceResponse validateResponse = oAuthClient.resource(
                serviceRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);
        String validateResponseBody = validateResponse.getBody();

        System.out.println("GET请求调用服务返回结果：" + validateResponseBody);
    }

    /**
     * Token获取
     * @param authCode
     * @return
     * @throws OAuthSystemException
     * @throws OAuthProblemException
     */
    private static String getAccessToken(String authCode) throws OAuthSystemException, OAuthProblemException {
        OAuthClientRequest accessTokenRequest = OAuthClientRequest
                .tokenLocation(OAUTH_SERVER_TOKEN_URL)
                .setGrantType(GrantType.AUTHORIZATION_CODE)
                .setClientId(CLIENT_ID).setClientSecret(CLIENT_SECRET)
                .setCode(authCode).setRedirectURI(OAUTH_SERVER_REDIRECT_URI)
                .buildQueryMessage();
        //获取access token
        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

        OAuthAccessTokenResponse tokenResponse =
                oAuthClient.accessToken(accessTokenRequest, OAuth.HttpMethod.POST);
        String body = tokenResponse.getBody();
        JSONObject jsonObject = JSONObject.parseObject(body);
        System.out.println("access_token : " + jsonObject.getString("access_token"));
        System.out.println("refresh_token : " + jsonObject.getString("refresh_token"));

        //验证token
        OAuthClientRequest validateRequest =
                new OAuthBearerClientRequest(OAUTH_SERVER_TOKEN_VALIDATE)
                        .setAccessToken(tokenResponse.getAccessToken()).buildHeaderMessage();
        OAuthResourceResponse validateResponse = oAuthClient.resource(
                validateRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);
        String validateResponseBody = validateResponse.getBody();
        System.out.println("token验证结果： " + validateResponseBody);
        return jsonObject.getString("access_token");
    }

    /**
     * 授权码获取
     * @return
     * @throws OAuthSystemException
     * @throws IOException
     * @throws OAuthProblemException
     */
    private static String getAuthCode() throws OAuthSystemException, IOException, OAuthProblemException {
        OAuthClientRequest codeTokenRequest = OAuthClientRequest
                // authorizationLocation Enum -- OAuthProviderType
                .authorizationLocation(OAUTH_SERVER_URL)
                .setClientId(CLIENT_ID)
                .setRedirectURI(OAUTH_SERVER_REDIRECT_URI)
                .setResponseType(ResponseType.CODE.toString())
                .setParameter("username", USERNAME)
                .setParameter("password", PASSWORD)
                .buildQueryMessage();
        String locationUri = codeTokenRequest.getLocationUri();
        System.out.println("获取授权码地址信息：" + locationUri);

        // 发起请求
//        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
//        OAuthResourceResponse codeResponse = oAuthClient.resource(
//                codeTokenRequest, OAuth.HttpMethod.POST, OAuthResourceResponse.class);

        URL url = new URL(locationUri);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);// 必须设置该属性,否则将会自动处理重定向请求
//
        String location = connection.getHeaderField("Location");
        System.out.println("重定向地址：" + location);
        String authCode = location.substring(location.indexOf("=") + 1);
        System.out.println("授权码：" + authCode);
        return authCode;
    }


}
