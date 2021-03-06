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

package org.metawidget.example.gwt.addressbook.client.ui.converter;

import org.metawidget.gwt.client.widgetprocessor.binding.simple.BaseConverter;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Richard Kennard
 */

public class EnumConverter<T extends Enum<T>>
	extends BaseConverter<T> {

	//
	// Private members
	//

	private Class<T>	mEnum;

	//
	// Constructor
	//

	public EnumConverter( Class<T> anEnum ) {

		mEnum = anEnum;
	}

	//
	// Public methods
	//

	public T convertFromWidget( Widget widget, Object value, Class<?> type ) {

		if ( value == null || "".equals( value ) ) {
			return null;
		}

		return Enum.valueOf( mEnum, (String) value );
	}

	@Override
	public Object convertForWidget( Widget widget, T value ) {

		if ( value == null ) {
			return null;
		}

		if ( widget instanceof Label ) {
			return value.toString();
		}

		return value.name();
	}
}
