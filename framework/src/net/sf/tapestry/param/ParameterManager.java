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
package net.sf.tapestry.param;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.tapestry.BindingException;
import net.sf.tapestry.IBinding;
import net.sf.tapestry.IComponent;
import net.sf.tapestry.IRequestCycle;
import net.sf.tapestry.IResourceResolver;
import net.sf.tapestry.RequiredParameterException;
import net.sf.tapestry.Tapestry;
import net.sf.tapestry.spec.ComponentSpecification;
import net.sf.tapestry.spec.Direction;
import net.sf.tapestry.spec.ParameterSpecification;
import net.sf.tapestry.util.prop.PropertyFinder;
import net.sf.tapestry.util.prop.PropertyInfo;

/**
 *  Manages a set of {@link IParameterConnector}s for a
 *  {@link IComponent}.
 *
 *  @author Howard Lewis Ship
 *  @version $Id$
 *  @since 2.0.3
 *
 **/

public class ParameterManager
{
    private static final Log LOG = LogFactory.getLog(ParameterManager.class);

    private static final Map scalarTypeMap = new HashMap();

    static {
        scalarTypeMap.put("boolean", Boolean.TYPE);
        scalarTypeMap.put("short", Short.TYPE);
        scalarTypeMap.put("int", Integer.TYPE);
        scalarTypeMap.put("long", Long.TYPE);
        scalarTypeMap.put("float", Float.class);
        scalarTypeMap.put("double", Double.class);
    }

    private IComponent _component;
    private IParameterConnector[] _connectors;

    public ParameterManager(IComponent component)
    {
        _component = component;
    }

    /**
     *  Invoked just before a component renders.  Converts bindings to values
     *  that are assigned to connected properties.
     * 
     **/
    
    public void setParameters(IRequestCycle cycle) throws RequiredParameterException
    {
        if (_connectors == null)
            setup(cycle);

        for (int i = 0; i < _connectors.length; i++)
            _connectors[i].setParameter(cycle);
    }

    /**
     *  Invoked just after the component renders.  Returns component properties
     *  back to initial values (unless the corresponding binding is
     *  {@link IBinding#isInvariant() invariant}).  In addition, for
     *  {@link Direction#FORM} parameters, the property is read and the binding
     *  is set from the property value (if the cycle is rewinding and the current
     *  form is rewinding).
     * 
     **/
    
    public void resetParameters(IRequestCycle cycle)
    {
        if (_connectors == null)
            return;

        for (int i = 0; i < _connectors.length; i++)
            _connectors[i].resetParameter(cycle);
    }

    private void setup(IRequestCycle cycle) throws RequiredParameterException
    {
        boolean debug = LOG.isDebugEnabled();

        if (debug)
            LOG.debug(_component + ": connecting parameters and properties");

        List list = new ArrayList();
        ComponentSpecification spec = _component.getSpecification();
        IResourceResolver resolver = _component.getPage().getEngine().getResourceResolver();

        IParameterConnector disabledConnector = null;

        Collection names = spec.getParameterNames();
        Iterator i = names.iterator();
        while (i.hasNext())
        {
            String name = (String) i.next();

            if (debug)
                LOG.debug("Connecting parameter " + name + ".");

            IBinding binding = _component.getBinding(name);
            if (binding == null)
            {
                if (debug)
                    LOG.debug("Not bound.");

                continue;
            }

            ParameterSpecification pspec = spec.getParameter(name);

            if (pspec.getDirection() == Direction.CUSTOM)
            {
                if (debug)
                    LOG.debug("Parameter is " + pspec.getDirection().getName() + ".");

                continue;
            }

            String propertyName = pspec.getPropertyName();

            if (debug && !name.equals(propertyName))
                LOG.debug("Connecting to property " + propertyName + ".");

            // Next,verify that there is a writable property with the same
            // name as the parameter.

            PropertyInfo propertyInfo = PropertyFinder.getPropertyInfo(_component.getClass(), propertyName);

            if (propertyInfo == null)
            {
                throw new ConnectedParameterException(
                    Tapestry.getString("ParameterManager.no-accessor", _component.getExtendedId(), propertyName),
                    _component,
                    name,
                    propertyName);
            }

            if (!propertyInfo.isReadWrite())
            {
                throw new ConnectedParameterException(
                    Tapestry.getString(
                        "ParameterManager.property-not-read-write",
                        _component.getExtendedId(),
                        propertyName),
                    _component,
                    name,
                    propertyName);
            }

            // Check if the parameter type matches the property type

            Class propertyType = propertyInfo.getType();
            Class parameterType = getType(pspec.getType(), resolver);

            if (parameterType == null)
            {
                throw new ConnectedParameterException(
                    Tapestry.getString("ParameterManager.java-type-not-specified", name, _component.getExtendedId()),
                    _component,
                    name,
                    propertyName);
            }

            if (!propertyType.equals(parameterType))
            {
                throw new ConnectedParameterException(
                    Tapestry.getString(
                        "ParameterManager.type-mismatch",
                        new String[] {
                            name,
                            _component.getExtendedId(),
                            parameterType.toString(),
                            propertyType.toString()}),
                    _component,
                    name,
                    propertyName);
            }

            // Here's where we will sniff it for type, for the moment
            // assume its some form of object (not scalar) type.

            IParameterConnector connector = createConnector(_component, name, binding, propertyType, parameterType);

            // Static bindings are set here and then forgotten
            // about.  Dynamic bindings are kept for later.

            if (binding.isInvariant())
            {
                if (debug)
                    LOG.debug("Setting invariant value using " + connector + ".");

                try
                {
                    connector.setParameter(cycle);
                }
                catch (BindingException ex)
                {
                    throw new ConnectedParameterException(
                        Tapestry.getString(
                            "ParameterManager.static-initialization-failure",
                            propertyName,
                            _component.getExtendedId(),
                            binding.toString()),
                        _component,
                        name,
                        propertyName,
                        ex);
                }
                
                continue;
            }
            
            
                if (debug)
                    LOG.debug("Adding " + connector + ".");

// To properly support forms elements, the disabled parameter
// must always be processed last.

if (name.equals("disabled"))
    disabledConnector = connector;
    else
                list.add(connector);
  
        }

            if (disabledConnector != null)
                list.add(disabledConnector);

        // Convert for List to array

        _connectors = (IParameterConnector[]) list.toArray(new IParameterConnector[list.size()]);

    }

    private IParameterConnector createConnector(
        IComponent component,
        String parameterName,
        IBinding binding,
        Class propertyType,
        Class requiredType)
    {
        // Could convert this code to use a Decorator, but then I'd need
        // some kind of factory for these parameter connectors.

        if (propertyType.equals(Boolean.TYPE))
            return new BooleanParameterConnector(component, parameterName, binding);

        if (propertyType.equals(Integer.TYPE))
            return new IntParameterConnector(component, parameterName, binding);

        if (propertyType.equals(Double.TYPE))
            return new DoubleParameterConnector(component, parameterName, binding);

        if (propertyType.equals(String.class))
            return new StringParameterConnector(component, parameterName, binding);

        // The default is for any kind of object type

        return new ObjectParameterConnector(component, parameterName, binding, requiredType);
    }

    private Class getType(String name, IResourceResolver resolver)
    {
        if (Tapestry.isNull(name))
            return null;

        Class result = (Class) scalarTypeMap.get(name);

        if (result != null)
            return result;

        return resolver.findClass(name);
    }
}