package com.lee.shiro.realm;

import com.lee.bean.User;
import com.lee.service.UserService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 自定义Realm
 */
public class CustomRealm extends AuthorizingRealm {

    @Autowired
    private UserService userService;

    //加密盐值
    public static final String SALT = "ad123das0-idn";
    public static final String REALM_NAME = "customRealm";

    /**
     * 授权逻辑
     *
     * @param principals 用户认证信息
     * @return AuthorizationInfo
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        //1、从用户认证信息中获取用户名
        String userName = (String) principals.getPrimaryPrincipal();
        //2、根据用户名查询数据库或缓存中此用户的角色
        Set<String> roleSet = getRoleSetByUserName(userName);
        //3、根据用户名查询数据库或缓存中此用户的角色权限
        Set<String> permissionSet = getPermissionSetByUserName(userName);

        //返回根据用户名从数据库查出的角色和权限new出来的AuthorizationInfo
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        authorizationInfo.setStringPermissions(permissionSet);
        authorizationInfo.setRoles(roleSet);
        return authorizationInfo;
    }


    /**
     * 认证逻辑
     *
     * @param token 主体传入的认证信息
     * @return AuthenticationInfo
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        //1、从主体提交的认证token中获取用户名
        String userName = (String) token.getPrincipal();
        //2、查询数据库密码
        String password = getPasswordByUserName(userName);
        //若密码为空(用户名不存在),则返回的AuthenticationInfo为空
        if (password == null) {
            return null;
        }
        //3、返回数据库查出的用户名密码new出来的AuthenticationInfo
        SimpleAuthenticationInfo authenticationInfo =
                new SimpleAuthenticationInfo(userName, password, REALM_NAME);
        //设置加密盐值
        authenticationInfo.setCredentialsSalt(ByteSource.Util.bytes(CustomRealm.SALT));
        return authenticationInfo;
    }


    //根据用户名查询数据库中此用户的角色
    private Set<String> getRoleSetByUserName(String userName) {
        List<String> roleList = userService.getRoleListByUserName(userName);
        return new HashSet<>(roleList);
    }

    //根据用户名查询数据库或缓存中此用户的角色权限
    private Set<String> getPermissionSetByUserName(String userName) {
        //先查此用户的所有角色
        List<String> roleList = userService.getRoleListByUserName(userName);
        //遍历查询每个角色拥有的权限
        Set<String> permissionSet = new HashSet<>();
        if (CollectionUtils.isNotEmpty(roleList)) {
            roleList.stream().forEach(role -> {
                List<String> permissionList = userService.getPermissionListByRoleName(role);
                if (CollectionUtils.isNotEmpty(permissionList)) {
                    permissionSet.addAll(permissionList);
                }
            });
        }
        return permissionSet;
    }

    //根据用户名查询数据库中的密码
    private String getPasswordByUserName(String userName) {
        User user = userService.getUserByUserName(userName);
        if (user == null) return null;
        else return user.getPassword();
    }

    //生成加盐md5加密后的密码
    public static void main(String[] args) {
        System.out.println(new Md5Hash("123", SALT));
    }

}
