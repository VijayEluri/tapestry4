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

package org.apache.tapestry.contrib.table.model.simple;

import java.io.Serializable;

import org.apache.tapestry.contrib.table.model.ITablePagingState;

/**
 * A minimal implementation of 
 * {@link org.apache.tapestry.contrib.table.model.ITablePagingState}.
 * 
 * @version $Id$
 * @author mindbridge
 */
public class SimpleTablePagingState implements ITablePagingState, Serializable
{
    private final static int DEFAULT_PAGE_SIZE = 10;

    private int m_nPageSize;
    private int m_nCurrentPage;

    public SimpleTablePagingState()
    {
        m_nPageSize = DEFAULT_PAGE_SIZE;
        m_nCurrentPage = 0;
    }

    /**
     * Returns the pageSize.
     * @return int
     */
    public int getPageSize()
    {
        return m_nPageSize;
    }

    /**
     * Sets the pageSize.
     * @param pageSize The pageSize to set
     */
    public void setPageSize(int pageSize)
    {
        m_nPageSize = pageSize;
    }

    /**
     * Returns the currentPage.
     * @return int
     */
    public int getCurrentPage()
    {
        return m_nCurrentPage;
    }

    /**
     * Sets the currentPage.
     * @param currentPage The currentPage to set
     */
    public void setCurrentPage(int currentPage)
    {
        m_nCurrentPage = currentPage;
    }

}