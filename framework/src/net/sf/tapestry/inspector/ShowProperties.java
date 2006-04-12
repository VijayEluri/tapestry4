//
// Tapestry Web Application Framework
// Copyright (c) 2000-2002 by Howard Lewis Ship
//
// Howard Lewis Ship
// http://sf.net/projects/tapestry
// mailto:hship@users.sf.net
//
// This library is free software.
//
// You may redistribute it and/or modify it under the terms of the GNU
// Lesser General Public License as published by the Free Software Foundation.
//
// Version 2.1 of the license should be included with this distribution in
// the file LICENSE, as well as License.html. If the license is not
// included with this distribution, you may find a copy at the FSF web
// site at 'www.gnu.org' or 'www.fsf.org', or you may write to the
// Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139 USA.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied waranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//

package net.sf.tapestry.inspector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sf.tapestry.BaseComponent;
import net.sf.tapestry.IEngine;
import net.sf.tapestry.IPage;
import net.sf.tapestry.IPageChange;
import net.sf.tapestry.IPageRecorder;
import net.sf.tapestry.IRequestCycle;
import net.sf.tapestry.Tapestry;
import net.sf.tapestry.event.PageEvent;
import net.sf.tapestry.event.PageRenderListener;
import net.sf.tapestry.util.prop.IPublicBean;

/**
 *  Component of the {@link Inspector} page used to display
 *  the persisent properties of the page.
 *
 *  @version $Id$
 *  @author Howard Lewis Ship
 *
 **/

public class ShowProperties extends BaseComponent implements PageRenderListener
{

    public static class AccessorElement implements IPublicBean, Comparable
    {
        public String propertyPath;
        public String propertyName;
        public String propertyType;
        public boolean error;

        /**
         *  The names will be unique and we want to sort
         *  into ascending order by name.
         *
         **/

        public int compareTo(Object other)
        {
            AccessorElement otherElement = (AccessorElement) other;

            return propertyName.compareTo(otherElement.propertyName);
        }
    }

    private List _properties;
    private IPageChange _change;
    private IPage _inspectedPage;

    /**
     *  Registers this component as a {@link PageRenderListener}.
     *
     *  @since 1.0.5
     *
     **/

    protected void finishLoad()
    {
        getPage().addPageRenderListener(this);
    }

    /**
     *  Does nothing.
     *
     *  @since 1.0.5
     *
     **/

    public void pageBeginRender(PageEvent event)
    {
    }

    /**
     *  @since 1.0.5
     *
     **/

    public void pageEndRender(PageEvent event)
    {
        _properties = null;
        _change = null;
        _inspectedPage = null;
    }

    private void buildProperties()
    {
        Inspector inspector = (Inspector) getPage();

        _inspectedPage = inspector.getInspectedPage();

        IEngine engine = getPage().getEngine();
        IPageRecorder recorder = engine.getPageRecorder(_inspectedPage.getName());

        // No page recorder?  No properties.

        if (recorder == null)
        {
            _properties = Collections.EMPTY_LIST;
            return;
        }
        
        if (recorder.getHasChanges())
            _properties = new ArrayList(recorder.getChanges());
    }

    /**
     *  Returns a {@link List} of {@link IPageChange} objects.
     *
     *  <p>Sort order is not defined.
     *
     **/

    public List getProperties()
    {
        if (_properties == null)
            buildProperties();

        return _properties;
    }

    public void setChange(IPageChange value)
    {
        _change = value;
    }

    public IPageChange getChange()
    {
        return _change;
    }

    /**
     *  Returns true if the current change has a null component path.
     *
     **/

    public boolean getDisableComponentLink()
    {
        return _change.getComponentPath() == null;
    }

    /**
     *  Returns the name of the value's class, if the value is non-null.
     *
     **/

    public String getValueClassName()
    {
        Object value;

        value = _change.getNewValue();

        if (value == null)
            return "<null>";

        return convertClassToName(value.getClass());
    }

    private String convertClassToName(Class cl)
    {
        // TODO: This only handles one-dimensional arrays
        // property.

        if (cl.isArray())
            return "array of " + cl.getComponentType().getName();

        return cl.getName();
    }

}