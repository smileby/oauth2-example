package com.baiyun.controller.oauth;

import com.baiyun.service.OAuthService;
import com.baiyun.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;

@Slf4j
@RestController
public class AccessTokenController {

    @Autowired
    private OAuthService oAuthService;

    /**
     * @param request
     * @return
     * @throws URISyntaxException
     * @throws OAuthSystemException
     */
    @RequestMapping("/accessToken")
    public HttpEntity token(HttpServletRequest request)
            throws URISyntaxException, OAuthSystemException {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type","application/json; charset=utf-8");

        try {
            //构建OAuth请求
            OAuthTokenRequest oauthRequest = new OAuthTokenRequest(request);

            //检查提交的客户端id是否认证
            if (!oAuthService.checkClientId(oauthRequest.getClientId())) {
                OAuthResponse response =
                        OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                                .setError(OAuthError.TokenResponse.INVALID_CLIENT)
                                .setErrorDescription(Constants.INVALID_CLIENT_ID)
                                .buildJSONMessage();
                log.error("未认证的client_id：{}", oauthRequest.getClientId());
                return new ResponseEntity(response.getBody(), headers, HttpStatus.valueOf(response.getResponseStatus()));
            }

            // 检查客户端安全KEY是否正确
            if (!oAuthService.checkClientSecret(oauthRequest.getClientSecret())) {
                OAuthResponse response =
                        OAuthASResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                                .setError(OAuthError.TokenResponse.UNAUTHORIZED_CLIENT)
                                .setErrorDescription(Constants.INVALID_CLIENT_ID)
                                .buildJSONMessage();
                log.error("未认证的clientSecret：{}", oauthRequest.getClientSecret());
                return new ResponseEntity(response.getBody(), headers, HttpStatus.valueOf(response.getResponseStatus()));
            }

            String authCode = oauthRequest.getParam(OAuth.OAUTH_CODE); // 授权码
            // 检查验证类型，此处只检查AUTHORIZATION_CODE类型，其他的还有PASSWORD或REFRESH_TOKEN
            if (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE).equals(GrantType.AUTHORIZATION_CODE.toString())) {
                if (!oAuthService.checkAuthCode(authCode)) {
                    OAuthResponse response = OAuthASResponse
                            .errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                            .setError(OAuthError.TokenResponse.INVALID_GRANT)
                            .setErrorDescription(Constants.INVALID_AUTH_CODE)
                            .buildJSONMessage();
                    log.error("失效的authCode：{}", authCode);
                    return new ResponseEntity(response.getBody(), headers, HttpStatus.valueOf(response.getResponseStatus()));
                }
            }

            //生成Access Token
            OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
            final String accessToken = oauthIssuerImpl.accessToken();
            String refreshToken = oauthIssuerImpl.refreshToken();
            // 保存accessToken, refreshToken
            String userName = oAuthService.getUsernameByAuthCode(authCode);
            oAuthService.cacheToken(accessToken, refreshToken, userName);
            // 清除authCode授权码，确保一个authCode只能使用一次
            oAuthService.deleteAuthCode(authCode);
            //生成OAuth响应
            OAuthResponse response = OAuthASResponse
                    .tokenResponse(HttpServletResponse.SC_OK)
                    .setAccessToken(accessToken)
                    .setExpiresIn(String.valueOf(oAuthService.getExpireIn()))
                    .setRefreshToken(refreshToken)
                    .buildJSONMessage();
            log.info("accessToken返回：{}", response.getBody());
            //根据OAuthResponse生成ResponseEntity
            return new ResponseEntity(response.getBody(), headers, HttpStatus.valueOf(response.getResponseStatus()));

        } catch (OAuthProblemException e) {
            log.error("token生成异常：{}", e.getMessage(), e);
            //构建错误响应
            OAuthResponse res = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST).error(e)
                    .buildJSONMessage();
            return new ResponseEntity(res.getBody(), headers, HttpStatus.valueOf(res.getResponseStatus()));
        }
    }

    /**
     * 刷新令牌
     *   access_token是调用授权关系接口的调用凭证，由于access_token有效期较短（目前为2个小时），当access_token超时后，
     *  可以使用refresh_token进行刷新，access_token刷新结果有两种：
     1. 若access_token已超时，那么进行refresh_token会获取一个新的access_token，新的超时时间；
     2. 若access_token未超时，那么进行refresh_token不会改变access_token，但超时时间会刷新，相当于续期access_token。
     refresh_token拥有较长的有效期（30天），当refresh_token失效的后，需要用户重新授权。
     * @param request
     * @throws IOException
     * @throws OAuthSystemException
     * @url http://localhost:8080/refresh_token?client_id={AppKey}&grant_type=refresh_token&refresh_token={refresh_token}
     */
    @RequestMapping(value = "/refresh_token")
    public HttpEntity refresh_token(HttpServletRequest request)throws IOException, OAuthSystemException {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type","application/json; charset=utf-8");
        try {
            //构建oauth2请求
            OAuthTokenRequest oauthRequest = new OAuthTokenRequest(request);
            //检查提交的客户端id是否认证
            if (!oAuthService.checkClientId(oauthRequest.getClientId())) {
                OAuthResponse response =
                        OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                                .setError(OAuthError.TokenResponse.INVALID_CLIENT)
                                .setErrorDescription(Constants.INVALID_CLIENT_ID)
                                .buildJSONMessage();
                return new ResponseEntity(response.getBody(), headers, HttpStatus.valueOf(response.getResponseStatus()));
            }
            // 验证 grant_type 是否是refresh_token
            if (GrantType.REFRESH_TOKEN.name().equalsIgnoreCase(oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE))) {
                OAuthResponse response = OAuthASResponse
                        .errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                        .setError(OAuthError.TokenResponse.INVALID_GRANT)
                        .setErrorDescription(Constants.INVALID_CLIENT_ID)
                        .buildJSONMessage();
                return new ResponseEntity(response.getBody(), headers, HttpStatus.valueOf(response.getResponseStatus()));
            }
            // 验证refresh_token的有效性
            if(!oAuthService.checkRefreshToken(oauthRequest.getRefreshToken())){
                OAuthResponse response =
                        OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                                .setError("expired_refresh_token")
                                .setErrorDescription("refresh_token过期，请重新授权")
                                .buildJSONMessage();
                return new ResponseEntity(response.getBody(), headers, HttpStatus.valueOf(response.getResponseStatus()));
            }

            /*
            * 刷新access_token有效期
             access_token是调用授权关系接口的调用凭证，由于access_token有效期（目前为2个小时）较短，当access_token超时后，可以使用refresh_token进行刷新，access_token刷新结果有两种：
             1. 若access_token已超时，那么进行refresh_token会获取一个新的access_token，新的超时时间；
             2. 若access_token未超时，那么进行refresh_token不会改变access_token，但超时时间会刷新，相当于续期access_token。
             refresh_token拥有较长的有效期（30天），当refresh_token失效的后，需要用户重新授权。
            * */
            Object accessTokenCache = oAuthService.getAccessToken(oauthRequest.getRefreshToken());
            String username = oAuthService.getUsernameByRefreshToken(oauthRequest.getRefreshToken());
            if (null == accessTokenCache) {
                //access_token已过期
                OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
                final String accessToken = oauthIssuerImpl.accessToken();

                oAuthService.cacheToken(accessToken, oauthRequest.getRefreshToken(), username);

                //构建oauth2授权返回信息
                OAuthResponse response = OAuthASResponse
                        .tokenResponse(HttpServletResponse.SC_OK)
                        .setAccessToken(accessToken)
                        .setExpiresIn(String.valueOf(oAuthService.getExpireIn()))
                        .setRefreshToken(oauthRequest.getRefreshToken())
                        .buildJSONMessage();

                //根据OAuthResponse生成ResponseEntity
                return new ResponseEntity(response.getBody(), headers, HttpStatus.valueOf(response.getResponseStatus()));
            }
            //access_token未超时，进行续期
            oAuthService.cacheToken(accessTokenCache.toString(), oauthRequest.getRefreshToken(), username);
            //构建oauth2授权返回信息
            OAuthResponse response = OAuthASResponse
                    .tokenResponse(HttpServletResponse.SC_OK)
                    .setAccessToken(accessTokenCache.toString())
                    .setExpiresIn(String.valueOf(oAuthService.getExpireIn()))
                    .setRefreshToken(oauthRequest.getRefreshToken())
                    .buildJSONMessage();

            //根据OAuthResponse生成ResponseEntity
            return new ResponseEntity(response.getBody(), headers, HttpStatus.valueOf(response.getResponseStatus()));
        } catch(OAuthProblemException e) {
            //构建错误响应
            OAuthResponse res = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST).error(e)
                    .buildJSONMessage();
            return new ResponseEntity(res.getBody(), headers, HttpStatus.valueOf(res.getResponseStatus()));
        }
    }

    /**
     * 验证accessToken
     *
     * @param accessToken
     * @return
     */
    @RequestMapping(value = "/checkAccessToken", method = RequestMethod.POST)
    public ResponseEntity checkAccessToken(@RequestParam("accessToken") String accessToken) {
        boolean b = oAuthService.checkAccessToken(accessToken);
        return b ? new ResponseEntity(HttpStatus.valueOf(HttpServletResponse.SC_OK)) : new ResponseEntity(HttpStatus.valueOf(HttpServletResponse.SC_UNAUTHORIZED));
    }
}
