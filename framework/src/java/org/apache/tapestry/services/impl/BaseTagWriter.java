package org.apache.tapestry.services.impl;

import org.apache.tapestry.IMarkupWriter;
import org.apache.tapestry.IPage;
import org.apache.tapestry.IRender;
import org.apache.tapestry.IRequestCycle;

/**
 * Contains code needed to render the &lt;base&gt; tag for pages. The &lt;base&gt; tag ensures that
 * the base URL for the rendered page matches the location of the page template in the servlet
 * context, so that relative URLs to static assets (images, stylesheets, etc.) will be processed
 * correctly. This is important starting with release 3.1, where HTML templates are no longer
 * restricted to the servlet root.
 * <p>
 * Note that pages outside of the application namespace (provided by the framework itself, or in a
 * library) are "virtually located" in the application root.
 * 
 * @author Howard M. Lewis Ship
 * @since 3.1
 */
public class BaseTagWriter implements IRender
{

    public void render(IMarkupWriter writer, IRequestCycle cycle)
    {
        IPage page = cycle.getPage();

        StringBuffer sb = new StringBuffer();
        sb.append("/");

        if (page.getNamespace().getId() == null)
        {
            String name = page.getPageName();
            int slashx = name.lastIndexOf('/');

            // Include the directory and trailing slash.
            if (slashx > 0)
                sb.append(name.substring(0, slashx + 1));
        }

        String url = cycle.getAbsoluteURL(sb.toString());

        writer.beginEmpty("base");
        writer.attribute("href", url);

        writer.println();
    }

}