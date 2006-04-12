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
package tutorial.workbench.table;

import java.text.DateFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import net.sf.tapestry.BaseComponent;
import net.sf.tapestry.IRequestCycle;
import net.sf.tapestry.components.Block;
import net.sf.tapestry.contrib.table.model.ITableColumn;
import net.sf.tapestry.contrib.table.model.ITableColumnModel;
import net.sf.tapestry.contrib.table.model.ITableModel;
import net.sf.tapestry.contrib.table.model.ITableModelSource;
import net.sf.tapestry.contrib.table.model.ITableRendererSource;
import net.sf.tapestry.contrib.table.model.common.BlockTableRendererSource;
import net.sf.tapestry.contrib.table.model.ognl.ExpressionTableColumn;
import net.sf.tapestry.contrib.table.model.simple.ITableColumnEvaluator;
import net.sf.tapestry.contrib.table.model.simple.SimpleSetTableDataModel;
import net.sf.tapestry.contrib.table.model.simple.SimpleTableColumn;
import net.sf.tapestry.contrib.table.model.simple.SimpleTableColumnModel;
import net.sf.tapestry.contrib.table.model.simple.SimpleTableModel;

/**
 * @author mindbridge
 *
 */
public class LocaleSelection
	extends BaseComponent
	implements ILocaleSelectionListener
{
	// immutable
	private ITableColumnModel m_objTableColumnModel;
	private VerbosityRating m_objVerbosityRating;

	// temporary
	private Locale m_objCurrentLocale;

	/**
	 * Creates a new LocaleSelection component
	 */
	public LocaleSelection()
	{
		m_objVerbosityRating = new VerbosityRating();
	}

	/**
	 * @see net.sf.tapestry.AbstractComponent#finishLoad()
	 */
	protected void finishLoad()
	{
		super.finishLoad();

		// We delay the initialization until now, since some columns
		// rely on the completed page structure
		m_objTableColumnModel = createColumnModel();
	}

	/**
	 * Creates the columns that will be displayed by the Table component. <p>
	 * 
	 * This method demonstrates different ways to define table columns.
	 * Choose the one the fits your needs best.
	 * 
	 * @return ITableColumnModel the created column model
	 */
	private ITableColumnModel createColumnModel()
	{
		// The column value is extracted via OGNL using ExpressionTableColumn
		ITableColumn objLocaleColumn =
			new ExpressionTableColumn("Locale", "toString()", true);

		// The column value is extracted in a custom evaluator class
		ITableColumn objCurrencyColumn =
			new SimpleTableColumn("Currency", new CurrencyEvaluator(), true);

		// The entire column is defined using a custom column class
		ITableColumn objDateFormatColumn = new DateFormatColumn(new Date());

		// The column value is extracted via OGNL using ExpressionTableColumn
		// and the renderer of the column is defined in a Block
		ExpressionTableColumn objVerbosityColumn =
			new ExpressionTableColumn(
				"Verbosity",
				"@tutorial.workbench.table.VerbosityRating@calculateVerbosity(#this)",
				true);
		Block objVerbosityBlock = (Block) getComponent("blockVerbosity");
		ITableRendererSource objVerbosityRenderer =
			new BlockTableRendererSource(objVerbosityBlock);
		objVerbosityColumn.setValueRendererSource(objVerbosityRenderer);

		// The renderer of the column is defined in a Block and contains a link
		SimpleTableColumn objDeleteColumn = new SimpleTableColumn("");
		Block objDeleteBlock = (Block) getComponent("blockDelete");
		ITableRendererSource objDeleteRenderer =
			new BlockTableRendererSource(objDeleteBlock);
		objDeleteColumn.setValueRendererSource(objDeleteRenderer);

		// Create the column model out of the above columns
		return new SimpleTableColumnModel(
			new ITableColumn[] {
				objLocaleColumn,
				objCurrencyColumn,
				objDateFormatColumn,
				objVerbosityColumn,
				objDeleteColumn });
	}

	/**
	 * Creates a table model to be used by the Table component
	 * @return ITableModel the table model
	 */
	public ITableModel getInitialTableModel()
	{
		SimpleSetTableDataModel objLocaleDataModel =
			new SimpleSetTableDataModel(new HashSet());
		return new SimpleTableModel(objLocaleDataModel, m_objTableColumnModel);
	}

	/**
	 * Returns the data model used by the displayed table
	 * @return SimpleSetTableDataModel the data model
	 */
	public SimpleSetTableDataModel getDataModel()
	{
		ITableModel objTableModel =
			((ITableModelSource) getComponent("table")).getTableModel();
		SimpleTableModel objSimpleTableModel = (SimpleTableModel) objTableModel;
		return (SimpleSetTableDataModel) objSimpleTableModel.getDataModel();
	}

	/**
	 * Returns the currentLocale.
	 * @return Locale
	 */
	public Locale getCurrentLocale()
	{
		return m_objCurrentLocale;
	}

	/**
	 * Sets the currentLocale.
	 * @param currentLocale The currentLocale to set
	 */
	public void setCurrentLocale(Locale currentLocale)
	{
		m_objCurrentLocale = currentLocale;
	}

	/**
	 * Returns the verbosity of the current locale. 
	 * This is used by the Block rendering the 'Verbosity' column
	 * @return int the current locale verbosity
	 */
	public int getCurrentLocaleVerbosity()
	{
		return VerbosityRating.calculateVerbosity(getCurrentLocale());
	}

	/**
	 * @see tutorial.workbench.table.ILocaleSelectionListener#localesSelected(Locale[])
	 */
	public void localesSelected(Locale[] arrLocales)
	{
		SimpleSetTableDataModel objDataModel = getDataModel();
		objDataModel.addRows(Arrays.asList(arrLocales));
	}

	/**
	 * Generates the context that will be passed to the deleteLocale() listener 
	 * if a "remove" link is selected. <p>
	 * 
	 * This is used by the Block rendering the 'Remove' column.
	 * 
	 * @return String[] the context for the deleteLocale() listener
	 */
	public String[] getDeleteLocaleContext()
	{
		Locale objLocale = getCurrentLocale();
		return new String[] {
			objLocale.getLanguage(),
			objLocale.getCountry(),
			objLocale.getVariant()};
	}

	/**
	 * A listener invoked when a "remove" link is selected. 
	 * It removes from the data model the locale corresponding to the link. <p>
	 * 
	 * @param objCycle the request cycle
	 */
	public void deleteLocale(IRequestCycle objCycle)
	{
		Object[] arrParams = objCycle.getServiceParameters();
		Locale objLocale =
			new Locale(
				arrParams[0].toString(),
				arrParams[1].toString(),
				arrParams[2].toString());
		getDataModel().removeRow(objLocale);
	}

	/**
	 * A class defining the logic for getting the currency symbol from a locale
	 */
	private static class CurrencyEvaluator implements ITableColumnEvaluator
	{
		/**
		 * @see net.sf.tapestry.contrib.table.model.simple.ITableColumnEvaluator#getColumnValue(ITableColumn, Object)
		 */
		public Object getColumnValue(ITableColumn objColumn, Object objRow)
		{
			Locale objLocale = (Locale) objRow;
			String strCountry = objLocale.getCountry();
			if (strCountry == null || strCountry.equals(""))
				return "";

			DecimalFormatSymbols objSymbols =
				new DecimalFormatSymbols(objLocale);
			return objSymbols.getCurrencySymbol();
		}
	}

	/**
	 * A class defining a column for displaying the date format
	 */
	private static class DateFormatColumn extends SimpleTableColumn
	{
		private Date m_objDate;

		public DateFormatColumn(Date objDate)
		{
			super("Date Format", true);
			m_objDate = objDate;
		}

		public Object getColumnValue(Object objRow)
		{
			Locale objLocale = (Locale) objRow;
			DateFormat objFormat =
				DateFormat.getDateTimeInstance(
					DateFormat.LONG,
					DateFormat.LONG,
					objLocale);
			return objFormat.format(m_objDate);
		}
	}

}