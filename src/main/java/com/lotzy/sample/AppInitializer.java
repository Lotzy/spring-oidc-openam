package com.lotzy.sample;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * <pre>
 * Title: AppInitializer class
 * Description: As of servlet 3.0 there is no need for web.xml so to run Spring REST, this class is needed to initialize the servlet container
 * </pre>
 *
 * @author Lotzy
 * @version 1.0
 */
public class AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

  @Override
  protected Class<?>[] getRootConfigClasses() {
      return new Class[] { AppConfigurator.class };
  }

  @Override
  protected Class<?>[] getServletConfigClasses() {
      return null;
  }

  @Override
  protected String[] getServletMappings() {
      return new String[] { "/*" };
  }

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		super.onStartup(servletContext);
		/*
		 * Needed to support a "request" scope in Spring Security filters,
		 * since they're configured as a Servlet Filter. But not necessary
		 * if they're configured as interceptors in Spring MVC.
		 */
		servletContext.addListener(new RequestContextListener());
	}

}
