package org.jasig.springframework.security.portlet.authentication;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;

import javax.portlet.PortletConfig;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;

import com.google.common.collect.ImmutableSet;

public class PortletXmlMappableAttributesRetrieverTest {
	
	@Test
	public void testTwoWithAttributes() throws Exception {
		final PortletXmlMappableAttributesRetriever portletXmlMappableAttributesRetriever = new PortletXmlMappableAttributesRetriever();
		
		final ResourceLoader resourceLoader = mock(ResourceLoader.class);
		when(resourceLoader.getResource("/WEB-INF/portlet.xml")).thenReturn(new ClassPathResource("/org/jasig/springframework/security/portlet/authentication/portlet_2_with_attributes.xml"));
		portletXmlMappableAttributesRetriever.setResourceLoader(resourceLoader);
		
		portletXmlMappableAttributesRetriever.afterPropertiesSet();
		
		final Set<String> mappableAttributes = portletXmlMappableAttributesRetriever.getMappableAttributes();
		
		final Set<String> expected = ImmutableSet.of("name1", "name2", "name3");
		assertEquals(expected, mappableAttributes);
	}
	
	@Test
	public void testTwoWithAttributesSpecificPortlet() throws Exception {
		final PortletXmlMappableAttributesRetriever portletXmlMappableAttributesRetriever = new PortletXmlMappableAttributesRetriever();
		
		final PortletConfig portletConfig = mock(PortletConfig.class);
		when(portletConfig.getPortletName()).thenReturn("ContextTestPortlet");
		portletXmlMappableAttributesRetriever.setPortletConfig(portletConfig);
		
		final ResourceLoader resourceLoader = mock(ResourceLoader.class);
		when(resourceLoader.getResource("/WEB-INF/portlet.xml")).thenReturn(new ClassPathResource("/org/jasig/springframework/security/portlet/authentication/portlet_2_with_attributes.xml"));
		portletXmlMappableAttributesRetriever.setResourceLoader(resourceLoader);
		
		portletXmlMappableAttributesRetriever.afterPropertiesSet();
		
		final Set<String> mappableAttributes = portletXmlMappableAttributesRetriever.getMappableAttributes();
		
		final Set<String> expected = ImmutableSet.of("name1", "name2");
		assertEquals(expected, mappableAttributes);
	}
	
	@Test
	public void testZeroNoAttributes() throws Exception {
		final PortletXmlMappableAttributesRetriever portletXmlMappableAttributesRetriever = new PortletXmlMappableAttributesRetriever();
		
		final ResourceLoader resourceLoader = mock(ResourceLoader.class);
		when(resourceLoader.getResource("/WEB-INF/portlet.xml")).thenReturn(new ClassPathResource("/org/jasig/springframework/security/portlet/authentication/portlet_0_no_attributes.xml"));
		portletXmlMappableAttributesRetriever.setResourceLoader(resourceLoader);
		
		portletXmlMappableAttributesRetriever.afterPropertiesSet();
		
		final Set<String> mappableAttributes = portletXmlMappableAttributesRetriever.getMappableAttributes();
		
		assertEquals(Collections.emptySet(), mappableAttributes);
	}
	
	@Test
	public void testOneNoAttributes() throws Exception {
		final PortletXmlMappableAttributesRetriever portletXmlMappableAttributesRetriever = new PortletXmlMappableAttributesRetriever();
		
		final ResourceLoader resourceLoader = mock(ResourceLoader.class);
		when(resourceLoader.getResource("/WEB-INF/portlet.xml")).thenReturn(new ClassPathResource("/org/jasig/springframework/security/portlet/authentication/portlet_1_no_attributes.xml"));
		portletXmlMappableAttributesRetriever.setResourceLoader(resourceLoader);
		
		portletXmlMappableAttributesRetriever.afterPropertiesSet();
		
		final Set<String> mappableAttributes = portletXmlMappableAttributesRetriever.getMappableAttributes();
		
		assertEquals(Collections.emptySet(), mappableAttributes);
	}
}
