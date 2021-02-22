package com.baiyun;

import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 原生HTTP方式调用测试
 */
public class TestOpenApi {


    public static final String CLIENT_ID = "6b758a22-e580-47b0-92cb-aff6e1afa219"; // 应用id CLIENT_ID

    public static final String CLIENT_SECRET = "bba2f454-0b0e-4de1-94de-55d6b08fdecd"; // 应用secret CLIENT_SECRET

    public static final String USERNAME = "admin"; // 用户名

    public static final String PASSWORD = "admin"; // 密码

    public static final String CODE = "code";

    public static final String OAUTH_SERVER_URL = "http://localhost:8080/authorize"; // 授权地址

    public static final String OAUTH_SERVER_TOKEN_URL = "http://localhost:8080/accessToken"; // ACCESS_TOKEN换取地址

    public static final String OAUTH_SERVER_REDIRECT_URI = "http://www.codegot.com"; // 回调地址

    public static final String OAUTH_SERVICE_API = "http://localhost:8080/v1/openapi/userInfo"; // 测试开放数据api

    // refresh_token、authorization_code
    public static final String GRANT_TYPE = "authorization_code";

    public static void main(String[] args) throws Exception{
        String authCode = getAuthCode();
//        String accessToken = getAccessToken(authCode);
        /**
         * 获取参数的方式，由服务端进行定义，调整时需要服务端进行调整
         */
//        getResult1(accessToken); // Query方式传递token
//        getResult2(accessToken); // 111 Header方式传递token
//        getResult3("981708d92724688112362eb6bf4a2ba7"); // Body方式传递token

    }

    /**
     * 获取服务结果, 服务端以QueryString方式获取token参数
     * @return
     */
    private static void getResult1(String accessToken) throws Exception {

        URL url = new URL(OAUTH_SERVICE_API + "?access_token=" + accessToken);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        connection.setDoOutput(true);

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        System.out.println(response.toString());
    }

    /**
     * 获取服务结果, 服务端从Header请求头中获取参数
     * @return
     */
    private static void getResult2(String accessToken) throws Exception {

        URL url = new URL(OAUTH_SERVICE_API);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        // TODO  Bearer 注意Bearer后面与token间的空格
        connection.addRequestProperty("Authorization", "Bearer " + accessToken);
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        connection.setDoOutput(true);

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        System.out.println(response.toString());
    }

    /**
     * 获取服务结果, 服务端从Body获取参数, POST请求的方式
     * @return
     */
    private static void getResult3(String accessToken) throws Exception {

        URL url = new URL(OAUTH_SERVICE_API);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        // 组装请求体
        StringBuffer sb = new StringBuffer();
        // 服务端优先使用access_token进行token获取，获取不到则使用oauth_token获取
        sb.append(URLEncoder.encode("oauth_token", "UTF-8")).append("=")
                .append(URLEncoder.encode(accessToken, "UTF-8"));
        //POST请求
        OutputStream out = connection.getOutputStream();
        out.write(sb.toString().getBytes("UTF-8"));
        out.flush();
        //读取响应
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        System.out.println(response.toString());
    }

    /**
     * 获取accessToken
     *
     * @return
     */
    private static String getAccessToken(String authCode) throws Exception {

        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put("client_id", CLIENT_ID);
        params.put("client_secret", CLIENT_SECRET);
        params.put("grant_type", GRANT_TYPE);
        params.put("code", authCode);
        params.put("redirect_uri", OAUTH_SERVER_REDIRECT_URI);

        StringBuilder postStr = new StringBuilder();

        for (Map.Entry<String, Object> param : params.entrySet()) {
            if (postStr.length() != 0) {
                postStr.append('&');
            }
            postStr.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postStr.append('=');
            postStr.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
        }

        byte[] postStrBytes = postStr.toString().getBytes("UTF-8");

        URL url = new URL(OAUTH_SERVER_TOKEN_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Content-Length", String.valueOf(postStrBytes.length));
        connection.getOutputStream().write(postStrBytes);

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        // {"access_token":"dc861f493265948f57d4cc6eb6cde2f2","expires_in":3600}
//        System.out.println(response.toString());
        Map<String, String> map = JSON.parseObject(response.toString(), Map.class);
        String accessToken = map.get("access_token");
        // dc861f493265948f57d4cc6eb6cde2f2
        System.out.println(accessToken);
        return accessToken;
    }

    /**
     * 获取授权码
     *
     * @return
     */
    private static String getAuthCode() throws Exception {

        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put("username", USERNAME);
        params.put("password", PASSWORD);
        params.put("client_id", CLIENT_ID);
        params.put("response_type", CODE);
        params.put("redirect_uri", OAUTH_SERVER_REDIRECT_URI);

        StringBuilder postStr = new StringBuilder();

        for (Map.Entry<String, Object> param : params.entrySet()) {
            if (postStr.length() != 0) {
                postStr.append('&');
            }
            postStr.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postStr.append('=');
            postStr.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
        }
//        System.out.println("请求参数：" + postStr.toString());
        byte[] postStrBytes = postStr.toString().getBytes("UTF-8");

        URL url = new URL("http://localhost:8080/authorize"); // 客户端授权地址
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Content-Length", String.valueOf(postStrBytes.length));
        connection.getOutputStream().write(postStrBytes);
        connection.setInstanceFollowRedirects(false);// 必须设置该属性,否则将会自动处理重定向请求

        String response = getResponse(connection);

        System.out.println(response);

        String location = connection.getHeaderField("Location");
        // http://www.codegot.com?code=6f056f610d9e840f2f8fdd1a1dbcf607
        System.out.println(location);
        return location.substring(location.indexOf("=") + 1);
    }

    private static void getService() throws Exception {

        URL url = new URL("http://localhost:8080/v1/openapi/getUserInfo");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        connection.setDoOutput(true);

        String response = getResponse(connection);

        System.out.println(response);
    }

    public static String getResponse(HttpURLConnection connection) throws Exception{
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuffer response;
        try {
            String inputLine;
            response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        } finally {
            in.close();
        }
        return response.toString();
    }
}
