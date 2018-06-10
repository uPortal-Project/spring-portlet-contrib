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
package org.jasig.springframework.mock.web.portlet;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.web.MockServletContext;


/**
 * <p>MockPortletContext class.</p>
 */
public class MockPortletContext extends org.springframework.mock.web.portlet.MockPortletContext {
    private MockServletContext servletContext;

    /**
     * <p>Constructor for MockPortletContext.</p>
     */
    public MockPortletContext() {
        super();
    }

    /**
     * <p>Constructor for MockPortletContext.</p>
     *
     * @param resourceLoader a {@link org.springframework.core.io.ResourceLoader} object.
     */
    public MockPortletContext(ResourceLoader resourceLoader) {
        super(resourceLoader);
    }

    /**
     * <p>Constructor for MockPortletContext.</p>
     *
     * @param resourceBasePath a {@link java.lang.String} object.
     * @param resourceLoader a {@link org.springframework.core.io.ResourceLoader} object.
     */
    public MockPortletContext(String resourceBasePath, ResourceLoader resourceLoader) {
        super(resourceBasePath, resourceLoader);
    }

    /**
     * <p>Constructor for MockPortletContext.</p>
     *
     * @param resourceBasePath a {@link java.lang.String} object.
     */
    public MockPortletContext(String resourceBasePath) {
        super(resourceBasePath);
    }

    /**
     * <p>Constructor for MockPortletContext.</p>
     *
     * @param servletContext a {@link org.springframework.mock.web.MockServletContext} object.
     */
    public MockPortletContext(MockServletContext servletContext) {
        super();

        this.servletContext = servletContext;
    }

    /** {@inheritDoc} */
    public String getMimeType(String file) {
        if (servletContext == null) {
            return super.getMimeType(file);
        }
        return servletContext.getMimeType(file);
    }

    /** {@inheritDoc} */
    public String getRealPath(String path) {
        if (servletContext == null) {
            return super.getRealPath(path);
        }

        return servletContext.getRealPath(path);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public Set<String> getResourcePaths(String path) {
        if (servletContext == null) {
            return super.getResourcePaths(path);
        }
        return servletContext.getResourcePaths(path);
    }

    /** {@inheritDoc} */
    public URL getResource(String path)
        throws java.net.MalformedURLException {
        if (servletContext == null) {
            return super.getResource(path);
        }

        if (path == null || !path.startsWith("/")) {
            throw new MalformedURLException("path must start with a '/'");
        }
        return servletContext.getResource(path);
    }

    /** {@inheritDoc} */
    public Object getAttribute(String name) {
        if (servletContext == null) {
            return super.getAttribute(name);
        }

        if (name == null) {
            throw new IllegalArgumentException("Attribute name == null");
        }

        return servletContext.getAttribute(name);
    }

    /**
     * <p>getAttributeNames.</p>
     *
     * @return a {@link java.util.Enumeration} object.
     */
    @SuppressWarnings("unchecked")
    public Enumeration<String> getAttributeNames() {
        if (servletContext == null) {
            return super.getAttributeNames();
        }

        return servletContext.getAttributeNames();
    }

    /** {@inheritDoc} */
    public String getInitParameter(String name) {
        if (servletContext == null) {
            return super.getInitParameter(name);
        }

        if (name == null) {
            throw new IllegalArgumentException("Parameter name == null");
        }

        return servletContext.getInitParameter(name);
    }

    /**
     * <p>getInitParameterNames.</p>
     *
     * @return a {@link java.util.Enumeration} object.
     */
    @SuppressWarnings("unchecked")
    public Enumeration<String> getInitParameterNames() {
        if (servletContext == null) {
            return super.getInitParameterNames();
        }

        return servletContext.getInitParameterNames();
    }

    /** {@inheritDoc} */
    public void log(String msg) {
        if (servletContext == null) {
            super.log(msg);
            return;
        }

        servletContext.log(msg);
    }

    /** {@inheritDoc} */
    public void log(String message, Throwable throwable) {
        if (servletContext == null) {
            super.log(message, throwable);
            return;
        }

        servletContext.log(message, throwable);
    }

    /** {@inheritDoc} */
    public void removeAttribute(String name) {
        if (servletContext == null) {
            super.removeAttribute(name);
            return;
        }

        if (name == null) {
            throw new IllegalArgumentException("Attribute name == null");
        }

        servletContext.removeAttribute(name);
    }

    /** {@inheritDoc} */
    public void setAttribute(String name, Object object) {
        if (servletContext == null) {
            super.setAttribute(name, object);
            return;
        }

        if (name == null) {
            throw new IllegalArgumentException("Attribute name == null");
        }

        servletContext.setAttribute(name, object);
    }

    /**
     * <p>getPortletContextName.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPortletContextName() {
        if (servletContext == null) {
            return super.getPortletContextName();
        }

        return servletContext.getServletContextName();
    }

    /** {@inheritDoc} */
    public void addInitParameter(String name, String value) {
        if (servletContext == null) {
            super.addInitParameter(name, value);
            return;
        }

        servletContext.addInitParameter(name, value);
    }

}
