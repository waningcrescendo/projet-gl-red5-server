/*
 * RED5 Open Source Media Server - https://github.com/Red5/ Copyright 2006-2023 by respective authors (see below). All rights reserved. Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless
 * required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package org.red5.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.selector.ContextSelector;
import ch.qos.logback.classic.util.ContextSelectorStaticBinder;
import ch.qos.logback.core.CoreConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LoggerFactory to simplify requests for Logger instances within Red5 applications. This class is
 * expected to be run only once per logger request and is optimized as such.
 *
 * @author Paul Gregoire (mondain@gmail.com)
 */
public class Red5LoggerFactory {

  public static final String LOGGER_CONTEXT_ATTRIBUTE = "logger.context";

  private static boolean useLogback = true;

  public static boolean DEBUG = true;

  // root logger
  private static Logger rootLogger;

  // context selector
  private static ContextSelector contextSelector;

  static {
    DEBUG = Boolean.valueOf(System.getProperty("logback.debug", "false"));
    try {
      rootLogger = LoggerFactory.getILoggerFactory().getLogger(Logger.ROOT_LOGGER_NAME);
      rootLogger.debug(
          "Red5LoggerFactory instanced by Thread: {}", Thread.currentThread().getName());
      rootLogger.debug(
          "Logging context selector: {} impl: {}",
          System.getProperty("logback.ContextSelector"),
          getContextSelector());
      // get the context selector here
      contextSelector = getContextSelector();
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  public static Logger getLogger(Class<?> clazz) {
    if (DEBUG) {
      System.out.printf(
          "getLogger for: %s thread: %s%n", clazz.getName(), Thread.currentThread().getName());
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      rootLogger.debug("Class loader: {}", cl);
      // if cl is WebappClassLoader type we can probably get the context from it
      // if (cl instanceof WebappClassLoader) {
      //    getContextName()
      // }
    }
    Logger logger = null;
    if (useLogback) {
      // determine the red5 app name or servlet context name
      final String threadName = Thread.currentThread().getName();
      // route the Launcher entries to the correct context
      if (threadName.startsWith("Loader:/")) {
        String contextName = threadName.split("Loader:/")[1];
        logger = getLogger(clazz, contextName);
      } else {
        logger = getLogger(clazz, CoreConstants.DEFAULT_CONTEXT_NAME);
      }
    }
    if (logger == null) {
      logger = LoggerFactory.getLogger(clazz);
    }
    return logger;
  }

  public static Logger getLogger(Class<?> clazz, String contextName) {
    return getLogger(clazz.getName(), contextName);
  }

  public static Logger getLogger(String name, String contextName) {
    if (DEBUG) {
      System.out.printf(
          "getLogger for: %s in context: %s thread: %s%n",
          name, contextName, Thread.currentThread().getName());
    }
    Logger logger = null;
    if (useLogback) {
      // disallow null context names
      if (contextName == null) {
        contextName = CoreConstants.DEFAULT_CONTEXT_NAME;
      }
      try {
        // get the context for the given context name or default if null
        LoggerContext context = contextSelector.getLoggerContext(contextName);
        // and if we get here, fall back to the default context
        if (context == null) {
          System.err.printf("No context named %s was found!!%n", contextName);
        }
        // get the logger from the context or default context
        if (context != null) {
          logger = context.getLogger(name);
          if (DEBUG) {
            rootLogger.debug(
                "Application name: {} in context: {}",
                context.getProperty(CoreConstants.CONTEXT_NAME_KEY),
                contextName);
          }
        }
      } catch (Exception e) {
        // no logback, use whatever logger is in-place
        rootLogger.error("Exception {}", e);
      }
    }
    if (logger == null) {
      logger = LoggerFactory.getLogger(name);
    }
    return logger;
  }

  public static ContextSelector getContextSelector() {
    ContextSelector selector = null;
    if (useLogback) {
      ContextSelectorStaticBinder contextSelectorBinder =
          ContextSelectorStaticBinder.getSingleton();
      selector = contextSelectorBinder.getContextSelector();
      if (selector == null) {
        if (DEBUG) {
          rootLogger.error("Context selector was null, creating default context");
        }
        LoggerContext defaultLoggerContext = new LoggerContext();
        defaultLoggerContext.setName(CoreConstants.DEFAULT_CONTEXT_NAME);
        try {
          contextSelectorBinder.init(defaultLoggerContext, null);
          selector = contextSelectorBinder.getContextSelector();
          rootLogger.debug("Context selector: {}", selector.getClass().getName());
        } catch (Exception e) {
          rootLogger.error("Exception {}", e);
        }
      }
    }
    return selector;
  }

  public static void setUseLogback(boolean useLogback) {
    Red5LoggerFactory.useLogback = useLogback;
  }
}
