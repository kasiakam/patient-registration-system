package com.mycompany.patientregistrationweb.services;

import com.mycompany.patientregistrationweb.model.PatientRegistry;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Initializes application-scoped resources.
 * Creates exactly one shared {@link PatientRegistry} instance for the entire
 * application lifecycle without using {@code static} for model storage.
 *
 * @author Katarzyna Kamińska
 * @version 1.0
 */
@WebListener
public class AppBootstrapListener implements ServletContextListener {

    /** ServletContext attribute name for the shared model instance. */
    private final String registryAttributeName = "patientRegistry";

    /**
     * Creates and stores the shared registry at application startup.
     *
     * @param sce servlet context event
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        ctx.setAttribute(registryAttributeName, new PatientRegistry());
    }

    /**
     * Removes shared objects at application shutdown.
     *
     * @param sce servlet context event
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        ctx.removeAttribute(registryAttributeName);
    }
}
