package com.netnovelreader

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.Contact
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@EnableSwagger2
@Configuration
class SwaggerConfig {

    @Bean
    fun createRestApi(): Docket {
        return Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo())
            // 是否开启
            .enable(true)
            .select()
            // 扫描的路径包
            .apis(RequestHandlerSelectors.basePackage("com.netnovelreader"))
            // 指定路径处理PathSelectors.any()代表所有的路径
            .paths(PathSelectors.any()).build().pathMapping("/")
    }

    private fun apiInfo(): ApiInfo {
        return ApiInfoBuilder()
            .title("NetNovelReader")
            .description("Reader")
            .contact(Contact("yangbo", "https://github.com/ya-b", "1599407175@qq.com"))
            .version("0.1.1")
            .build()
    }
}