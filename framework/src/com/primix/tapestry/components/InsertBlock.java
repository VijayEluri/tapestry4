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

package com.primix.tapestry.components;

import com.primix.tapestry.*;

/**
 *  Renders the text and components wrapped by a {@link Block} component.
 *
 *  <p>It is possible for an InsertBlock to obtain a Block
 *  from a page <em>other than</em> the render page.  This works, even when
 *  the Block contains links, forms and form components.  The action and
 *  direct services will create URLs that properly address this situation.
 *
 *  <p>However, because the rendering page can't know
 *  ahead of time about these foriegn Blocks,
 *  {@link com.primix.tapestry.event.PageRenderListener} methods
 *  (for components and objects of the foriegn page)
 *  via InsertBlock will <em>not</em> be executed.  This specifically
 *  affects the methods of the {@link com.primix.tapestry.event.PageRenderListener} 
 *  interface.
 *
 * <p>
 * <table border=1>
 * <tr>
 *    <th>Parameter</th>
 *    <th>Type</th>
 *    <th>Read / Write </th>
 *    <th>Required</th>
 *    <th>Default</th>
 *    <th>Description</th>
 * </tr>
 *
 * <tr>
 *  <td>block</td>
 *  <td>{@link Block}</td>
 *  <td>R</td>
 *  <td>no</td>
 *  <td>&nbsp;</td>
 *  <td>The {@link Block} whose contents are to be rendered.</td>
 * </tr>
 *
 *  </table>
 
 * <p>Informal parameters are not allowed.  The component may not have a body.
 *
 * @author Howard Ship
 * @version $Id$
 */

public class InsertBlock extends AbstractComponent
{
	private IBinding blockBinding;

	public void setBlockBinding(IBinding value)
	{
		blockBinding = value;
	}

	public IBinding getBlockBinding()
	{
		return blockBinding;
	}

	/**
	 *  If the block parameter is bound and not null,
	 *  then {@link IComponent#renderWrapped(IResponseWriter, IRequestCycle)}
	 *  is invoked on it.
	 *
	 */

	public void render(IResponseWriter writer, IRequestCycle cycle)
		throws RequestCycleException
	{
		if (blockBinding == null)
			return;

		Block block = (Block) blockBinding.getObject("block", Block.class);

		if (block != null)
			block.renderWrapped(writer, cycle);
	}
}