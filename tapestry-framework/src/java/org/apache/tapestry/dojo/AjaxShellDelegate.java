// Copyright 2004, 2005 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.apache.tapestry.dojo;

import org.apache.hivemind.util.Defense;
import org.apache.tapestry.IAsset;
import org.apache.tapestry.IMarkupWriter;
import org.apache.tapestry.IRender;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.engine.IEngineService;

/**
 * The default rendering delegate responseible for include the 
 * dojo sources into the {@link Shell} component.
 *
 * @author jkuhnert
 */
public class AjaxShellDelegate implements IRender
{
    /** Client side debug log level. */
    public static final String BROWSER_LOG_DEBUG="DEBUG";
    /** Client side info log level. */
    public static final String BROWSER_LOG_INFO="INFO";
    /** Client side warning log level. */
    public static final String BROWSER_LOG_WARNING="WARNING";
    /** Client side error log level. */
    public static final String BROWSER_LOG_ERROR="ERROR";
    /** Client side critical log level. */
    public static final String BROWSER_LOG_CRITICAL="CRITICAL";
    
    private IAsset _dojoSource;
    
    private IAsset _dojoPath;
    
    private IAsset _tapestrySource;
    
    private IEngineService _assetService;
    
    private String _browserLogLevel = BROWSER_LOG_WARNING;
    
    private boolean _debug;
    
    /**
     * {@inheritDoc}
     */
    public void render(IMarkupWriter writer, IRequestCycle cycle)
    {
        // TODO: Add ability to make all of these parameters more configurable
        
        // first configure dojo, has to happen before package include
        StringBuffer str = new StringBuffer("<script type=\"text/javascript\">");
        str.append("djConfig = { isDebug: ").append(_debug).append(",")
        .append(" debugContainerId:'debug',")
        .append(" baseRelativePath:\"")
        .append(_assetService.getLink(true,
                _dojoPath.getResourceLocation().getPath()).getAbsoluteURL())
        .append("\", preventBackButtonFix: false, parseWidgets: false };")
        .append(" </script>\n\n ");
        
        // include the core dojo.js package
        str.append("<script type=\"text/javascript\" src=\"")
        .append(_assetService.getLink(true,
                _dojoSource.getResourceLocation()
                .getPath()).getAbsoluteURL()).append("\"></script>");
        
        // include core tapestry.js package
        str.append("<script type=\"text/javascript\" src=\"")
        .append(_assetService.getLink(true,
                _tapestrySource.getResourceLocation()
                .getPath()).getAbsoluteURL()).append("\"></script>");
        
        // logging configuration
        str.append("\n<script type=\"text/javascript\">")
        .append("dojo.require(\"dojo.logging.Logger\");\n")
        .append("dojo.log.setLevel(dojo.log.getLevel(\"").append(_browserLogLevel)
        .append("\"));</script>");
        
        writer.printRaw(str.toString());
    }
    
    /**
     * Sets the dojo logging level. Similar to log4j style
     * log levels. 
     * @param level The string constant for the level, valid values
     *              are:
     *              <p>
     *              <ul>
     *              <li>{@link #BROWSER_LOG_DEBUG}</li>
     *              <li>{@link #BROWSER_LOG_INFO}</li>
     *              <li>{@link #BROWSER_LOG_WARNING}</li>
     *              <li>{@link #BROWSER_LOG_ERROR}</li>
     *              <li>{@link #BROWSER_LOG_CRITICAL}</li>
     *              </ul>
     *              </p>
     */
    public void setLogLevel(String level)
    {
        Defense.notNull("level", level);
        
        _browserLogLevel = level;
    }
    
    /**
     * Allows for turning browser debugging on/off.
     * 
     * @param debug If false, no logging output will be written.
     */
    public void setDebug(boolean debug)
    {
        _debug = debug;
    }
    
    /**
     * Sets a valid path to the base dojo javascript installation
     * directory.
     * @param dojoSource
     */
    public void setDojoSource(IAsset dojoSource)
    {
        _dojoSource = dojoSource;
    }
    
    /**
     * Sets the dojo baseRelativePath value.
     * @param dojoPath
     */
    public void setDojoPath(IAsset dojoPath)
    {
        _dojoPath = dojoPath;
    }
    
    /**
     * Sets a valid base path to resolve tapestry.js.
     * @param tapestrySource
     */
    public void setTapestrySource(IAsset tapestrySource)
    {
        _tapestrySource = tapestrySource;
    }
    
    /**
     * Injected asset service.
     * @param service
     */
    public void setAssetService(IEngineService service)
    {
        _assetService = service;
    }
}
