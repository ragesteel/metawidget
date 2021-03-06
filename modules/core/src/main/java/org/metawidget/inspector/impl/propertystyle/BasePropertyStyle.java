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

package org.metawidget.inspector.impl.propertystyle;

import java.util.Map;

import org.metawidget.inspector.impl.BaseTraitStyle;
import org.metawidget.inspector.impl.BaseTraitStyleConfig;

/**
 * Convenience implementation for PropertyStyles.
 *
 * @author Richard Kennard
 */

public abstract class BasePropertyStyle
	extends BaseTraitStyle<Property>
	implements PropertyStyle {

	//
	// Constructor
	//

	protected BasePropertyStyle( BaseTraitStyleConfig config ) {

		super( config );
	}

	//
	// Public methods
	//

	public Map<String, Property> getProperties( Class<?> clazz ) {

		return getTraits( clazz );
	}

	//
	// Protected methods
	//

	@Override
	protected final Map<String, Property> getUncachedTraits( Class<?> clazz ) {

		return inspectProperties( clazz );
	}

	/**
	 * @return the properties of the given class. Never null.
	 */

	protected abstract Map<String, Property> inspectProperties( Class<?> clazz );
}
