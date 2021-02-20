## 现有的java版对oauth2的实现
https://oauth.net/code/java/
## apache olth
http://oltu.apache.org/

## OAuth2的四种授权场景
> 1、授权码（authorization code）方式，指的是第三方应用先申请一个授权码，然后再用该码获取令牌。
> 2、隐藏式（implicit）允许直接向前端颁发令牌。这种方式没有授权码这个中间步骤，所以称为（授权码）"隐藏式"。
> 3、密码式（password）允许用户把用户名和密码，直接告诉该应用。该应用就使用你的密码，申请令牌，这种方式称为"密码式"。
> 4、凭证式（client credentials）适用于没有前端的命令行应用，即在命令行下请求令牌。

