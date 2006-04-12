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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import net.sf.tapestry.DefaultResourceResolver;
import net.sf.tapestry.IComponentStringsSource;
import net.sf.tapestry.IPage;
import net.sf.tapestry.IResourceLocation;
import net.sf.tapestry.IResourceResolver;
import net.sf.tapestry.Tapestry;
import net.sf.tapestry.engine.DefaultStringsSource;
import net.sf.tapestry.parse.SpecificationParser;
import net.sf.tapestry.resource.ClasspathResourceLocation;
import net.sf.tapestry.spec.ComponentSpecification;
import net.sf.tapestry.spec.IApplicationSpecification;
import net.sf.tapestry.spec.ILibrarySpecification;
import net.sf.tapestry.util.IPropertyHolder;

/**
 *  Base class for Tapestry test cases.
 *
 *  @author Howard Lewis Ship
 *  @version $Id$
 *  @since 2.2
 * 
 **/

public class TapestryTestCase extends TestCase
{
    protected static final boolean IS_JDK13 =
        System.getProperty("java.specification.version").equals("1.3");

    private IResourceResolver _resolver = new DefaultResourceResolver();

    public TapestryTestCase(String name)
    {
        super(name);
    }

    protected IPage createPage(String specificationPath, Locale locale)
    {
        IResourceLocation location = new ClasspathResourceLocation(_resolver, specificationPath);

        IComponentStringsSource source = new DefaultStringsSource();
        MockEngine engine = new MockEngine();
        engine.setComponentStringsSource(source);

        MockPage result = new MockPage();
        result.setEngine(engine);
        result.setLocale(locale);

        ComponentSpecification spec = new ComponentSpecification();
        spec.setSpecificationLocation(location);
        result.setSpecification(spec);

        return result;
    }

    protected ComponentSpecification parseComponent(String simpleName) throws Exception
    {
        SpecificationParser parser = new SpecificationParser();

        IResourceLocation location = getSpecificationResourceLocation(simpleName);

        return parser.parseComponentSpecification(location);
    }

    protected ComponentSpecification parsePage(String simpleName) throws Exception
    {
        SpecificationParser parser = new SpecificationParser();

        IResourceLocation location = getSpecificationResourceLocation(simpleName);

        return parser.parsePageSpecification(location);
    }

    protected IApplicationSpecification parseApp(String simpleName) throws Exception
    {
        SpecificationParser parser = new SpecificationParser();

        IResourceLocation location = getSpecificationResourceLocation(simpleName);

        return parser.parseApplicationSpecification(location, _resolver);
    }

    protected IResourceLocation getSpecificationResourceLocation(String simpleName)
    {
        String adjustedClassName = "/" + getClass().getName().replace('.', '/') + ".class";

        IResourceLocation classResourceLocation =
            new ClasspathResourceLocation(_resolver, adjustedClassName);

        IResourceLocation appSpecLocation = classResourceLocation.getRelativeLocation(simpleName);
        return appSpecLocation;
    }

    protected ILibrarySpecification parseLib(String simpleName) throws Exception
    {
        SpecificationParser parser = new SpecificationParser();

        IResourceLocation location = getSpecificationResourceLocation(simpleName);

        return parser.parseLibrarySpecification(location, _resolver);
    }

    protected void checkList(String propertyName, String[] expected, String[] actual)
    {
        checkList(propertyName, expected, Arrays.asList(actual));
    }

    protected void checkList(String propertyName, String[] expected, List actual)
    {
        int count = Tapestry.size(actual);

        assertEquals(propertyName + " element count", expected.length, count);

        for (int i = 0; i < count; i++)
        {
            assertEquals("propertyName[" + i + "]", expected[i], actual.get(i));
        }
    }

    protected void checkProperty(IPropertyHolder h, String propertyName, String expectedValue)
    {
        assertEquals("Property " + propertyName + ".", expectedValue, h.getProperty(propertyName));
    }

    protected void checkException(Throwable ex, String string)
    {
        if (ex.getMessage().indexOf(string) >= 0)
            return;

        throw new AssertionFailedError(
            "Exception " + ex + " does not contain sub-string '" + string + "'.");
    }

    protected void unreachable()
    {
        throw new AssertionFailedError("This code should be unreachable.");
    }
}