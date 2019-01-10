package com.lee.config;

import com.lee.filter.AnyRolesFilter;
import com.lee.shiro.matcher.CredentialsMatcher;
import com.lee.shiro.realm.CustomRealm;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.servlet.Filter;
import java.util.LinkedHashMap;

/**
 * Shiro配置类
 */
@Configuration
public class ShiroConfig {

    //自定义过滤器
    @Bean
    public AnyRolesFilter anyRolesFilter(){
        return new AnyRolesFilter();
    }


    /** 配置过滤规则 */
    @Bean("shiroFilter")
    public ShiroFilterFactoryBean shiroFilter(@Qualifier("securityManager")SecurityManager securityManager){
        ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
        shiroFilter.setSecurityManager(securityManager);

        // 登录页url
        shiroFilter.setLoginUrl("/login.html");
        // 登录成功跳转url
        shiroFilter.setSuccessUrl("/index.html");
        // 没有授权访问跳转的url
        shiroFilter.setUnauthorizedUrl("/403.html");

        //注册自定义的AuthorizationFilter
        LinkedHashMap<String, Filter> filtersMap = new LinkedHashMap<>();
        filtersMap.put("anyRoles", anyRolesFilter());  //对应的过滤器链中校验配置：anyRoles[demoRole,user]
        shiroFilter.setFilters(filtersMap);

        // 配置过滤器链,顺序为从上到下
        // anon:无需认证访问的路径
        // authc:认证后才能访问的路径,所以 "/*" 一般放在最下面
        //当应用开启了rememberMe时,用户下次访问时可以是一个user,但不会是authc,因为authc是需要重新认证的
        LinkedHashMap<String, String> filterChainDefinitionsMap = new LinkedHashMap<>();
        filterChainDefinitionsMap.put("/login.html","anon");
        filterChainDefinitionsMap.put("/subLogin","anon");
        filterChainDefinitionsMap.put("/403.html","anon");
        filterChainDefinitionsMap.put("/druid/**","anon");   //不过滤访问druid监控中心的请求
        filterChainDefinitionsMap.put("/testRole2","anyRoles[demoRole,user]");  //使用自定义filter过滤
        filterChainDefinitionsMap.put("/testPermission","perms[user:insert,user:update,user:select]");
        filterChainDefinitionsMap.put("/**","user");  //其他一切url需要认证后才能访问,"记住我"的用户也被允许
        shiroFilter.setFilterChainDefinitionMap(filterChainDefinitionsMap);

        return shiroFilter;
    }

    //DelegatingFilterProxy:一个filter的代理,名为shiroFilter,通过spring容器来管理其生命周期
    @Bean
    public FilterRegistrationBean delegatingFilterProxy(){
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        DelegatingFilterProxy proxy = new DelegatingFilterProxy();
        proxy.setTargetFilterLifecycle(true);
        proxy.setTargetBeanName("shiroFilter");
        filterRegistrationBean.setFilter(proxy);
        return filterRegistrationBean;
    }

    //自定义密码比较器
    @Bean("credentialsMatcher")
    public CredentialsMatcher credentialsMatcher(){
        return new CredentialsMatcher();
    }


    @Bean
    public EhCacheManager getEhCacheManager() {
        EhCacheManager em = new EhCacheManager();
        em.setCacheManagerConfigFile("classpath:config/shiro-ehcache.xml");
        return em;
    }

    //自定义realm
    @Bean("customRealm")
    public CustomRealm customRealm(@Qualifier("credentialsMatcher") CredentialsMatcher matcher){
        CustomRealm customRealm = new CustomRealm();
        //使用内存缓存
        customRealm.setCacheManager(new MemoryConstrainedCacheManager());
        //设置自定义密码比较器
        customRealm.setCredentialsMatcher(matcher);
        return customRealm;
    }

    /**
     * 记住密码Cookie
     */
    @Bean
    public SimpleCookie rememberMeCookie() {
        SimpleCookie simpleCookie = new SimpleCookie("rememberMe");
        simpleCookie.setHttpOnly(true);
        simpleCookie.setMaxAge(7 * 24 * 60 * 60);//7天
        return simpleCookie;
    }

    /**
     * rememberMe管理器, cipherKey是加密rememberMe Cookie的密钥,由Base64算法生成;默认AES算法;
     */
    @Bean
    public CookieRememberMeManager rememberMeManager(SimpleCookie rememberMeCookie) {
        CookieRememberMeManager manager = new CookieRememberMeManager();
        manager.setCipherKey(Base64.decode("Z3VucwAAAAAAAAAAAAAAAA=="));
        manager.setCookie(rememberMeCookie);
        return manager;
    }


    //创建SecurityManager
    @Bean("securityManager")
    public SecurityManager securityManager(@Qualifier("customRealm")CustomRealm customRealm){
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(customRealm);
        //设置rememberMe管理器
        securityManager.setRememberMeManager(rememberMeManager(rememberMeCookie()));
        //使用ehCache缓存
        securityManager.setCacheManager(getEhCacheManager());
        return securityManager;
    }



    //开启aop注解方式登录、校验角色和权限
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(@Qualifier("securityManager")SecurityManager securityManager){
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }
    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator(){
        DefaultAdvisorAutoProxyCreator creator = new DefaultAdvisorAutoProxyCreator();
        creator.setProxyTargetClass(true);
        return creator;
    }


}
