package config

import java.util.Locale

import nz.net.ultraq.thymeleaf.LayoutDialect
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, EnableAspectJAutoProxy}
import org.springframework.context.support.{PropertySourcesPlaceholderConfigurer, ReloadableResourceBundleMessageSource}
import org.springframework.core.io.ClassPathResource
import org.springframework.web.servlet.ViewResolver
import org.springframework.web.servlet.config.annotation.{DefaultServletHandlerConfigurer, EnableWebMvc, ResourceHandlerRegistry, WebMvcConfigurerAdapter}
import org.springframework.web.servlet.i18n.{CookieLocaleResolver, LocaleChangeInterceptor}
import org.thymeleaf.spring4.SpringTemplateEngine
import org.thymeleaf.spring4.view.ThymeleafViewResolver
import org.thymeleaf.templateresolver.ServletContextTemplateResolver

@Configuration
@EnableWebMvc
@EnableAspectJAutoProxy
@ComponentScan(basePackages = Array("aop", "$name;format=\"camel\"$"))
class WebConfig extends WebMvcConfigurerAdapter {

  override def addResourceHandlers(registry: ResourceHandlerRegistry): Unit = {
    registry.addResourceHandler("/resources/**").addResourceLocations("/resources/")
  }

  override def configureDefaultServletHandling(configurer: DefaultServletHandlerConfigurer): Unit = {
    configurer.enable
  }

  @Bean
  def properties: PropertySourcesPlaceholderConfigurer = {
    val propertySourcesPlaceholderConfigurer: PropertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer
    propertySourcesPlaceholderConfigurer.setLocations(
      new ClassPathResource("version.properties"),
      new ClassPathResource("cdn.properties"),
      new ClassPathResource("environment.properties"))
    propertySourcesPlaceholderConfigurer
  }

  @Bean
  def templateResolver: ServletContextTemplateResolver = {
    val resolver: ServletContextTemplateResolver = new ServletContextTemplateResolver
    resolver.setPrefix("/WEB-INF/view/")
    resolver.setSuffix(".html")
    resolver.setTemplateMode("HTML5")
    resolver.setCharacterEncoding("UTF-8")
    resolver.setCacheable(false)
    resolver
  }

  @Bean
  def templateEngine: SpringTemplateEngine = {
    val engine: SpringTemplateEngine = new SpringTemplateEngine
    engine.setTemplateResolver(templateResolver)
    engine.addDialect(new LayoutDialect())
    engine
  }

  @Bean
  def viewResolver: ViewResolver = {
    val viewResolver: ThymeleafViewResolver = new ThymeleafViewResolver
    viewResolver.setTemplateEngine(templateEngine)
    viewResolver.setOrder(1)
    viewResolver.setViewNames(Array[String]("*"))
    viewResolver.setCache(false)
    viewResolver.setCharacterEncoding("UTF-8")
    viewResolver
  }

  @Bean
  def messageSource: ReloadableResourceBundleMessageSource = {
    val source: ReloadableResourceBundleMessageSource = new ReloadableResourceBundleMessageSource
    source.setBasename("classpath:i18n/messages")
    source.setDefaultEncoding("UTF-8")
    source
  }

  @Bean
  def localeResolver: CookieLocaleResolver = {
    val localeResolver: CookieLocaleResolver = new CookieLocaleResolver
    localeResolver.setCookiePath("/")
    localeResolver.setCookieName("lang")
    localeResolver.setCookieMaxAge(Integer.MAX_VALUE)
    localeResolver.setDefaultLocale(Locale.TAIWAN)
    localeResolver
  }

  @Bean
  def defaultLocale: Locale = Locale.TAIWAN

  @Bean
  def localeChangeInterceptor: LocaleChangeInterceptor = {
    val localeChangeInterceptor: LocaleChangeInterceptor = new LocaleChangeInterceptor
    localeChangeInterceptor.setParamName("lang")
    localeChangeInterceptor
  }

  @Bean
  def version(@Value("${version}") versionValue: String): String = versionValue

  @Bean
  def cdn(@Value("${cdn}") cdnValue: String): String = cdnValue

}
