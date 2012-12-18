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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.portlet.PortletContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.jasig.springframework.beans.TestBean;
import org.jasig.springframework.beans.factory.LifecycleBean;
import org.jasig.springframework.mock.web.portlet.MockPortletContext;
import org.jasig.springframework.web.portlet.SimplePortletApplicationContext;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.mock.web.MockServletContext;
import org.springframework.mock.web.portlet.MockPortletConfig;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.portlet.context.ConfigurablePortletApplicationContext;


public class PortletContextLoaderTests {

    @Test
    public void testContextLoaderListenerWithDefaultContext() {
        MockServletContext sc = new MockServletContext("");
        sc.addInitParameter(PortletContextLoader.CONFIG_LOCATION_PARAM,
                "/org/springframework/web/context/WEB-INF/applicationContext.xml "
                        + "/org/springframework/web/context/WEB-INF/context-addition.xml");
        ServletContextListener listener = new PortletContextLoaderListener();
        ServletContextEvent event = new ServletContextEvent(sc);
        listener.contextInitialized(event);
        PortletContextLoader contextLoader = (PortletContextLoader) sc.getAttribute(PortletApplicationContextUtils2.ROOT_PORTLET_APPLICATION_CONTEXT_LOADER_ATTRIBUTE);
        assertNotNull(contextLoader);
        
        //initialize the portlet application context, needed because PortletContextLoaderListener.contextInitialized doesn't actually create
        //the portlet app context due to lack of PortletContext reference
        MockPortletContext pc = new MockPortletContext(sc);
        PortletApplicationContext context = PortletApplicationContextUtils2.getPortletApplicationContext(pc);
        
        assertTrue("Correct PortletApplicationContext exposed in PortletContext", context instanceof ContribXmlPortletApplicationContext);
        assertTrue(PortletContextLoader.getCurrentPortletApplicationContext() instanceof ContribXmlPortletApplicationContext);
        LifecycleBean lb = (LifecycleBean) context.getBean("lifecycle");
        assertTrue("Has father", context.containsBean("father"));
        assertTrue("Has rod", context.containsBean("rod"));
        assertTrue("Has kerry", context.containsBean("kerry"));
        assertTrue("Not destroyed", !lb.isDestroyed());
        assertFalse(context.containsBean("beans1.bean1"));
        assertFalse(context.containsBean("beans1.bean2"));
        listener.contextDestroyed(event);
        assertTrue("Destroyed", lb.isDestroyed());
        assertNull(sc.getAttribute(PortletApplicationContext.ROOT_PORTLET_APPLICATION_CONTEXT_ATTRIBUTE));
        assertNull(PortletContextLoader.getCurrentPortletApplicationContext());
    }

    @Test
    public void testContextLoaderListenerWithRegisteredContextInitializer() {
        MockServletContext sc = new MockServletContext("");
        sc.addInitParameter(PortletContextLoader.CONFIG_LOCATION_PARAM,
                "org/springframework/web/context/WEB-INF/ContextLoaderTests-acc-context.xml");
        sc.addInitParameter(PortletContextLoader.CONTEXT_INITIALIZER_CLASSES_PARAM,
                StringUtils.arrayToCommaDelimitedString(
                        new Object[]{TestContextInitializer.class.getName(), TestWebContextInitializer.class.getName()}));
        PortletContextLoaderListener listener = new PortletContextLoaderListener();
        listener.contextInitialized(new ServletContextEvent(sc));
        
        //initialize the portlet application context, needed because PortletContextLoaderListener.contextInitialized doesn't actually create
        //the portlet app context due to lack of PortletContext reference
        MockPortletContext pc = new MockPortletContext(sc);
        PortletApplicationContextUtils2.getPortletApplicationContext(pc);
        
        PortletApplicationContext pac = PortletContextLoader.getCurrentPortletApplicationContext();
        TestBean testBean = pac.getBean(TestBean.class);
        assertThat(testBean.getName(), equalTo("testName"));
//        assertThat(pac.getServletContext().getAttribute("initialized"), notNullValue());
        assertThat(pac.getPortletContext().getAttribute("initialized"), notNullValue());
    }
    

    @Test
    public void testRegisteredContextInitializerCanAccessServletContextParamsViaEnvironment() {
        MockServletContext sc = new MockServletContext("");
        // config file doesn't matter.  just a placeholder
        sc.addInitParameter(PortletContextLoader.CONFIG_LOCATION_PARAM,
                "/org/springframework/web/context/WEB-INF/empty-context.xml");

        sc.addInitParameter("someProperty", "someValue");
        sc.addInitParameter(PortletContextLoader.CONTEXT_INITIALIZER_CLASSES_PARAM, EnvApplicationContextInitializer.class.getName());
        PortletContextLoaderListener listener = new PortletContextLoaderListener();
        listener.contextInitialized(new ServletContextEvent(sc));
        
        //initialize the portlet application context, needed because PortletContextLoaderListener.contextInitialized doesn't actually create
        //the portlet app context due to lack of PortletContext reference
        MockPortletContext pc = new MockPortletContext(sc);
        PortletApplicationContextUtils2.getPortletApplicationContext(pc);
    }

    @Test
    public void testContextLoaderListenerWithUnkownContextInitializer() {
        MockServletContext sc = new MockServletContext("");
        // config file doesn't matter.  just a placeholder
        sc.addInitParameter(PortletContextLoader.CONFIG_LOCATION_PARAM,
                "/org/springframework/web/context/WEB-INF/empty-context.xml");
        sc.addInitParameter(PortletContextLoader.CONTEXT_INITIALIZER_CLASSES_PARAM,
                StringUtils.arrayToCommaDelimitedString(new Object[]{UnknownContextInitializer.class.getName()}));
        PortletContextLoaderListener listener = new PortletContextLoaderListener();
        listener.contextInitialized(new ServletContextEvent(sc));
        
        try {
            //initialize the portlet application context, needed because PortletContextLoaderListener.contextInitialized doesn't actually create
            //the portlet app context due to lack of PortletContext reference
            MockPortletContext pc = new MockPortletContext(sc);
            PortletApplicationContextUtils2.getPortletApplicationContext(pc);
            
            fail("expected exception");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("not assignable"));
        }
    }

    @Test
    public void testContextLoaderWithCustomContextAndParent() throws Exception {
        MockServletContext sc = new MockServletContext("");
        sc.addInitParameter(ContextLoader.CONTEXT_CLASS_PARAM, StaticWebApplicationContext.class.getName());
        sc.addInitParameter(PortletContextLoader.CONTEXT_CLASS_PARAM, SimplePortletApplicationContext.class.getName());
        ContextLoaderListener servletListener = new ContextLoaderListener();
        ServletContextListener listener = new PortletContextLoaderListener();
        ServletContextEvent event = new ServletContextEvent(sc);
        
        servletListener.contextInitialized(event);
        listener.contextInitialized(event);

        //initialize the portlet application context, needed because PortletContextLoaderListener.contextInitialized doesn't actually create
        //the portlet app context due to lack of PortletContext reference
        MockPortletContext pc = new MockPortletContext(sc);
        PortletApplicationContextUtils2.getPortletApplicationContext(pc);
        
        PortletApplicationContext wc = (PortletApplicationContext) pc.getAttribute(PortletApplicationContext.ROOT_PORTLET_APPLICATION_CONTEXT_ATTRIBUTE);
        assertTrue("Correct PortletApplicationContext exposed in PortletContext", wc instanceof SimplePortletApplicationContext);
    }

    @Test
    public void testContextLoaderWithInvalidLocation() throws Exception {
        MockServletContext sc = new MockServletContext("");
        sc.addInitParameter(PortletContextLoader.CONFIG_LOCATION_PARAM, "/WEB-INF/myContext.xml");
        ServletContextListener listener = new PortletContextLoaderListener();
        ServletContextEvent event = new ServletContextEvent(sc);
        listener.contextInitialized(event);
        try {
            //initialize the portlet application context, needed because PortletContextLoaderListener.contextInitialized doesn't actually create
            //the portlet app context due to lack of PortletContext reference
            MockPortletContext pc = new MockPortletContext(sc);
            PortletApplicationContextUtils2.getPortletApplicationContext(pc);
            
            fail("Should have thrown BeanDefinitionStoreException");
        }
        catch (BeanDefinitionStoreException ex) {
            // expected
            assertTrue(ex.getCause() instanceof FileNotFoundException);
        }
    }

    @Test
    public void testContextLoaderWithInvalidContext() throws Exception {
        MockServletContext sc = new MockServletContext("");
        sc.addInitParameter(PortletContextLoader.CONTEXT_CLASS_PARAM,
                "org.springframework.web.context.support.InvalidWebApplicationContext");
        ServletContextListener listener = new PortletContextLoaderListener();
        ServletContextEvent event = new ServletContextEvent(sc);
        listener.contextInitialized(event);
        try {
            //initialize the portlet application context, needed because PortletContextLoaderListener.contextInitialized doesn't actually create
            //the portlet app context due to lack of PortletContext reference
            MockPortletContext pc = new MockPortletContext(sc);
            PortletApplicationContextUtils2.getPortletApplicationContext(pc);
            fail("Should have thrown ApplicationContextException");
        }
        catch (ApplicationContextException ex) {
            // expected
            assertTrue(ex.getCause() instanceof ClassNotFoundException);
        }
    }

    @Test
    public void testContextLoaderWithDefaultLocation() throws Exception {
        MockServletContext sc = new MockServletContext("");
        ServletContextListener listener = new PortletContextLoaderListener();
        ServletContextEvent event = new ServletContextEvent(sc);
        listener.contextInitialized(event);
        try {
            //initialize the portlet application context, needed because PortletContextLoaderListener.contextInitialized doesn't actually create
            //the portlet app context due to lack of PortletContext reference
            MockPortletContext pc = new MockPortletContext(sc);
            PortletApplicationContextUtils2.getPortletApplicationContext(pc);
            fail("Should have thrown BeanDefinitionStoreException");
        }
        catch (BeanDefinitionStoreException ex) {
            // expected
            assertTrue(ex.getCause() instanceof IOException);
            assertTrue(ex.getCause().getMessage().contains("/WEB-INF/portletApplicationContext.xml"));
        }
    }

    @Test
    public void testFrameworkServletWithDefaultLocation() throws Exception {
        ContribDispatcherPortlet portlet = new ContribDispatcherPortlet();
        portlet.setContextClass(ContribXmlPortletApplicationContext.class);
        try {
            portlet.init(new MockPortletConfig(new MockPortletContext(""), "test"));
            fail("Should have thrown BeanDefinitionStoreException");
        }
        catch (BeanDefinitionStoreException ex) {
            // expected
            assertTrue(ex.getCause() instanceof IOException);
            assertTrue(ex.getCause().getMessage().contains("/WEB-INF/test-portlet.xml"));
        }
    }

    @Test
    public void testFrameworkServletWithCustomLocation() throws Exception {
        ContribDispatcherPortlet portlet = new ContribDispatcherPortlet();
        portlet.setContextConfigLocation("/org/springframework/web/context/WEB-INF/testNamespace.xml "
                + "/org/springframework/web/context/WEB-INF/context-addition.xml");
        portlet.init(new MockPortletConfig(new MockPortletContext(""), "test"));
        assertTrue(portlet.getPortletApplicationContext().containsBean("kerry"));
        assertTrue(portlet.getPortletApplicationContext().containsBean("kerryX"));
    }

    @Test
    public void testClassPathXmlApplicationContext() throws IOException {
        ApplicationContext context = new ClassPathXmlApplicationContext(
                "/org/springframework/web/context/WEB-INF/applicationContext.xml");
        assertTrue("Has father", context.containsBean("father"));
        assertTrue("Has rod", context.containsBean("rod"));
        assertFalse("Hasn't kerry", context.containsBean("kerry"));
        assertTrue("Doesn't have spouse", ((TestBean) context.getBean("rod")).getSpouse() == null);
        assertTrue("myinit not evaluated", "Roderick".equals(((TestBean) context.getBean("rod")).getName()));

        context = new ClassPathXmlApplicationContext(new String[] {
            "/org/springframework/web/context/WEB-INF/applicationContext.xml",
            "/org/springframework/web/context/WEB-INF/context-addition.xml" });
        assertTrue("Has father", context.containsBean("father"));
        assertTrue("Has rod", context.containsBean("rod"));
        assertTrue("Has kerry", context.containsBean("kerry"));
    }

    @Test
    public void testSingletonDestructionOnStartupFailure() throws IOException {
        try {
            new ClassPathXmlApplicationContext(new String[] {
                "/org/springframework/web/context/WEB-INF/applicationContext.xml",
                "/org/springframework/web/context/WEB-INF/fail.xml" }) {

                public void refresh() throws BeansException {
                    try {
                        super.refresh();
                    }
                    catch (BeanCreationException ex) {
                        DefaultListableBeanFactory factory = (DefaultListableBeanFactory) getBeanFactory();
                        assertEquals(0, factory.getSingletonCount());
                        throw ex;
                    }
                }
            };
            fail("Should have thrown BeanCreationException");
        }
        catch (BeanCreationException ex) {
            // expected
        }
    }

    private static class TestContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext applicationContext) {
            ConfigurableEnvironment environment = applicationContext.getEnvironment();
            environment.getPropertySources().addFirst(new PropertySource<Object>("testPropertySource") {
                @Override
                public Object getProperty(String key) {
                    return "name".equals(key) ? "testName" : null;
                }
            });
        }
    }

    private static class TestWebContextInitializer implements ApplicationContextInitializer<ConfigurablePortletApplicationContext> {
        public void initialize(ConfigurablePortletApplicationContext applicationContext) {
            ServletContext ctx = applicationContext.getServletContext(); // type-safe access to servlet-specific methods
            if (ctx != null) {
                ctx.setAttribute("initialized", true);
            }
            
            PortletContext pc = applicationContext.getPortletContext();
            pc.setAttribute("initialized", true);
        }
    }

    private static class EnvApplicationContextInitializer implements ApplicationContextInitializer<ConfigurablePortletApplicationContext> {
        public void initialize(ConfigurablePortletApplicationContext applicationContext) {
            // test that ApplicationContextInitializers can access ServletContext properties
            // via the environment (SPR-8991)
            String value = applicationContext.getEnvironment().getRequiredProperty("someProperty");
            assertThat(value, is("someValue"));
        }
    }

    private static interface UnknownApplicationContext extends ConfigurableApplicationContext {
        void unheardOf();
    }

    private static class UnknownContextInitializer implements ApplicationContextInitializer<UnknownApplicationContext> {
        public void initialize(UnknownApplicationContext applicationContext) {
            applicationContext.unheardOf();
        }
    }
}
