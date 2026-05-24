package com.bfzy.platform.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 全局配置.
 * <p>
 * 扫描所有模块的 Mapper 接口。
 * 分页插件由 MyBatis-Plus 3.5.16+ 自动配置，无需手动声明。
 * </p>
 *
 * @author zhangyu
 */
@Configuration
@MapperScan("com.bfzy.platform.**.mapper")
public class MybatisPlusConfig {

}
