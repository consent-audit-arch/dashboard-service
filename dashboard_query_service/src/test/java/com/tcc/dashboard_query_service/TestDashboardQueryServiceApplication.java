package com.tcc.dashboard_query_service;

import org.springframework.boot.SpringApplication;

public class TestDashboardQueryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(DashboardQueryServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
