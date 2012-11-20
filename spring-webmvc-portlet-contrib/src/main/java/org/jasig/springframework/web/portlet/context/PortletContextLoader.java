/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.springframework.web.portlet.context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.portlet.PortletContext;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.portlet.context.ConfigurablePortletApplicationContext;
import org.springframework.web.portlet.context.PortletApplicationContextUtils;
import org.springframework.web.portlet.context.XmlPortletApplicationContext;

/**
 * Performs the actual initialization work for the root application context.
 * Called by {@link PortletContextLoaderListener}.
 *
 * <p>Looks for a {@link #CONTEXT_CLASS_PARAM "portletContextClass"} context-param
 * at the <code>web.xml</code> level to specify the context
 * class type, falling back to the default of
 * {@link ContribXmlPortletApplicationContext}
 * if not found. With the default PortletContextLoader implementation, any context class
 * specified needs to implement the ConfigurablePortletApplicationContext interface.
 *
 * <p>Processes a {@link #CONFIG_LOCATION_PARAM "portletContextConfigLocation"}
 * context-param at the <code>web.xml</code> level and passes its value to the context instance, parsing it into
 * potentially multiple file paths which can be separated by any number of
 * commas and spaces, e.g. "WEB-INF/portletApplicationContext1.xml,
 * WEB-INF/portletApplicationContext2.xml". Ant-style path patterns are supported as well,
 * e.g. "WEB-INF/portlet*Context.xml,WEB-INF/springPortlet*.xml" or "WEB-INF/&#42;&#42;/portlet*Context.xml".
 * If not explicitly specified, the context implementation is supposed to use a
 * default location (with {@link ContribXmlPortletApplicationContext}: "/WEB-INF/portletApplicationContext.xml").
 *
 * <p>Note: In case of multiple config locations, later bean definitions will
 * override ones defined in previously loaded files, at least when using one of
 * Spring's default ApplicationContext implementations. This can be leveraged
 * to deliberately override certain bean definitions via an extra XML file.
 *
 * <p>Above and beyond loading the root portlet application context, this class
 * can optionally load or obtain and hook up a shared parent context to
 * the root application context. See the
 * {@link #loadParentContext(PortletContext)} method for more information.
 *
 * @author Juergen Hoeller
 * @author Colin Sampaleanu
 * @author Sam Brannen
 * @author Eric Dalquist
 * @see PortletContextLoaderListener
 * @see PortletApplicationContext
 * @see XmlPortletApplicationContext
 */
public class PortletContextLoader {
    
    /**
     * Config param for the root {@link PortletApplicationContext} implementation class to use: {@value}
     * @see #determineContextClass(PortletContext)
     * @see #createWebApplicationContext(PortletContext, ApplicationContext)
     */
    public static final String CONTEXT_CLASS_PARAM = "portletContextClass";

    /**
     * Config param for the root PortletApplicationContext id,
     * to be used as serialization id for the underlying BeanFactory: {@value}
     */
    public static final String CONTEXT_ID_PARAM = "portletContextId";

    /**
     * Config param for which {@link ApplicationContextInitializer} classes to use
     * for initializing the web application context: {@value}
     * @see #customizeContext(PortletContext, ConfigurablePortletApplicationContext)
     */
    public static final String CONTEXT_INITIALIZER_CLASSES_PARAM = "portletContextInitializerClasses";

    /**
     * Name of portlet context parameter (i.e., {@value}) that can specify the
     * config location for the root context, falling back to the implementation's
     * default otherwise.
     * @see org.springframework.web.portlet.context.XmlPortletApplicationContext#DEFAULT_CONFIG_LOCATION
     */
    public static final String CONFIG_LOCATION_PARAM = "portletContextConfigLocation";

    /**
     * Name of the class path resource (relative to the PortletContextLoader class)
     * that defines PortletContextLoader's default strategy names.
     */
    private static final String DEFAULT_STRATEGIES_PATH = "PortletContextLoader.properties";


    private static final Properties defaultStrategies;

    static {
        // Load default strategy implementations from properties file.
        // This is currently strictly internal and not meant to be customized
        // by application developers.
        try {
            ClassPathResource resource = new ClassPathResource(DEFAULT_STRATEGIES_PATH, PortletContextLoader.class);
            defaultStrategies = PropertiesLoaderUtils.loadProperties(resource);
        }
        catch (IOException ex) {
            throw new IllegalStateException("Could not load 'PortletContextLoader.properties': " + ex.getMessage());
        }
    }


    /**
     * Map from (thread context) ClassLoader to corresponding 'current' WebApplicationContext.
     */
    private static final Map<ClassLoader, PortletApplicationContext> currentContextPerThread =
            new ConcurrentHashMap<ClassLoader, PortletApplicationContext>(1);

    /**
     * The 'current' WebApplicationContext, if the PortletContextLoader class is
     * deployed in the web app ClassLoader itself.
     */
    private static volatile PortletApplicationContext currentContext;
    
    /**
     * The {@link ServletContext} the portlet application exists in
     */
    private final ServletContext servletContext;

    /**
     * The root PortletApplicationContext instance that this loader manages.
     */
    private PortletApplicationContext context;

    /**
     * Holds BeanFactoryReference when loading parent factory via
     * ContextSingletonBeanFactoryLocator.
     */
    private BeanFactoryReference parentContextRef;
    
    public PortletContextLoader(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * Initialize Spring's portlet application context for the given portlet context,
     * using the application context provided at construction time, or creating a new one
     * according to the "{@link #CONTEXT_CLASS_PARAM contextClass}" and
     * "{@link #CONFIG_LOCATION_PARAM contextConfigLocation}" context-params.
     * @param portletContext current portlet context
     * @return the new PortletApplicationContext
     * @see #CONTEXT_CLASS_PARAM
     * @see #CONFIG_LOCATION_PARAM
     */
    public PortletApplicationContext initWebApplicationContext(PortletContext portletContext) {
        if (portletContext.getAttribute(PortletApplicationContext.ROOT_PORTLET_APPLICATION_CONTEXT_ATTRIBUTE) != null) {
            throw new IllegalStateException(
                    "Cannot initialize context because there is already a root portlet application context present - " +
                    "check whether you have multiple PortletContextLoader* definitions in your portlet.xml!");
        }

        Log logger = LogFactory.getLog(PortletContextLoader.class);
        portletContext.log("Initializing Spring root PortletApplicationContext");
        if (logger.isInfoEnabled()) {
            logger.info("Root portlet PortletApplicationContext: initialization started");
        }
        long startTime = System.nanoTime();

        try {
            // Store context in local instance variable, to guarantee that
            // it is available on PortletContext shutdown.
            if (this.context == null) {
                this.context = createPortletApplicationContext(portletContext);
            }
            if (this.context instanceof ConfigurablePortletApplicationContext) {
                configureAndRefreshPortletApplicationContext((ConfigurablePortletApplicationContext)this.context, portletContext);
            }
            portletContext.setAttribute(PortletApplicationContext.ROOT_PORTLET_APPLICATION_CONTEXT_ATTRIBUTE, this.context);

            ClassLoader ccl = Thread.currentThread().getContextClassLoader();
            if (ccl == PortletContextLoader.class.getClassLoader()) {
                currentContext = this.context;
            }
            else if (ccl != null) {
                currentContextPerThread.put(ccl, this.context);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Published root PortletApplicationContext as PortletContext attribute with name [" +
                        PortletApplicationContext.ROOT_PORTLET_APPLICATION_CONTEXT_ATTRIBUTE + "]");
            }
            if (logger.isInfoEnabled()) {
                long elapsedTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
                logger.info("Root PortletApplicationContext: initialization completed in " + elapsedTime + " ms");
            }

            return this.context;
        }
        catch (RuntimeException ex) {
            logger.error("Portlet context initialization failed", ex);
            portletContext.setAttribute(PortletApplicationContext.ROOT_PORTLET_APPLICATION_CONTEXT_ATTRIBUTE, ex);
            throw ex;
        }
        catch (Error err) {
            logger.error("Portlet context initialization failed", err);
            portletContext.setAttribute(PortletApplicationContext.ROOT_PORTLET_APPLICATION_CONTEXT_ATTRIBUTE, err);
            throw err;
        }
    }

    /**
     * Instantiate the root PortletApplicationContext for this loader, either the
     * default context class or a custom context class if specified.
     * <p>This implementation expects custom contexts to implement the
     * {@link ConfigurablePortletApplicationContext} interface.
     * Can be overridden in subclasses.
     * <p>In addition, {@link #customizeContext} gets called prior to refreshing the
     * context, allowing subclasses to perform custom modifications to the context.
     * @param sc current portlet context
     * @return the root WebApplicationContext
     * @see ConfigurablePortletApplicationContext
     */
    protected PortletApplicationContext createPortletApplicationContext(PortletContext sc) {
        Class<?> contextClass = determineContextClass(sc);
        if (!PortletApplicationContext.class.isAssignableFrom(contextClass)) {
            throw new ApplicationContextException("Custom context class [" + contextClass.getName() +
                    "] is not of type [" + PortletApplicationContext.class.getName() + "]");
        }
        PortletApplicationContext wac =
                (PortletApplicationContext) BeanUtils.instantiateClass(contextClass);
        return wac;
    }

    protected void configureAndRefreshPortletApplicationContext(ConfigurablePortletApplicationContext pac, PortletContext pc) {
        if (ObjectUtils.identityToString(pac).equals(pac.getId())) {
            // The application context id is still set to its original default value
            // -> assign a more useful id based on available information
            String idParam = pc.getInitParameter(CONTEXT_ID_PARAM);
            if (idParam != null) {
                pac.setId(idParam);
            }
            else {
                // Generate default id...
                pac.setId(ConfigurablePortletApplicationContext.APPLICATION_CONTEXT_ID_PREFIX +
                        ObjectUtils.getDisplayString(pc.getPortletContextName()));
            }
        }

        // Determine parent for root web application context, if any.
        ApplicationContext parent = loadParentContext(pc);

        pac.setParent(parent);
        pac.setPortletContext(pc);
        String initParameter = pc.getInitParameter(CONFIG_LOCATION_PARAM);
        if (initParameter != null) {
            pac.setConfigLocation(initParameter);
        }
        else {
            try {
                pac.setConfigLocation("/WEB-INF/portletApplicationContext.xml");
            }
            catch (UnsupportedOperationException e) {
                //Ignore, may get triggered if the context doesn't support config locations
            }
        }
        customizeContext(pc, pac);
        pac.refresh();
    }

    /**
     * Return the PortletApplicationContext implementation class to use, either the
     * default XmlPortletApplicationContext or a custom context class if specified.
     * @param portletContext current portlet context
     * @return the WebApplicationContext implementation class to use
     * @see #CONTEXT_CLASS_PARAM
     * @see org.springframework.web.portlet.context.XmlPortletApplicationContext
     */
    protected Class<?> determineContextClass(PortletContext portletContext) {
        String contextClassName = portletContext.getInitParameter(CONTEXT_CLASS_PARAM);
        if (contextClassName != null) {
            try {
                return ClassUtils.forName(contextClassName, ClassUtils.getDefaultClassLoader());
            }
            catch (ClassNotFoundException ex) {
                throw new ApplicationContextException(
                        "Failed to load custom context class [" + contextClassName + "]", ex);
            }
        }
        else {
            contextClassName = defaultStrategies.getProperty(PortletApplicationContext.class.getName());
            try {
                return ClassUtils.forName(contextClassName, PortletContextLoader.class.getClassLoader());
            }
            catch (ClassNotFoundException ex) {
                throw new ApplicationContextException(
                        "Failed to load default context class [" + contextClassName + "]", ex);
            }
        }
    }

    /**
     * Return the {@link ApplicationContextInitializer} implementation classes to use
     * if any have been specified by {@link #CONTEXT_INITIALIZER_CLASSES_PARAM}.
     * @param portletContext current portlet context
     * @see #CONTEXT_INITIALIZER_CLASSES_PARAM
     */
    @SuppressWarnings("unchecked")
    protected List<Class<ApplicationContextInitializer<ConfigurableApplicationContext>>>
            determineContextInitializerClasses(PortletContext portletContext) {
        String classNames = portletContext.getInitParameter(CONTEXT_INITIALIZER_CLASSES_PARAM);
        List<Class<ApplicationContextInitializer<ConfigurableApplicationContext>>> classes =
            new ArrayList<Class<ApplicationContextInitializer<ConfigurableApplicationContext>>>();
        if (classNames != null) {
            for (String className : StringUtils.tokenizeToStringArray(classNames, ",")) {
                try {
                    Class<?> clazz = ClassUtils.forName(className, ClassUtils.getDefaultClassLoader());
                    Assert.isAssignable(ApplicationContextInitializer.class, clazz,
                            "class [" + className + "] must implement ApplicationContextInitializer");
                    classes.add((Class<ApplicationContextInitializer<ConfigurableApplicationContext>>)clazz);
                }
                catch (ClassNotFoundException ex) {
                    throw new ApplicationContextException(
                            "Failed to load context initializer class [" + className + "]", ex);
                }
            }
        }
        return classes;
    }

    /**
     * Customize the {@link ConfigurablePortletApplicationContext} created by this
     * PortletContextLoader after config locations have been supplied to the context
     * but before the context is <em>refreshed</em>.
     * <p>The default implementation {@linkplain #determineContextInitializerClasses(PortletContext)
     * determines} what (if any) context initializer classes have been specified through
     * {@linkplain #CONTEXT_INITIALIZER_CLASSES_PARAM context init parameters} and
     * {@linkplain ApplicationContextInitializer#initialize invokes each} with the
     * given web application context.
     * <p>Any {@code ApplicationContextInitializers} implementing
     * {@link org.springframework.core.Ordered Ordered} or marked with @{@link
     * org.springframework.core.annotation.Order Order} will be sorted appropriately.
     * @param portletContext the current portlet context
     * @param applicationContext the newly created application context
     * @see #createPortletApplicationContext(PortletContext)
     * @see #CONTEXT_INITIALIZER_CLASSES_PARAM
     * @see ApplicationContextInitializer#initialize(ConfigurableApplicationContext)
     */
    protected void customizeContext(PortletContext portletContext, ConfigurablePortletApplicationContext applicationContext) {
        List<Class<ApplicationContextInitializer<ConfigurableApplicationContext>>> initializerClasses =
                determineContextInitializerClasses(portletContext);

        if (initializerClasses.size() == 0) {
            // no ApplicationContextInitializers have been declared -> nothing to do
            return;
        }

        ArrayList<ApplicationContextInitializer<ConfigurableApplicationContext>> initializerInstances =
            new ArrayList<ApplicationContextInitializer<ConfigurableApplicationContext>>();

        for (Class<ApplicationContextInitializer<ConfigurableApplicationContext>> initializerClass : initializerClasses) {
            Class<?> contextClass = applicationContext.getClass();
            Class<?> initializerContextClass =
                GenericTypeResolver.resolveTypeArgument(initializerClass, ApplicationContextInitializer.class);
            Assert.isAssignable(initializerContextClass, contextClass, String.format(
                    "Could not add context initializer [%s] as its generic parameter [%s] " +
                    "is not assignable from the type of application context used by this " +
                    "context loader [%s]", initializerClass.getName(), initializerContextClass, contextClass));
            initializerInstances.add(BeanUtils.instantiateClass(initializerClass));
        }
        
        //TODO remove cast when ContribXmlPortletApplicationContext is merged into super classes
        ((ConfigurablePortletEnvironment)applicationContext.getEnvironment()).initPropertySources(this.servletContext, portletContext, null);

        Collections.sort(initializerInstances, new AnnotationAwareOrderComparator());
        for (ApplicationContextInitializer<ConfigurableApplicationContext> initializer : initializerInstances) {
            initializer.initialize(applicationContext);
        }
    }

    /**
     * Template method with default implementation (which may be overridden by a
     * subclass), to load or obtain an ApplicationContext instance which will be
     * used as the parent context of the root PortletApplicationContext. If the
     * return value from the method is null, no parent context is set.
     * <p>The default implementation uses
     * {@link PortletApplicationContextUtils#getWebApplicationContext(PortletContext)}
     * to load a parent context.
     * @param portletContext current portlet context
     * @return the parent application context, or <code>null</code> if none
     */
    protected ApplicationContext loadParentContext(PortletContext portletContext) {
        return PortletApplicationContextUtils.getWebApplicationContext(portletContext);
    }

    /**
     * Close Spring's web application context for the given portlet context. If
     * the default {@link #loadParentContext(PortletContext)} implementation,
     * which uses ContextSingletonBeanFactoryLocator, has loaded any shared
     * parent context, release one reference to that shared parent context.
     * <p>If overriding {@link #loadParentContext(PortletContext)}, you may have
     * to override this method as well.
     * @param servletContext the PortletContext that the WebApplicationContext runs in
     */
    public void closeWebApplicationContext(ServletContext servletContext) {
        servletContext.log("Closing Spring root PortletApplicationContext");
        try {
            if (this.context instanceof ConfigurablePortletApplicationContext) {
                ((ConfigurablePortletApplicationContext) this.context).close();
            }
        }
        finally {
            ClassLoader ccl = Thread.currentThread().getContextClassLoader();
            if (ccl == PortletContextLoader.class.getClassLoader()) {
                currentContext = null;
            }
            else if (ccl != null) {
                currentContextPerThread.remove(ccl);
            }
            servletContext.removeAttribute(PortletApplicationContext.ROOT_PORTLET_APPLICATION_CONTEXT_ATTRIBUTE);
            if (this.parentContextRef != null) {
                this.parentContextRef.release();
            }
        }
    }


    /**
     * Obtain the Spring root portlet application context for the current thread
     * (i.e. for the current thread's context ClassLoader, which needs to be
     * the web application's ClassLoader).
     * @return the current root web application context, or <code>null</code>
     * if none found
     * @see org.springframework.web.context.support.SpringBeanAutowiringSupport
     */
    public static PortletApplicationContext getCurrentPortletApplicationContext() {
        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        if (ccl != null) {
            PortletApplicationContext ccpt = currentContextPerThread.get(ccl);
            if (ccpt != null) {
                return ccpt;
            }
        }
        return currentContext;
    }

}
