/* 
 * Copyright 2015 Sanjeev Laha
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gwdg.shibboleth.config.attribute.resolver.dataConnector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.opensaml.xml.util.DatatypeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import edu.internet2.middleware.shibboleth.common.config.attribute.resolver.dataConnector.BaseDataConnectorBeanDefinitionParser;

/** Spring definition configuration parser for mailman connector */
public class MailmanDataConnectorBeanDefinitionParser extends BaseDataConnectorBeanDefinitionParser {
    
    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(MailmanDataConnectorBeanDefinitionParser.class);
    
    /** mailman connector type name. */
    public static final QName SCHEMA_TYPE = new QName(MailmanDataConnectorNamespaceHandler.NAMESPACE, "MailmanDataConnector");
    
    /** Name of QueryTemplate. */
    public static final QName QUERY_TEMPLATE_ELEMENT_NAME = new QName(MailmanDataConnectorNamespaceHandler.NAMESPACE, "QueryTemplate");
    
    /** Name of AttributeMapper */
    public static final QName KEY_ELEMENT_NAME = new QName(MailmanDataConnectorNamespaceHandler.NAMESPACE, "AttributeMap");

    /** {@inheritDoc} */
    protected Class getBeanClass(Element element) {
        return MailmanDataConnectorFactoryBean.class;
    }
    
    /** {@inheritDoc} */
    protected void doParse(String pluginId, Element pluginConfig, Map<QName, List<Element>> pluginConfigChildren,
                           BeanDefinitionBuilder pluginBuilder, ParserContext parserContext) {
        super.doParse(pluginId, pluginConfig, pluginConfigChildren, pluginBuilder, parserContext);
        
        String mailmanHost = pluginConfig.getAttributeNS(null, "mailmanHost");
        log.info("mailman connector {} mailman HOST: {}", pluginId, mailmanHost);
        pluginBuilder.addPropertyValue("mailmanHost", mailmanHost);
        
        String mailmanPort = pluginConfig.getAttributeNS(null, "mailmanPort");
        log.info("mailman connector {} mailman PORT: {}", pluginId, mailmanPort);
        pluginBuilder.addPropertyValue("mailmanPort", mailmanPort);

        String username = pluginConfig.getAttributeNS(null, "username");
        log.info("mailman connector {} mailman USERNAME: {}", pluginId, username);
        pluginBuilder.addPropertyValue("username", username);

        String password = pluginConfig.getAttributeNS(null, "password");
        pluginBuilder.addPropertyValue("password", password);
        
        Map<String, String> keyAttributeMaps = parseAttributeMappings(pluginId, pluginConfigChildren, pluginBuilder);
        pluginBuilder.addPropertyValue("keyAttributeMap", keyAttributeMaps);
        
        String queryTemplate = pluginConfigChildren.get(QUERY_TEMPLATE_ELEMENT_NAME).get(0).getTextContent();
        queryTemplate = DatatypeHelper.safeTrimOrNullString(queryTemplate);
        log.debug("mailman connector {} query template: {}", pluginId, queryTemplate);
        pluginBuilder.addPropertyValue("queryTemplate", queryTemplate);

        String templateEngineRef = pluginConfig.getAttributeNS(null, "templateEngine");
        pluginBuilder.addPropertyReference("templateEngine", templateEngineRef);
    }
    
    /**
     * Parse mailman Key entries
     *
     * @param pluginId the id of this connector
     * @param pluginConfigChildren configuration elements
     * @param pluginBuilder the bean definition parser
     * @return the mailman key attribute mappings
     */
    protected Map<String, String> parseAttributeMappings(String pluginId, 
            Map<QName, List<Element>> pluginConfigChildren,
            BeanDefinitionBuilder pluginBuilder) {
        
        Map<String, String> attributeMapper = new HashMap<String, String>();
        String keyName;
        String attributeName;
        
        if (pluginConfigChildren.containsKey(KEY_ELEMENT_NAME)) {
            for (Element e : pluginConfigChildren.get(KEY_ELEMENT_NAME)) {
               
                keyName = DatatypeHelper.safeTrimOrNullString(e.getAttributeNS(null, "mailmanKey"));
                attributeName = DatatypeHelper.safeTrimOrNullString(e.getAttributeNS(null, "attributeID"));
                attributeMapper.put(keyName, attributeName);
            }
            log.debug("Mailman connector {} key attribute maps: {}", pluginId, attributeMapper);
        }
        return attributeMapper;
    }
    
}
