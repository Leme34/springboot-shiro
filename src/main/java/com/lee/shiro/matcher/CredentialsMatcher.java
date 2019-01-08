package com.lee.shiro.matcher;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;

//自定义密码比较器
public class CredentialsMatcher extends HashedCredentialsMatcher {

    //密码比对逻辑
    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        this.setHashAlgorithmName("md5");  //使用md5算法
        this.setHashIterations(1);         //使用算法1次加密
        //执行默认认证处理
        return super.doCredentialsMatch(token, info);
    }
}
