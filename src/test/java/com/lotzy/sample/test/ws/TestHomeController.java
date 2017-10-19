package com.lotzy.sample.test.ws;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.lotzy.sample.test.JUnitConfigurator;

/**
 * <pre>
 * Title: TestHomeController class
 * Description: JUnit test class for the REST end-points served by HomeController. Spring security context is not initialized.
 * </pre>
 *
 * @author Lotzy
 * @version 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = JUnitConfigurator.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@WebAppConfiguration
public class TestHomeController {

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
	}


	/**
	 * Test method for {@link com.lotzy.sample.ws.GreetingRestController#greet()}.
	 */
	@Test
	public void testGreet() throws Exception {
		this.mockMvc.perform(get("/rest/greet").accept(MediaType.TEXT_PLAIN))
		.andDo(print())
		.andExpect(status().isOk())
		.andExpect(content().string("Hello world!"));
	}
}
