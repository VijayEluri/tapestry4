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

package net.sf.tapestry.vlib;

import net.sf.tapestry.IRequestCycle;
import net.sf.tapestry.PageRedirectException;
import net.sf.tapestry.RequestCycleException;
import net.sf.tapestry.callback.PageCallback;
import net.sf.tapestry.form.IFormComponent;
import net.sf.tapestry.html.BasePage;
import net.sf.tapestry.valid.IValidationDelegate;
import net.sf.tapestry.vlib.pages.Login;

/**
 *  Base page used for pages that should be protected by the {@link Login} page.
 *  If the user is not logged in, they are redirected to the Login page first.
 *  Also, implements an error property and a validationDelegate.
 *
 *  @author Howard Lewis Ship
 *  @version $Id$
 * 
 **/

public class Protected extends BasePage implements IErrorProperty
{
    private String error;
    private IValidationDelegate validationDelegate;

    public void detach()
    {
        error = null;
        validationDelegate = null;

        super.detach();
    }

    public IValidationDelegate getValidationDelegate()
    {
        if (validationDelegate == null)
            validationDelegate = new SimpleValidationDelegate();

        return validationDelegate;
    }

    public void setError(String value)
    {
        error = value;
    }

    public String getError()
    {
        return error;
    }

    protected void setErrorField(String componentId, String message, String value)
    {
        IFormComponent component = (IFormComponent) getComponent(componentId);

        IValidationDelegate delegate = getValidationDelegate();

        delegate.setFormComponent(component);
        delegate.record(message, null, value);

    }

    /**
     *  Returns true if the delegate indicates an error, or the error property is not null.
     *
     **/

    protected boolean isInError()
    {
        return error != null || getValidationDelegate().getHasErrors();
    }

    /**
     *  Checks if the user is logged in.  If not, they are sent
     *  to the {@link Login} page before coming back to whatever this
     *  page is.
     *
     **/

    public void validate(IRequestCycle cycle) throws RequestCycleException
    {
        Visit visit = (Visit) getVisit();

        if (visit != null && visit.isUserLoggedIn())
            return;

        // User not logged in ... redirect through the Login page.

        Login login = (Login) cycle.getPage("Login");

        login.setCallback(new PageCallback(this));

        throw new PageRedirectException(login);
    }
}