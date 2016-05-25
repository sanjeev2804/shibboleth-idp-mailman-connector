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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.shibboleth.common.config.BaseSpringNamespaceHandler;

public class MailmanDataConnectorNamespaceHandler extends BaseSpringNamespaceHandler {
    /** Class logger */
    private static final Logger log = LoggerFactory.getLogger(MailmanDataConnectorNamespaceHandler.class);
    
    /** Namespace for this handler */
    public static final String NAMESPACE = "http://gwdg.de/mailman-connector";

    /** {@inheritDoc} */
    public void init() {
        log.debug("Registering a MailmanDataConnectorBeanDefinitionParser against QName {}",
                MailmanDataConnectorBeanDefinitionParser.SCHEMA_TYPE);
        registerBeanDefinitionParser(MailmanDataConnectorBeanDefinitionParser.SCHEMA_TYPE,
                new MailmanDataConnectorBeanDefinitionParser());
    }

}
