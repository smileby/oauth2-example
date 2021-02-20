package com.baiyun.controller.oauth;

import com.alibaba.fastjson.JSON;
import com.baiyun.model.Status;
import com.baiyun.model.User;
import com.baiyun.service.ClientService;
import com.baiyun.service.OAuthService;
import com.baiyun.service.UserService;
import com.baiyun.utils.Constants;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.message.types.ResponseType;
import org.apache.oltu.oauth2.common.utils.OAuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * oauth2授权
 */
@Controller
public class AuthorizeController {
    @Autowired
    private OAuthService oAuthService;
    @Autowired
    private ClientService clientService;
    @Autowired
    private UserService userService;

    /**
     * 对外开放的授权接口
     *      第三方通过code进行获取 access_token的时候需要用到，code的超时时间为10分钟，一个code只能成功换取一次access_token即失效。
     * @return
     */
    @RequestMapping(value = "/authorize")
    public Object authorize(Model model, HttpServletRequest request) throws URISyntaxException, OAuthSystemException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type","application/json; charset=utf-8");
            // 构建OAuth 授权请求
            OAuthAuthzRequest oauthRequest = new OAuthAuthzRequest(request);
            // 检查传入的client_id、client_secret是否认证
            if (!oAuthService.checkClientId(oauthRequest.getClientId())) {
                OAuthResponse response =
                        OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                                .setError(OAuthError.TokenResponse.INVALID_CLIENT)
                                .setErrorDescription(Constants.INVALID_CLIENT_ID)
                                .buildJSONMessage();
                return new ResponseEntity(response.getBody(), headers, HttpStatus.valueOf(response.getResponseStatus()));
            }

            //如果用户没有登录，跳转到登陆页面
            if (!login(request)) {//登录失败时跳转到登陆页面
                model.addAttribute("client", clientService.findByClientId(oauthRequest.getClientId()));
                return "manager/oauth2login";
            }
            String username = request.getParameter("username"); //获取用户名

            //生成授权码 UUIDValueGenerator OR MD5Generator
            String authorizationCode = null;

            //responseType目前仅支持CODE，另外还有TOKEN
            String responseType = oauthRequest.getParam(OAuth.OAUTH_RESPONSE_TYPE);
            if (responseType.equals(ResponseType.CODE.toString())) {
                OAuthIssuerImpl oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
                authorizationCode = oauthIssuerImpl.authorizationCode();
                // 授权码绑定用户名， 设置超期自动失效策略--baiyun
                oAuthService.addAuthCode(authorizationCode, username);
//                oAuthService.addAuthCode(authorizationCode, DigestUtils.sha1Hex(oauthRequest.getClientId()+oauthRequest.getRedirectURI()));
            }

            //进行OAuth响应构建
            OAuthASResponse.OAuthAuthorizationResponseBuilder builder =
                    OAuthASResponse.authorizationResponse(request, HttpServletResponse.SC_FOUND);
            //设置授权码
            builder.setCode(authorizationCode);
            //得到到客户端重定向地址
//            oauthRequest.getRedirectURI();
            String redirectURI = oauthRequest.getParam(OAuth.OAUTH_REDIRECT_URI);

            //构建响应
            final OAuthResponse response = builder.location(redirectURI).buildQueryMessage();

            //根据OAuthResponse返回ResponseEntity响应
            headers = new HttpHeaders();
            headers.set("Content-Type","application/json; charset=utf-8");
            headers.setLocation(new URI(response.getLocationUri()));
            return new ResponseEntity(headers, HttpStatus.valueOf(response.getResponseStatus()));
        } catch (OAuthProblemException e) {
            //出错处理
            String redirectUri = e.getRedirectUri();
            if (OAuthUtils.isEmpty(redirectUri)) {
                //告诉客户端没有传入redirectUri直接报错
                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Type", "application/json; charset=utf-8");
                Status status = new Status();
                status.setCode(HttpStatus.NOT_FOUND.value());
                status.setMsg(Constants.INVALID_REDIRECT_URI);
                return new ResponseEntity(JSON.toJSONString(status), headers, HttpStatus.NOT_FOUND);
            }
            //返回错误消息（如?error=）
            final OAuthResponse response =
                    OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND)
                            .error(e).location(redirectUri).buildQueryMessage();
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(new URI(response.getLocationUri()));
            return new ResponseEntity(headers, HttpStatus.valueOf(response.getResponseStatus()));
        }
    }

    /**
     * 判断是否登录
     * @param request
     * @return
     */
    private boolean login(HttpServletRequest request) {
        if ("get".equalsIgnoreCase(request.getMethod())) {
            request.setAttribute("error", "非法的请求");
            return false;
        }
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            request.setAttribute("error", "登录失败:用户名或密码不能为空");
            return false;
        }
        try {
            // 写登录逻辑
            User user = userService.findByUsername(username);
            if (user != null) {
                if (!userService.checkUser(username, password, user.getSalt(), user.getPassword())) {
                    request.setAttribute("error", "登录失败:密码不正确");
                    return false;
                } else {
                    return true;
                }
            } else {
                request.setAttribute("error", "登录失败:用户名不正确");
                return false;
            }
        } catch (Exception e) {
            request.setAttribute("error", "登录失败:" + e.getClass().getName());
            return false;
        }
    }
}
