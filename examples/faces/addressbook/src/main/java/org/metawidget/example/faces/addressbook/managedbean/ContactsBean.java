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

package org.metawidget.example.faces.addressbook.managedbean;

import javax.faces.model.SelectItem;

import org.metawidget.example.shared.addressbook.controller.ContactsController;

/**
 * @author Richard Kennard
 */

public class ContactsBean
	extends ContactsController {

	//
	// Private statics
	//

	private static final SelectItem[]	ALL_TITLES_SELECT_ITEMS;

	static {
		ALL_TITLES_SELECT_ITEMS = new SelectItem[ALL_TITLES.length];

		for ( int loop = 0; loop < ALL_TITLES.length; loop++ ) {
			ALL_TITLES_SELECT_ITEMS[loop] = new SelectItem( ALL_TITLES[loop] );
		}
	}

	//
	// Public methods
	//

	public SelectItem[] getAllTitlesAsSelectItems() {

		return ALL_TITLES_SELECT_ITEMS;
	}
}
