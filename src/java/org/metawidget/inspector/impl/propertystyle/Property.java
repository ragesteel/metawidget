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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Interface over getter/setter-based, field-based, or Groovy-based properties, so that
 * <code>Inspectors</code> can treat them all the same.
 *
 * @author Richard Kennard
 */

public interface Property
{
	//
	//
	// Methods
	//
	//

	String getName();

	Class<?> getType();

	boolean isReadable();

	/**
	 * Read the given property for the given object.
	 * <p>
	 * Used by PropertyInspector to determine subtypes, and by BasePropertyInspector to traverse
	 * the objects graph.
	 */

	Object read( Object obj );

	boolean isWritable();

	<T extends Annotation> T getAnnotation( Class<T> annotation );

	boolean isAnnotationPresent( Class<? extends Annotation> annotation );

	Type getGenericType();
}