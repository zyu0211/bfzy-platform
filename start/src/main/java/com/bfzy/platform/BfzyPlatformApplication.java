package com.bfzy.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * BFZY Platform 全局启动入口.
 * <p>
 * 通过 {@code scanBasePackages} 扫描 {@code com.bfzy.platform} 下的所有包，
 * 自动发现 common、hello-world 等模块的 Spring Bean（Controller、Service、Config 等）。
 * </p>
 */
@SpringBootApplication(scanBasePackages = "com.bfzy.platform")
public class BfzyPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(BfzyPlatformApplication.class, args);
    }
}
