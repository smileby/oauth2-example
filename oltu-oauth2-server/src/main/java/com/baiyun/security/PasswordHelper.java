package com.baiyun.security;


import com.baiyun.model.User;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PasswordHelper {

    private RandomNumberGenerator randomNumberGenerator = new SecureRandomNumberGenerator();

    /**
     * 加密算法
     */
    @Value("${password.algorithmName}")
    private String algorithmName = "md5";
    /**
     * 散列的次数
     */
    @Value("${password.hashIterations}")
    private int hashIterations = 2;

    public void setRandomNumberGenerator(RandomNumberGenerator randomNumberGenerator) {
        this.randomNumberGenerator = randomNumberGenerator;
    }

    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public void setHashIterations(int hashIterations) {
        this.hashIterations = hashIterations;
    }

    public void encryptPassword(User user) {
        /** 生成随机数作为盐值*/
        user.setSalt(randomNumberGenerator.nextBytes().toHex());

        /*
         * MD5加密：
         * 使用SimpleHash类对原始密码进行加密。
         * 第一个参数代表使用MD5方式加密
         * 第二个参数为原始密码
         * 第三个参数为盐值，（用户名  + 盐值的方式）
         * 第四个参数为加密次数
         * 最后用toHex()方法将加密后的密码转成String
         * */
        String newPassword = new SimpleHash(
                algorithmName,
                user.getPassword(),
                ByteSource.Util.bytes(user.getCredentialsSalt()),
                hashIterations).toHex();

        user.setPassword(newPassword);
    }

    /**
     * 根据用户名和盐值加密
     *
     * @param username
     * @param password
     * @param salt
     */
    public String encryptPassword(String username, String password, String salt) {
         /*
         * 使用SimpleHash类对原始密码进行加密。
         * 第一个参数代表使用的加密方式
         * 第二个参数为原始密码
         * 第三个参数为盐值，（用户名  + 盐值的方式）
         * 第四个参数为加密次数，及hash散列次数
         * 最后用toHex()方法将加密后的密码转成String
         * */
        String pwd = new SimpleHash(
                algorithmName,
                password,
                ByteSource.Util.bytes(username + salt),
                hashIterations).toHex();
        return pwd;
    }

}
