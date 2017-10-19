package com.lotzy.sample.ws;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * <pre>
 * Title: HomeController class
 * Description: RestController implementation
 * </pre>
 *
 * @author Lotzy
 * @version 1.0
 */
@RestController
@RequestMapping("/rest")
public class HomeController {

	private static final Logger log = LoggerFactory.getLogger(HomeController.class);

	/**
	 * Simple REST method without params
	 * @return String representing the greeting
	 */
	@RequestMapping(value="/greet", method=RequestMethod.GET)
	public ResponseEntity<String> greet() {
		log.debug("Greet called!");
		String greeting = "Hello world!";
		return new ResponseEntity<String>(greeting, HttpStatus.OK);
	}

	/**
	 * Prints the authenticated user Principal
	 * @param principal - Principal to print
	 * @return JSON representing the authenticated user principal
	 */
	@RequestMapping(value="/user", method=RequestMethod.GET)
	public Principal user(Principal principal) {
		return principal;
	}

}
