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

package de.gwdg.shibboleth.attribute.resolver.provider.dataConnector;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.BasicAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.AttributeResolutionException;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ShibbolethResolutionContext;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.BaseDataConnector;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.TemplateEngine;

public class MailmanDataConnector extends BaseDataConnector {
    
    /** Logger. */
    private final Logger log = LoggerFactory.getLogger(MailmanDataConnector.class);
    
    /** Template name that produces the query to use. */
    private String queryTemplateName;

    /** Template that produces the query to use. */
    private String queryTemplate;
    
    /** Template engine that create a real query from the query template. */
    private TemplateEngine queryCreator;
        
    /** Mapping object between mailman document keys and shibboleth attributes */
    private Map<String, String> keyAttributeMap;
    
    /** mailman server host */
    private String mailmanHost;
    
    /** mailman server port */
    private String mailmanPort;
    
    /** mailman db name */
    private String mailmanName;
    
    /** mailman user name */
    private String username;
    
    /** mailman password */
    private String password;
    
    public MailmanDataConnector(String mailmanHost, String mailmanPort,
            String username, String password) {
        this.mailmanHost = mailmanHost;
        this.mailmanPort = mailmanPort;
        this.username = username;
        this.password = password;
        
        keyAttributeMap = new HashMap<String, String>();
    }

    /**
     * Initialises the mailman connection and the query template creator
     */
    public void initialize() {
        log.debug("Initializing mailman connection");
   
        registerTemplate();
    }

    private void registerTemplate() {
        queryTemplateName = "shibboleth.resolver.dc." + getId();
        queryCreator.registerTemplate(queryTemplateName, queryTemplate);
    }

    /** {@inheritDoc} */
    public Map<String, BaseAttribute> resolve(ShibbolethResolutionContext resolutionContext)
        throws AttributeResolutionException {

        Map<String, BaseAttribute> result = new HashMap<String, BaseAttribute>();
        
        String queryStr = queryCreator.createStatement(queryTemplateName, resolutionContext,
                getDependencyIds(), null);
        log.debug("mailman connector {} search query: {}", getId(), queryStr);
        String command1="list_lists -ab";
        String grouplist="find_member -w ";
        StringBuilder output_final = new StringBuilder();
        String uid = resolutionContext.getAttributeRequestContext().getPrincipalName();
        try{
            
            java.util.Properties config = new java.util.Properties(); 
            config.put("StrictHostKeyChecking", "no");
            JSch jsch = new JSch();
            Session session=jsch.getSession(username, mailmanHost, Integer.parseInt(mailmanPort));
            session.setPassword(password);
            session.setConfig(config);
            session.connect();
            //System.out.println("Connected");
             
            Channel channel=session.openChannel("exec");
            ((ChannelExec)channel).setCommand(command1);
            channel.setInputStream(null);
            ((ChannelExec)channel).setErrStream(System.err);
             
            InputStream in=channel.getInputStream();
            channel.connect();
            byte[] tmp=new byte[27];
            StringBuilder output = new StringBuilder();
            while(true){
              while(in.available()>0){
                int i=in.read(tmp, 0, 27);
                if(i<0)break;
                output = new StringBuilder().append(output).append(new String(tmp, 0, i).toString());
              }
              
              if(channel.isClosed()){
                //System.out.println("exit-status: "+channel.getExitStatus());
                break;
              }
              try{Thread.sleep(1000);}catch(Exception ee){}
            }
            //System.out.print(output.indexOf("\n"));
            //String grouplist = output.substring(output.indexOf("\n")+1);
            log.debug("mailman connector {} search query: {}", getId(), output);
            if(output.length()!=0 || output != null){
	            String[] lines = output.toString().split("\\n");
	            for(String groupname: lines){
	              grouplist = grouplist + "-l" + groupname + " ";
	            }
	            grouplist = grouplist + uid;
	            log.debug("mailman connector {} search query: {}", getId(), grouplist);
	            channel=session.openChannel("exec");
	            ((ChannelExec)channel).setCommand(grouplist);
	            channel.setInputStream(null);
	            ((ChannelExec)channel).setErrStream(System.err);
	            in=channel.getInputStream();
	            channel.connect();
	            tmp=new byte[27];
	            
	            
	            while(true){
	              while(in.available()>0){
	                int i=in.read(tmp, 0, 27);
	                if(i<0)break;
	                output_final = new StringBuilder().append(output_final).append(new String(tmp, 0, i).toString());
	                log.debug("mailman connector {} search query: {}", getId(), output_final);
	              }
	              
	              if(channel.isClosed()){
	                //System.out.println("exit-status: "+channel.getExitStatus());
	                break;
	              }
	              try{Thread.sleep(1000);}catch(Exception ee){}
	            }
	            //System.out.println(output_final);
            }
                        
            channel.disconnect();
            session.disconnect();
            //System.out.println("DONE");
        }catch(Exception e){
            e.printStackTrace();
        }
        
        
        String key = "Value";
        String[] lines = output_final.toString().split("\\n");
        lines[0] = "";
        String value = "";
        for(String s: lines){
            value = value + "," + s;
        }
        value = value.substring(2,value.length());
        BaseAttribute<String> attr;
        attr = new BasicAttribute<String>(key);
        //String abc = output_final;
        attr.getValues().add(value);
        result.put(key, attr);
        
        log.debug("Reached the end. {}",result.get(key));
        
       
        return result;
    }

    /** {@inheritDoc} */
    public void validate() throws AttributeResolutionException {
        log.debug("Validating mailman connector {} configuration.", getId());

        
    }

    /**
     * Gets the mailman server host
     * 
     * @return the mailman server host
     */
    public String getmailmanHost() {
        return mailmanHost;
    }

    /**
     * Sets the mailman server host
     *
     * @param mailmanHost the mailman server host
     */
    public void setmailmanHost(String mailmanHost) {
        this.mailmanHost = mailmanHost;
    }

    /**
     * Gets the mailman server port
     * 
     * @return the mailman server hosts
     */
    public String getmailmanPort() {
        return mailmanPort;
    }

    /**
     * Sets the mailman server port
     * 
     * @param mailmanPort the mailman server port
     */
    public void setmailmanPort(String mailmanPort) {
        this.mailmanPort = mailmanPort;
    }

    /**
     * Gets the mailman username to use to log in
     * 
     * @return the mailman username to use to log in
     */
    public String getUsername() {
        return username;
    }
 
    /**
     * Sets the mailman username to use to log in
     * 
     * @param username the mailman username to use to log in
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the password of the user used to log into the mailman
     * 
     * @return the password of the user used to log into the mailman
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password of the user used to log into the mailman
     * 
     * @param password the password of the user used to log into the mailman
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the template used to create a query.
     *
     * @return template used to create a query
     */
    public String getQueryTemplate() {
        return queryTemplate;
    }

    /**
     * Sets the template used to create a query.
     *
     * @param template used to create a query
     */
    public void setQueryTemplate(String template) {
        queryTemplate = template;
    }

    /**
     * Gets the engine used to build the query.
     *
     * @return engine used to build the query
     */
    public TemplateEngine getTemplateEngine() {
        return queryCreator;
    }

    /**
     * Sets the engine used to build the query.
     *
     * @param engine used to build the query
     */
    public void setTemplateEngine(TemplateEngine engine) {
        queryCreator = engine;
        registerTemplate();
    }
    

    /**
     * Puts a key mapping in the keyAttributeMap
     * 
     * @param key The mailman key
     * @param value The corresponding Shibboleth attribute
     */
    public void putAttributeMaping(String key, String value) {
        keyAttributeMap.put(key, value);        
    }
}
