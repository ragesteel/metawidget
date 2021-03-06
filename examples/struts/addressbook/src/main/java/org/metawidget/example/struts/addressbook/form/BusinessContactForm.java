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

package org.metawidget.example.struts.addressbook.form;

import org.metawidget.inspector.annotation.UiComesAfter;
import org.metawidget.inspector.annotation.UiSection;

/**
 * @author Richard Kennard
 */

public class BusinessContactForm
	extends ContactForm {

	//
	// Private statics
	//

	private static final long	serialVersionUID	= 1l;

	//
	// Private members
	//

	private String				mCompany;

	private int					mNumberOfStaff;

	//
	// Public methods
	//

	@UiComesAfter( "surname" )
	public String getCompany() {

		return mCompany;
	}

	public void setCompany( String company ) {

		mCompany = company;
	}

	@UiComesAfter( "communications" )
	@UiSection( "Other" )
	public int getNumberOfStaff() {

		return mNumberOfStaff;
	}

	public void setNumberOfStaff( int numberOfStaff ) {

		mNumberOfStaff = numberOfStaff;
	}
}
