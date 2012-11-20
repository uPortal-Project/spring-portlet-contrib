/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jasig.springframework.web.portlet;

import java.util.Locale;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.jasig.springframework.web.portlet.context.PortletApplicationContext;
import org.jasig.springframework.web.portlet.support.PortletRequestContextUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.ui.context.Theme;
import org.springframework.ui.context.ThemeSource;
import org.springframework.ui.context.support.SimpleTheme;
import org.springframework.ui.context.support.UiApplicationContextUtils;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.context.StaticPortletApplicationContext;
import org.springframework.web.portlet.mvc.Controller;
import org.springframework.web.portlet.mvc.SimpleFormController;
import org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping;
import org.springframework.web.servlet.theme.AbstractThemeResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.XmlViewResolver;


/**
 * @author Juergen Hoeller
 * @since 21.05.2003
 */
public class SimplePortletApplicationContext extends StaticPortletApplicationContext implements PortletApplicationContext {

	public void refresh() throws BeansException {
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("commandClass", "org.jasig.springframework.beans.TestBean");
		pvs.add("formView", "form");
		registerSingleton("/form.do", SimpleFormController.class, pvs);

		registerSingleton("/locale.do", LocaleChecker.class);

		addMessage("test", Locale.ENGLISH, "test message");
		addMessage("test", Locale.CANADA, "Canadian & test message");
		addMessage("testArgs", Locale.ENGLISH, "test {0} message {1}");
		addMessage("testArgsFormat", Locale.ENGLISH, "test {0} message {1,number,#.##} X");

		registerSingleton(UiApplicationContextUtils.THEME_SOURCE_BEAN_NAME, DummyThemeSource.class);

		registerSingleton("handlerMapping", BeanNameUrlHandlerMapping.class);
		registerSingleton("viewResolver", InternalResourceViewResolver.class);

		pvs = new MutablePropertyValues();
		pvs.add("location", "org/springframework/web/context/WEB-INF/sessionContext.xml");
		registerSingleton("viewResolver2", XmlViewResolver.class, pvs);

		super.refresh();
	}


	public static class LocaleChecker implements Controller {
		@Override
        public void handleActionRequest(ActionRequest request, ActionResponse response) throws Exception {
		    if (!(PortletRequestContextUtils.getPortletApplicationContext(request) instanceof SimplePortletApplicationContext)) {
                throw new PortletException("Incorrect PortletApplicationContext");
            }
        }

        @Override
        public ModelAndView handleRenderRequest(RenderRequest request, RenderResponse response) throws Exception {
            if (!(PortletRequestContextUtils.getPortletApplicationContext(request) instanceof SimplePortletApplicationContext)) {
                throw new PortletException("Incorrect PortletApplicationContext");
            }
            return null;
        }
	}


	public static class DummyThemeSource implements ThemeSource {

		private StaticMessageSource messageSource;

		public DummyThemeSource() {
			this.messageSource = new StaticMessageSource();
			this.messageSource.addMessage("themetest", Locale.ENGLISH, "theme test message");
			this.messageSource.addMessage("themetestArgs", Locale.ENGLISH, "theme test message {0}");
		}

		public Theme getTheme(String themeName) {
			if (AbstractThemeResolver.ORIGINAL_DEFAULT_THEME_NAME.equals(themeName)) {
				return new SimpleTheme(AbstractThemeResolver.ORIGINAL_DEFAULT_THEME_NAME, this.messageSource);
			}
			else {
				return null;
			}
		}
	}

}
