## 现有的java版对oauth2的实现
https://oauth.net/code/java/
## apache olth
http://oltu.apache.org/

## 大致流程
（A）用户访问客户端，客户端将用户导向认证服务器。
（B）用户给予客户端授权。
（C）用户给予授权后，认证服务器将用户导向客户端事先指定的"重定向URI"（redirection URI），同时附上一个授权码。
（D）客户端收到授权码，附上早先的"重定向URI"，向认证服务器申请令牌。这一步是在客户端的后台的服务器上完成的，对用户不可见。
（E）认证服务器核对了授权码和重定向URI，确认无误后，向客户端发送访问令牌（access token）和更新令牌（refresh token）。
（F）客户端使用令牌（access token）获取服务端接口信息

## Documentation
https://cwiki.apache.org/confluence/display/OLTU/Documentation
https://cwiki.apache.org/confluence/display/OLTU/OAuth+2.0+Authorization+Server
https://cwiki.apache.org/confluence/display/OLTU/OAuth+2.0+Resource+Server

## demo参考
https://svn.apache.org/repos/asf/oltu/trunk/demos/client-demo/
https://www.cnblogs.com/hujunzheng/p/7126766.html?utm_source=debugrun&utm_medium=referral

##参考资料： 
> oauth2的四种服务端实现 https://github.com/spring2go/oauth2lab  