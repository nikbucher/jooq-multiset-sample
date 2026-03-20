package io.bucher.sample;

import org.springframework.boot.SpringApplication;

public class TestJooqMultisetaggSampleApplication {

	void main(String[] args) {
		SpringApplication.from(JooqMultisetaggSampleApplication::main)
				.with(TestcontainersConfiguration.class)
				.run(args);
	}

}
