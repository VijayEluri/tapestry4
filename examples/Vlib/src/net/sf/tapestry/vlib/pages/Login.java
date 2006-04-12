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

package net.sf.tapestry.vlib.pages;

import java.rmi.RemoteException;

import javax.servlet.http.Cookie;

import net.sf.tapestry.IEngine;
import net.sf.tapestry.IRequestCycle;
import net.sf.tapestry.RequestCycleException;
import net.sf.tapestry.callback.ICallback;
import net.sf.tapestry.html.BasePage;
import net.sf.tapestry.valid.IField;
import net.sf.tapestry.valid.IValidationDelegate;
import net.sf.tapestry.valid.ValidatorException;
import net.sf.tapestry.vlib.IErrorProperty;
import net.sf.tapestry.vlib.SimpleValidationDelegate;
import net.sf.tapestry.vlib.VirtualLibraryEngine;
import net.sf.tapestry.vlib.Visit;
import net.sf.tapestry.vlib.ejb.IOperations;
import net.sf.tapestry.vlib.ejb.LoginException;
import net.sf.tapestry.vlib.ejb.Person;

/**
 *  Allows the user to login, by providing email address and password.
 *  After succesfully logging in, a cookie is placed on the client browser
 *  that provides the default email address for future logins (the cookie
 *  persists for a week).
 *
 *  @author Howard Lewis Ship
 *  @version $Id$
 * 
 **/

public class Login extends BasePage implements IErrorProperty
{
    private String email;
    private String password;
    private String error;
    private ICallback callback;
    private IValidationDelegate validationDelegate;

    /**
     *  The name of a cookie to store on the user's machine that will identify
     *  them next time they log in.
     *
     **/

    private static final String COOKIE_NAME = "net.sf.tapestry.vlib.Login.email";

    private final static int ONE_WEEK = 7 * 24 * 60 * 60;

    public void detach()
    {
        email = null;
        password = null;
        error = null;
        callback = null;

        super.detach();
    }

    public IValidationDelegate getValidationDelegate()
    {
        if (validationDelegate == null)
            validationDelegate = new SimpleValidationDelegate();

        return validationDelegate;
    }

    public void setEmail(String value)
    {
        email = value;
    }

    /**
     *  Gets the email address.  If not previously set, it is retrieve from
     *  the cookie (thus forming the default).
     *
     **/

    public String getEmail()
    {
        // If not set, see if a value was previously recorded in a Cookie

        if (email == null)
            email = getRequestCycle().getRequestContext().getCookieValue(COOKIE_NAME);

        return email;
    }

    public void setPassword(String value)
    {
        password = value;
    }

    public String getPassword()
    {
        return password;
    }

    public void setError(String value)
    {
        error = value;
    }

    public String getError()
    {
        return error;
    }

    protected void setErrorField(String componentId, String message)
    {
        IField field;

        field = (IField) getComponent(componentId);

        IValidationDelegate delegate = getValidationDelegate();
        delegate.setFormComponent(field);
        delegate.record(new ValidatorException(message));
    }

    public void setCallback(ICallback value)
    {
        callback = value;

        fireObservedChange("callback", value);
    }

    public ICallback getCallback()
    {
        return callback;
    }

    /**
     *  Attempts to login.  If successful, updates the application's user property
     *  and redirects to the target page (or the home page if no target page is specified).
     *
     *  <p>Clears the target page property.
     *
     *  <p>If the user name is not known, or the password is invalid, then an error
     *  message is displayed.
     *
     **/

    public void attemptLogin(IRequestCycle cycle) throws RequestCycleException
    {
        // An error, from a validation field, may already have occured.

        if (getValidationDelegate().getHasErrors() || error != null)
            return;

        VirtualLibraryEngine vengine = (VirtualLibraryEngine) getEngine();

        for (int i = 0; i < 2; i++)
        {
            try
            {
                IOperations operations = vengine.getOperations();

                Person person = operations.login(email, password);

                loginUser(person, cycle);

                break;

            }
            catch (LoginException ex)
            {
                String fieldName = ex.isPasswordError() ? "inputPassword" : "inputEmail";

                setErrorField(fieldName, ex.getMessage());
                return;
            }
            catch (RemoteException ex)
            {
                vengine.rmiFailure("Remote exception validating user.", ex, i > 0);
            }
        }
    }

    /**
     *  Sets up the {@link Person} as the logged in user, creates
     *  a cookie for thier email address (for subsequent logins),
     *  and redirects to the appropriate page ({@link MyLibrary}, or
     *  a specified page).
     *
     **/

    public void loginUser(Person person, IRequestCycle cycle)
        throws RequestCycleException, RemoteException
    {
        String email = person.getEmail();

        // Get the visit object; this will likely force the
        // creation of the visit object and an HttpSession.

        Visit visit = (Visit) getVisit();
        visit.setUser(person);

        // After logging in, go to the MyLibrary page, unless otherwise
        // specified.

        if (callback == null)
            cycle.setPage("MyLibrary");
        else
            callback.performCallback(cycle);

        // I've found that failing to set a maximum age and a path means that
        // the browser (IE 5.0 anyway) quietly drops the cookie.

        IEngine engine = getEngine();
        Cookie cookie = new Cookie(COOKIE_NAME, email);
        cookie.setPath(engine.getServletPath());
        cookie.setMaxAge(ONE_WEEK);

        // Record the user's email address in a cookie

        cycle.getRequestContext().addCookie(cookie);

        engine.forgetPage(getName());
    }
}