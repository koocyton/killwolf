package com.doopp.gauss.server.undertow;

import com.doopp.gauss.server.configuration.ApplicationProperties;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;

import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.InstanceFactory;
import io.undertow.servlet.api.ServletContainerInitializerInfo;
import io.undertow.servlet.util.ImmediateInstanceFactory;

import static io.undertow.Handlers.path;
import static io.undertow.Handlers.websocket;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;

import javax.net.ssl.*;
import javax.servlet.ServletContainerInitializer;
import java.security.KeyStore;
import java.util.HashSet;

public class UndertowServer implements InitializingBean, DisposableBean {

    private String webAppName;
    private Resource webAppRoot;
    private String host = "127.0.0.1";
    private int port = 8088;
    private int sslPort = 8089;
    private Resource jksFile;
    private String jksPassword;
    private String jksSecret;
    private ServletContainerInitializer servletContainerInitializer;

    private Undertow server;
    private DeploymentManager manager;

    @Override
    public void afterPropertiesSet() throws Exception {
        // web servlet
        InstanceFactory<? extends ServletContainerInitializer> instanceFactory = new ImmediateInstanceFactory<>(servletContainerInitializer);
        ServletContainerInitializerInfo sciInfo = new ServletContainerInitializerInfo(WebAppServletContainerInitializer.class, instanceFactory, new HashSet<>());
        DeploymentInfo deploymentInfo = Servlets.deployment()
                .addServletContainerInitalizer(sciInfo)
                // .addServlet(Servlets.servlet("default", DefaultServlet.class))
                .setResourceManager(new FileResourceManager(webAppRoot.getFile(), 0))
                .setClassLoader(UndertowServer.class.getClassLoader())
                .setContextPath(webAppName)
                .setDeploymentName(webAppName + "-war");

        manager = Servlets.defaultContainer().addDeployment(deploymentInfo);
        manager.deploy();
        HttpHandler httpHandler = path()
                .addPrefixPath("/", manager.start())
                .addPrefixPath("/game-socket", websocket(new GameSocketConnectionCallback()));

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(getKeyManagers(), null, null);

        server = Undertow.builder()
                // .setServerOption(UndertowOptions.ENABLE_HTTP2, true)
                .addHttpListener(port, host)
                .addHttpsListener(sslPort, host, sslContext)
                // .addHttpsListener(8443, host, sslContext)
                .setHandler(httpHandler)
                .build();
        server.start();

        System.out.print("\n >>> Undertow web server started at http://" + host + ":" + port + " and https://" + host + ":" + sslPort + "\n\n");
    }

    @Override
    public void destroy() throws Exception {
        server.stop();
        manager.stop();
        manager.undeploy();
        System.console().printf("Undertow web server on port " + port + " stopped");
    }

    private KeyManager[] getKeyManagers() {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(jksFile.getInputStream(), this.jksPassword.toCharArray());
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, this.jksSecret.toCharArray());
            return keyManagerFactory.getKeyManagers();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setServletContainerInitializer(ServletContainerInitializer servletContainerInitializer) {
        this.servletContainerInitializer = servletContainerInitializer;
    }

    public void setApplicationProperties(ApplicationProperties properties) {
        this.webAppName = properties.s("server.webAppName");
        this.webAppRoot = properties.r("server.webAppRoot");
        this.host = properties.s("server.host");
        this.port = properties.i("server.port");
        this.sslPort = properties.i("server.sslPort");
        this.jksFile = properties.r("server.jks.file");
        this.jksPassword = properties.s("server.jks.password");
        this.jksSecret = properties.s("server.jks.secret");
    }
}
