package com.mdc.mcat.config;

import com.mdc.mspring.context.annotation.ComponentScan;
import com.mdc.mspring.context.annotation.Configuration;
import com.mdc.mspring.context.annotation.Import;
import com.mdc.mspring.mvc.config.WebMvcConfiguration;

/**
 * @author ShuangShu
 * @version 1.0
 * @description: TODO
 * @date 2023/9/10 16:35
 */
@Configuration
@Import({WebMvcConfiguration.class})
@ComponentScan("com.mdc.mspring.webapp")
public class WebAppConfig {

}
