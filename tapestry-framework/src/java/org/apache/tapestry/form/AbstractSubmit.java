// Copyright 2005 The Apache Software Foundation
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

package org.apache.tapestry.form;

import org.apache.hivemind.util.Defense;
import org.apache.tapestry.*;
import org.apache.tapestry.engine.DirectServiceParameter;
import org.apache.tapestry.engine.IEngineService;
import org.apache.tapestry.json.JSONObject;
import org.apache.tapestry.listener.ListenerInvoker;
import org.apache.tapestry.util.ScriptUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Superclass for components submitting their form.
 * 
 * @author Richard Lewis-Shell
 * @since 4.0
 */

abstract class AbstractSubmit extends AbstractFormComponent implements IDynamicInvoker
{   
    /**
     * Determine if this submit component was clicked.
     * 
     * @param cycle
     * @param name
     * @return true if this submit was clicked
     */
    protected abstract boolean isClicked(IRequestCycle cycle, String name);

    /**
     * @see org.apache.tapestry.form.AbstractFormComponent#rewindFormComponent(org.apache.tapestry.IMarkupWriter, org.apache.tapestry.IRequestCycle)
     */
    protected void rewindFormComponent(IMarkupWriter writer, IRequestCycle cycle)
    {
        if (isClicked(cycle, getName()))
            handleClick(cycle, getForm());
    }

    void handleClick(final IRequestCycle cycle, IForm form)
    {
        if (isParameterBound("selected"))
            setSelected(getTag());

        final IActionListener listener = getListener();
        final IActionListener action = getAction();

        if (listener == null && action == null)
            return;

        final ListenerInvoker listenerInvoker = getListenerInvoker();

        Object parameters = getParameters();
        if (parameters != null)
        {
            if (parameters instanceof Collection)
            {
                cycle.setListenerParameters(((Collection) parameters).toArray());
            }
            else
            {
                cycle.setListenerParameters(new Object[] { parameters });
            }
        }
        
        // Invoke 'listener' now, but defer 'action' for later
        if (listener != null)
            listenerInvoker.invokeListener(listener, AbstractSubmit.this, cycle);
        
        if (action != null) {
            Runnable notify = new Runnable()
            {
                public void run()
                {
                    listenerInvoker.invokeListener(action, AbstractSubmit.this, cycle);
                }
            };

            form.addDeferredRunnable(notify);
        }
    }

    /**
     * Manages rendering of important submit client side bindings, like invoking the right submit
     * type or any of the optional {@link IDynamicInvoker} parameters.
     * 
     * @param writer The writer to use to write content.
     * @param cycle The current request cycle.
     */
    protected void renderSubmitBindings(IMarkupWriter writer, IRequestCycle cycle)
    {
        if (isDisabled())
            return;
        
        String type = getSubmitType();
        
        Defense.notNull(type, "submitType");
        
        Map parms = null;
        JSONObject json = null;
        List update = getUpdateComponents();
        
        if (isAsync() || (update != null && update.size() > 0)) {
            
            IForm form = getForm();
            
            parms = new HashMap();
            parms.put("submit", this);
            parms.put("key", ScriptUtils.functionHash(type + this.hashCode()));
            
            json = new JSONObject();
            
            json.put("async", Boolean.TRUE);
            json.put("json", isJson());
            
            DirectServiceParameter dsp = new DirectServiceParameter(form, null, this);
            
            json.put("url", getDirectService().getLink(true, dsp).getURL());
        }
        
        if (!type.equals(FormConstants.SUBMIT_NORMAL)) {
            if (!isParameterBound("onClick") && !isParameterBound("onclick")
                && (!isAsync() && (update == null || update.size() == 0))) {
                
                StringBuffer str = new StringBuffer();
                
                str.append("tapestry.form.").append(type);
                str.append("('").append(getForm().getClientId()).append("',");
                str.append("'").append(getName()).append("'");
                
                if (json != null){
                    str.append(",").append(json.toString());
                }
                
                str.append(")");
                
                writer.attribute("onClick", str.toString());
                return;
            } else {
                if (parms == null) {
                    parms = new HashMap();
                    
                    parms.put("submit", this);
                    parms.put("key", ScriptUtils.functionHash(type + this.hashCode()));
                }
                
                parms.put("type", type);
            }
        }
        
        if (parms != null) {
            
            if (json != null) {
                parms.put("parms", json.toString());
            }
            
            PageRenderSupport prs = TapestryUtils.getPageRenderSupport(cycle, this);
            getSubmitScript().execute(this, cycle, prs, parms);

            setSubmitBindingBound(true);
        }
    }

    /**
     * Used internall to track whether or not an async submit binding was rendered
     * as a result of calling {@link #renderSubmitBindings(org.apache.tapestry.IMarkupWriter, org.apache.tapestry.IRequestCycle)}.
     *
     * <p>
     * Currently this is used to track javascript contributions between the base and subclasses so that
     * duplicate client side bindings aren't created - such as the case with {@link LinkSubmit} where
     * client side javascript is always bound to the click of the link - with only the XHR behaviour
     * changing depending on the configuration of the component.
     * </p>
     *
     * @return True if submit bindings have been configured for this component instance, false otherwise.
     */
    public abstract boolean isSubmitBindingBound();
    public abstract void setSubmitBindingBound(boolean value);

    /** parameter. */
    public abstract IActionListener getListener();
    
    /** parameter. */
    public abstract IActionListener getAction();

    /** parameter. */
    public abstract Object getTag();

    /** parameter. */
    public abstract void setSelected(Object tag);

    /** parameter. */
    public abstract boolean getDefer();

    /** parameter. */
    public abstract Object getParameters();

    /** The type of submission, normal/cancel/refresh. */
    public abstract String getSubmitType();
    
    /**
     * {@inheritDoc}
     */
    public abstract List getUpdateComponents();
    
    /**
     * {@inheritDoc}
     */
    public abstract boolean isAsync();
    
    /**
     * {@inheritDoc}
     */
    public abstract boolean isJson();



    /** Injected. */
    public abstract IEngineService getDirectService();
    
    /** Injected. */
    public abstract ListenerInvoker getListenerInvoker();
    
    /** Injected. */
    public abstract IScript getSubmitScript();
}
