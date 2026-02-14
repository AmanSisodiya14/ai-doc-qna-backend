package com.tech.aidocqna;

import com.tech.aidocqna.config.AppProperties;
import com.tech.aidocqna.config.EmbeddingProperties;
import com.tech.aidocqna.config.GroqProperties;
import com.tech.aidocqna.config.TranscriptionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableConfigurationProperties({
    AppProperties.class,
    GroqProperties.class,
    EmbeddingProperties.class,
    TranscriptionProperties.class
})
public class AidocqnaApplication {

	public static void main(String[] args) {
		SpringApplication.run(AidocqnaApplication.class, args);
	}

}
