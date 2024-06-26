package br.com.ada.currencyapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CurrencyApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CurrencyApiApplication.class, args);
	}

}
