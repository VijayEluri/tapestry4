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

package com.primix.tapestry.util.io;

import java.util.*;
import java.io.*;

/**
 *  Squeezes a {@link Short}.
 *
 *  @author Howard Ship
 *  @version $Id$
 *
 */

class ShortAdaptor implements ISqueezeAdaptor
{
	private static final String PREFIX = "s";

	/**
	 *  Registers using the prefix 's'.
	 *
	 */

	public void register(DataSqueezer squeezer)
	{
		squeezer.register(PREFIX, Short.class, this);
	}

	/**
	 *  Invoked <code>toString()</code> on data (which is type {@link Short}),
	 *  and prefixs the result.
	 *
	 */

	public String squeeze(DataSqueezer squeezer, Object data)
	
	{
		return PREFIX + data.toString();
	}

	/**
	 *  Constructs a {@link Short} from the string, after stripping
	 *  the prefix.
	 *
	 */

	public Object unsqueeze(DataSqueezer squeezer, String string)
	{
		return new Short(string.substring(1));
	}

}