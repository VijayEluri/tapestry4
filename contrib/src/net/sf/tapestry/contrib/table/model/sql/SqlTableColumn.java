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
package net.sf.tapestry.contrib.table.model.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.sf.tapestry.contrib.table.model.simple.SimpleTableColumn;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @version $Id$
 * @author mindbridge
 */
public class SqlTableColumn extends SimpleTableColumn
{
	private static final Log LOG = LogFactory.getLog(SqlTableColumn.class);

	/**
	 * Creates an SqlTableColumn
	 * @param strSqlField the identifying name of the column and the SQL field it refers to
	 * @param strDisplayName the display name of the column
	 */
	public SqlTableColumn(String strSqlField, String strDisplayName)
	{
		super(strSqlField, strDisplayName);
	}

	/**
	 * Creates an SqlTableColumn
	 * @param strSqlField the identifying name of the column and the SQL field it refers to
	 * @param strDisplayName the display name of the column
	 * @param bSortable whether the column is sortable
	 */
	public SqlTableColumn(
		String strSqlField,
		String strDisplayName,
		boolean bSortable)
	{
		super(strSqlField, strDisplayName, bSortable);
	}

	/**
	 * @see net.sf.tapestry.contrib.table.model.simple.SimpleTableColumn#getColumnValue(Object)
	 */
	public Object getColumnValue(Object objRow)
	{
		try
		{
			ResultSet objRS = (ResultSet) objRow;
			Object objValue = objRS.getObject(getColumnName());
			if (objValue == null)
				objValue = "";
			return objValue;
		}
		catch (SQLException e)
		{
			LOG.error("Cannot get the value for column: " + getColumnName(), e);
			return "";
		}
	}

}