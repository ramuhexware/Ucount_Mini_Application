package com.freddieapp.ratecalculator;

import com.freddieapp.ratecalculator.config.AppConfig;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.File;

public class RateCalculatorServer {

    private static final Logger log = LoggerFactory.getLogger(RateCalculatorServer.class);
    private static final int PORT = 8090;

    public static void main(String[] args) throws Exception {
        log.info("Starting Rate Calculator Service - Pure Spring Framework (No Spring Boot) on port {}", PORT);

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(PORT);
        tomcat.getConnector(); // Initialize default HTTP connector

        // Create base directory for embedded tomcat
        File baseDir = new File(System.getProperty("java.io.tmpdir"), "tomcat-rate-calc");
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        tomcat.setBaseDir(baseDir.getAbsolutePath());

        // Create Context
        Context context = tomcat.addContext("", baseDir.getAbsolutePath());

        // Create Spring Web Context
        AnnotationConfigWebApplicationContext springContext = new AnnotationConfigWebApplicationContext();
        springContext.register(AppConfig.class);

        // Register Spring DispatcherServlet
        DispatcherServlet dispatcherServlet = new DispatcherServlet(springContext);
        Tomcat.addServlet(context, "dispatcherServlet", dispatcherServlet).setLoadOnStartup(1);
        context.addServletMappingDecoded("/*", "dispatcherServlet");

        log.info("Embedded Tomcat configured with Spring DispatcherServlet. Starting server...");
        tomcat.start();
        log.info("Rate Calculator Service is running at http://localhost:{}/", PORT);

        tomcat.getServer().await();
    }
}
