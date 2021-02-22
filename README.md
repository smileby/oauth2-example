# oauth2-example

## OAuth2的四种授权场景
> 1、授权码（authorization code）方式，指的是第三方应用先申请一个授权码，然后再用该码获取令牌。
> 2、隐藏式（implicit）允许直接向前端颁发令牌。这种方式没有授权码这个中间步骤，所以称为（授权码）"隐藏式"。
> 3、密码式（password）允许用户把用户名和密码，直接告诉该应用。该应用就使用你的密码，申请令牌，这种方式称为"密码式"。
> 4、凭证式（client credentials）适用于没有前端的命令行应用，即在命令行下请求令牌。

## 认证流程
> 第一步客户端调用URL 带参数clientid和Secret。
> 第二步服务器端收到参数之后 先验证clientid和Secret合法性，然后进行登陆操作。登陆成功之后，利用MD5生成授权码code返回给客户端
> 第三步客户端可以将code保存下来，如果需要获取详细的数据的话可以 再以ulr带参数code 去访问服务器端
> 第四部 服务器端验证code的准确性然后生成access token,同时返回给客户端。
> 第五步 如果客户端想要调用服务器端什么资源 访问的url 带上token参数就可以了http://localhost:8080/v1/openapi/userInfo?access_token=XXXXXX


