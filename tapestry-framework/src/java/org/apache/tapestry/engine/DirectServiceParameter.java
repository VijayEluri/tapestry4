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

package org.apache.tapestry.engine;

import org.apache.hivemind.util.Defense;
import org.apache.tapestry.IDirect;
import org.apache.tapestry.IDynamicInvoker;

import java.util.Collection;

/**
 * Parameter object used by {@link org.apache.tapestry.engine.DirectService}.
 * 
 * @author Howard M. Lewis Ship
 * @since 4.0
 */
public class DirectServiceParameter
{
    private IDirect _direct;

    private Object[] _serviceParameters;

    private String[] _updateParts;
    
    private boolean _json;
    
    private boolean _async;
    
    public DirectServiceParameter(IDirect direct)
    {
        this(direct, null);
    }
    
    public DirectServiceParameter(IDirect direct, Object[] serviceParameters)
    {
        this(direct, serviceParameters, null);
    }
    
    public DirectServiceParameter(IDirect direct, Object[] serviceParameters, IDynamicInvoker invoker)
    {
        Defense.notNull(direct, "direct");
        
        _direct = direct;
        _serviceParameters = serviceParameters;
        
        if (invoker == null) {
            
            Collection comps = direct.getUpdateComponents();
            if (comps == null)
                _updateParts = new String[0];
            else
                _updateParts = (String[])comps.toArray(new String[comps.size()]);
            
            _json = direct.isJson();
            _async = direct.isAsync();
        } else {
            
            Collection comps = invoker.getUpdateComponents();
            if (comps == null)
                _updateParts = new String[0];
            else
                _updateParts = (String[])comps.toArray(new String[comps.size()]);
            
            _json = invoker.isJson();
            _async = invoker.isAsync();
        }
        
        // if they gave only an updateComponents param make it async by default
        
        if (!_json && !_async && _updateParts.length > 0)
            _async = true;
    }
    
    public IDirect getDirect()
    {
        return _direct;
    }

    public Object[] getServiceParameters()
    {
        return _serviceParameters;
    }

    public String[] getUpdateParts()
    {
        return _updateParts;
    }
    
    public boolean isJson()
    {
        return _json;
    }
    
    public boolean isAsync()
    {
        return _async;
    }
}
