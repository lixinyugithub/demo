package com.nbteam.hislite.frame.config.swagger;

import java.util.List;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import com.fasterxml.classmate.TypeResolver;

import springfox.documentation.spi.service.RequestHandlerProvider;
import springfox.documentation.spi.service.contexts.Defaults;
import springfox.documentation.spring.web.DocumentationCache;
import springfox.documentation.spring.web.plugins.DocumentationPluginsBootstrapper;
import springfox.documentation.spring.web.plugins.DocumentationPluginsManager;
import springfox.documentation.spring.web.scanners.ApiDocumentationScanner;


@Component
public class DocumentationPluginsBootstrapperEx extends DocumentationPluginsBootstrapper{
	
	
	RequestMappingHandlerAdapter  requestMappingHandlerAdapter;
	/***
	 * Swagger2 文档API JSON的生成 DocumentationPluginsBootstrapper 插件
	 *  依赖于 RequestMappingHandlerAdapter中的JSON序列号实现。由于引入Feign RPC 调用jar包
	 *  导致类实例化载顺序发生变化。DocumentationPluginsBootstrapper 加载先于RequestMappingHandlerAdapter
	 *  API 序列化JSON 时出现空指针异常。（）
	 *  
	 *   修改方案: 扩展DocumentationPluginsBootstrapper类，@Autowired构造函数上注解，控制requestMappingHandlerAdapter
	 *   加载，才能初始DocumentationPluginsBootstrapper
	 * @param documentationPluginsManager
	 * @param handlerProviders
	 * @param scanned
	 * @param resourceListing
	 * @param typeResolver
	 * @param defaults
	 * @param servletContext
	 * @param requestMappingHandlerAdapter  
	 */
	@Autowired
	public DocumentationPluginsBootstrapperEx(DocumentationPluginsManager documentationPluginsManager,
			List<RequestHandlerProvider> handlerProviders, DocumentationCache scanned,
			ApiDocumentationScanner resourceListing, TypeResolver typeResolver, Defaults defaults,
			ServletContext servletContext,RequestMappingHandlerAdapter  requestMappingHandlerAdapter) {
		super(documentationPluginsManager, handlerProviders, scanned, resourceListing, typeResolver, defaults, servletContext);
		this.requestMappingHandlerAdapter = requestMappingHandlerAdapter;
	}

	public DocumentationPluginsBootstrapperEx(DocumentationPluginsManager documentationPluginsManager,
			List<RequestHandlerProvider> handlerProviders, DocumentationCache scanned,
			ApiDocumentationScanner resourceListing, TypeResolver typeResolver, Defaults defaults,
			ServletContext servletContext) {
		super(documentationPluginsManager, handlerProviders, scanned, resourceListing, typeResolver, defaults, servletContext);
	}
}
