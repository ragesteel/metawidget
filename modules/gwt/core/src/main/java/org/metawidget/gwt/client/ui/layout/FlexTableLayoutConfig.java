// Metawidget
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package org.metawidget.gwt.client.ui.layout;

import org.metawidget.layout.iface.LayoutException;
import org.metawidget.util.simple.ObjectUtils;

/**
 * Configures a FlexTableLayout prior to use. Once instantiated, Layouts are immutable.
 *
 * @author Richard Kennard
 */

public class FlexTableLayoutConfig {

	//
	// Private members
	//

	private int			mNumberOfColumns	= 1;

	private String		mTableStyleName;

	private String[]	mColumnStyleNames;

	private String		mFooterStyleName;

	//
	// Public methods
	//

	/**
	 * @return this, as part of a fluent interface
	 */

	public FlexTableLayoutConfig setNumberOfColumns( int numberOfColumns ) {

		if ( numberOfColumns < 0 ) {
			throw LayoutException.newException( "numberOfColumns must be >= 0" );
		}

		mNumberOfColumns = numberOfColumns;

		return this;
	}

	/**
	 * @return this, as part of a fluent interface
	 */

	public FlexTableLayoutConfig setTableStyleName( String tableStyleName ) {

		mTableStyleName = tableStyleName;

		return this;
	}

	/**
	 * Array of CSS style classes to apply to table columns in order of: label column, component
	 * column, required column.
	 *
	 * @return this, as part of a fluent interface
	 */

	public FlexTableLayoutConfig setColumnStyleNames( String... columnStyleNames ) {

		mColumnStyleNames = columnStyleNames;

		return this;
	}

	/**
	 * @return this, as part of a fluent interface
	 */

	public FlexTableLayoutConfig setFooterStyleName( String footerStyleName ) {

		mFooterStyleName = footerStyleName;

		return this;
	}

	@Override
	public boolean equals( Object that ) {

		if ( this == that ) {
			return true;
		}

		if ( !ObjectUtils.nullSafeClassEquals( this, that )) {
			return false;
		}

		if ( mNumberOfColumns != ( (FlexTableLayoutConfig) that ).mNumberOfColumns ) {
			return false;
		}

		if ( !ObjectUtils.nullSafeEquals( mTableStyleName, ( (FlexTableLayoutConfig) that ).mTableStyleName ) ) {
			return false;
		}

		if ( !ObjectUtils.nullSafeEquals( mColumnStyleNames, ( (FlexTableLayoutConfig) that ).mColumnStyleNames ) ) {
			return false;
		}

		if ( !ObjectUtils.nullSafeEquals( mFooterStyleName, ( (FlexTableLayoutConfig) that ).mFooterStyleName ) ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {

		int hashCode = 1;
		hashCode = 31 * hashCode + mNumberOfColumns;
		hashCode = 31 * hashCode + ObjectUtils.nullSafeHashCode( mTableStyleName );
		hashCode = 31 * hashCode + ObjectUtils.nullSafeHashCode( mColumnStyleNames );
		hashCode = 31 * hashCode + ObjectUtils.nullSafeHashCode( mFooterStyleName );

		return hashCode;
	}

	//
	// Protected methods
	//

	protected int getNumberOfColumns() {

		return mNumberOfColumns;
	}

	protected String getTableStyleName() {

		return mTableStyleName;
	}

	protected String[] getColumnStyleNames() {

		return mColumnStyleNames;
	}

	protected String getFooterStyleName() {

		return mFooterStyleName;
	}
}
