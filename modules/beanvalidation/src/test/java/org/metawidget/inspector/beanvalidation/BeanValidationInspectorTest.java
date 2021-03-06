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

package org.metawidget.inspector.beanvalidation;

import static org.metawidget.inspector.InspectionResultConstants.*;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import junit.framework.TestCase;

import org.metawidget.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Richard Kennard
 */

public class BeanValidationInspectorTest
	extends TestCase {

	//
	// Public methods
	//

	public void testInspection() {

		BeanValidationInspector inspector = new BeanValidationInspector();
		Document document = XmlUtils.documentFromString( inspector.inspect( new Foo(), Foo.class.getName() ) );

		assertEquals( "inspection-result", document.getFirstChild().getNodeName() );

		// Entity

		Element entity = (Element) document.getFirstChild().getFirstChild();
		assertEquals( ENTITY, entity.getNodeName() );
		assertEquals( Foo.class.getName(), entity.getAttribute( TYPE ) );
		assertFalse( entity.hasAttribute( NAME ) );

		// Properties

		Element property = XmlUtils.getChildWithAttributeValue( entity, NAME, "bar" );
		assertEquals( PROPERTY, property.getNodeName() );
		assertEquals( TRUE, property.getAttribute( REQUIRED ) );
		assertTrue( 2 == property.getAttributes().getLength() );

		property = XmlUtils.getChildWithAttributeValue( entity, NAME, "baz" );
		assertEquals( PROPERTY, property.getNodeName() );
		assertEquals( "1", property.getAttribute( MAXIMUM_INTEGER_DIGITS ) );
		assertEquals( "2", property.getAttribute( MAXIMUM_FRACTIONAL_DIGITS ) );
		assertTrue( 3 == property.getAttributes().getLength() );

		property = XmlUtils.getChildWithAttributeValue( entity, NAME, "range" );
		assertEquals( PROPERTY, property.getNodeName() );
		assertEquals( "1", property.getAttribute( MINIMUM_VALUE ) );
		assertEquals( "99", property.getAttribute( MAXIMUM_VALUE ) );
		assertEquals( "2", property.getAttribute( MINIMUM_LENGTH ) );
		assertEquals( "25", property.getAttribute( MAXIMUM_LENGTH ) );
		assertTrue( 5 == property.getAttributes().getLength() );
	}

	//
	// Inner class
	//

	public static class Foo {

		@NotNull
		public String	bar;

		@Digits( integer = 1, fraction = 2 )
		public String	baz;

		@Min( 1 )
		@Max( 99 )
		@Size( min = 2, max = 25 )
		public int		range;
	}
}
