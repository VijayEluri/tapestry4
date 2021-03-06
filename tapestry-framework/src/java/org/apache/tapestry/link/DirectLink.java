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

package org.apache.tapestry.link;

import java.util.Collection;

import org.apache.tapestry.IActionListener;
import org.apache.tapestry.IDirect;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.IScript;
import org.apache.tapestry.engine.DirectServiceParameter;
import org.apache.tapestry.engine.IEngineService;
import org.apache.tapestry.engine.ILink;
import org.apache.tapestry.listener.ListenerInvoker;

/**
 * A component for creating a link using the direct service; used for actions that are not dependant
 * on dynamic page state. [ <a href="../../../../../components/link/directlink.html">Component
 * Reference </a>]
 * 
 */

public abstract class DirectLink extends AbstractLinkComponent implements IDirect
{
    public abstract IActionListener getListener();

    /**
     * Returns true if the stateful parameter is bound to a true value. If stateful is not bound,
     * also returns the default, true.
     */
    
    public abstract boolean isStateful();
    
    public ILink getLink(IRequestCycle cycle)
    {
        Object[] serviceParameters = constructServiceParameters(getParameters());
        
        DirectServiceParameter dsp = new DirectServiceParameter(this, serviceParameters);
        
        return getEngine().getLink(isStateful(), dsp);
    }
    
    /**
     * Converts a service parameters value to an array of objects. This is used by the
     * {@link DirectLink},{@link ServiceLink}and {@link ExternalLink}components.
     * 
     * @param parameterValue
     *            the input value which may be
     *            <ul>
     *              <li>null (returns null)
     *              <li>An array of Object (returns the array)
     *              <li>A {@link Collection}(returns an array of the values in the Collection})
     *              <li>A single object (returns the object as a single-element array)
     *            </ul>
     * @return An array representation of the input object.
     * @since 2.2
     */

    public static Object[] constructServiceParameters(Object parameterValue)
    {
        if (parameterValue == null)
            return null;

        if (parameterValue instanceof Object[])
            return (Object[]) parameterValue;

        if (parameterValue instanceof Collection)
            return ((Collection) parameterValue).toArray();

        return new Object[] { parameterValue };
    }

    /**
     * Invoked by the direct service to trigger the application-specific action by notifying the
     * {@link IActionListener listener}. If the listener parameter is not bound, attempt to locate
     * an implicit listener named by the capitalized component id, prefixed by "do".
     * 
     * @throws org.apache.tapestry.StaleSessionException
     *             if the component is stateful, and the session is new.
     */

    public void trigger(IRequestCycle cycle)
    {
        IActionListener listener = getListener();

        if (listener == null)
	        listener = getContainer().getListeners().getImplicitListener(this);

        getListenerInvoker().invokeListener(listener, this, cycle);
    }

    /** @since 2.2 * */

    public abstract Object getParameters();

    /**
     * Injected.
     * 
     * @since 4.0
     */

    public abstract ListenerInvoker getListenerInvoker();
    
    /**
     * Injected.
     * 
     * @since 4.1
     */
    public abstract IEngineService getEngine();
    
    /**
     * Injected.
     * @return The script to process asynchronous connection hookups.
     */
    public abstract IScript getScript();
}
