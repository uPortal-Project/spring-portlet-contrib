package org.jasig.portlet.spring.portlet.test;

import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ServletConfigAware;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.portlet.context.PortletConfigAware;
import org.springframework.web.portlet.context.PortletContextAware;

/**
 * Utility that prints out info about the app context it is wired up in
 * 
 * @author Eric Dalquist
 */
public class ServletPortletAwareTester 
        implements ApplicationContextAware, DisposableBean, ServletContextAware, ServletConfigAware, PortletContextAware, PortletConfigAware {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final String name;

    public ServletPortletAwareTester(String name) {
        this.name = name;
        logger.debug("Created {}({})", this.getClass().getSimpleName(), this.name);
    }

    @Override
    public void setPortletConfig(PortletConfig portletConfig) {
        logger.debug("{} - set PortletConfig {}", this.name, portletConfig.getPortletName());        
    }

    @Override
    public void setPortletContext(PortletContext portletContext) {
        logger.debug("{} - set PortletContext {}", this.name, portletContext.getPortletContextName());
    }

    @Override
    public void setServletConfig(ServletConfig servletConfig) {
        logger.debug("{} - set ServletConfig {}", this.name, servletConfig.getServletName());        
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        logger.debug("{} - set ServletContext {}", this.name, servletContext.getContextPath());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        logger.debug("{} - ApplicationContext is a {}", this.name, applicationContext.getClass());
        logger.debug("{} - ApplicationContext id is {}", this.name, applicationContext.getId());
        
        String contextIds = applicationContext.getId(); 
        ApplicationContext parent = applicationContext.getParent();
        if (parent != null) {
            while (parent != null) {
                contextIds = contextIds + " => " + parent.getId();
                parent = parent.getParent();
            }
            
            logger.debug("{} - ApplicationContext heirarchy is {}", this.name, contextIds);
        }
        else {
            logger.debug("{} - ApplicationContext has no parent", this.name);
        }
    }

    @Override
    public void destroy() throws Exception {
        logger.debug("{} - destroy", this.name);        
    }
}
