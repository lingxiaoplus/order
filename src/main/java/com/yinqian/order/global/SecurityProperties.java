package com.yinqian.order.global;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author renml
 * @date 2020/11/19 14:40
 */
@Data
@ConfigurationProperties(prefix = "cors")
@Component
public class SecurityProperties {
    private List<String> allowedOrigins;
    private List<String> allowedMethods;
    private List<String> ignorePaths;
}
