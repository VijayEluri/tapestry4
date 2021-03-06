// Copyright Jun 2, 2006 The Apache Software Foundation
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
package org.apache.tapestry.internal.event;


/**
 * Provides a mapping for listener methods that are bound to events, used
 * internally by {@link ComponentEventProperty}.
 */
public class EventBoundListener implements Cloneable
{
    // the method name to invoke
    private String _methodName;
    // if not null the form to submit before invoking listener
    private String _formId;
    // if _formId set whether or not to validate form when submitted
    private boolean _validateForm;
    // The targeted component to listen to events on
    private String _componentId;
    // The id path of the component that the listener method is connected to
    private String _componentIdPath;
    
    // If targeting a form, whether or not to submit it asynchronously
    private boolean _async;
    // Whether or not to focus the form
    private boolean _focus;
    // If this is an autoSubmit form bound event, ie we need to discover the formId dynamically
    private boolean _autoSubmit;
    
    /**
     * Creates a new listener binding. 
     * @param methodName
     *          The method to invoke.
     */
    public EventBoundListener(String methodName, String componentId)
    {
        this(methodName, componentId, true);
    }

    /**
     * Creates a new listener binding.
     * @param methodName
     *          The method to invoke.
     */
    public EventBoundListener(String methodName, String componentId, boolean autoSubmit)
    {
        this(methodName, null, false, componentId, true, false, autoSubmit);
    }

    /**
     * Creates a new listener binding. 
     * @param methodName
     *          The method to invoke.
     * @param formId
     *          If not null the form to submit before invoking listener
     * @param validateForm
     *          If formId is set, whether or not to validate form when submitting.
     */
    public EventBoundListener(String methodName, String formId, 
            boolean validateForm, String componentId, boolean async, boolean focus, boolean autoSubmit)
    {
        _methodName = methodName;
        _formId = formId;
        _validateForm = validateForm;
        _componentId = componentId;
        _async = async;
        _focus = focus;
        _autoSubmit = autoSubmit;
    }

    /**
     * Creates a new listener binding.
     * @param methodName
     *          The method to invoke.
     * @param formId
     *          If not null the form to submit before invoking listener
     * @param validateForm
     *          If formId is set, whether or not to validate form when submitting.
     */
    public EventBoundListener(String methodName, String formId,
            boolean validateForm, String componentId, boolean async, boolean focus)
    {
        this(methodName, formId, validateForm, componentId, async, focus, true);
    }
    
    /**
     * @return the formId
     */
    public String getFormId()
    {
        return _formId;
    }

    public void setFormId(String id)
    {
        _formId = id;
    }

    /**
     * @return the methodName
     */
    public String getMethodName()
    {
        return _methodName;
    }
    
    /**
     * @return the componentId
     */
    public String getComponentId()
    {
        return _componentId;
    }

    public void setComponentId(String id)
    {
        _componentId = id;
    }

    public void setComponentIdPath(String idPath)
    {
        _componentIdPath = idPath;
    }

    /**
     * @return the validateForm
     */
    public boolean isValidateForm()
    {
        return _validateForm;
    }
    
    /**
     * Whether or not listener should submit form
     * asynchronously.
     * 
     * @return True if listener is asynchronous.
     */
    public boolean isAsync()
    {
        return _async;
    }
    
    public boolean shouldFocusForm()
    {
        return _focus;
    }

    public boolean isAutoSubmit()
    {
        return _autoSubmit;
    }

    public String getComponentIdPath()
    {
        return _componentIdPath;
    }

    public Object clone()
    throws CloneNotSupportedException
    {
        return super.clone();
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_componentId == null) ? 0 : _componentId.hashCode());
        result = prime * result + ((_methodName == null) ? 0 : _methodName.hashCode());
        result = prime * result + ((_formId == null) ? 0 : _formId.hashCode());
        return result;
    }
    
    /** 
     * {@inheritDoc}
     */
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final EventBoundListener other = (EventBoundListener) obj;
        if (_componentId == null) {
            if (other._componentId != null) return false;
        } else if (!_componentId.equals(other._componentId)) return false;
        if (_methodName == null) {
            if (other._methodName != null) return false;
        } else if (!_methodName.equals(other._methodName)) return false;
        return true;
    }
}
