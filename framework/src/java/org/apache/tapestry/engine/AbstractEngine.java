// Copyright 2004 The Apache Software Foundation
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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hivemind.ApplicationRuntimeException;
import org.apache.hivemind.ClassResolver;
import org.apache.tapestry.ApplicationServlet;
import org.apache.tapestry.Constants;
import org.apache.tapestry.Defense;
import org.apache.tapestry.IEngine;
import org.apache.tapestry.IPage;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.PageRedirectException;
import org.apache.tapestry.RedirectException;
import org.apache.tapestry.StaleLinkException;
import org.apache.tapestry.StaleSessionException;
import org.apache.tapestry.Tapestry;
import org.apache.tapestry.listener.ListenerMap;
import org.apache.tapestry.request.RequestContext;
import org.apache.tapestry.request.ResponseOutputStream;
import org.apache.tapestry.services.ComponentMessagesSource;
import org.apache.tapestry.services.DataSqueezer;
import org.apache.tapestry.services.Infrastructure;
import org.apache.tapestry.services.ObjectPool;
import org.apache.tapestry.services.TemplateSource;
import org.apache.tapestry.spec.IApplicationSpecification;

import com.sun.jndi.ldap.pool.Pool;

/**
 * Basis for building real Tapestry applications. Immediate subclasses provide different strategies
 * for managing page state and other resources between request cycles.
 * <p>
 * Note: much of this description is <em>in transition</em> as part of Tapestry 3.1. All ad-hoc
 * singletons and such are being replaced with HiveMind services.
 * <p>
 * Uses a shared instance of {@link ITemplateSource},{@link ISpecificationSource},
 * {@link IScriptSource}and {@link IComponentMessagesSource}stored as attributes of the
 * {@link ServletContext}(they will be shared by all sessions).
 * <p>
 * An engine is designed to be very lightweight. Particularily, it should <b>never </b> hold
 * references to any {@link IPage}or {@link org.apache.tapestry.IComponent}objects. The entire
 * system is based upon being able to quickly rebuild the state of any page(s).
 * <p>
 * Where possible, instance variables should be transient. They can be restored inside
 * {@link #setupForRequest(RequestContext)}.
 * <p>
 * In practice, a subclass (usually {@link BaseEngine}) is used without subclassing. Instead, a
 * visit object is specified. To facilitate this, the application specification may include a
 * property, <code>org.apache.tapestry.visit-class</code> which is the class name to instantiate
 * when a visit object is first needed. See {@link #createVisit(IRequestCycle)}for more details.
 * <p>
 * Some of the classes' behavior is controlled by JVM system properties (typically only used during
 * development): <table border=1>
 * <tr>
 * <th>Property</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>org.apache.tapestry.enable-reset-service</td>
 * <td>If true, enabled an additional service, reset, that allow page, specification and template
 * caches to be cleared on demand. See {@link #isResetServiceEnabled()}.</td>
 * </tr>
 * <tr>
 * <td>org.apache.tapestry.disable-caching</td>
 * <td>If true, then the page, specification, template and script caches will be cleared after each
 * request. This slows things down, but ensures that the latest versions of such files are used.
 * Care should be taken that the source directories for the files preceeds any versions of the files
 * available in JARs or WARs.</td>
 * </tr>
 * </table>
 * 
 * @author Howard Lewis Ship
 */

public abstract class AbstractEngine implements IEngine, Externalizable, HttpSessionBindingListener
{
    private static final Log LOG = LogFactory.getLog(AbstractEngine.class);

    /**
     * @since 2.0.4
     */

    private static final long serialVersionUID = 6884834397673817117L;

    /**
     * The link to the world of HiveMind services.
     * 
     * @since 3.1
     */
    private transient Infrastructure _infrastructure;

    private transient String _contextPath;

    private transient String _servletPath;

    private transient boolean _stateful;

    private transient ListenerMap _listeners;

    /**
     * An object used to contain application-specific server side state.
     */

    private Object _visit;

    /**
     * The globally shared application object. Typically, this is created when first needed, shared
     * between sessions and engines, and stored in the {@link ServletContext}.
     * 
     * @since 2.3
     */

    private transient Object _global;

    /**
     * The base name for the servlet context key used to store the application-defined Global
     * object, if any.
     * 
     * @since 2.3
     */

    public static final String GLOBAL_NAME = "org.apache.tapestry.global";

    /**
     * The name of the application property that will be used to determine the encoding to use when
     * generating the output
     * 
     * @since 3.0
     */

    public static final String OUTPUT_ENCODING_PROPERTY_NAME = "org.apache.tapestry.output-encoding";

    /**
     * The default encoding that will be used when generating the output. It is used if no output
     * encoding property has been specified.
     * 
     * @since 3.0
     */

    public static final String DEFAULT_OUTPUT_ENCODING = "UTF-8";

    /**
     * The curent locale for the engine, which may be changed at any time.
     */

    private Locale _locale;

    /**
     * The name of the application specification property used to specify the class of the visit
     * object.
     */

    public static final String VISIT_CLASS_PROPERTY_NAME = "org.apache.tapestry.visit-class";

    /**
     * If true (set from the JVM system parameter <code>org.apache.tapestry.disable-caching</code>)
     * then the cache of pages, specifications and template will be cleared after each request.
     */

    private static final boolean _disableCaching = Boolean
            .getBoolean("org.apache.tapestry.disable-caching");

    /**
     * Set to true when there is a (potential) change to the internal state of the engine, set to
     * false when the engine is stored into the {@link HttpSession}.
     * 
     * @since 3.0
     */

    private transient boolean _dirty;

    /**
     * The instance of {@link IMonitorFactory}used to create a monitor.
     * 
     * @since 3.0
     */

    private transient IMonitorFactory _monitorFactory;

    /**
     * Sets the Exception page's exception property, then renders the Exception page.
     * <p>
     * If the render throws an exception, then copious output is sent to <code>System.err</code>
     * and a {@link ServletException}is thrown.
     */

    protected void activateExceptionPage(IRequestCycle cycle, ResponseOutputStream output,
            Throwable cause) throws ServletException
    {
        try
        {
            IPage exceptionPage = cycle.getPage(getExceptionPageName());

            exceptionPage.setProperty("exception", cause);

            cycle.activate(exceptionPage);

            renderResponse(cycle, output);

        }
        catch (Throwable ex)
        {
            // Worst case scenario. The exception page itself is broken, leaving
            // us with no option but to write the cause to the output.

            reportException(
                    Tapestry.getMessage("AbstractEngine.unable-to-process-client-request"),
                    cause);

            // Also, write the exception thrown when redendering the exception
            // page, so that can get fixed as well.

            reportException(
                    Tapestry.getMessage("AbstractEngine.unable-to-present-exception-page"),
                    ex);

            // And throw the exception.

            throw new ServletException(ex.getMessage(), ex);
        }
    }

    /**
     * Writes a detailed report of the exception to <code>System.err</code>.
     */

    public void reportException(String reportTitle, Throwable ex)
    {
        _infrastructure.getRequestExceptionReporter().reportRequestException(reportTitle, ex);
    }

    /**
     * Invoked at the end of the request cycle to release any resources specific to the request
     * cycle.
     */

    protected abstract void cleanupAfterRequest(IRequestCycle cycle);

    /**
     * Extends the description of the class generated by {@link #toString()}. If a subclass adds
     * additional instance variables that should be described in the instance description, it may
     * overide this method. This implementation does nothing.
     * 
     * @see #toString()
     */

    protected void extendDescription(ToStringBuilder builder)
    {

    }

    /**
     * Returns the locale for the engine. This is initially set by the {@link ApplicationServlet}
     * but may be updated by the application.
     */

    public Locale getLocale()
    {
        return _locale;
    }

    /**
     * Overriden in subclasses that support monitoring. Should create and return an instance of
     * {@link IMonitor}that is appropriate for the request cycle described by the
     * {@link RequestContext}.
     * <p>
     * The monitor is used to create a {@link RequestCycle}.
     * <p>
     * This implementation uses a {@link IMonitorFactory}to create the monitor instance. The
     * factory is provided as an application extension. If the application extension does not exist,
     * {@link DefaultMonitorFactory}is used.
     * <p>
     * As of release 3.0, this method should <em>not</em> return null.
     */

    public IMonitor getMonitor(RequestContext context)
    {
        if (_monitorFactory == null)
        {
            IApplicationSpecification spec = getSpecification();

            if (spec.checkExtension(Tapestry.MONITOR_FACTORY_EXTENSION_NAME))
                _monitorFactory = (IMonitorFactory) spec.getExtension(
                        Tapestry.MONITOR_FACTORY_EXTENSION_NAME,
                        IMonitorFactory.class);
            else
                _monitorFactory = DefaultMonitorFactory.SHARED;
        }

        return _monitorFactory.createMonitor(context);
    }

    /** @see Infrastructure#getPageSource() */

    public IPageSource getPageSource()
    {
        return _infrastructure.getPageSource();
    }

    /**
     * Returns a service with the given name.
     * 
     * @see Infrastructure#getServiceMap()
     * @see org.apache.tapestry.services.ServiceMap
     */

    public IEngineService getService(String name)
    {
        return _infrastructure.getServiceMap().getService(name);
    }

    public String getServletPath()
    {
        return _servletPath;
    }

    /**
     * Returns the context path, the prefix to apply to any URLs so that they are recognized as
     * belonging to the Servlet 2.2 context.
     * 
     * @see org.apache.tapestry.asset.ContextAsset
     */

    public String getContextPath()
    {
        return _contextPath;
    }

    /** @see Infrastructure#getApplicationSpecification() */

    public IApplicationSpecification getSpecification()
    {
        return _infrastructure.getApplicationSpecification();
    }

    /** @see Infrastructure#getSpecificationSource() */

    public ISpecificationSource getSpecificationSource()
    {
        return _infrastructure.getSpecificationSource();
    }

    /** @see Infrastructure#getTemplateSource() */

    public TemplateSource getTemplateSource()
    {
        return _infrastructure.getTemplateSource();
    }

    /**
     * Reads the state serialized by {@link #writeExternal(ObjectOutput)}.
     * <p>
     * This always set the stateful flag. By default, a deserialized session is stateful (else, it
     * would not have been serialized).
     */

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        _stateful = true;

        String localeName = in.readUTF();
        _locale = Tapestry.getLocale(localeName);

        _visit = in.readObject();
    }

    /**
     * Writes the following properties:
     * <ul>
     * <li>locale name ({@link Locale#toString()})
     * <li>visit
     * </ul>
     */

    public void writeExternal(ObjectOutput out) throws IOException
    {
        out.writeUTF(_locale.toString());
        out.writeObject(_visit);
    }

    /**
     * Invoked, typically, when an exception occurs while servicing the request. This method resets
     * the output, sets the new page and renders it.
     */

    protected void redirect(String pageName, IRequestCycle cycle, ResponseOutputStream out,
            ApplicationRuntimeException exception) throws IOException, ServletException
    {
        // Discard any output from the previous page.

        out.reset();

        IPage page = cycle.getPage(pageName);

        cycle.activate(page);

        renderResponse(cycle, out);
    }

    /**
     * Delegates to
     * {@link org.apache.tapestry.services.ResponseRenderer#renderResponse(IRequestCycle, ResponseOutputStream)}.
     */

    public void renderResponse(IRequestCycle cycle, ResponseOutputStream output)
            throws ServletException, IOException
    {
        _infrastructure.getResponseRenderer().renderResponse(cycle, output);
    }

    /**
     * Delegate method for the servlet. Services the request.
     */

    public boolean service(RequestContext context) throws ServletException, IOException
    {
        IRequestCycle cycle = null;
        ResponseOutputStream output = null;
        IMonitor monitor = null;

        if (_infrastructure == null)
            _infrastructure = (Infrastructure) context.getAttribute(Constants.INFRASTRUCTURE_KEY);

        try
        {
            setupForRequest(context);

            monitor = getMonitor(context);

            output = new ResponseOutputStream(context.getResponse());
        }
        catch (Exception ex)
        {
            reportException(Tapestry.getMessage("AbstractEngine.unable-to-begin-request"), ex);

            throw new ServletException(ex.getMessage(), ex);
        }

        IEngineService service = null;

        try
        {
            try
            {
                try
                {
                    String serviceName = extractServiceName(context);

                    if (Tapestry.isBlank(serviceName))
                        serviceName = Tapestry.HOME_SERVICE;

                    // Must have a service to create the request cycle.
                    // Must have a request cycle to report an exception.

                    service = getService(serviceName);
                }
                catch (Exception ex)
                {
                    service = getService(Tapestry.HOME_SERVICE);
                    cycle = createRequestCycle(context, service, monitor);

                    throw ex;
                }

                cycle = createRequestCycle(context, service, monitor);

                monitor.serviceBegin(service.getName(), context.getRequestURI());

                // Invoke the service, which returns true if it may have changed
                // the state of the engine (most do return true).

                service.service(cycle, output);

                // Return true only if the engine is actually dirty. This cuts
                // down
                // on the number of times the engine is stored into the
                // session unceccesarily.

                return _dirty;
            }
            catch (PageRedirectException ex)
            {
                handlePageRedirectException(ex, cycle, output);
            }
            catch (RedirectException ex)
            {
                handleRedirectException(cycle, ex);
            }
            catch (StaleLinkException ex)
            {
                handleStaleLinkException(ex, cycle, output);
            }
            catch (StaleSessionException ex)
            {
                handleStaleSessionException(ex, cycle, output);
            }
        }
        catch (Exception ex)
        {
            monitor.serviceException(ex);

            // Discard any output (if possible). If output has already been sent
            // to
            // the client, then things get dicey. Note that this block
            // gets activated if the StaleLink or StaleSession pages throws
            // any kind of exception.

            // Attempt to switch to the exception page. However, this may itself
            // fail
            // for a number of reasons, in which case a ServletException is
            // thrown.

            output.reset();

            if (LOG.isDebugEnabled())
                LOG.debug("Uncaught exception", ex);

            activateExceptionPage(cycle, output, ex);
        }
        finally
        {
            if (service != null)
                monitor.serviceEnd(service.getName());

            try
            {
                cycle.cleanup();

                // Closing the buffered output closes the underlying stream as
                // well.

                if (output != null)
                    output.forceFlush();

                cleanupAfterRequest(cycle);
            }
            catch (Exception ex)
            {
                reportException(Tapestry.getMessage("AbstractEngine.exception-during-cleanup"), ex);
            }

            if (_disableCaching)
            {
                try
                {
                    _infrastructure.getResetEventCoordinator().fireResetEvent();
                }
                catch (Exception ex)
                {
                    reportException(Tapestry
                            .getMessage("AbstractEngine.exception-during-cache-clear"), ex);
                }
            }

        }

        return _dirty;
    }

    /**
     * Handles {@link PageRedirectException}which involves executing
     * {@link IPage#validate(IRequestCycle)}on the target page (of the exception), until either a
     * loop is found, or a page succesfully validates and can be activated.
     * <p>
     * This should generally not be overriden in subclasses.
     * 
     * @since 3.0
     */

    protected void handlePageRedirectException(PageRedirectException ex, IRequestCycle cycle,
            ResponseOutputStream output) throws IOException, ServletException
    {
        List pageNames = new ArrayList();

        String pageName = ex.getTargetPageName();

        while (true)
        {
            if (pageNames.contains(pageName))
            {
                // Add the offending page to pageNames so it shows in the
                // list.

                pageNames.add(pageName);

                StringBuffer buffer = new StringBuffer();
                int count = pageNames.size();

                for (int i = 0; i < count; i++)
                {
                    if (i > 0)
                        buffer.append("; ");

                    buffer.append(pageNames.get(i));
                }

                throw new ApplicationRuntimeException(Tapestry.format(
                        "AbstractEngine.validate-cycle",
                        buffer.toString()));
            }

            // Record that this page has been a target.

            pageNames.add(pageName);

            try
            {
                // Attempt to activate the new page.

                cycle.activate(pageName);

                break;
            }
            catch (PageRedirectException ex2)
            {
                pageName = ex2.getTargetPageName();
            }
        }

        // Discard any output from the previous page.

        output.reset();

        renderResponse(cycle, output);
    }

    /**
     * Invoked from {@link #service(RequestContext)}to create an instance of {@link IRequestCycle}
     * for the current request. This implementation creates an returns an instance of
     * {@link RequestCycle}.
     * 
     * @since 3.0
     */

    protected IRequestCycle createRequestCycle(RequestContext context, IEngineService service,
            IMonitor monitor)
    {
        return new RequestCycle(this, context, service, monitor);
    }

    /**
     * Invoked by {@link #service(RequestContext)}if a {@link StaleLinkException}is thrown by the
     * {@link IEngineService service}. This implementation sets the message property of the
     * StaleLink page to the message provided in the exception, then invokes
     * {@link #redirect(String, IRequestCycle, ResponseOutputStream, ApplicationRuntimeException)}
     * to render the StaleLink page.
     * <p>
     * Subclasses may overide this method (without invoking this implementation). A common practice
     * is to present an error message on the application's Home page.
     * <p>
     * Alternately, the application may provide its own version of the StaleLink page, overriding
     * the framework's implementation (probably a good idea, because the default page hints at
     * "application errors" and isn't localized). The overriding StaleLink implementation must
     * implement a message property of type String.
     * 
     * @since 0.2.10
     */

    protected void handleStaleLinkException(StaleLinkException ex, IRequestCycle cycle,
            ResponseOutputStream output) throws IOException, ServletException
    {
        String staleLinkPageName = getStaleLinkPageName();
        IPage page = cycle.getPage(staleLinkPageName);

        page.setProperty("message", ex.getMessage());

        redirect(staleLinkPageName, cycle, output, ex);
    }

    /**
     * Invoked by {@link #service(RequestContext)}if a {@link StaleSessionException}is thrown by
     * the {@link IEngineService service}. This implementation invokes
     * {@link #redirect(String, IRequestCycle, ResponseOutputStream, ApplicationRuntimeException)}
     * to render the StaleSession page.
     * <p>
     * Subclasses may overide this method (without invoking this implementation). A common practice
     * is to present an eror message on the application's Home page.
     * 
     * @since 0.2.10
     */

    protected void handleStaleSessionException(StaleSessionException ex, IRequestCycle cycle,
            ResponseOutputStream output) throws IOException, ServletException
    {
        redirect(getStaleSessionPageName(), cycle, output, ex);
    }

    /**
     * Changes the locale for the engine.
     */

    public void setLocale(Locale value)
    {
        Defense.notNull(value, "locale");

        // Because locale changes are expensive (it involves writing a cookie
        // and all that),
        // we're careful not to really change unless there's a true change in
        // value.

        if (!value.equals(_locale))
        {
            _locale = value;
            markDirty();
        }
    }

    /**
     * Invoked from {@link #service(RequestContext)}to ensure that the engine's instance variables
     * are setup. This allows the application a chance to restore transient variables that will not
     * have survived deserialization. Determines the servlet prefix: this is the base URL used by
     * {@link IEngineService services}to build URLs. It consists of two parts: the context path and
     * the servlet path.
     * <p>
     * The servlet path is retrieved from {@link HttpServletRequest#getServletPath()}.
     * <p>
     * The context path is retrieved from {@link HttpServletRequest#getContextPath()}.
     * <p>
     * The global object is retrieved from {@link IEngine#getGlobal()}method.
     * <p>
     * The final path is available via the {@link #getServletPath()}method.
     * <p>
     * Subclasses should invoke this implementation first, then perform their own setup.
     */

    protected void setupForRequest(RequestContext context)
    {
        HttpServlet servlet = context.getServlet();
        ServletContext servletContext = servlet.getServletContext();
        HttpServletRequest request = context.getRequest();

        // servletPath is null, so this means either we're doing the
        // first request in this session, or we're handling a subsequent
        // request in another JVM (i.e. another server in the cluster).
        // In any case, we have to do some late (re-)initialization.

        if (_servletPath == null)
        {
            // Get the path *within* the servlet context

            // In rare cases related to the tagsupport service, getServletPath()
            // is wrong
            // (its a JSP, which invokes Tapestry as an include, thus muddling
            // what
            // the real servlet and servlet path is). In those cases, the JSP
            // tag
            // will inform us.

            String path = (String) request
                    .getAttribute(Tapestry.TAG_SUPPORT_SERVLET_PATH_ATTRIBUTE);

            if (path == null)
                path = request.getServletPath();

            // Get the context path, which may be the empty string
            // (but won't be null).

            _contextPath = request.getContextPath();

            _servletPath = _contextPath + path;
        }

        String servletName = context.getServlet().getServletName();

        if (_global == null)
        {
            String name = GLOBAL_NAME + ":" + servletName;

            _global = servletContext.getAttribute(name);

            if (_global == null)
            {
                _global = createGlobal(context);

                servletContext.setAttribute(name, _global);
            }
        }

        String encoding = request.getCharacterEncoding();
        if (encoding == null)
        {
            encoding = getOutputEncoding();
            try
            {
                request.setCharacterEncoding(encoding);
            }
            catch (UnsupportedEncodingException e)
            {
                throw new IllegalArgumentException(Tapestry.format("illegal-encoding", encoding));
            }
            catch (NoSuchMethodError e)
            {
                // Servlet API 2.2 compatibility
                // Behave okay if the setCharacterEncoding() method is
                // unavailable
            }
            catch (AbstractMethodError e)
            {
                // Servlet API 2.2 compatibility
                // Behave okay if the setCharacterEncoding() method is
                // unavailable
            }
        }
    }

    /**
     * @see Infrastructure#getClassResolver()
     */

    public ClassResolver getClassResolver()
    {
        return _infrastructure.getClassResolver();
    }

    /**
     * Generates a description of the instance. Invokes {@link #extendDescription(ToStringBuilder)}
     * to fill in details about the instance.
     * 
     * @see #extendDescription(ToStringBuilder)
     */

    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this);

        builder.append("dirty", _dirty);
        builder.append("locale", _locale);
        builder.append("stateful", _stateful);
        builder.append("visit", _visit);

        extendDescription(builder);

        return builder.toString();
    }

    /**
     * Implemented by subclasses to return the names of the active pages (pages for which recorders
     * exist). May return the empty list, but should not return null.
     */

    abstract public Collection getActivePageNames();

    /**
     * Gets the visit object, if it has been created already.
     * <p>
     * If the visit is non-null then the {@link #isDirty()}flag is set (because the engine can't
     * tell what the caller will <i>do </i> with the visit).
     */

    public Object getVisit()
    {
        if (_visit != null)
            markDirty();

        return _visit;
    }

    /**
     * Gets the visit object, invoking {@link #createVisit(IRequestCycle)}to create it lazily if
     * needed. If cycle is null, the visit will not be lazily created.
     * <p>
     * After creating the visit, but before returning, the {@link HttpSession}will be created, and
     * {@link #setStateful()}will be invoked.
     * <p>
     * Sets the {@link #isDirty()}flag, if the return value is not null.
     */

    public Object getVisit(IRequestCycle cycle)
    {
        if (_visit == null && cycle != null)
        {
            _visit = createVisit(cycle);

            // Now that a visit object exists, we need to force the creation
            // of a HttpSession.

            cycle.getRequestContext().createSession();

            setStateful();
        }

        if (_visit != null)
            markDirty();

        return _visit;
    }

    /**
     * Updates the visit object and sets the {@link #isDirty() dirty flag}.
     */

    public void setVisit(Object value)
    {
        _visit = value;

        markDirty();
    }

    public boolean getHasVisit()
    {
        return _visit != null;
    }

    /**
     * Invoked to lazily create a new visit object when it is first referenced (by
     * {@link #getVisit(IRequestCycle)}). This implementation works by looking up the name of the
     * class to instantiate in the {@link #getPropertySource() configuration}.
     * <p>
     * Subclasses may want to overide this method if some other means of instantiating a visit
     * object is required.
     */

    protected Object createVisit(IRequestCycle cycle)
    {
        String visitClassName;
        Class visitClass;
        Object result = null;

        visitClassName = getPropertySource().getPropertyValue(VISIT_CLASS_PROPERTY_NAME);

        if (LOG.isDebugEnabled())
            LOG.debug("Creating visit object as instance of " + visitClassName);

        visitClass = getClassResolver().findClass(visitClassName);

        try
        {
            result = visitClass.newInstance();
        }
        catch (Throwable t)
        {
            throw new ApplicationRuntimeException(Tapestry.format(
                    "AbstractEngine.unable-to-instantiate-visit",
                    visitClassName), t);
        }

        return result;
    }

    /**
     * Returns the global object for the application. The global object is created at the start of
     * the request ({@link #setupForRequest(RequestContext)}invokes
     * {@link #createGlobal(RequestContext)}if needed), and is stored into the
     * {@link ServletContext}. All instances of the engine for the application share the global
     * object; however, the global object is explicitly <em>not</em> replicated to other servers
     * within a cluster.
     * 
     * @since 2.3
     */

    public Object getGlobal()
    {
        return _global;
    }

    public IScriptSource getScriptSource()
    {
        return _infrastructure.getScriptSource();
    }

    public boolean isStateful()
    {
        return _stateful;
    }

    /**
     * Invoked by subclasses to indicate that some state must now be stored in the engine (and that
     * the engine should now be stored in the HttpSession). The caller is responsible for actually
     * creating the HttpSession (it will have access to the {@link RequestContext}).
     * 
     * @since 1.0.2
     */

    protected void setStateful()
    {
        _stateful = true;
    }

    /**
     * Allows subclasses to include listener methods easily.
     * 
     * @since 1.0.2
     */

    public ListenerMap getListeners()
    {
        if (_listeners == null)
            _listeners = new ListenerMap(this);

        return _listeners;
    }

    /**
     * Invoked when a {@link RedirectException}is thrown during the processing of a request.
     * 
     * @throws ApplicationRuntimeException
     *             if an {@link IOException},{@link ServletException}is thrown by the redirect,
     *             or if no {@link RequestDispatcher}can be found for local resource.
     * @since 2.2
     */

    protected void handleRedirectException(IRequestCycle cycle, RedirectException ex)
    {
        String location = ex.getRedirectLocation();

        if (LOG.isDebugEnabled())
            LOG.debug("Redirecting to: " + location);

        RedirectAnalyzer analyzer = new RedirectAnalyzer(location);

        analyzer.process(cycle);
    }

    /**
     * @since 2.0.4
     */

    public ComponentMessagesSource getComponentMessagesSource()
    {
        return _infrastructure.getComponentMessagesSource();
    }

    /**
     * @see Infrastructure#getDataSqueezer()
     */

    public DataSqueezer getDataSqueezer()
    {
        return _infrastructure.getDataSqueezer();
    }

    /**
     * Invoked from {@link #service(RequestContext)}to extract, from the URL, the name of the
     * service. The current implementation expects the first pathInfo element to be the service
     * name. At some point in the future, the method of constructing and parsing URLs may be
     * abstracted into a developer-selected class.
     * <p>
     * Subclasses may override this method if the application defines specific services with unusual
     * URL encoding rules.
     * <p>
     * This implementation simply extracts the value for query parameter
     * {@link Tapestry#SERVICE_QUERY_PARAMETER_NAME}and extracts the service name from that.
     * <p>
     * For supporting the JSP tags, this method first checks for attribute
     * {@link Tapestry#TAG_SUPPORT_SERVICE_ATTRIBUTE}. If non-null, then
     * {@link Tapestry#TAGSUPPORT_SERVICE}is returned.
     * 
     * @since 2.2
     */

    protected String extractServiceName(RequestContext context)
    {
        if (context.getRequest().getAttribute(Tapestry.TAG_SUPPORT_SERVICE_ATTRIBUTE) != null)
            return Tapestry.TAGSUPPORT_SERVICE;

        String serviceData = context.getParameter(Tapestry.SERVICE_QUERY_PARAMETER_NAME);

        if (serviceData == null)
            return Tapestry.HOME_SERVICE;

        // The service name is anything before the first slash,
        // if there is one.

        int slashx = serviceData.indexOf('/');

        if (slashx < 0)
            return serviceData;

        return serviceData.substring(0, slashx);
    }

    /** @since 2.3 */

    public IPropertySource getPropertySource()
    {
        return _infrastructure.getApplicationPropertySource();
    }

    /** @since 3.0 */

    protected String getExceptionPageName()
    {
        return EXCEPTION_PAGE;
    }

    /** @since 3.0 */

    protected String getStaleLinkPageName()
    {
        return STALE_LINK_PAGE;
    }

    /** @since 3.0 */

    protected String getStaleSessionPageName()
    {
        return STALE_SESSION_PAGE;
    }

    /**
     * Creates the shared Global object. This implementation looks for an configuration property,
     * <code>org.apache.tapestry.global-class</code>, and instantiates that class using a
     * no-arguments constructor. If the property is not defined, a synchronized
     * {@link java.util.HashMap}is created.
     * 
     * @since 2.3
     */

    protected Object createGlobal(RequestContext context)
    {
        String className = getPropertySource().getPropertyValue("org.apache.tapestry.global-class");

        if (Tapestry.isBlank(className))
            return Collections.synchronizedMap(new HashMap());

        Class globalClass = getClassResolver().findClass(className);

        try
        {
            return globalClass.newInstance();
        }
        catch (Exception ex)
        {
            throw new ApplicationRuntimeException(Tapestry.format(
                    "AbstractEngine.unable-to-instantiate-global",
                    className), ex);
        }
    }

    /** @see Infrastructure#getObjectPool() */

    public ObjectPool getPool()
    {
        return _infrastructure.getObjectPool();
    }

    public IComponentClassEnhancer getComponentClassEnhancer()
    {
        throw new UnsupportedOperationException("getComponentClassEnhancer()");
    }

    /**
     * Returns true if the engine has (potentially) changed state since the last time it was stored
     * into the {@link javax.servlet.http.HttpSession}. Various events set this property to true.
     * 
     * @since 3.0
     */

    public boolean isDirty()
    {
        return _dirty;
    }

    /**
     * Invoked to set the dirty flag, indicating that the engine should be stored into the
     * {@link javax.servlet.http.HttpSession}.
     * 
     * @since 3.0
     */

    protected void markDirty()
    {
        if (!_dirty)
            LOG.debug("Setting dirty flag.");

        _dirty = true;
    }

    /**
     * Clears the dirty flag when a engine is stored into the {@link HttpSession}.
     * 
     * @since 3.0
     */

    public void valueBound(HttpSessionBindingEvent arg0)
    {
        LOG.debug(_dirty ? "Clearing dirty flag." : "Dirty flag already cleared.");

        _dirty = false;
    }

    /**
     * Does nothing.
     * 
     * @since 3.0
     */

    public void valueUnbound(HttpSessionBindingEvent arg0)
    {
    }

    /**
     * The encoding to be used if none has been defined using the output encoding property. Override
     * this method to change the default.
     * 
     * @return the default output encoding
     * @since 3.0
     */
    protected String getDefaultOutputEncoding()
    {
        return DEFAULT_OUTPUT_ENCODING;
    }

    /**
     * Returns the encoding to be used to generate the servlet responses and accept the servlet
     * requests. The encoding is defined using the org.apache.tapestry.output-encoding and is UTF-8
     * by default
     * 
     * @since 3.0
     * @see org.apache.tapestry.IEngine#getOutputEncoding()
     */
    public String getOutputEncoding()
    {
        IPropertySource source = getPropertySource();

        String encoding = source.getPropertyValue(OUTPUT_ENCODING_PROPERTY_NAME);
        if (encoding == null)
            encoding = getDefaultOutputEncoding();

        return encoding;
    }

    /** @since 3.1 */
    public Infrastructure getInfrastructure()
    {
        return _infrastructure;
    }
}