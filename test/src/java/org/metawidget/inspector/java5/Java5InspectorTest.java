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

package org.metawidget.inspector.java5;

import static org.metawidget.inspector.InspectionResultConstants.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.metawidget.inspector.composite.CompositeInspector;
import org.metawidget.inspector.composite.CompositeInspectorConfig;
import org.metawidget.inspector.iface.Inspector;
import org.metawidget.inspector.java5.Java5Inspector;
import org.metawidget.inspector.propertytype.PropertyTypeInspectionResultConstants;
import org.metawidget.inspector.propertytype.PropertyTypeInspector;
import org.metawidget.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Richard Kennard
 */

public class Java5InspectorTest
	extends TestCase
{
	//
	// Public methods
	//

	public void testInspection()
	{
		Inspector inspector = new Java5Inspector();

		Document document = XmlUtils.documentFromString( inspector.inspect( new Bar(), Bar.class.getName() ));

		assertTrue( "inspection-result".equals( document.getFirstChild().getNodeName() ) );

		// Entity

		Element entity = (Element) document.getFirstChild().getFirstChild();
		assertTrue( ENTITY.equals( entity.getNodeName() ) );
		assertTrue( Bar.class.getName().equals( entity.getAttribute( TYPE ) ) );
		assertTrue( !entity.hasAttribute( NAME ) );

		// Properties

		Element property = XmlUtils.getChildWithAttributeValue( entity, NAME, "foo" );
		assertTrue( PROPERTY.equals( property.getNodeName() ) );
		assertTrue( Foo.class.getName().equals( property.getAttribute( TYPE ) ) );
		assertTrue( "FOO1,FOO2".equals( property.getAttribute( LOOKUP ) ) );
		assertTrue( "foo1,foo2".equals( property.getAttribute( LOOKUP_LABELS ) ) );
		assertTrue( 4 == property.getAttributes().getLength() );

		property = XmlUtils.getChildWithAttributeValue( entity, NAME, "baz" );
		assertTrue( PROPERTY.equals( property.getNodeName() ) );

		String genericArguments = Set.class.getName() + "<" + String.class.getName() + ">," + List.class.getName() + "<" + Set.class.getName() + "<" + Date.class.getName() + ">>";
		assertTrue( genericArguments.equals( property.getAttribute( PARAMETERIZED_TYPE ) ) );

		// Check there are no more properties (eg. getClass)

		assertTrue( entity.getChildNodes().getLength() == 2 );

		// Test with an enum instance

		Bar bar = new Bar();
		bar.foo = Foo.FOO1;
		document = XmlUtils.documentFromString( inspector.inspect( bar, Bar.class.getName() ));
		assertTrue( "inspection-result".equals( document.getFirstChild().getNodeName() ) );
		entity = (Element) document.getFirstChild().getFirstChild();
		property = XmlUtils.getChildWithAttributeValue( entity, NAME, "foo" );
		assertTrue( PROPERTY.equals( property.getNodeName() ) );
		assertTrue( Foo.class.getName().equals( property.getAttribute( TYPE ) ) );
		assertTrue( "FOO1,FOO2".equals( property.getAttribute( LOOKUP ) ) );
		assertTrue( "foo1,foo2".equals( property.getAttribute( LOOKUP_LABELS ) ) );
		assertTrue( 4 == property.getAttributes().getLength() );

		// Test pointed directly at an enum

		document = XmlUtils.documentFromString( inspector.inspect( Foo.FOO1, Foo.class.getName() ));
		assertTrue( "inspection-result".equals( document.getFirstChild().getNodeName() ) );
		entity = (Element) document.getFirstChild().getFirstChild();
		assertTrue( ENTITY.equals( entity.getNodeName() ) );
		assertTrue( Foo.class.getName().equals( entity.getAttribute( TYPE ) ) );
		assertTrue( "FOO1,FOO2".equals( entity.getAttribute( LOOKUP ) ) );
		assertTrue( "foo1,foo2".equals( entity.getAttribute( LOOKUP_LABELS ) ) );
		assertTrue( 3 == entity.getAttributes().getLength() );
		assertTrue( !entity.hasChildNodes() );

		// Test pointed directly at an empty enum via a parent

		document = XmlUtils.documentFromString( inspector.inspect( new Bar(), Bar.class.getName(), "foo" ));
		assertTrue( "inspection-result".equals( document.getFirstChild().getNodeName() ) );
		assertTrue( ENTITY.equals( entity.getNodeName() ) );
		assertTrue( Foo.class.getName().equals( entity.getAttribute( TYPE ) ) );
		assertTrue( "FOO1,FOO2".equals( entity.getAttribute( LOOKUP ) ) );
		assertTrue( "foo1,foo2".equals( entity.getAttribute( LOOKUP_LABELS ) ) );
		assertTrue( 3 == entity.getAttributes().getLength() );
		assertTrue( !entity.hasChildNodes() );

		// Test an enum with PropertyTypeInspector

		inspector = new CompositeInspector( new CompositeInspectorConfig().setInspectors( new PropertyTypeInspector(), new Java5Inspector() ));
		document = XmlUtils.documentFromString( inspector.inspect( bar, Bar.class.getName() ));
		assertTrue( "inspection-result".equals( document.getFirstChild().getNodeName() ) );
		entity = (Element) document.getFirstChild().getFirstChild();
		property = XmlUtils.getChildWithAttributeValue( entity, NAME, "foo" );
		assertTrue( PROPERTY.equals( property.getNodeName() ) );
		assertTrue( Foo.class.getName().equals( property.getAttribute( TYPE ) ) );
		assertTrue( Foo.FOO1.getClass().getName().equals( property.getAttribute( PropertyTypeInspectionResultConstants.ACTUAL_CLASS ) ) );
		assertTrue( "FOO1,FOO2".equals( property.getAttribute( LOOKUP ) ) );
		assertTrue( "foo1,foo2".equals( property.getAttribute( LOOKUP_LABELS ) ) );
		assertTrue( 5 == property.getAttributes().getLength() );
	}

	public void testInspectString()
	{
		Java5Inspector inspector = new Java5Inspector();

		// Should 'short circuit' and return null, as an optimization for CompositeInspector

		assertTrue( null == inspector.inspect( "foo", String.class.getName() ));
	}

	//
	// Inner classes
	//

	protected enum Foo
	{
		FOO1
		{
			@Override
			public String toString()
			{
				return "foo1";
			}
		},

		FOO2
		{
			@Override
			public String toString()
			{
				return "foo2";
			}
		}
	}

	protected static class Bar
	{
		public Foo									foo;

		public Map<Set<String>, List<Set<Date>>>	baz;
	}
}