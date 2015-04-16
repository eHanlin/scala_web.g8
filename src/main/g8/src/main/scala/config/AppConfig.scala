package config

import java.util.Locale

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}
import org.springframework.context.support.{PropertySourcesPlaceholderConfigurer, ReloadableResourceBundleMessageSource}
import org.springframework.core.io.ClassPathResource

@Configuration
@ComponentScan(basePackages = Array("$name;format="camel"$.repository", "$name;format="camel"$.service"))
class AppConfig {

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
  def messageSource: ReloadableResourceBundleMessageSource = {
    val source: ReloadableResourceBundleMessageSource = new ReloadableResourceBundleMessageSource
    source.setBasename("classpath:i18n/messages")
    source.setDefaultEncoding("UTF-8")
    source
  }

  @Bean
  def defaultLocale: Locale = Locale.TAIWAN

  @Bean
  def version(@Value("\${version}") versionValue: String): String = versionValue

  @Bean
  def cdn(@Value("\${cdn}") cdnValue: String): String = cdnValue

}
