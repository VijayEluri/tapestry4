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
package net.sf.tapestry.junit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.AssertionFailedError;
import net.sf.tapestry.ApplicationRuntimeException;
import net.sf.tapestry.BindingException;
import net.sf.tapestry.IActionListener;
import net.sf.tapestry.IRequestCycle;
import net.sf.tapestry.RequestCycleException;
import net.sf.tapestry.listener.ListenerMap;
import ognl.Ognl;

/**
 *  Tests the {@link net.sf.tapestry.listener.ListenerMap} and
 *  {@link net.sf.tapestry.listener.ListenerMapPropertyAccessor}
 *  classes.
 * 
 *  @author Howard Lewis Ship
 *  @version $Id$
 *  @since 2.2
 * 
 **/

public class TestListenerMap extends TapestryTestCase
{
    public static class Listener
    {
        public int invokeCount = 0;

        public void nonListenerMethod()
        {
        }

        public String almostListenerMethod(IRequestCycle cycle)
        {
            return null;
        }

        public void notQuiteListenerMethod(IRequestCycle cycle) throws BindingException
        {
        }

        public void actualListenerMethod(IRequestCycle cycle)
        {
            invokeCount++;
        }

        public void listenerThrows(IRequestCycle cycle) throws RequestCycleException
        {
            invokeCount++;
        }

        public static void nearMiss(IRequestCycle cycle)
        {
        }

        static void mustBePublic(IRequestCycle cycle)
        {
        }

        public void tooManyExceptionsThrown(IRequestCycle cycle) throws RequestCycleException, BindingException
        {
        }

        public void invokeAndThrow(IRequestCycle cycle) throws RequestCycleException
        {
            throw new RequestCycleException("From invokeAndThrow");
        }

        public void invokeAndThrowRuntime(IRequestCycle cycle)
        {
            throw new RuntimeException("From invokeAndThrowRuntime");
        }

        public String toString()
        {
            return "TestListenerMap.Listener[" + invokeCount + "]";
        }
    }

    public static class ListenerSubclass extends Listener
    {
        public void subclassMethod(IRequestCycle cycle)
        {
            invokeCount++;
        }
    }

    public TestListenerMap(String name)
    {
        super(name);
    }

    private void attempt(String methodName, Listener listener, ListenerMap map)
    {
        int count = listener.invokeCount;

        IActionListener l = (IActionListener) map.getListener(methodName);

        assertNotNull(l);

        try
        {
            l.actionTriggered(null, null);
        }
        catch (RequestCycleException ex)
        {
            throw new AssertionFailedError("Unexpected RequestCycleException.");
        }

        assertEquals("Invoke count.", count + 1, listener.invokeCount);
    }

    public void testListenerMethods()
    {
        Listener l = new Listener();
        ListenerMap m = new ListenerMap(l);

        attempt("actualListenerMethod", l, m);
        attempt("listenerThrows", l, m);
    }

    public void testListenerNames()
    {
        Listener l = new Listener();
        ListenerMap m = new ListenerMap(l);

        List names = new ArrayList(m.getListenerNames());
        Collections.sort(names);

        checkList(
            "Method names.",
            new String[] { "actualListenerMethod", "invokeAndThrow", "invokeAndThrowRuntime", "listenerThrows" },
            names);
    }

    public void testSubclassMethods()
    {
        Listener l = new ListenerSubclass();
        ListenerMap m = new ListenerMap(l);

        attempt("actualListenerMethod", l, m);
        attempt("listenerThrows", l, m);
        attempt("subclassMethod", l, m);
    }

    public void testSubclassListenerNames()
    {
        Listener l = new ListenerSubclass();
        ListenerMap m = new ListenerMap(l);

        List names = new ArrayList(m.getListenerNames());
        Collections.sort(names);

        checkList(
            "Method names.",
            new String[] {
                "actualListenerMethod",
                "invokeAndThrow",
                "invokeAndThrowRuntime",
                "listenerThrows",
                "subclassMethod" },
            names);
    }

    public void testListenerMethodPropertyAccess() throws Exception
    {
        Listener l = new ListenerSubclass();
        ListenerMap m = new ListenerMap(l);

        IActionListener al = (IActionListener) Ognl.getValue("subclassMethod", m);

        int count = l.invokeCount;

        al.actionTriggered(null, null);

        assertEquals("Invocation count.", count + 1, l.invokeCount);
    }

    public void testPropertyAccess() throws Exception
    {
        Listener l = new ListenerSubclass();
        ListenerMap m = new ListenerMap(l);

        // class is a handy, read-only property.

        Class c = (Class) Ognl.getValue("class", m);

        assertEquals("ListenerMap class property.", ListenerMap.class, c);
    }

    public void testInvokeAndThrow() throws Exception
    {
        Listener l = new ListenerSubclass();
        ListenerMap m = new ListenerMap(l);
        IActionListener listener = (IActionListener) m.getListener("invokeAndThrow");

        try
        {
            listener.actionTriggered(null, null);

            throw new AssertionFailedError("Unreachable.");
        }
        catch (RequestCycleException ex)
        {
            checkException(ex, "From invokeAndThrow");
        }
    }

    public void testInvokeAndThrowRuntime() throws Exception
    {
        Listener l = new ListenerSubclass();
        ListenerMap m = new ListenerMap(l);
        IActionListener listener = (IActionListener) m.getListener("invokeAndThrowRuntime");

        try
        {
            listener.actionTriggered(null, null);

            throw new AssertionFailedError("Unreachable.");
        }
        catch (ApplicationRuntimeException ex)
        {
            checkException(
                ex,
                "Unable to invoke method invokeAndThrowRuntime on TestListenerMap.Listener[0]: From invokeAndThrowRuntime");
        }
    }

    public void testToString() throws Exception
    {
        Listener l = new Listener();
        ListenerMap m = new ListenerMap(l);

        IActionListener listener = (IActionListener) m.getListener("actualListenerMethod");

        assertEquals(
            "ToString",
            "SyntheticListener[TestListenerMap.Listener[0] public void net.sf.tapestry.junit.TestListenerMap$Listener.actualListenerMethod(net.sf.tapestry.IRequestCycle)]",
            listener.toString());

        assertEquals("ToString", "ListenerMap[TestListenerMap.Listener[0]]", m.toString());
    }

    public void testIsCached() throws Exception
    {
        Listener l = new Listener();
        ListenerMap m = new ListenerMap(l);

        IActionListener listener = (IActionListener) m.getListener("actualListenerMethod");

        assertSame("Listener", listener, m.getListener("actualListenerMethod"));
    }

    public void testInvalidMethod() throws Exception
    {
        Listener l = new Listener();
        ListenerMap m = new ListenerMap(l);

        try
        {
            m.getListener("notQuiteListenerMethod");

            throw new AssertionFailedError("Unreachable.");
        }
        catch (ApplicationRuntimeException ex)
        {
            checkException(
                ex,
                "Object TestListenerMap.Listener[0] does not implement a listener method named 'notQuiteListenerMethod'.");
        }
    }

}