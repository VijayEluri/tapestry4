// Copyright May 9, 2006 The Apache Software Foundation
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
package org.apache.tapestry.services.impl;

import org.apache.hivemind.Location;
import org.apache.tapestry.*;
import org.apache.tapestry.asset.AssetFactory;
import org.apache.tapestry.engine.IEngineService;
import org.apache.tapestry.engine.ILink;
import org.apache.tapestry.engine.NullWriter;
import org.apache.tapestry.markup.MarkupFilter;
import org.apache.tapestry.markup.MarkupWriterImpl;
import org.apache.tapestry.markup.MarkupWriterSource;
import org.apache.tapestry.markup.UTFMarkupFilter;
import org.apache.tapestry.services.Infrastructure;
import org.apache.tapestry.services.RequestLocaleManager;
import org.apache.tapestry.services.ResponseBuilder;
import org.apache.tapestry.services.ServiceConstants;
import org.apache.tapestry.util.ContentType;
import org.apache.tapestry.web.WebResponse;
import static org.easymock.EasyMock.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Tests functionality of {@link DojoAjaxResponseBuilder}.
 */
@SuppressWarnings("cast")
@Test(sequential=true)
public class DojoAjaxResponseBuilderTest extends BaseComponentTestCase
{

    private static CharArrayWriter _writer;

    private PrintWriter newPrintWriter()
    {
        _writer = new CharArrayWriter();

        return new PrintWriter(_writer);
    }

    @AfterClass
    protected void cleanup() throws Exception
    {
        _writer = null;
    }

    private void assertOutput(String expected)
    {
        assertEquals(_writer.toString(), expected);

        _writer.reset();
    }

    public void test_Null_Render()
    {
        IRender render = newMock(IRender.class);
        IRequestCycle cycle = newMock(IRequestCycle.class);
        IMarkupWriter writer = newWriter();

        ResponseBuilder builder = new DojoAjaxResponseBuilder(cycle, writer, null);

        render.render(NullWriter.getSharedInstance(), cycle);

        replay();

        builder.render(writer, render, cycle);

        verify();

        assertSame(builder.getWriter(), writer);
    }

    public void test_Normal_Render()
    {
        IRender render = newMock(IRender.class);
        IRequestCycle cycle = newMock(IRequestCycle.class);
        IMarkupWriter writer = newMock(IMarkupWriter.class);

        ResponseBuilder builder = new DojoAjaxResponseBuilder(cycle, writer, null);

        render.render(NullWriter.getSharedInstance(), cycle);

        replay();

        builder.render(null, render, cycle);

        verify();

        assertSame(builder.getWriter(), writer);
    }

    public void test_Null_Contains()
    {
        IRequestCycle cycle = newMock(IRequestCycle.class);
        IMarkupWriter writer = newWriter();

        ResponseBuilder builder = new DojoAjaxResponseBuilder(cycle, writer, null);

        replay();

        builder.isBodyScriptAllowed(null);

        verify();
    }

    public void test_Partial_Render()
    {
        IRender render = newMock(IRender.class);

        IComponent comp1 = newMock(IComponent.class);
        IRequestCycle cycle = newMock(IRequestCycle.class);
        IMarkupWriter writer = newMock(IMarkupWriter.class);
        NestedMarkupWriter nested = newMock(NestedMarkupWriter.class);

        Infrastructure infra = newMock(Infrastructure.class);

        List parts = new ArrayList();
        parts.add("id1");

        DojoAjaxResponseBuilder builder = new DojoAjaxResponseBuilder(cycle, writer, parts);

        render.render(NullWriter.getSharedInstance(), cycle);

        expect(comp1.getClientId()).andReturn("id1").anyTimes();
        expect(comp1.peekClientId()).andReturn("id1").anyTimes();
        expect(cycle.getInfrastructure()).andReturn(infra);
        expect(infra.getOutputEncoding()).andReturn("UTF-8");

        writer.printRaw("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.printRaw("<!DOCTYPE html "
                        + "PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" "
                        + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\" [" + NEWLINE
                        + "<!ENTITY nbsp '&#160;'>" + NEWLINE
                        + "]>" + NEWLINE);
        writer.printRaw("<ajax-response>");

        expect(writer.getNestedWriter()).andReturn(nested);

        nested.begin("response");
        nested.attribute("id", "id1");
        nested.attribute("type", ResponseBuilder.ELEMENT_TYPE);

        comp1.render(nested, cycle);

        replay();

        builder.render(null, render, cycle);

        assertTrue(builder.contains(comp1));
        assertTrue(IComponent.class.isInstance(comp1));

        builder.render(null, comp1, cycle);

        verify();

        assertSame(builder.getWriter(), writer);
    }

    public void test_Page_Render()
    {
        IPage page = newMock(IPage.class);
        checkOrder(page, false);

        IRequestCycle cycle = newMock(IRequestCycle.class);
        RequestLocaleManager rlm = newMock(RequestLocaleManager.class);

        MarkupWriterSource mrs = newMock(MarkupWriterSource.class);
        WebResponse resp = newMock(WebResponse.class);
        AssetFactory assetFactory = newMock(AssetFactory.class);
        IEngineService pageService = newEngineService();

        List errorPages = new ArrayList();

        List parts = new ArrayList();
        parts.add("id1");

        expect(page.getPageName()).andReturn("RequestPage").anyTimes();
        expect(cycle.getParameter(ServiceConstants.PAGE)).andReturn("RequestPage").anyTimes();
        expect(page.peekClientId()).andReturn("pageId");

        expect(cycle.renderStackIterator()).andReturn(Collections.EMPTY_LIST.iterator());

        page.render(NullWriter.getSharedInstance(), cycle);

        replay();

        DojoAjaxResponseBuilder builder =
          new DojoAjaxResponseBuilder(cycle, rlm, mrs, resp, errorPages, assetFactory, "", pageService);

        builder.render(null, page, cycle);

        verify();
    }

    public void test_New_Page_Render()
    {
        IPage page = newMock(IPage.class);
        checkOrder(page, false);

        IRequestCycle cycle = newMock(IRequestCycle.class);
        IMarkupWriter writer = newMock(IMarkupWriter.class);
        NestedMarkupWriter nwriter = newNestedWriter();

        ILink link = newMock(ILink.class);

        RequestLocaleManager rlm = newMock(RequestLocaleManager.class);
        MarkupWriterSource mrs = newMock(MarkupWriterSource.class);
        WebResponse resp = newMock(WebResponse.class);
        AssetFactory assetFactory = newMock(AssetFactory.class);
        IEngineService pageService = newEngineService();

        List errorPages = new ArrayList();

        List parts = new ArrayList();
        parts.add("id1");

        expect(page.getPageName()).andReturn("RequestPage").anyTimes();
        expect(cycle.getParameter(ServiceConstants.PAGE)).andReturn("anotherPage").anyTimes();

        expect(writer.getNestedWriter()).andReturn(nwriter);
        nwriter.begin("response");
        nwriter.attribute("type", ResponseBuilder.PAGE_TYPE);

        expect(pageService.getLink(true, "RequestPage")).andReturn(link);
        expect(link.getAbsoluteURL()).andReturn("/new/url");

        nwriter.attribute("url", "/new/url");

        replay();

        DojoAjaxResponseBuilder builder =  new DojoAjaxResponseBuilder(cycle, rlm, mrs, resp, errorPages, assetFactory, "", pageService);
        builder.setWriter(writer);

        builder.render(null, page, cycle);

        verify();
    }

    private static final String NEWLINE = System.getProperty("line.separator");

    public void test_Render_Response_Already_Started()
      throws IOException
    {
        IPage page = newMock(IPage.class);
        checkOrder(page, false);
        
        IRequestCycle cycle = newMock(IRequestCycle.class);
        Infrastructure infra = newMock(Infrastructure.class);
        IMarkupWriter writer = newBufferWriter();

        Location l = newLocation();

        RequestLocaleManager rlm = newMock(RequestLocaleManager.class);
        MarkupWriterSource mrs = newMock(MarkupWriterSource.class);
        WebResponse resp = newMock(WebResponse.class);
        AssetFactory assetFactory = newMock(AssetFactory.class);
        IEngineService pageService = newEngineService();

        List errorPages = new ArrayList();

        List parts = new ArrayList();
        parts.add("id1");

        PrintWriter pw = newPrintWriter();
        
        rlm.persistLocale();
        expect(cycle.getInfrastructure()).andReturn(infra).anyTimes();
        expect(infra.getOutputEncoding()).andReturn(("UTF-8")).anyTimes();
        expect(cycle.getParameters("updateParts")).andReturn((String[])parts.toArray(new String[parts.size()]));
        expect(resp.getPrintWriter(isA(ContentType.class))).andReturn(pw);
        expect(mrs.newMarkupWriter(eq(pw), isA(ContentType.class))).andReturn(writer);

        expect(cycle.getAttribute(TapestryUtils.PAGE_RENDER_SUPPORT_ATTRIBUTE)).andReturn(null);
        cycle.setAttribute(eq(TapestryUtils.PAGE_RENDER_SUPPORT_ATTRIBUTE), isA(PageRenderSupport.class));

        cycle.renderPage(isA(DojoAjaxResponseBuilder.class));
        
        // only done to simulate a caught internal stale link / other
        // exception that would cause a new renderPage() request
        
        expectLastCall().andThrow(new RedirectException("redir"));

        cycle.renderPage(isA(DojoAjaxResponseBuilder.class));
        cycle.removeAttribute(TapestryUtils.PAGE_RENDER_SUPPORT_ATTRIBUTE);

        replay();

        DojoAjaxResponseBuilder builder =  new DojoAjaxResponseBuilder(cycle, rlm, mrs, resp, errorPages, assetFactory, "", pageService);

        try
        {
            builder.renderResponse(cycle);
            unreachable();
        } catch (RedirectException e)
        {
            IMarkupWriter fakeWriter = builder.getComponentWriter("fakeComp");
            fakeWriter.beginEmpty("div");
            fakeWriter.attribute("id", "fakeComp");

            builder.renderResponse(cycle);
        }

        verify();

        assertBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" " +
                     "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\" [" + NEWLINE +
                     "<!ENTITY nbsp '&#160;'>" + NEWLINE +
                     "]>" + NEWLINE +
                     "<ajax-response></ajax-response>");
    }

    public void test_Allowed_Scripts()
    {
        IRequestCycle cycle = newMock(IRequestCycle.class);
        IComponent comp = newMock(IComponent.class);
        IMarkupWriter writer = newWriter();

        List parts = new ArrayList();
        parts.add("comp1");

        ResponseBuilder builder = new DojoAjaxResponseBuilder(cycle, writer, parts);

        expect(comp.getClientId()).andReturn("comp");

        expect(cycle.renderStackIterator()).andReturn(Collections.EMPTY_LIST.iterator());

        expect(comp.getClientId()).andReturn("comp1");
        expect(comp.getClientId()).andReturn("comp");

        expect(cycle.renderStackIterator()).andReturn(Collections.EMPTY_LIST.iterator());

        replay();

        assertFalse(builder.isBodyScriptAllowed(comp));
        assertTrue(builder.isExternalScriptAllowed(comp));
        assertFalse(builder.isInitializationScriptAllowed(comp));

        verify();
    }

    public void test_Script_Contains_Stack()
    {
        IRequestCycle cycle = newMock(IRequestCycle.class);
        IComponent comp = newMock(IComponent.class);
        checkOrder(comp, false);

        IMarkupWriter writer = newWriter();

        List parts = new ArrayList();
        parts.add("comp1");

        List stack = new ArrayList();
        stack.add(comp);

        ResponseBuilder builder = new DojoAjaxResponseBuilder(cycle, writer, parts);

        expect(comp.getClientId()).andReturn("comp").anyTimes();

        expect(cycle.renderStackIterator()).andReturn(stack.iterator()).anyTimes();

        replay();

        assertFalse(builder.isBodyScriptAllowed(comp));
        assertFalse(builder.isExternalScriptAllowed(comp));
        assertFalse(builder.isInitializationScriptAllowed(comp));

        verify();
    }

    public void test_Write_Body_Script()
    {
        MarkupFilter filter = new UTFMarkupFilter();
        PrintWriter writer = newPrintWriter();
        IRequestCycle cycle = newMock(IRequestCycle.class);
        Infrastructure inf = newMock(Infrastructure.class);

        replay();

        IMarkupWriter mw = new MarkupWriterImpl("text/html", writer, filter);
        DojoAjaxResponseBuilder builder = new DojoAjaxResponseBuilder(cycle, mw, null);

        String bscript = "var e=4;";
        String imageInit = "image initializations";
        String preload = "preloadedvarname";

        verify();

        expect(cycle.getInfrastructure()).andReturn(inf);
        expect(inf.getOutputEncoding()).andReturn("UTF-8");

        replay();

        builder.beginResponse();
        builder.beginBodyScript(mw, cycle);
        builder.writeImageInitializations(mw, imageInit, preload, cycle);
        builder.writeBodyScript(mw, bscript, cycle);
        builder.endBodyScript(mw, cycle);
        builder.endResponse();

        assertOutput("<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\" [" + NEWLINE +
                     "<!ENTITY nbsp \'&#160;\'>" + NEWLINE +
                     "]>" + NEWLINE +
                     "<ajax-response><response id=\"bodyscript\" type=\"script\"><script>" + NEWLINE +
                     "//<![CDATA[" + NEWLINE +
                     NEWLINE +
                     "preloadedvarname = [];" + NEWLINE +
                     "if (document.images) {" + NEWLINE +
                     "image initializations}" + NEWLINE +
                     "var e=4;" + NEWLINE +
                     "//]]>" + NEWLINE +
                     "</script></response></ajax-response>");

        verify();
    }

    public void test_Write_External_Scripts()
    {
        MarkupFilter filter = new UTFMarkupFilter();
        PrintWriter writer = newPrintWriter();
        IRequestCycle cycle = newMock(IRequestCycle.class);
        Infrastructure inf = newMock(Infrastructure.class);

        replay();

        IMarkupWriter mw = new MarkupWriterImpl("text/html", writer, filter);
        DojoAjaxResponseBuilder builder = new DojoAjaxResponseBuilder(cycle, mw, null);

        String script1 = "http://noname/js/package.js";
        String script2 = "http://noname/js/package2.js";

        verify();

        expect(cycle.getInfrastructure()).andReturn(inf);
        expect(inf.getOutputEncoding()).andReturn("UTF-8");

        replay();

        builder.beginResponse();
        builder.writeExternalScript(mw, script1, cycle);
        builder.writeExternalScript(mw, script2, cycle);
        builder.endResponse();

        assertOutput("<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\" [" + NEWLINE +
                     "<!ENTITY nbsp \'&#160;\'>" + NEWLINE +
                     "]>" + NEWLINE +
                     "<ajax-response><response id=\"includescript\" type=\"script\">"
                     + "<include url=\"http://noname/js/package.js\" />"
                     + "<include url=\"http://noname/js/package2.js\" /></response></ajax-response>");

        verify();
    }

    public void test_Write_Initialization_Script()
    {
        IRequestCycle cycle = newMock(IRequestCycle.class);
        MarkupFilter filter = new UTFMarkupFilter();
        PrintWriter writer = newPrintWriter();
        Infrastructure inf = newMock(Infrastructure.class);

        replay();

        IMarkupWriter mw = new MarkupWriterImpl("text/html", writer, filter);
        DojoAjaxResponseBuilder builder = new DojoAjaxResponseBuilder(cycle, mw, null);

        String script = "doThisInInit();";

        verify();

        expect(cycle.getInfrastructure()).andReturn(inf);
        expect(inf.getOutputEncoding()).andReturn("UTF-8");

        replay();

        builder.beginResponse();
        builder.writeInitializationScript(mw, script);
        builder.endResponse();

        assertOutput("<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\" [" + NEWLINE +
                     "<!ENTITY nbsp \'&#160;\'>" + NEWLINE +
                     "]>" + NEWLINE +
                     "<ajax-response><response id=\"initializationscript\" type=\"script\"><script>" + NEWLINE +
                     "//<![CDATA[" + NEWLINE +
                     "doThisInInit();" + NEWLINE +
                     "//]]>" + NEWLINE +
                     "</script></response></ajax-response>");

        verify();
    }
}
