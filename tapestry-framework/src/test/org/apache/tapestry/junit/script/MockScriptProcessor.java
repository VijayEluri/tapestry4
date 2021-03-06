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

package org.apache.tapestry.junit.script;

import org.apache.hivemind.Resource;
import org.apache.tapestry.IComponent;
import org.apache.tapestry.IScriptProcessor;
import org.apache.tapestry.util.IdAllocator;
import org.apache.tapestry.util.PageRenderSupportImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Used by {@link org.apache.tapestry.junit.script.TestScript}.
 *
 * @author Howard Lewis Ship
 * @since 3.0
 */
public class MockScriptProcessor implements IScriptProcessor
{
    private StringBuffer _body;

    private StringBuffer _initialization;

    private StringBuffer _postInitialization;

    private List _externalScripts;

    private IdAllocator _idAllocator = new IdAllocator();

    public void reset()
    {
        if (_body != null)
            _body.delete(0, _body.length());
        if (_initialization != null)
            _initialization.delete(0, _initialization.length());
        if (_externalScripts != null)
            _externalScripts.clear();
        _idAllocator.clear();
    }

    public void addBodyScript(String script)
    {
        addBodyScript(null, script);
    }

    public void addBodyScript(IComponent target, String script)
    {
        if (_body == null)
            _body = new StringBuffer();

        _body.append(script);
    }

    public String getBody()
    {
        if (_body == null)
            return null;

        return _body.toString();
    }

    public void addInitializationScript(String script)
    {
        addInitializationScript(null, script);
    }

    public void addInitializationScript(IComponent target, String script)
    {
        if (_initialization == null)
            _initialization = new StringBuffer();

        _initialization.append(script);
    }

    public void addScriptAfterInitialization(IComponent target, String script)
    {
        if (_postInitialization == null)
            _postInitialization = new StringBuffer();

        _postInitialization.append(script);
    }

    public String getInitialization()
    {
        if (_initialization == null && _postInitialization == null)
            return null;

        return PageRenderSupportImpl.getContent(_initialization)
               + PageRenderSupportImpl.getContent(_postInitialization);
    }

    public void addExternalScript(Resource scriptResource)
    {
        addExternalScript(null, scriptResource);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isBodyScriptAllowed(IComponent target)
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isExternalScriptAllowed(IComponent target)
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isInitializationScriptAllowed(IComponent target)
    {
        return true;
    }

    public void addExternalScript(IComponent target, Resource scriptResource)
    {
        if (_externalScripts == null)
            _externalScripts = new ArrayList();

        _externalScripts.add(scriptResource);
    }

    public Resource[] getExternalScripts()
    {
        if (_externalScripts == null)
            return null;

        int count = _externalScripts.size();

        return (Resource[]) _externalScripts.toArray(new Resource[count]);
    }

    public String getUniqueString(String baseValue)
    {
        return _idAllocator.allocateId(baseValue);
    }

}
