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
package net.sf.tapestry.parse;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.tapestry.INamespace;
import net.sf.tapestry.IResourceLocation;
import net.sf.tapestry.IResourceResolver;
import net.sf.tapestry.ITemplateSource;
import net.sf.tapestry.Tapestry;
import net.sf.tapestry.bean.IBeanInitializer;
import net.sf.tapestry.spec.AssetSpecification;
import net.sf.tapestry.spec.AssetType;
import net.sf.tapestry.spec.BeanLifecycle;
import net.sf.tapestry.spec.BeanSpecification;
import net.sf.tapestry.spec.BindingSpecification;
import net.sf.tapestry.spec.BindingType;
import net.sf.tapestry.spec.ComponentSpecification;
import net.sf.tapestry.spec.ContainedComponent;
import net.sf.tapestry.spec.Direction;
import net.sf.tapestry.spec.ExtensionSpecification;
import net.sf.tapestry.spec.IApplicationSpecification;
import net.sf.tapestry.spec.ILibrarySpecification;
import net.sf.tapestry.spec.ListenerBindingSpecification;
import net.sf.tapestry.spec.ParameterSpecification;
import net.sf.tapestry.spec.PropertySpecification;
import net.sf.tapestry.spec.SpecFactory;
import net.sf.tapestry.util.IPropertyHolder;
import net.sf.tapestry.util.xml.AbstractDocumentParser;
import net.sf.tapestry.util.xml.DocumentParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *  Used to parse an application or component specification into a
 *  {@link ApplicationSpecification} or {@link ComponentSpecification}.
 *  This may someday be revised to use
 *  Java XML Binding (once JAXB is available), or
 *  to use Jakarta's Digester.
 *
 *
 *  <table border=1
 *	<tr>
 *	  <th>Version</th> <th>PUBLIC ID</th> <th>SYSTEM ID</th> <th>Description</th>
 *  </tr>
 *
 * 
 *  <tr valign="top">
 *  <td>1.3</td>
 *  <td><code>-//Howard Lewis Ship//Tapestry Specification 1.3//EN</code></td>
 * <td><code>http://tapestry.sf.net/dtd/Tapestry_1_3.dtd</code></td>
 *  <td>
 *  Version of specification introduced in release 2.2.
 * </td>
 * </tr>
 *
 *  <tr valign="top">
 *  <td>1.4</td>
 *  <td><code>-//Howard Lewis Ship//Tapestry Specification 1.3//EN</code></td>
 * <td><code>http://tapestry.sf.net/dtd/Tapestry_1_3.dtd</code></td>
 *  <td>
 *  Version of specification introduced in release 2.4.
 * </td>
 * </tr>
 * 
 * 
 *  </table>
 *
 *  @version $Id$
 *  @author Howard Lewis Ship
 * 
 **/

public class SpecificationParser extends AbstractDocumentParser
{
    /** @since 2.2 **/

    public static final String TAPESTRY_DTD_1_3_PUBLIC_ID =
        "-//Howard Lewis Ship//Tapestry Specification 1.3//EN";

    /** @since 2.2 **/

    public static final String TAPESTRY_DTD_1_4_PUBLIC_ID =
        "-//Apache Software Foundation//Tapestry Specification 1.4//EN";

    /**
     *  Like modified property name, but allows periods in the name as
     *  well.
     * 
     *  @since 2.2
     * 
     **/

    public static final String EXTENDED_PROPERTY_NAME_PATTERN = "^_?[a-zA-Z](\\w|-|\\.)*$";

    /**
     *  Perl5 pattern that parameter names must conform to.  
     *  Letter, followed by letter, number or underscore.
     * 
     *  @since 2.2
     * 
     **/

    public static final String PARAMETER_NAME_PATTERN =
        AbstractDocumentParser.SIMPLE_PROPERTY_NAME_PATTERN;

    /**
     *  Perl5 pattern that property names (that can be connected to
     *  parameters) must conform to.  
     *  Letter, followed by letter, number or underscore.
     *  
     * 
     *  @since 2.2
     * 
     **/

    public static final String PROPERTY_NAME_PATTERN =
        AbstractDocumentParser.SIMPLE_PROPERTY_NAME_PATTERN;

    /**
     *  Perl5 pattern for page names.  Letter
     *  followed by letter, number, dash, underscore or period.
     * 
     *  @since 2.2
     * 
     **/

    public static final String PAGE_NAME_PATTERN = EXTENDED_PROPERTY_NAME_PATTERN;

    /**
     *  Perl5 pattern for component alias. 
     *  Letter, followed by letter, number, or underscore.
     * 
     *  @since 2.2
     * 
     **/

    public static final String COMPONENT_ALIAS_PATTERN =
        AbstractDocumentParser.SIMPLE_PROPERTY_NAME_PATTERN;

    /**
     *  Perl5 pattern for helper bean names.  
     *  Letter, followed by letter, number or underscore.
     * 
     *  @since 2.2
     * 
     **/

    public static final String BEAN_NAME_PATTERN =
        AbstractDocumentParser.SIMPLE_PROPERTY_NAME_PATTERN;

    /**
     *  Perl5 pattern for component ids.  Letter, followed by
     *  letter, number or underscore.
     * 
     *  @since 2.2
     * 
     **/

    public static final String COMPONENT_ID_PATTERN =
        AbstractDocumentParser.SIMPLE_PROPERTY_NAME_PATTERN;

    /**
     *  Perl5 pattern for asset names.  Letter, followed by
     *  letter, number or underscore.
     * 
     *  @since 2.2
     * 
     **/

    public static final String ASSET_NAME_PATTERN =
        AbstractDocumentParser.SIMPLE_PROPERTY_NAME_PATTERN;

    /**
     *  Perl5 pattern for service names.  Letter
     *  followed by letter, number, dash, underscore or period.
     * 
     *  @since 2.2
     * 
     **/

    public static final String SERVICE_NAME_PATTERN = EXTENDED_PROPERTY_NAME_PATTERN;

    /**
     *  Perl5 pattern for library ids.  Letter followed
     *  by letter, number or underscore.
     * 
     *  @since 2.2
     * 
     **/

    public static final String LIBRARY_ID_PATTERN =
        AbstractDocumentParser.SIMPLE_PROPERTY_NAME_PATTERN;

    /**
     *  Per5 pattern for extension names.  Letter followed
     *  by letter, number, dash, period or underscore. 
     * 
     *  @since 2.2
     * 
     **/

    public static final String EXTENSION_NAME_PATTERN = EXTENDED_PROPERTY_NAME_PATTERN;

    /**
     *  Perl5 pattern for component types.  Component types are an optional
     *  namespace prefix followed by a normal identifier.
     * 
     *  @since 2.2
     **/

    public static final String COMPONENT_TYPE_PATTERN = "^(_?[a-zA-Z]\\w*:)?[a-zA-Z_](\\w)*$";

    /**
     *  We can share a single map for all the XML attribute to object conversions,
     *  since the keys are unique.
     * 
     **/

    private static final Map _conversionMap = new HashMap();

    /** @since 1.0.9 **/

    private SpecFactory _factory;

    private interface IConverter
    {
        public Object convert(String value) throws DocumentParseException;
    }

    private static class BooleanConverter implements IConverter
    
    {
        public Object convert(String value) throws DocumentParseException
        {
            Object result = _conversionMap.get(value.toLowerCase());

            if (result == null || !(result instanceof Boolean))
                throw new DocumentParseException(
                    Tapestry.getString("SpecificationParser.fail-convert-boolean", value));

            return result;
        }
    }

    private static class IntConverter implements IConverter
    {
        public Object convert(String value) throws DocumentParseException
        {
            try
            {
                return new Integer(value);
            }
            catch (NumberFormatException ex)
            {
                throw new DocumentParseException(
                    Tapestry.getString("SpecificationParser.fail-convert-int", value),
                    ex);
            }
        }
    }

    private static class LongConverter implements IConverter
    {
        public Object convert(String value) throws DocumentParseException
        {
            try
            {
                return new Long(value);
            }
            catch (NumberFormatException ex)
            {
                throw new DocumentParseException(
                    Tapestry.getString("SpecificationParser.fail-convert-long", value),
                    ex);
            }
        }
    }

    private static class DoubleConverter implements IConverter
    {
        public Object convert(String value) throws DocumentParseException
        {
            try
            {
                return new Double(value);
            }
            catch (NumberFormatException ex)
            {
                throw new DocumentParseException(
                    Tapestry.getString("SpecificationParser.fail-convert-double", value),
                    ex);
            }
        }
    }

    private static class StringConverter implements IConverter
    {
        public Object convert(String value)
        {
            return value.trim();
        }
    }

    // Identify all the different acceptible values.
    // We continue to sneak by with a single map because
    // there aren't conflicts;  when we have 'foo' meaning
    // different things in different places in the DTD, we'll
    // need multiple maps.

    static {

        _conversionMap.put("true", Boolean.TRUE);
        _conversionMap.put("t", Boolean.TRUE);
        _conversionMap.put("1", Boolean.TRUE);
        _conversionMap.put("y", Boolean.TRUE);
        _conversionMap.put("yes", Boolean.TRUE);
        _conversionMap.put("on", Boolean.TRUE);

        _conversionMap.put("false", Boolean.FALSE);
        _conversionMap.put("f", Boolean.FALSE);
        _conversionMap.put("0", Boolean.FALSE);
        _conversionMap.put("off", Boolean.FALSE);
        _conversionMap.put("no", Boolean.FALSE);
        _conversionMap.put("n", Boolean.FALSE);

        _conversionMap.put("none", BeanLifecycle.NONE);
        _conversionMap.put("request", BeanLifecycle.REQUEST);
        _conversionMap.put("page", BeanLifecycle.PAGE);
        _conversionMap.put("render", BeanLifecycle.RENDER);

        _conversionMap.put("boolean", new BooleanConverter());
        _conversionMap.put("int", new IntConverter());
        _conversionMap.put("double", new DoubleConverter());
        _conversionMap.put("String", new StringConverter());
        _conversionMap.put("long", new LongConverter());

        _conversionMap.put("in", Direction.IN);
        _conversionMap.put("form", Direction.FORM);
        _conversionMap.put("custom", Direction.CUSTOM);
    }

    public SpecificationParser()
    {
        register(TAPESTRY_DTD_1_3_PUBLIC_ID, "Tapestry_1_3.dtd");
        register(TAPESTRY_DTD_1_4_PUBLIC_ID, "Tapestry_1_4.dtd");

        _factory = new SpecFactory();
    }

    /**
     *  Parses an input stream containing a page or component specification and assembles
     *  a {@link ComponentSpecification} from it.  
     *
     *  @throws DocumentParseException if the input stream cannot be fully
     *  parsed or contains invalid data.
     *
     **/

    public ComponentSpecification parseComponentSpecification(IResourceLocation resourceLocation)
        throws DocumentParseException
    {
        Document document;

        try
        {
            document = parse(resourceLocation, "component-specification");

            return convertComponentSpecification(document, false);
        }
        finally
        {
            setResourceLocation(null);
        }
    }

    /**
     *  Parses an input stream containing a page specification and assembles
     *  a {@link ComponentSpecification} from it.  
     *
     *  @throws DocumentParseException if the input stream cannot be fully
     *  parsed or contains invalid data.
     * 
     *  @since 2.2
     *
     **/

    public ComponentSpecification parsePageSpecification(IResourceLocation resourceLocation)
        throws DocumentParseException
    {
        try
        {
            Document document = parse(resourceLocation, "page-specification");

            ComponentSpecification result = convertComponentSpecification(document, true);

            result.setPageSpecification(true);

            // Set defaults

            result.setAllowBody(true);
            result.setAllowInformalParameters(false);

            return result;
        }
        finally
        {
            setResourceLocation(null);
        }
    }

    /**
     *  Parses an resource containing an application specification and assembles
     *  an {@link ApplicationSpecification} from it.
     *
     *  @throws DocumentParseException if the input stream cannot be fully
     *  parsed or contains invalid data.
     *
     **/

    public IApplicationSpecification parseApplicationSpecification(
        IResourceLocation resourceLocation,
        IResourceResolver resolver)
        throws DocumentParseException
    {
        try
        {
            Document document = parse(resourceLocation, "application");

            return convertApplicationSpecification(document, resolver);
        }
        finally
        {
            setResourceLocation(null);
        }
    }

    /**
     *  Parses an input stream containing a library specification and assembles
     *  a {@link LibrarySpecification} from it.
     *
     *  @throws DocumentParseException if the input stream cannot be fully
     *  parsed or contains invalid data.
     * 
     *  @since 2.2
     *
     **/

    public ILibrarySpecification parseLibrarySpecification(
        IResourceLocation resourceLocation,
        IResourceResolver resolver)
        throws DocumentParseException
    {
        try
        {
            Document document = parse(resourceLocation, "library-specification");

            return convertLibrarySpecification(document, resolver);
        }
        finally
        {
            setResourceLocation(null);
        }
    }

    private boolean getBooleanAttribute(Node node, String attributeName)
    {
        String attributeValue = getAttribute(node, attributeName);

        return attributeValue.equals("yes");
    }

    protected IApplicationSpecification convertApplicationSpecification(
        Document document,
        IResourceResolver resolver)
        throws DocumentParseException
    {
        IApplicationSpecification specification = _factory.createApplicationSpecification();

        Element root = document.getDocumentElement();

        specification.setName(getAttribute(root, "name"));
        specification.setEngineClassName(getAttribute(root, "engine-class"));

        processLibrarySpecification(document, specification, resolver);

        return specification;
    }

    /** @since 2.2 **/

    protected ILibrarySpecification convertLibrarySpecification(
        Document document,
        IResourceResolver resolver)
        throws DocumentParseException
    {
        ILibrarySpecification specification = _factory.createLibrarySpecification();

        processLibrarySpecification(document, specification, resolver);

        return specification;
    }

    /**
     *  Processes the embedded elements inside a LibrarySpecification.
     * 
     *  @since 2.2
     * 
     **/

    private void processLibrarySpecification(
        Document document,
        ILibrarySpecification specification,
        IResourceResolver resolver)
        throws DocumentParseException
    {
        specification.setPublicId(document.getDoctype().getPublicId());
        specification.setSpecificationLocation(getResourceLocation());
        specification.setResourceResolver(resolver);

        Element root = document.getDocumentElement();

        for (Node node = root.getFirstChild(); node != null; node = node.getNextSibling())
        {
            if (isElement(node, "page"))
            {
                convertPage(specification, node);
                continue;
            }

            // component-type is in DTD 1.4, component-alias in DTD 1.3

            if (isElement(node, "component-alias") || isElement(node, "component-type"))
            {
                convertComponentType(specification, node);
                continue;
            }

            if (isElement(node, "property"))
            {
                convertProperty(specification, node);
                continue;
            }

            if (isElement(node, "service"))
            {
                convertService(specification, node);
                continue;
            }

            if (isElement(node, "description"))
            {
                specification.setDescription(getValue(node));
                continue;
            }

            if (isElement(node, "library"))
            {
                convertLibrary(specification, node);
                continue;
            }

            if (isElement(node, "extension"))
            {
                convertExtension(specification, node);
                continue;
            }
        }

        specification.instantiateImmediateExtensions();
    }

    /**  @since 2.2 **/

    private void convertLibrary(ILibrarySpecification specification, Node node)
        throws DocumentParseException
    {
        String id = getAttribute(node, "id");

        validate(id, LIBRARY_ID_PATTERN, "SpecificationParser.invalid-library-id");

        if (id.equals(INamespace.FRAMEWORK_NAMESPACE))
            throw new DocumentParseException(
                Tapestry.getString(
                    "SpecificationParser.framework-library-id-is-reserved",
                    INamespace.FRAMEWORK_NAMESPACE),
                getResourceLocation());

        String specificationPath = getAttribute(node, "specification-path");

        specification.setLibrarySpecificationPath(id, specificationPath);
    }

    private void convertPage(ILibrarySpecification specification, Node node)
        throws DocumentParseException
    {
        String name = getAttribute(node, "name");

        validate(name, PAGE_NAME_PATTERN, "SpecificationParser.invalid-page-name");

        String specificationPath = getAttribute(node, "specification-path");

        specification.setPageSpecificationPath(name, specificationPath);
    }

    private void convertComponentType(ILibrarySpecification specification, Node node)
        throws DocumentParseException
    {
        String type = getAttribute(node, "type");

        validate(type, COMPONENT_ALIAS_PATTERN, "SpecificationParser.invalid-component-type");

        String path = getAttribute(node, "specification-path");

        specification.setComponentSpecificationPath(type, path);
    }

    private void convertProperty(IPropertyHolder holder, Node node) throws DocumentParseException
    {
        String name = getAttribute(node, "name");

        // Starting in DTD 1.4, the value may be specified
        // as an attribute.  Only if that is null do we
        // extract the node's value.

        String value = getExtendedAttribute(node, "value", true);

        holder.setProperty(name, value);
    }

    protected ComponentSpecification convertComponentSpecification(Document document, boolean isPage)
        throws DocumentParseException
    {
        ComponentSpecification specification = _factory.createComponentSpecification();
        Element root = document.getDocumentElement();

        specification.setPublicId(document.getDoctype().getPublicId());
        specification.setSpecificationLocation(getResourceLocation());

        // Only components specify these two attributes.  For pages they either can't be specified.

        if (!isPage)
        {
            specification.setAllowBody(getBooleanAttribute(root, "allow-body"));
            specification.setAllowInformalParameters(
                getBooleanAttribute(root, "allow-informal-parameters"));
        }

        specification.setComponentClassName(getAttribute(root, "class"));

        for (Node node = root.getFirstChild(); node != null; node = node.getNextSibling())
        {
            if (isElement(node, "parameter"))
            {
                convertParameter(specification, node);
                continue;
            }

            if (isElement(node, "reserved-parameter"))
            {
                if (isPage)
                    throw new DocumentParseException(
                        Tapestry.getString(
                            "SpecificationParser.not-allowed-for-page",
                            "reserved-parameter"),
                        getResourceLocation());

                convertReservedParameter(specification, node);
                continue;
            }

            if (isElement(node, "bean"))
            {
                convertBean(specification, node);
                continue;
            }

            if (isElement(node, "component"))
            {
                convertComponent(specification, node);
                continue;
            }

            if (isElement(node, "external-asset"))
            {
                convertAsset(specification, node, AssetType.EXTERNAL, "URL");
                continue;
            }

            if (isElement(node, "context-asset"))
            {
                convertAsset(specification, node, AssetType.CONTEXT, "path");
                continue;
            }

            if (isElement(node, "private-asset"))
            {
                convertAsset(specification, node, AssetType.PRIVATE, "resource-path");
                continue;
            }

            if (isElement(node, "property"))
            {
                convertProperty(specification, node);
                continue;
            }

            if (isElement(node, "description"))
            {
                specification.setDescription(getValue(node));
                continue;
            }

            if (isElement(node, "property-specification"))
            {
                convertPropertySpecification(specification, node);
                continue;
            }
        }

        return specification;
    }

    private void convertParameter(ComponentSpecification specification, Node node)
        throws DocumentParseException
    {
        ParameterSpecification param = _factory.createParameterSpecification();

        String name = getAttribute(node, "name");

        validate(name, PARAMETER_NAME_PATTERN, "SpecificationParser.invalid-parameter-name");

        String type = getAttribute(node, "type");

        // The attribute was called "java-type" in the 1.3 and earlier DTD

        if (type == null)
            type = getAttribute(node, "java-type");

        if (type == null)
            type = "java.lang.Object";

        param.setType(type);

        param.setRequired(getBooleanAttribute(node, "required"));

        String propertyName = getAttribute(node, "property-name");

        // If not specified, use the name of the parameter.

        if (propertyName == null)
        {
            propertyName = name;

            validate(
                propertyName,
                PROPERTY_NAME_PATTERN,
                "SpecificationParser.invalid-property-name");
        }

        param.setPropertyName(propertyName);

        String direction = getAttribute(node, "direction");

        if (direction != null)
            param.setDirection((Direction) _conversionMap.get(direction));

        specification.addParameter(name, param);

        Node child = node.getFirstChild();
        if (child != null && isElement(child, "description"))
        {
            param.setDescription(getValue(child));
        }
    }

    /**
     *  @since 1.0.4
     *
     **/

    private void convertBean(ComponentSpecification specification, Node node)
        throws DocumentParseException
    {
        String name = getAttribute(node, "name");

        validate(name, BEAN_NAME_PATTERN, "SpecificationParser.invalid-bean-name");

        String className = getAttribute(node, "class");
        String lifecycleString = getAttribute(node, "lifecycle");

        BeanLifecycle lifecycle = (BeanLifecycle) _conversionMap.get(lifecycleString);

        BeanSpecification bspec = _factory.createBeanSpecification(className, lifecycle);

        specification.addBeanSpecification(name, bspec);

        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
        {
            if (isElement(child, "description"))
            {
                bspec.setDescription(getValue(child));
                continue;
            }

            if (isElement(child, "property"))
            {
                convertProperty(bspec, child);
                continue;
            }

            if (isElement(child, "set-property"))
            {
                convertSetProperty(bspec, child);
                continue;
            }

            if (isElement(child, "set-string-property"))
            {
                convertSetStringProperty(bspec, child);
                continue;
            }
        }
    }

    /**
     *  This is a new simplified version structured around OGNL, in the 1.3 DTD.
     * 
     *  @since 2.2
     * 
     **/

    private void convertSetProperty(BeanSpecification spec, Node node)
        throws DocumentParseException
    {
        String name = getAttribute(node, "name");
        String expression = getAttribute(node, "expression");

        IBeanInitializer iz = _factory.createExpressionBeanInitializer(name, expression);

        spec.addInitializer(iz);
    }

    /**
     *  String properties in the 1.3 DTD are handled a little differently.
     * 
     *  @since 2.2
     * 
     **/

    private void convertSetStringProperty(BeanSpecification spec, Node node)
        throws DocumentParseException
    {
        String name = getAttribute(node, "name");
        String key = getAttribute(node, "key");

        IBeanInitializer iz = _factory.createStringBeanInitializer(name, key);

        spec.addInitializer(iz);
    }

    /** @since 1.0.8 **/

    private void convertFieldValue(BeanSpecification spec, String propertyName, Node node)
    {
        String fieldName = getAttribute(node, "field-name");
        IBeanInitializer iz = _factory.createFieldBeanInitializer(propertyName, fieldName);

        spec.addInitializer(iz);
    }

    /**
     *  @since 2.2
     * 
     **/

    private void convertExpressionValue(BeanSpecification spec, String propertyName, Node node)
    {
        String expression = getAttribute(node, "expression");
        IBeanInitializer iz = _factory.createExpressionBeanInitializer(propertyName, expression);

        spec.addInitializer(iz);
    }

    /** @since 1.0.5 **/

    private void convertStaticValue(BeanSpecification spec, String propertyName, Node node)
        throws DocumentParseException
    {
        String type = getAttribute(node, "type");
        String value = getValue(node);

        IConverter converter = (IConverter) _conversionMap.get(type);

        if (converter == null)
            throw new DocumentParseException(
                Tapestry.getString("SpecificationParser.unknown-static-value-type", type),
                getResourceLocation());

        Object staticValue = converter.convert(value);

        IBeanInitializer iz = _factory.createStaticBeanInitializer(propertyName, staticValue);

        spec.addInitializer(iz);
    }

    private void convertComponent(ComponentSpecification specification, Node node)
        throws DocumentParseException
    {
        String id = getAttribute(node, "id");

        validate(id, COMPONENT_ID_PATTERN, "SpecificationParser.invalid-component-id");

        String type = getAttribute(node, "type");
        String copyOf = getAttribute(node, "copy-of");
        ContainedComponent c;

        if (type != null && copyOf != null)
            throw new DocumentParseException(
                Tapestry.getString("SpecificationParser.both-type-and-copy-of", id),
                getResourceLocation());

        if (copyOf != null)
            c = copyExistingComponent(specification, copyOf);
        else
        {
            if (type == null)
                throw new DocumentParseException(
                    Tapestry.getString("SpecificationParser.missing-type-or-copy-of", id),
                    getResourceLocation());

            // In prior versions, its more free-form, because you can specify the path to
            // a component as well.  In version 3, you must use an alias and define it
            // in a library.

            validate(type, COMPONENT_TYPE_PATTERN, "SpecificationParser.invalid-component-type");

            c = _factory.createContainedComponent();
            c.setType(type);
        }

        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
        {
            if (isElement(child, "binding"))
            {
                convertBinding(c, child, BindingType.DYNAMIC, "expression");
                continue;
            }

            // Field binding is in 1.3 DTD, but removed from 1.4

            if (isElement(child, "field-binding"))
            {
                convertBinding(c, child, BindingType.FIELD, "field-name");
                continue;
            }

            if (isElement(child, "listener-binding"))
            {
                convertListenerBinding(c, child);
                continue;
            }

            if (isElement(child, "inherited-binding"))
            {
                convertBinding(c, child, BindingType.INHERITED, "parameter-name");
                continue;
            }

            if (isElement(child, "static-binding"))
            {
                convertBinding(c, child, BindingType.STATIC, "value");
                continue;
            }

            // <string-binding> added in release 2.0.4

            if (isElement(child, "string-binding"))
            {
                convertBinding(c, child, BindingType.STRING, "key");
                continue;
            }

            if (isElement(child, "property"))
            {
                convertProperty(c, child);
                continue;
            }
        }

        specification.addComponent(id, c);
    }

    private void convertBinding(
        ContainedComponent component,
        Node node,
        BindingType type,
        String attributeName)
        throws DocumentParseException
    {
        String name = getAttribute(node, "name");

        String value = getExtendedAttribute(node, attributeName, true);

        BindingSpecification binding = _factory.createBindingSpecification(type, value);

        component.setBinding(name, binding);
    }

    private void convertListenerBinding(ContainedComponent component, Node node)
    {
        String name = getAttribute(node, "name");
        String language = getAttribute(node, "language");

        // The script itself is the character data wrapped by the element.

        String script = getValue(node);

        ListenerBindingSpecification binding =
            _factory.createListenerBindingSpecification(language, script);

        component.setBinding(name, binding);
    }

    private ContainedComponent copyExistingComponent(ComponentSpecification spec, String id)
        throws DocumentParseException
    {
        ContainedComponent c = spec.getComponent(id);
        if (c == null)
            throw new DocumentParseException(
                Tapestry.getString("SpecificationParser.unable-to-copy", id),
                getResourceLocation());

        ContainedComponent result = _factory.createContainedComponent();

        result.setType(c.getType());
        result.setCopyOf(id);

        Iterator i = c.getBindingNames().iterator();
        while (i.hasNext())
        {
            String name = (String) i.next();
            BindingSpecification binding = c.getBinding(name);
            result.setBinding(name, binding);
        }

        return result;
    }

    private void convertAsset(
        ComponentSpecification specification,
        Node node,
        AssetType type,
        String attributeName)
        throws DocumentParseException
    {
        String name = getAttribute(node, "name");

        // As a special case, allow the exact value through (even though
        // it is not, technically, a valid asset name).

        if (!name.equals(ITemplateSource.TEMPLATE_ASSET_NAME))
            validate(name, ASSET_NAME_PATTERN, "SpecificationParser.invalid-asset-name");

        String value = getAttribute(node, attributeName);
        AssetSpecification asset = _factory.createAssetSpecification(type, value);

        specification.addAsset(name, asset);

        processPropertiesInNode(asset, node);
    }

    /**
     *  Used in several places where an element's only possible children are
     *  &lt;property&gt; elements.
     * 
     **/

    private void processPropertiesInNode(IPropertyHolder holder, Node node)
        throws DocumentParseException
    {
        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
        {
            if (isElement(child, "property"))
            {
                convertProperty(holder, child);
                continue;
            }
        }
    }

    /**
     *  @since 1.0.5
     *
     **/

    private void convertReservedParameter(ComponentSpecification spec, Node node)
    {
        String name = getAttribute(node, "name");

        spec.addReservedParameterName(name);
    }

    /**
     *  @since 1.0.9
     * 
     **/

    private void convertService(ILibrarySpecification spec, Node node)
        throws DocumentParseException
    {
        String name = getAttribute(node, "name");

        validate(name, SERVICE_NAME_PATTERN, "SpecificationParser.invalid-service-name");

        String className = getAttribute(node, "class");

        spec.setServiceClassName(name, className);
    }

    /**
     *  Sets the SpecFactory which instantiates Tapestry spec objects.
     * 
     *  @since 1.0.9
     **/

    public void setFactory(SpecFactory factory)
    {
        _factory = factory;
    }

    /**
     *  Returns the current SpecFactory which instantiates Tapestry spec objects.
     * 
     *  @since 1.0.9
     * 
     **/

    public SpecFactory getFactory()
    {
        return _factory;
    }

    /** @since 2.2 **/

    private void convertExtension(ILibrarySpecification specification, Node node)
        throws DocumentParseException
    {
        String name = getAttribute(node, "name");
        String className = getAttribute(node, "class");
        boolean immediate = getBooleanAttribute(node, "immediate");

        validate(name, EXTENSION_NAME_PATTERN, "SpecificationParser.invalid-extension-name");

        ExtensionSpecification exSpec = _factory.createExtensionSpecification();

        exSpec.setClassName(className);
        exSpec.setImmediate(immediate);

        specification.addExtensionSpecification(name, exSpec);

        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
        {
            if (isElement(child, "configure"))
            {
                convertConfigure(exSpec, child);
                continue;
            }

            if (isElement(child, "property"))
            {
                convertProperty(exSpec, child);
                continue;
            }
        }
    }

    /** @since 2.2 **/

    private void convertConfigure(ExtensionSpecification spec, Node node)
        throws DocumentParseException
    {
        String propertyName = getAttribute(node, "property-name");
        String type = getAttribute(node, "type");

        String value = getExtendedAttribute(node, "value", true);

        validate(propertyName, PROPERTY_NAME_PATTERN, "SpecificationParser.invalid-property-name");

        IConverter converter = (IConverter) _conversionMap.get(type);

        if (converter == null)
            throw new DocumentParseException(
                Tapestry.getString("SpecificationParser.unknown-static-value-type", type),
                getResourceLocation());

        Object objectValue = converter.convert(value);

        spec.addConfiguration(propertyName, objectValue);
    }

    /** @since 2.4 **/

    private void convertPropertySpecification(ComponentSpecification spec, Node node)
        throws DocumentParseException
    {
        PropertySpecification ps = _factory.createPropertySpecification();

        String name = getAttribute(node, "name");

        validate(name, PROPERTY_NAME_PATTERN, "SpecificationParser.invalid-property-name");

        ps.setName(name);

        String type = getExtendedAttribute(node, "type", false);

        if (!Tapestry.isNull(type))
            ps.setType(type);

        boolean persistent = getBooleanAttribute(node, "persistent");

        ps.setPersistent(persistent);

        ps.setInitialValue(getExtendedAttribute(node, "initial-value", false));

        spec.addPropertySpecification(ps);
    }

    /** 
     *  Used with many elements that allow a value to be specified as either
     *  an attribute, or as wrapped character data.  This handles that case,
     *  and makes it an error to specify both.
     * 
     * @since 2.4 
     * 
     **/

    protected String getExtendedAttribute(Node node, String attributeName, boolean required)
        throws DocumentParseException
    {
        String attributeValue = getAttribute(node, attributeName);
        boolean nullAttributeValue = Tapestry.isNull(attributeValue);
        String bodyValue = getValue(node);
        boolean nullBodyValue = Tapestry.isNull(bodyValue);

        if (!nullAttributeValue && !nullBodyValue)
            throw new DocumentParseException(
                Tapestry.getString(
                    "SpecificationParser.no-attribute-and-body",
                    attributeName,
                    node.getNodeName()),
                getResourceLocation());

        if (required && nullAttributeValue && nullBodyValue)
            throw new DocumentParseException(
                Tapestry.getString(
                    "SpecificationParser.required-extended-attribute",
                    node.getNodeName(),
                    attributeName),
                getResourceLocation());

        if (nullAttributeValue)
            return bodyValue;

        return attributeValue;
    }
}