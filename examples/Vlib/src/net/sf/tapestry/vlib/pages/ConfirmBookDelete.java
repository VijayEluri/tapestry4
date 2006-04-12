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
import net.sf.tapestry.IRequestCycle;
import net.sf.tapestry.html.BasePage;
import net.sf.tapestry.vlib.VirtualLibraryEngine;
import net.sf.tapestry.vlib.ejb.Book;
import net.sf.tapestry.vlib.ejb.IOperations;

/**
 *  Presents a confirmation page before deleting a book.  If the user
 *  selects "yes", the book is deleted; otherwise the user is returned
 *  to the {@link MyLibrary} page.
 *
 *  @author Howard Lewis Ship
 *  @version $Id$
 * 
 **/

public class ConfirmBookDelete extends BasePage
{
    private String bookTitle;
    private Integer bookPK;

    public void detach()
    {
        super.detach();

        bookTitle = null;
        bookPK = null;
    }

    public String getBookTitle()
    {
        return bookTitle;
    }

    public Integer getBookPrimaryKey()
    {
        return bookPK;
    }

    /** 
     * Invoked (by {@link MyLibrary}) to select a book to be
     * deleted.  This method sets the temporary page properties
     * (bookPrimaryKey and bookTitle) and invoked {@link IRequestCycle#setPage(IPage)}.
     *
     **/

    public void selectBook(Integer bookPK, IRequestCycle cycle)
    {
        this.bookPK = bookPK;

        VirtualLibraryEngine vengine = (VirtualLibraryEngine) getEngine();

        for (int i = 0; i < 2; i++)
        {
            try
            {
                IOperations operations = vengine.getOperations();
                Book book = operations.getBook(bookPK);

                bookTitle = book.getTitle();

                break;
            }
            catch (FinderException ex)
            {
                throw new ApplicationRuntimeException(ex);
            }
            catch (RemoteException ex)
            {
                vengine.rmiFailure("Remote exception reading read book #" + bookPK + ".", ex, i > 0);
            }
        }

        cycle.setPage(this);
    }

    /**
     *  Hooked up to the yes component, this actually deletes the book.
     *
     **/

    public void deleteBook(IRequestCycle cycle)
    {
        Object[] parameters = cycle.getServiceParameters();
        Integer bookPK =(Integer)parameters[0];

        VirtualLibraryEngine vengine = (VirtualLibraryEngine) getEngine();
        Book book = null;

        for (int i = 0; i < 2; i++)
        {
            try
            {
                IOperations operations = vengine.getOperations();

                book = operations.deleteBook(bookPK);

                break;
            }
            catch (RemoveException ex)
            {
                throw new ApplicationRuntimeException(ex);
            }
            catch (RemoteException ex)
            {
                vengine.rmiFailure("Remote exception deleting book #" + bookPK + ".", ex, i > 0);
            }
        }

        MyLibrary myLibrary = (MyLibrary) cycle.getPage("MyLibrary");

        myLibrary.setMessage("Deleted book: " + book.getTitle());

        cycle.setPage(myLibrary);
    }
}