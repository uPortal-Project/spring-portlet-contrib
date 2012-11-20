package org.jasig.springframework.web.portlet.support;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;

import org.jasig.springframework.web.portlet.context.ContribDispatcherPortlet;
import org.jasig.springframework.web.portlet.context.PortletApplicationContext;
import org.jasig.springframework.web.portlet.context.PortletApplicationContextUtils2;
import org.springframework.context.ApplicationContext;
import org.springframework.web.portlet.context.PortletApplicationContextUtils;

/**
 * Utility class for easy access to request-specific state which has been
 * set by the {@link ContribDispatcherPortlet}.
 *
 * <p>Supports lookup of current PortletApplicationContext.
 *
 * @author Eric Dalquist
 * @see ContribDispatcherPortlet
 */
public class PortletRequestContextUtils {

    /**
     * Look for the PortletApplicationContext associated with the DispatcherPortlet
     * that has initiated request processing.
     * @param request current portlet request
     * @return the request-specific portlet application context
     * @throws IllegalStateException if no portlet-specific context has been found
     */
    public static PortletApplicationContext getPortletApplicationContext(PortletRequest request)
        throws IllegalStateException {
        
        return getPortletApplicationContext(request, null);
    }

    /**
     * Look for the PortletApplicationContext associated with the DispatcherPortlet
     * that has initiated request processing, and for the global context if none
     * was found associated with the current request. This method is useful to
     * allow components outside the framework, such as JSP tag handlers,
     * to access the most specific application context available.
     * @param request current portlet request
     * @param portletContext current portlet context
     * @return the request-specific PortletApplicationContext, or the global one
     * if no request-specific context has been found
     * @throws IllegalStateException if neither a portlet-specific nor a
     * global context has been found
     */
    public static PortletApplicationContext getPortletApplicationContext(
            PortletRequest request, PortletContext portletContext) throws IllegalStateException {

        PortletApplicationContext portletApplicationContext = (PortletApplicationContext) request.getAttribute(
                ContribDispatcherPortlet.PORTLET_APPLICATION_CONTEXT_ATTRIBUTE);
        if (portletApplicationContext == null) {
            if (portletContext == null) {
                throw new IllegalStateException("No PortletApplicationContext found: not in a DispatcherPortlet request?");
            }
            portletApplicationContext = PortletApplicationContextUtils2.getRequiredPortletApplicationContext(portletContext);
        }
        
        return portletApplicationContext;
    }

    /**
     * Look for the PortletApplicationContext associated with the DispatcherPortlet
     * that has initiated request processing, for the global portlet context if none
     * was found associated with the current request, and for the global context if no
     * global portlet context was found. This method is useful to
     * allow components outside the framework, such as JSP tag handlers,
     * to access the most specific application context available.
     * @param request current portlet request
     * @param portletContext current portlet context
     * @return the request-specific PortletApplicationContext, or the global one
     * if no request-specific context has been found
     * @throws IllegalStateException if neither a portlet-specific nor a
     * global context has been found
     */
    public static ApplicationContext getWebApplicationContext(
            PortletRequest request, PortletContext portletContext) throws IllegalStateException {

        PortletApplicationContext portletApplicationContext = (PortletApplicationContext) request.getAttribute(
                ContribDispatcherPortlet.PORTLET_APPLICATION_CONTEXT_ATTRIBUTE);
        if (portletApplicationContext != null) {
            return portletApplicationContext;
        }
        
        if (portletContext == null) {
            throw new IllegalStateException("No PortletApplicationContext found: not in a DispatcherPortlet request?");
        }
        portletApplicationContext = PortletApplicationContextUtils2.getPortletApplicationContext(portletContext);
        if (portletApplicationContext != null) {
            return portletApplicationContext;
        }

        return PortletApplicationContextUtils.getRequiredWebApplicationContext(portletContext);
    }
}
