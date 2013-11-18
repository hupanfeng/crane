package com.hdd.crane.config.builder;

import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

import com.hdd.crane.config.Configuration;
import com.hdd.crane.config.builder.parser.XNode;
import com.hdd.crane.config.builder.parser.XPathParser;
import com.hdd.crane.io.Resources;
import com.hdd.crane.server.Service;
import com.hdd.crane.wharf.server.CustomsService;
import com.hdd.crane.wharf.server.WharfService;
import com.hdd.crane.wharf.startup.WharfServer;

public class XMLConfigBuilder {

    private Configuration configuration;
    private XPathParser parser;

    public XMLConfigBuilder(Reader reader) {
        this(reader, null);
    }

    public XMLConfigBuilder(Reader reader, Properties props) {
        this(new XPathParser(reader, true, props, new XMLMapperEntityResolver()), props);
    }

    public XMLConfigBuilder(InputStream inputStream) {
        this(inputStream, null);
    }

    public XMLConfigBuilder(InputStream inputStream, Properties props) {
        this(new XPathParser(inputStream, true, props, new XMLMapperEntityResolver()), props);
    }

    private XMLConfigBuilder(XPathParser parser, Properties props) {
        configuration = new Configuration();
        this.configuration.setVariables(props);
        this.parser = parser;
    }

    public Configuration parse() {
        parseConfiguration(parser.evalNode("/configuration"));
        return configuration;
    }

    private void parseConfiguration(XNode root) {
        try {
            // int monitorInterval = root.getIntAttribute("monitorInterval");
            // configuration.setMonitorInterval(monitorInterval);
            // parseProperties(root.evalNode("properties"));
            parseServer(root.evalNode("server"));
        } catch (Exception e) {
            throw new BuilderException("Error parsing Configuration. Cause: " + e, e);
        }
    }

    private void parseServer(XNode serverNode) throws Exception {
        String serverName = serverNode.getStringAttribute("name");
        switch (serverName) {
        case "wharf":
            configuration.setServer(new WharfServer());
        default:
            break;
        }
        parseServices(serverNode);
    }

    private void parseServices(XNode parent) throws Exception {
        if (parent != null) {
            Service service = null;
            for (XNode child : parent.getChildren()) {
                String serviceName = child.getStringAttribute("name");
                switch (serviceName) {
                case "wharf":
                    service = new WharfService();
                    break;
                case "customs":
                    service = new CustomsService();
                    break;
                default:
                    throw new Exception("service name set error!");
                }
                if (null != child.getStringAttribute("ip")) {
                    service.setIP(child.getStringAttribute("ip"));
                }
                if (null != child.getIntAttribute("port")) {
                    service.setPort(child.getIntAttribute("port"));
                }
                if (null != child.getStringAttribute("serverType")) {
                    service.setServerType(child.getStringAttribute("serverType"));
                }
                if (null != child.getIntAttribute("bufferSize")) {
                    service.setBufferSize(child.getIntAttribute("bufferSize"));
                }
                if (null != child.getIntAttribute("idleTime")) {
                    service.setIdleTime(child.getIntAttribute("idleTime"));
                }
                configuration.getServer().addService(service);
            }
        }
    }

    private void parseProperties(XNode context) throws Exception {
        if (context != null) {
            Properties defaults = context.getChildrenAsProperties();
            String resource = context.getStringAttribute("resource");
            String url = context.getStringAttribute("url");
            if (resource != null && url != null) {
                throw new BuilderException("The properties element cannot specify both a URL and a resource based property file reference.  Please specify one or the other.");
            }
            if (resource != null) {
                defaults.putAll(Resources.getResourceAsProperties(resource));
            } else if (url != null) {
                defaults.putAll(Resources.getUrlAsProperties(url));
            }
            Properties vars = configuration.getVariables();
            if (vars != null) {
                defaults.putAll(vars);
            }
            parser.setVariables(defaults);
            configuration.setVariables(defaults);
        }
    }

}
