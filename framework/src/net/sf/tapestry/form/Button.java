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
package net.sf.tapestry.form;

import net.sf.tapestry.IBinding;
import net.sf.tapestry.IForm;
import net.sf.tapestry.IMarkupWriter;
import net.sf.tapestry.IRequestCycle;
import net.sf.tapestry.RequestCycleException;

/**
 *  Implements a component that manages an HTML &lt;input type=button&gt; form element.
 *
 *  [<a href="../../../../../ComponentReference/Button.html">Component Reference</a>]
 *  
 *  <p>This component is useful for attaching JavaScript onclick event handlers.
 *
 *  @author Howard Lewis Ship
 *  @author Paul Geerts
 *  @author Malcolm Edgar
 *  @version $Id$
 **/
public class Button extends AbstractFormComponent
{
	private String _label;
	private boolean _disabled;
	private IBinding _selectedBinding;
	private String _name;

	public String getName()
	{
		return _name;
	}

	public void setSelectedBinding(IBinding value)
	{
		_selectedBinding = value;
	}

	public IBinding getSelectedBinding()
	{
		return _selectedBinding;
	}

	protected void renderComponent(IMarkupWriter writer, IRequestCycle cycle)
		throws RequestCycleException
	{
		IForm form = getForm(cycle);

		boolean rewinding = form.isRewinding();

		_name = form.getElementId(this);

		if (rewinding)
		{
			return;
		}

		writer.beginEmpty("input");
		writer.attribute("type", "button");
		writer.attribute("name", _name);

		if (_disabled)
		{
			writer.attribute("disabled");
		}
		if (_label != null)
		{
			writer.attribute("value", _label);
		}
		generateAttributes(writer, cycle);

		writer.closeTag();
	}

	public String getLabel()
	{
		return _label;
	}

	public void setLabel(String label)
	{
		_label = label;
	}

	public boolean isDisabled()
	{
		return _disabled;
	}

	public void setDisabled(boolean disabled)
	{
		_disabled = disabled;
	}
}