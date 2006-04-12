/*
 * Tapestry Web Application Framework
 * Copyright (c) 2000-2001 by Howard Lewis Ship
 *
 * Howard Lewis Ship
 * http://sf.net/projects/tapestry
 * mailto:hship@users.sf.net
 *
 * This library is free software.
 *
 * You may redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation.
 *
 * Version 2.1 of the license should be included with this distribution in
 * the file LICENSE, as well as License.html. If the license is not
 * included with this distribution, you may find a copy at the FSF web
 * site at 'www.gnu.org' or 'www.fsf.org', or you may write to the
 * Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139 USA.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied waranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 */

package com.primix.vlib.pages;

import com.primix.tapestry.components.*;
import com.primix.tapestry.spec.*;
import com.primix.tapestry.*;
import com.primix.vlib.ejb.*;
import com.primix.vlib.*;
import com.primix.vlib.components.*;
import javax.ejb.*;
import java.util.*;
import java.rmi.*;
import javax.rmi.*;

/**
 *  Shows a list of the user's books, allowing books to be editted or
 *  even deleted.
 *
 *  <p>Note that, unlike elsewhere, book titles do not link to the
 * {@link ViewBook} page.  It seems to me there would be a conflict between
 * that behavior and the edit behavior; making the book titles not be links
 *  removes the ambiguity over what happens when the book title is clicked
 *  (view vs. edit).
 *
 * @author Howard Ship
 * @version $Id$
 */

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

	public void finishLoad(
		IPageLoader loader,
		ComponentSpecification specification)
		throws PageLoaderException
	{
		super.finishLoad(loader, specification);

		browser = (Browser) getComponent("browser");
	}

	/**
	 *  A dirty little secret of Tapestry and page recorders:  persistent
	 *  properties must be set before the render (when this method is invoked)
	 *  and can't change during the render.  We force
	 *  the creation of the owned book query and re-execute it whenever
	 *  the MyLibrary page is rendered.
	 *
	 */

	public void beginResponse(IResponseWriter writer, IRequestCycle cycle)
		throws RequestCycleException
	{
		super.beginResponse(writer, cycle);

		Visit visit = (Visit) getVisit();
		Integer userPK = visit.getUserPK();

		VirtualLibraryEngine vengine = (VirtualLibraryEngine) engine;

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
	 */

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
	 */

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
	 */

	public void editBook(String[] context, IRequestCycle cycle)
	{
		Integer bookPK = new Integer(context[0]);
		EditBook page = (EditBook) cycle.getPage("EditBook");

		page.beginEdit(bookPK, cycle);
	}

	/**
	 *  Listener invoked to allow a user to delete a book.
	 *
	 */

	public void deleteBook(String[] context, IRequestCycle cycle)
	{
		Integer bookPK = new Integer(context[0]);

		ConfirmBookDelete page = (ConfirmBookDelete) cycle.getPage("ConfirmBookDelete");
		page.selectBook(bookPK, cycle);
	}

	private void returnBook(Integer bookPK)
	{
		VirtualLibraryEngine vengine = (VirtualLibraryEngine) engine;

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
	 */

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