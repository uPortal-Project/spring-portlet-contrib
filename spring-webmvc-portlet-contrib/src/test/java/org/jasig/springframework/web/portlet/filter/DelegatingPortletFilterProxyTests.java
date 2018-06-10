/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.springframework.web.portlet.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.filter.FilterChain;
import javax.portlet.filter.FilterConfig;
import javax.portlet.filter.RenderFilter;

import org.jasig.springframework.mock.web.portlet.MockFilterConfig;
import org.jasig.springframework.web.portlet.context.ContribStaticPortletApplicationContext;
import org.jasig.springframework.web.portlet.context.PortletApplicationContext;
import org.junit.Test;
import org.springframework.mock.web.portlet.MockPortletContext;
import org.springframework.mock.web.portlet.MockRenderRequest;
import org.springframework.mock.web.portlet.MockRenderResponse;

public class DelegatingPortletFilterProxyTests {

    @Test
    public void testDelegatingPortletFilterProxy() throws PortletException, IOException {
        PortletContext pc = new MockPortletContext();

        ContribStaticPortletApplicationContext pac = new ContribStaticPortletApplicationContext();
        pac.setPortletContext(pc);
        pac.registerSingleton("targetFilter", MockRenderFilter.class);
        pac.refresh();
        pc.setAttribute(PortletApplicationContext.ROOT_PORTLET_APPLICATION_CONTEXT_ATTRIBUTE, pac);

        MockRenderFilter targetFilter = (MockRenderFilter) pac.getBean("targetFilter");

        MockFilterConfig proxyConfig = new MockFilterConfig(pc);
        proxyConfig.addInitParameter("targetBeanName", "targetFilter");
        DelegatingPortletFilterProxy filterProxy = new DelegatingPortletFilterProxy();
        filterProxy.init(proxyConfig);

        MockRenderRequest request = new MockRenderRequest();
        MockRenderResponse response = new MockRenderResponse();
        filterProxy.doFilter(request, response, null);

        assertNull(targetFilter.filterConfig);
        assertEquals(Boolean.TRUE, request.getAttribute("called"));

        filterProxy.destroy();
        assertNull(targetFilter.filterConfig);
    }

    @Test
    public void testDelegatingPortletFilterProxyAndCustomContextAttribute() throws PortletException, IOException {
        PortletContext sc = new MockPortletContext();

        ContribStaticPortletApplicationContext wac = new ContribStaticPortletApplicationContext();
        wac.setPortletContext(sc);
        wac.registerSingleton("targetFilter", MockRenderFilter.class);
        wac.refresh();
        sc.setAttribute("CUSTOM_ATTR", wac);

        MockRenderFilter targetFilter = (MockRenderFilter) wac.getBean("targetFilter");

        MockFilterConfig proxyConfig = new MockFilterConfig(sc);
        proxyConfig.addInitParameter("targetBeanName", "targetFilter");
        proxyConfig.addInitParameter("contextAttribute", "CUSTOM_ATTR");
        DelegatingPortletFilterProxy filterProxy = new DelegatingPortletFilterProxy();
        filterProxy.init(proxyConfig);

        MockRenderRequest request = new MockRenderRequest();
        MockRenderResponse response = new MockRenderResponse();
        filterProxy.doFilter(request, response, null);

        assertNull(targetFilter.filterConfig);
        assertEquals(Boolean.TRUE, request.getAttribute("called"));

        filterProxy.destroy();
        assertNull(targetFilter.filterConfig);
    }

    @Test
    public void testDelegatingPortletFilterProxyWithFilterName() throws PortletException, IOException {
        PortletContext sc = new MockPortletContext();

        ContribStaticPortletApplicationContext wac = new ContribStaticPortletApplicationContext();
        wac.setPortletContext(sc);
        wac.registerSingleton("targetFilter", MockRenderFilter.class);
        wac.refresh();
        sc.setAttribute(PortletApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, wac);

        MockRenderFilter targetFilter = (MockRenderFilter) wac.getBean("targetFilter");

        MockFilterConfig proxyConfig = new MockFilterConfig(sc, "targetFilter");
        DelegatingPortletFilterProxy filterProxy = new DelegatingPortletFilterProxy();
        filterProxy.init(proxyConfig);

        MockRenderRequest request = new MockRenderRequest();
        MockRenderResponse response = new MockRenderResponse();
        filterProxy.doFilter(request, response, null);

        assertNull(targetFilter.filterConfig);
        assertEquals(Boolean.TRUE, request.getAttribute("called"));

        filterProxy.destroy();
        assertNull(targetFilter.filterConfig);
    }

    @Test
    public void testDelegatingPortletFilterProxyWithLazyContextStartup() throws PortletException, IOException {
        PortletContext sc = new MockPortletContext();

        MockFilterConfig proxyConfig = new MockFilterConfig(sc);
        proxyConfig.addInitParameter("targetBeanName", "targetFilter");
        DelegatingPortletFilterProxy filterProxy = new DelegatingPortletFilterProxy();
        filterProxy.init(proxyConfig);

        ContribStaticPortletApplicationContext wac = new ContribStaticPortletApplicationContext();
        wac.setPortletContext(sc);
        wac.registerSingleton("targetFilter", MockRenderFilter.class);
        wac.refresh();
        sc.setAttribute(PortletApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, wac);

        MockRenderFilter targetFilter = (MockRenderFilter) wac.getBean("targetFilter");

        MockRenderRequest request = new MockRenderRequest();
        MockRenderResponse response = new MockRenderResponse();
        filterProxy.doFilter(request, response, null);

        assertNull(targetFilter.filterConfig);
        assertEquals(Boolean.TRUE, request.getAttribute("called"));

        filterProxy.destroy();
        assertNull(targetFilter.filterConfig);
    }

    @Test
    public void testDelegatingPortletFilterProxyWithTargetFilterLifecycle() throws PortletException, IOException {
        PortletContext sc = new MockPortletContext();

        ContribStaticPortletApplicationContext wac = new ContribStaticPortletApplicationContext();
        wac.setPortletContext(sc);
        wac.registerSingleton("targetFilter", MockRenderFilter.class);
        wac.refresh();
        sc.setAttribute(PortletApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, wac);

        MockRenderFilter targetFilter = (MockRenderFilter) wac.getBean("targetFilter");

        MockFilterConfig proxyConfig = new MockFilterConfig(sc);
        proxyConfig.addInitParameter("targetBeanName", "targetFilter");
        proxyConfig.addInitParameter("targetFilterLifecycle", "true");
        DelegatingPortletFilterProxy filterProxy = new DelegatingPortletFilterProxy();
        filterProxy.init(proxyConfig);
        assertEquals(proxyConfig, targetFilter.filterConfig);

        MockRenderRequest request = new MockRenderRequest();
        MockRenderResponse response = new MockRenderResponse();
        filterProxy.doFilter(request, response, null);

        assertEquals(proxyConfig, targetFilter.filterConfig);
        assertEquals(Boolean.TRUE, request.getAttribute("called"));

        filterProxy.destroy();
        assertNull(targetFilter.filterConfig);
    }


    public static class MockRenderFilter implements RenderFilter {

        public FilterConfig filterConfig;

        public void init(FilterConfig filterConfig) throws PortletException {
            this.filterConfig = filterConfig;
        }

        @Override
        public void doFilter(RenderRequest request, RenderResponse response, FilterChain chain) throws IOException, PortletException {
            request.setAttribute("called", Boolean.TRUE);
        }

        public void destroy() {
            this.filterConfig = null;
        }
    }

}
