/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation", "Tapestry" 
 *    must not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache" 
 *    or "Tapestry", nor may "Apache" or "Tapestry" appear in their 
 *    name, without prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE TAPESTRY CONTRIBUTOR COMMUNITY
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.tapestry.engine;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Properties;

import org.apache.tapestry.IMessages;

/**
 *  Implementation of {@link org.apache.tapestry.IMessages}.  This is basically
 *  a wrapper around an instance of {@link Properties}.  This ensures
 *  that the properties are, in fact, read-only (which ensures that
 *  they don't have to be synchronized).
 *
 *  @author Howard Lewis Ship
 *  @version $Id$
 *  @since 2.0.4
 *
 **/

public class ComponentMessages implements IMessages
{
    private Properties _properties;
    private Locale _locale;

    public ComponentMessages(Locale locale, Properties properties)
    {
        _locale = locale;
        _properties = properties;
    }

    public String getMessage(String key, String defaultValue)
    {
        return _properties.getProperty(key, defaultValue);
    }

    public String getMessage(String key)
    {
        String result = _properties.getProperty(key);

        if (result == null)
            result = "[" + key.toUpperCase() + "]";

        return result;
    }

    public String format(String key, Object argument1, Object argument2, Object argument3)
    {
        return format(key, new Object[] { argument1, argument2, argument3 });
    }

    public String format(String key, Object argument1, Object argument2)
    {
        return format(key, new Object[] { argument1, argument2 });
    }

    public String format(String key, Object argument)
    {
        return format(key, new Object[] { argument });
    }

    public String format(String key, Object[] arguments)
    {
        String pattern = getMessage(key);

		// This ugliness is mandated for JDK 1.3 compatibility, which has a bug 
		// in MessageFormat ... the
		// pattern is applied in the constructor, using the system default Locale,
		// regardless of what locale is later specified!
		// It appears that the problem does not exist in JDK 1.4.
		
        MessageFormat messageFormat = new MessageFormat("");
        messageFormat.setLocale(_locale);
        messageFormat.applyPattern(pattern);

        return messageFormat.format(arguments);
    }

}