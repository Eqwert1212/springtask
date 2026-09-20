package kz.bitlab.G118springfirstapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

// This local directory has no login accounts; CSRF is configured separately.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class G118SpringFirstAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(G118SpringFirstAppApplication.class, args);
	}

}

