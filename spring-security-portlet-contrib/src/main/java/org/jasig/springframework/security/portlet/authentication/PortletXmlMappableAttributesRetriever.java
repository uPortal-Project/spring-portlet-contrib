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
package org.jasig.springframework.security.portlet.authentication;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.portlet.PortletConfig;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathVariableResolver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.core.authority.mapping.MappableAttributesRetriever;
import org.springframework.security.web.authentication.preauth.j2ee.WebXmlMappableAttributesRetriever;
import org.springframework.web.portlet.context.PortletConfigAware;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This <tt>MappableAttributesRetriever</tt> implementation reads the list of defined Portlet
 * roles from a <tt>portlet.xml</tt> file and returns these from {{@link #getMappableAttributes()}.
 * <p>If configured in a portlet application level context then all security-role-refs from all
 * portlets are merged into a single list. If configured in a portlet level context then only
 * the security-role-refs from that portlet are used in the list.
 *
 * @author Ruud Senden
 * @author Luke Taylor
 * @author Eric Dalquist
 * @since 2.0
 * @see WebXmlMappableAttributesRetriever
 */
public class PortletXmlMappableAttributesRetriever  implements ResourceLoaderAware, PortletConfigAware, MappableAttributesRetriever, InitializingBean {
    protected final Log logger = LogFactory.getLog(getClass());

    private ResourceLoader resourceLoader;
    private PortletConfig portletConfig;
    private Set<String> mappableAttributes;

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
	public void setPortletConfig(PortletConfig portletConfig) {
		this.portletConfig = portletConfig;
	}

	public Set<String> getMappableAttributes() {
        return mappableAttributes;
    }

    /**
     * Loads the portlet.xml file using the configured <tt>ResourceLoader</tt> and
     * parses the role-name elements from it, using these as the set of <tt>mappableAttributes</tt>.
     */
    public void afterPropertiesSet() throws Exception {
        Resource portletXml = resourceLoader.getResource("/WEB-INF/portlet.xml");
        Document doc = getDocument(portletXml.getInputStream());
        
        final XPathExpression roleNamesExpression;
        if (portletConfig == null) {
        	final XPathFactory xPathFactory = XPathFactory.newInstance();
        	
        	final XPath xPath = xPathFactory.newXPath();
        	roleNamesExpression = xPath.compile("/portlet-app/portlet/security-role-ref/role-name");
        }
        else {
        	final XPathFactory xPathFactory = XPathFactory.newInstance();
            xPathFactory.setXPathVariableResolver(new XPathVariableResolver() {
                @Override
                public Object resolveVariable(QName variableName) {
                    if ("portletName".equals(variableName.getLocalPart())) {
                        return portletConfig.getPortletName();
                    }

                    return null;
                }
            });
            final XPath xPath = xPathFactory.newXPath();
        	roleNamesExpression = xPath.compile("/portlet-app/portlet[portlet-name=$portletName]/security-role-ref/role-name");
        }
        
        final NodeList securityRoles = (NodeList)roleNamesExpression.evaluate(doc, XPathConstants.NODESET);
        final Set<String> roleNames = new HashSet<String>();

        for (int i=0; i < securityRoles.getLength(); i++) {
            Element secRoleElt = (Element) securityRoles.item(i);
            String roleName = secRoleElt.getTextContent().trim();
            roleNames.add(roleName);
            logger.info("Retrieved role-name '" + roleName + "' from portlet.xml");
        }
        
        if (roleNames.isEmpty()) {
        	logger.info("No security-role-ref elements found in " + portletXml + (portletConfig == null ? "" : " for portlet " + portletConfig.getPortletName()));
        }

        mappableAttributes = Collections.unmodifiableSet(roleNames);
    }

    /**
     * @return Document for the specified InputStream
     */
    private Document getDocument(InputStream aStream) {
        Document doc;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            DocumentBuilder db = factory.newDocumentBuilder();
            db.setEntityResolver(new MyEntityResolver());
            doc = db.parse(aStream);
            return doc;
        } catch (FactoryConfigurationError e) {
            throw new RuntimeException("Unable to parse document object", e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Unable to parse document object", e);
        } catch (SAXException e) {
            throw new RuntimeException("Unable to parse document object", e);
        } catch (IOException e) {
            throw new RuntimeException("Unable to parse document object", e);
        } finally {
            try {
                aStream.close();
            } catch (IOException e) {
                logger.warn("Failed to close input stream for portlet.xml", e);
            }
        }
    }

    /**
     * We do not need to resolve external entities, so just return an empty
     * String.
     */
    private static final class MyEntityResolver implements EntityResolver {
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            return new InputSource(new StringReader(""));
        }
    }
}