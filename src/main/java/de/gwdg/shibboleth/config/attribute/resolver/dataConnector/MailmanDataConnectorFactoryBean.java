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

import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gwdg.shibboleth.attribute.resolver.provider.dataConnector.MailmanDataConnector;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.TemplateEngine;
import edu.internet2.middleware.shibboleth.common.config.attribute.resolver.dataConnector.BaseDataConnectorFactoryBean;

public class MailmanDataConnectorFactoryBean extends BaseDataConnectorFactoryBean {

    /** logger. */
    private final Logger log = LoggerFactory.getLogger(MailmanDataConnectorFactoryBean.class);
    
    /** mailman server host */
    private String mailmanHost;
    
    /** mailman server port */
    private String mailmanPort;
        
    /** mailman user name */
    private String username;
    
    /** mailman password */
    private String password;
    
    /** Template that create the query to use */
    private String queryTemplate;
    
    /** Template engine used to construct queries. */
    private TemplateEngine templateEngine;
    
    /** Mapping object between mailman attributes and shibboleth attributes */
    private Map<String, String> keyAttributeMap;
    
    @Override
    /** {@inheritDoc} */
    public Class getObjectType() {
        return MailmanDataConnector.class;
    }

    @Override
    /** {@inheritDoc} */
    protected Object createInstance() throws Exception {
        MailmanDataConnector connector = new MailmanDataConnector(getmailmanHost(), getmailmanPort(),
                getUsername(), getPassword());
        populateDataConnector(connector);
        connector.setQueryTemplate(getQueryTemplate());
        connector.setTemplateEngine(getTemplateEngine());
        
        if (!keyAttributeMap.isEmpty()) {
            for (Entry<String, String> mapping : keyAttributeMap.entrySet()) {
                connector.putAttributeMaping(mapping.getKey(), mapping.getValue());
            }
        }
        
        // TODO uncomment and adapt
//        if (getKeyAttributeMap() != null) {
//            Map<String, MongoDbKeyAttributeMapper> keyAttributeMap = connector.getKeyAttributeMap();
//            for (MongoDbKeyAttributeMapper attributeMapper : getKeyAttributeMap()) {
//                keyAttributeMap.put(attributeMapper.getMongoKey(), attributeMapper);
//            }
//        }
        
        connector.initialize();
        
        return connector;
    }
    
    public String getmailmanHost() {
        return mailmanHost;
    }

    public void setmailmanHost(String mailmanHost) {
        this.mailmanHost = mailmanHost;
    }

    public String getmailmanPort() {
        return mailmanPort;
    }

    public void setmailmanPort(String mailmanPort) {
        this.mailmanPort = mailmanPort;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getQueryTemplate() {
        return queryTemplate;
    }

    public void setQueryTemplate(String queryTemplate) {
        this.queryTemplate = queryTemplate;
    }

    public TemplateEngine getTemplateEngine() {
        return templateEngine;
    }

    public void setTemplateEngine(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public Map<String, String> getKeyAttributeMap() {
        return keyAttributeMap;
    }

    public void setKeyAttributeMap(Map<String, String> keyAttributeMap) {
        this.keyAttributeMap = keyAttributeMap;
    }

}
