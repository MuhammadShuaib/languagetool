/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.server;

import com.sun.net.httpserver.HttpServer;
import org.languagetool.JLanguageTool;
import org.languagetool.gui.Tools;

import java.net.InetSocketAddress;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.*;

import static org.languagetool.server.HTTPServerConfig.DEFAULT_HOST;

/**
 * A small embedded HTTP server that checks text. Returns XML, prints debugging
 * to stdout/stderr. Note that by default the server only accepts connections from 
 * localhost for security reasons.
 * 
 * @author Daniel Naber
 * @author Ankit
 */
public class HTTPServer extends Server {

  private final ExecutorService executorService;

  /**
   * Prepare a server on the given port - use run() to start it. Accepts
   * connections from localhost only.
   */
  public HTTPServer() {
    this(new HTTPServerConfig());
  }

  /**
   * Prepare a server on localhost on the given port - use run() to start it. Accepts
   * connections from localhost only.
   * @throws PortBindingException if we cannot bind to the given port, e.g. because something else is running there
   */
  public HTTPServer(HTTPServerConfig config) {
    this(config, false, DEFAULT_ALLOWED_IPS);
  }

  
  /**
   * Prepare a server on localhost on the given port - use run() to start it. Accepts
   * connections from localhost only.
   * @param runInternally if true, then the server was started from the GUI.
   * @throws PortBindingException if we cannot bind to the given port, e.g. because something else is running there
   */
  public HTTPServer(HTTPServerConfig config, boolean runInternally) {
    this(config, runInternally, DEFAULT_HOST, DEFAULT_ALLOWED_IPS);
  }

  
  /**
   * Prepare a server on localhost on the given port - use run() to start it. The server will bind to localhost.
   * @param runInternally if true, then the server was started from the GUI.
   * @param allowedIps the IP addresses from which connections are allowed or <code>null</code> to allow any host
   * @throws PortBindingException if we cannot bind to the given port, e.g. because something else is running there
   */
  public HTTPServer(HTTPServerConfig config, boolean runInternally, Set<String> allowedIps) {
    this(config, runInternally, DEFAULT_HOST, allowedIps);
  }
  
  /**
   * Prepare a server on the given host and port - use run() to start it.
   * @param runInternally if true, then the server was started from the GUI.
   * @param host the host to bind to, e.g. <code>"localhost"</code> or <code>null</code> to bind to any host
   * @param allowedIps the IP addresses from which connections are allowed or <code>null</code> to allow any host
   * @throws PortBindingException if we cannot bind to the given port, e.g. because something else is running there
   * @since 1.7
   */
  public HTTPServer(HTTPServerConfig config, boolean runInternally, String host, Set<String> allowedIps) {
    this.port = config.getPort();
    this.host = host;
    try {
      InetSocketAddress address = host != null ? new InetSocketAddress(host, port) : new InetSocketAddress(port);
      server = HttpServer.create(address, 0);
      final RequestLimiter limiter = getRequestLimiterOrNull(config);
      final LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
      httpHandler = new LanguageToolHttpHandler(config.isVerbose(), allowedIps, runInternally, limiter, workQueue);
      httpHandler.setMaxTextLength(config.getMaxTextLength());
      httpHandler.setAllowOriginUrl(config.getAllowOriginUrl());
      httpHandler.setMaxCheckTimeMillis(config.getMaxCheckTimeMillis());
      httpHandler.setTrustXForwardForHeader(config.getTrustXForwardForHeader());
      if (config.getMode() == HTTPServerConfig.Mode.AfterTheDeadline) {
        httpHandler.setAfterTheDeadlineMode(config.getAfterTheDeadlineLanguage());
      }
      httpHandler.setLanguageModel(config.getLanguageModelDir());
      httpHandler.setMaxWorkQueueSize(config.getMaxWorkQueueSize());
      httpHandler.setRulesConfigurationFile(config.getRulesConfigFile());
      server.createContext("/", httpHandler);
      executorService = getExecutorService(workQueue, config);
      server.setExecutor(executorService);
    } catch (Exception e) {
      final ResourceBundle messages = JLanguageTool.getMessageBundle();
      final String message = Tools.makeTexti18n(messages, "http_server_start_failed", host, Integer.toString(port));
      throw new PortBindingException(message, e);
    }
  }

  @Override
  public void stop() {
    super.stop();
    if (executorService != null) {
      executorService.shutdownNow();
    }
  }

  public static void main(String[] args) {
    if (args.length > 5 || usageRequested(args)) {
      System.out.println("Usage: " + HTTPServer.class.getSimpleName() + " [--config propertyFile] [--port|-p port] [--public]");
      System.out.println("  --config file  a Java property file (one key=value entry per line) with values for:");
      printCommonConfigFileOptions();
      printCommonOptions();
      System.exit(1);
    }
    final boolean runInternal = false;
    final HTTPServerConfig config = new HTTPServerConfig(args);
    try {
      final HTTPServer server;
      System.out.println("WARNING: running in HTTP mode, consider using " + HTTPSServer.class.getName() + " for encrypted connections");
      if (config.isPublicAccess()) {
        System.out.println("WARNING: running in public mode, LanguageTool API can be accessed without restrictions!");
        server = new HTTPServer(config, runInternal, null, null);
      } else {
        server = new HTTPServer(config, runInternal, DEFAULT_HOST, DEFAULT_ALLOWED_IPS);
      }
      server.run();
    } catch (Exception e) {
      throw new RuntimeException("Could not start LanguageTool HTTP server on " + DEFAULT_HOST + ", port " + config.getPort(), e);
    }
  }

  @Override
  protected String getProtocol() {
    return "http";
  }

}

