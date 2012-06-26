package org.jasig.springframework.security.portlet.authentication;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.core.authority.mapping.MappableAttributesRetriever;
import org.springframework.security.web.authentication.preauth.j2ee.WebXmlMappableAttributesRetriever;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This <tt>MappableAttributesRetriever</tt> implementation reads the list of defined Portlet
 * roles from a <tt>portlet.xml</tt> file and returns these from {{@link #getMappableAttributes()}.
 *
 * @author Ruud Senden
 * @author Luke Taylor
 * @author Eric Dalquist
 * @since 2.0
 * @see WebXmlMappableAttributesRetriever
 */
public class PortletXmlMappableAttributesRetriever  implements ResourceLoaderAware, MappableAttributesRetriever, InitializingBean {
    protected final Log logger = LogFactory.getLog(getClass());

    private ResourceLoader resourceLoader;
    private Set<String> mappableAttributes;

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
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
        NodeList portletApp = doc.getElementsByTagName("portlet-app");
        if (portletApp.getLength() != 1) {
            throw new IllegalArgumentException("Failed to find 'portlet-app' element in resource" + portletXml);
        }
        NodeList securityRoles = ((Element)portletApp.item(0)).getElementsByTagName("security-role");

        ArrayList<String> roleNames = new ArrayList<String>();

        for (int i=0; i < securityRoles.getLength(); i++) {
            Element secRoleElt = (Element) securityRoles.item(i);
            NodeList roles = secRoleElt.getElementsByTagName("role-name");

            if (roles.getLength() > 0) {
                String roleName = ((Element)roles.item(0)).getTextContent().trim();
                roleNames.add(roleName);
                logger.info("Retrieved role-name '" + roleName + "' from web.xml");
            } else {
                logger.info("No security-role elements found in " + portletXml);
            }
        }

        mappableAttributes = Collections.unmodifiableSet(new HashSet<String>(roleNames));
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