package com.baiyun.config.filter;

import com.alibaba.fastjson.JSON;
import com.baiyun.model.Status;
import com.baiyun.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.ParameterStyle;
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest;
import org.apache.oltu.oauth2.rs.response.OAuthRSResponse;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

@Slf4j
@Component
public class Oauth2Filter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse res = (HttpServletResponse) response;

        try {
            HttpServletRequest hsr = (HttpServletRequest)request;
            if(allowAnonAccess(hsr)){
                // 允许匿名访问的路径, 过滤器放行
                chain.doFilter(request, response);
                return;
            }

            /*
                构建OAuth资源请求
                1、queryString方式获取参数
                2、从Header请求头中获取参数
                3、从body中获取参数
                默认从Header中获取请求
             */
            OAuthAccessResourceRequest oauthRequest = new OAuthAccessResourceRequest(hsr);
//            OAuthAccessResourceRequest oauthRequest = new OAuthAccessResourceRequest(hsr, ParameterStyle.QUERY);
//             OAuthAccessResourceRequest oauthRequest = new OAuthAccessResourceRequest(hsr, ParameterStyle.HEADER);
//             OAuthAccessResourceRequest oauthRequest = new OAuthAccessResourceRequest(hsr, ParameterStyle.BODY);

            String accessToken = oauthRequest.getAccessToken();
            log.info("传入的token：{}", accessToken);
            //验证Access Token
            if (!checkAccessToken(accessToken)) {
                log.error("AuthFilter执行accessToken验证失败：无效或失效的token");
                // 如果不存在/过期了，返回未验证错误，需重新验证
                oAuthFaileResponse(res);
                return;
            }
            chain.doFilter(request, response);
        } catch (OAuthProblemException e) {
            log.error("AuthFilter执行异常：{}", e.getMessage(), e);
            e.printStackTrace();
            try {
                oAuthFaileResponse(res);
            } catch (OAuthSystemException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "error trying to access oauth server", ex);
            }
        } catch (OAuthSystemException e) {
            log.error("AuthFilter执行异常2：{}", e.getMessage(), e);
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "error trying to access oauth server", e);
        }
    }

    private boolean allowAnonAccess(HttpServletRequest hsr) {
        String requestURI = hsr.getRequestURI();
        // 静态资源路径
        if("/".equals(requestURI) || requestURI.endsWith("html") || requestURI.endsWith("css")
                || requestURI.startsWith("/page")){
            return true;
        }
        // 授权获取路径
        if(requestURI.startsWith("/accessToken") || requestURI.startsWith("/authorize")
                || requestURI.startsWith("/refresh_token") || requestURI.startsWith("/checkAccessToken") ){
            return true;
        }
        return false;
    }

    /**
     * oAuth认证失败时的输出
     *
     * @param res
     * @throws OAuthSystemException
     * @throws IOException
     */
    private void oAuthFaileResponse(HttpServletResponse res) throws OAuthSystemException, IOException {
        OAuthResponse oauthResponse = OAuthRSResponse
                .errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                .setRealm(Constants.RESOURCE_SERVER_NAME)
                .setError(OAuthError.ResourceResponse.INVALID_TOKEN)
                .buildHeaderMessage();
        res.addHeader(OAuth.HeaderType.WWW_AUTHENTICATE, oauthResponse.getHeader(OAuth.HeaderType.WWW_AUTHENTICATE));
        res.setContentType("application/json; charset=utf-8");
        res.setCharacterEncoding("utf-8");
        PrintWriter writer = res.getWriter();
        writer.write(JSON.toJSONString(getStatus(HttpStatus.UNAUTHORIZED.value(), Constants.INVALID_ACCESS_TOKEN)));
        writer.flush();
        writer.close();
    }

    /**
     * 验证accessToken存在
     *
     * @param accessToken
     * @return
     * @throws IOException
     */
    private boolean checkAccessToken(String accessToken) throws IOException {
        URL url = new URL(Constants.CHECK_ACCESS_CODE_URL + accessToken);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.disconnect();
        return HttpServletResponse.SC_OK == conn.getResponseCode();
    }

    private Status getStatus(int code, String msg) {
        Status status = new Status();
        status.setCode(code);
        status.setMsg(msg);
        return status;
    }

    @Override
    public void destroy() {

    }


}
