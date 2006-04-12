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

package com.primix.tapestry;

/**
 *  General wrapper for any exception (normal or runtime) that may occur during
 *  a request cycle.
 *
 *  @author Howard Ship
 *  @version $Id$
 */

public class ApplicationRuntimeException extends RuntimeException
{
	private Throwable rootCause;

	public ApplicationRuntimeException(Throwable rootCause)
	{
		super(rootCause.getMessage());

		this.rootCause = rootCause;
	}

	public ApplicationRuntimeException(String message)
	{
		super(message);
	}

	public ApplicationRuntimeException(String message, Throwable rootCause)
	{
		super(message);

		this.rootCause = rootCause;
	}

	public Throwable getRootCause()
	{
		return rootCause;
	}
}