package com.example.webfluxemailservice;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;
import org.thymeleaf.spring6.SpringWebFluxTemplateEngine;

import java.util.List;
import java.util.Map;
import java.util.Set;

@SpringBootApplication
public class WebfluxEmailServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebfluxEmailServiceApplication.class, args);
	}

	@Bean
	RouterFunction routerFunction(EmailClient emailClient) {
		return RouterFunctions.route()
				.POST("/email/{type}", serverRequest -> {
					try {
						var type = EmailType.valueOf(serverRequest.pathVariable("type"));
						var emailBody = emailClient
								.getEmailBody(type, new EmailBody("John", "c01", "a03", List.of("1", "2")));
						System.out.println(emailBody);
						return ServerResponse.ok().bodyValue("Sent Successfully");
					} catch (Exception e) {
						return ServerResponse.badRequest().bodyValue(e.getMessage());
					}
				})
				.build();
	}
}

@Controller
@RequiredArgsConstructor
class MyControllers {
	private final EmailClient emailClient;
	@GetMapping("/greeting")
	public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
		model.addAttribute("name", name);
		return "greeting";
	}

	@GetMapping("/e-mail")
	public String email(@RequestParam(name="type", required=true, defaultValue = "request") EmailType name, Model model) {
		model.addAttribute("type", name.name().toLowerCase());
		model.addAttribute("emailBody",new EmailBody("John", "c01", "a03", List.of("1", "2")));
		return "email";
	}
}

enum EmailType {
	REQUEST, APPROVE, REJECT, CANCEL
}

record EmailBody(String custName, String custId, String ageId, List<String> ids) {}

@Configuration
@RequiredArgsConstructor
class EmailClient {
	private final SpringWebFluxTemplateEngine templateEngine;

	public String getEmailBody(EmailType type, EmailBody emailBody) {
		Context thymeleafContext = new Context();
		thymeleafContext.setVariable("type", type.name().toLowerCase());
		thymeleafContext.setVariable("emailBody", emailBody);
		return templateEngine.process("email", thymeleafContext);
	}
}