package dev.przbetkier.twitteo

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@EnableAutoConfiguration
@ComponentScan
@ConfigurationPropertiesScan
class TwitteoApplication {
	companion object {
		@JvmStatic fun main(args: Array<String>) {
			SpringApplication.run(TwitteoApplication::class.java, *args)
		}
	}
}
