package com.lee.controller;

import com.lee.bean.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
public class UserController {
    //跳转templates中的登录页login.html
    @RequestMapping("/login.html")
    public String toLogin(){
        return "login";
    }

    /**
     * ================================配置文件+编码 方式登录、校验角色和权限====================================
     * 需要先登录,若未登录不会自动跳转登录页
     * 没有权限会抛出特定的异常
     */
    @PostMapping(value = "/subLogin",produces = "application/json;charset=utf-8")
    public String login(User user){
        //创建主体
        Subject subject = SecurityUtils.getSubject();
        //根据主体信息生成token
        UsernamePasswordToken token =
                new UsernamePasswordToken(user.getUsername(), user.getPassword());

        //设置记住我,实现免登录，过期时间在配置文件中配置
        token.setRememberMe(true);

        //主体提交认证请求
        try {
            subject.login(token);
            //抛异常的方式校验角色
            subject.checkRoles("admin","user");
            //boolean方式校验角色
//            if (subject.hasRoles(Arrays.asList("admin","user")){
//                System.out.println("有角色: admin , user");
//            }
            //校验权限
            subject.checkPermissions("user:insert","user:update","user:select");
        } catch (AuthenticationException e) {
            e.printStackTrace();
            log.error(e.getMessage(),e);
            return "403";
        } catch (AuthorizationException e) {
            e.printStackTrace();
            log.error(e.getMessage(),e);
            return "403";
        }
        //认证成功并校验成功
        log.info("有角色: admin , user ; 有权限 user:insert , user:update,user:select");
        return "index";
    }

    //配置文件+编码 方式校验角色失败例子,抛出异常,返回状态码500
    //此处使用自定义过滤器 配置在spring-shiro.xml中过滤链的/testRole2 = anyRoles["demoRole","user"]
    @GetMapping("/testRole2")
    @ResponseBody
    public String testRole2(){
        return "testRole success";
    }


    /**
     * ================================注解方式登录、校验角色和权限 ,不能同时使用配置====================================
     * 若未登录会自动跳转spring-shiro.xml中配置的loginUrl,但是不会跳转回去原请求的url
     * 没有权限会跳转spring-shiro.xml中配置的unauthorizedUrl
     */

    //注解校验角色成功例子
    @RequiresRoles({"admin","user"})   //拥有全部角色才能访问，相当于spring-shiro.xml中过滤链的/testRole1 = roles["admin","user"]
    @GetMapping("/testRole1")
    @ResponseBody
    public String testRole1(){
        return "testRole success";
    }


    //注解校验权限成功例子
    @RequiresPermissions({"user:insert","user:update","user:select"})
    @GetMapping("/testPermission")
    @ResponseBody
    public String testPermission(){
        return "testPermission success";
    }


    /**
     * 退出登录
     */
    @RequestMapping("/logout")
    public String logout(){
        //取得主体
        Subject subject = SecurityUtils.getSubject();
        if (subject!=null){
            subject.logout();
        }
        //返回登录页
        return "login";
    }

}
