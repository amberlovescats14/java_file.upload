package com.amber.uploadingfiles;

import com.amber.uploadingfiles.services.StorageProperties;
import com.amber.uploadingfiles.services.StorageSvc;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class UploadingFilesApplication {

	public static void main(String[] args) {
		SpringApplication.run(UploadingFilesApplication.class, args);
	}

	@Bean
	CommandLineRunner init(StorageSvc storageSvc) {
		return (args) -> {
			storageSvc.deleteAll();
			storageSvc.init();
		};
	}
}