package com.lee.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DruidConfig {

    //配置Druid的监控
    //1、配置一个管理后台的Servlet
    @Bean
    public ServletRegistrationBean statViewServlet(){
        ServletRegistrationBean bean = new ServletRegistrationBean(new StatViewServlet(), "/druid/*");
        Map<String,String> initParams = new HashMap<>();  //构建参数map
        //设置登录druid控制中心的用户名密码
        initParams.put("loginUsername","admin");
        initParams.put("loginPassword","123456");
        //IP白名单
        initParams.put("allow","");//默认就是允许所有访问
        //IP黑名单
        initParams.put("deny","192.168.11.100");
        //登录监控中心的用户名密码
        initParams.put("LoginUsername","admin");
        initParams.put("LoginPassword","admin");

        //能够重置数据
        initParams.put("resetEnable","false");
        //设置参数map
        bean.setInitParameters(initParams);
        return bean;
    }

    //2、配置一个web监控的filter
    @Bean
    public FilterRegistrationBean webStatFilter(){
        FilterRegistrationBean bean = new FilterRegistrationBean(new WebStatFilter());
        //过滤所有
        bean.addUrlPatterns("/*");
        //排除静态资源
        bean.addInitParameter("exclusions","*.js,*.css,*.gif,*.jpg,*.png,/druid/*");
        return  bean;
    }


    @Bean("dataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")  //从配置文件中读取
    public DataSource dataSource(){
        return DataSourceBuilder.create().type(DruidDataSource.class).build();
    }


//    @Value("${mybatis.mapper-locations}")
//    private String MAPPER_LOCATIONS;
//    @Bean
//    public SqlSessionFactoryBean sqlSessionFactory(@Qualifier("dataSource")DataSource dataSource){
//        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
//        bean.setDataSource(dataSource);
//        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
//        //从配置文件读取mapper的位置
//        try {
//            bean.setMapperLocations(resolver.getResources(MAPPER_LOCATIONS));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return bean;
//    }


}
