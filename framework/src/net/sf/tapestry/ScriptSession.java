/*
 *  ====================================================================
 *  The Apache Software License, Version 1.1
 *
 *  Copyright (c) 2002 The Apache Software Foundation.  All rights
 *  reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 *  3. The end-user documentation included with the redistribution,
 *  if any, must include the following acknowledgment:
 *  "This product includes software developed by the
 *  Apache Software Foundation (http://www.apache.org/)."
 *  Alternately, this acknowledgment may appear in the software itself,
 *  if and wherever such third-party acknowledgments normally appear.
 *
 *  4. The names "Apache" and "Apache Software Foundation" and
 *  "Apache Tapestry" must not be used to endorse or promote products
 *  derived from this software without prior written permission. For
 *  written permission, please contact apache@apache.org.
 *
 *  5. Products derived from this software may not be called "Apache",
 *  "Apache Tapestry", nor may "Apache" appear in their name, without
 *  prior written permission of the Apache Software Foundation.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of the Apache Software Foundation.  For more
 *  information on the Apache Software Foundation, please see
 *  <http://www.apache.org/>.
 */
package net.sf.tapestry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *  The result of executing a script, the session is used during the parsing
 *  process as well.  Following {@link IScript#execute(Map)}, the session
 *  provides access to output symbols as well as the body and initialization
 *  blocks created by the script tokens.
 *
 *  @author Howard Lewis Ship
 *  @version $Id$
 *  @since 0.2.9
 * 
 **/

public class ScriptSession
{
    private IResourceLocation _scriptLocation;
    private Map _symbols;
    private String _body;
    private String _initialization;

    /**
     *  List of included scripts.
     *
     *  @since 1.0.5
     *
     **/

    private List _includes;

    public ScriptSession(IResourceLocation scriptPath, Map symbols)
    {
        _scriptLocation = scriptPath;
        _symbols = symbols;
    }

    public IResourceLocation getScriptPath()
    {
        return _scriptLocation;
    }

    public Map getSymbols()
    {
        return _symbols;
    }

    /**
     *  Returns a list of scripts included by the
     *  the executed script.  These are not URLs, they
     *  are resource paths (i.e., in the classpath).
     *
     *  @since 1.0.5
     *
     **/

    public List getIncludedScripts()
    {
        return _includes;
    }

    public void addIncludedScript(String resourcePath)
    {
        if (_includes == null)
            _includes = new ArrayList();

        _includes.add(resourcePath);
    }

    public void setBody(String value)
    {
        _body = value;
    }

    public String getBody()
    {
        return _body;
    }

    public String getInitialization()
    {
        return _initialization;
    }

    public void setInitialization(String value)
    {
        _initialization = value;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("ScriptSession[");
        buffer.append(_scriptLocation);
        buffer.append(']');

        return buffer.toString();
    }
}