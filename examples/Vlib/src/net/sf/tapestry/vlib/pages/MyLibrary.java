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
package net.sf.tapestry.vlib.pages;

import java.rmi.RemoteException;

import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import net.sf.tapestry.ApplicationRuntimeException;
import net.sf.tapestry.IMarkupWriter;
import net.sf.tapestry.IRequestCycle;
import net.sf.tapestry.RequestCycleException;
import net.sf.tapestry.vlib.Protected;
import net.sf.tapestry.vlib.VirtualLibraryEngine;
import net.sf.tapestry.vlib.Visit;
import net.sf.tapestry.vlib.components.Browser;
import net.sf.tapestry.vlib.ejb.Book;
import net.sf.tapestry.vlib.ejb.IBookQuery;
import net.sf.tapestry.vlib.ejb.IOperations;

/**
 *  Shows a list of the user's books, allowing books to be editted or
 *  even deleted.
 *
 *  <p>Note that, unlike elsewhere, book titles do not link to the
 *  {@link ViewBook} page.  It seems to me there would be a conflict between
 *  that behavior and the edit behavior; making the book titles not be links
 *  removes the ambiguity over what happens when the book title is clicked
 *  (view vs. edit).
 *
 *  @author Howard Lewis Ship
 *  @version $Id$
 * 
 **/

public class MyLibrary extends Protected
{
    private String message;
    private IBookQuery ownedQuery;

    private Book currentBook;

    private Browser browser;
    private Browser borrowedBooksBrowser;

    public void detach()
    {
        message = null;
        ownedQuery = null;
        currentBook = null;

        super.detach();
    }

    public void finishLoad()
    {
        browser = (Browser) getComponent("browser");
    }

    /**
     *  A dirty little secret of Tapestry and page recorders:  persistent
     *  properties must be set before the render (when this method is invoked)
     *  and can't change during the render.  We force
     *  the creation of the owned book query and re-execute it whenever
     *  the MyLibrary page is rendered.
     *
     **/

    public void beginResponse(IMarkupWriter writer, IRequestCycle cycle) throws RequestCycleException
    {
        super.beginResponse(writer, cycle);

        Visit visit = (Visit) getVisit();
        Integer userPK = visit.getUserPK();

        VirtualLibraryEngine vengine = (VirtualLibraryEngine) getEngine();

        for (int i = 0; i < 2; i++)
        {
            try
            {
                IBookQuery query = getOwnedQuery();
                int count = query.ownerQuery(userPK);

                if (count != browser.getResultCount())
                    browser.initializeForResultCount(count);

                break;
            }
            catch (RemoteException ex)
            {
                vengine.rmiFailure("Remote exception accessing owned books.", ex, i > 0);

                setOwnedQuery(null);
            }
        }
    }

    public void setOwnedQuery(IBookQuery value)
    {
        ownedQuery = value;

        fireObservedChange("ownedQuery", ownedQuery);
    }

    /**
     *  Gets the query object responsible for the finding books owned by the user.
     *
     **/

    public IBookQuery getOwnedQuery()
    {
        if (ownedQuery == null)
        {
            VirtualLibraryEngine vengine = (VirtualLibraryEngine) getEngine();
            setOwnedQuery(vengine.createNewQuery());
        }

        return ownedQuery;
    }

    /**
     *  Updates the currentBook dynamic page property.
     *
     **/

    public void setCurrentBook(Book value)
    {
        currentBook = value;
    }

    public Book getCurrentBook()
    {
        return currentBook;
    }

    public boolean getOmitHolderLink()
    {
        return !currentBook.isBorrowed();
    }

    public void setMessage(String value)
    {
        message = value;
    }

    public String getMessage()
    {
        return message;
    }

    /**
     *  Listener invoked to allow a user to edit a book.
     *
     **/

    public void editBook(IRequestCycle cycle)
    {
        Object[] parameters = cycle.getServiceParameters();
        Integer bookPK = (Integer)parameters[0];
        EditBook page = (EditBook) cycle.getPage("EditBook");

        page.beginEdit(bookPK, cycle);
    }

    /**
     *  Listener invoked to allow a user to delete a book.
     *
     **/

    public void deleteBook(IRequestCycle cycle)
    {
        Object[] parameters = cycle.getServiceParameters();
        Integer bookPK = (Integer)parameters[0];

        ConfirmBookDelete page = (ConfirmBookDelete) cycle.getPage("ConfirmBookDelete");
        page.selectBook(bookPK, cycle);
    }

    private void returnBook(Integer bookPK)
    {
        VirtualLibraryEngine vengine = (VirtualLibraryEngine) getEngine();

        for (int i = 0; i < 2; i++)
        {
            try
            {
                IOperations operations = vengine.getOperations();
                Book book = operations.returnBook(bookPK);

                setMessage("Returned book: " + book.getTitle());

                break;
            }
            catch (FinderException ex)
            {
                setError("Could not return book: " + ex.getMessage());
                return;
            }
            catch (RemoteException ex)
            {
                vengine.rmiFailure("Remote exception returning book.", ex, i > 0);
            }
        }
    }

    /**
     *  Removes the book query bean.
     **/

    public void cleanupPage()
    {
        try
        {
            if (ownedQuery != null)
                ownedQuery.remove();
        }
        catch (RemoveException ex)
        {
            throw new ApplicationRuntimeException(ex);
        }
        catch (RemoteException ex)
        {
            throw new ApplicationRuntimeException(ex);
        }

        super.cleanupPage();
    }
}