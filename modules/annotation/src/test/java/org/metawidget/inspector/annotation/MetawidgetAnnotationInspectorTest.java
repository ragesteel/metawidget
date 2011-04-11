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

package org.metawidget.inspector.annotation;

import static org.metawidget.inspector.InspectionResultConstants.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import junit.framework.TestCase;

import org.metawidget.inspector.iface.InspectorException;
import org.metawidget.inspector.impl.BaseObjectInspector;
import org.metawidget.inspector.impl.BaseObjectInspectorConfig;
import org.metawidget.inspector.impl.propertystyle.Property;
import org.metawidget.test.model.annotatedaddressbook.Address;
import org.metawidget.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Richard Kennard
 */

public class MetawidgetAnnotationInspectorTest
	extends TestCase {

	//
	// Private members
	//

	private MetawidgetAnnotationInspector	mInspector;

	//
	// Public methods
	//

	@Override
	public void setUp() {

		mInspector = new MetawidgetAnnotationInspector();
	}

	public void testInspection() {

		String inspect = mInspector.inspect( new Address(), Address.class.getName() );
		internalTestInspection( XmlUtils.documentFromString( inspect ) );

		Element domInspect = mInspector.inspectAsDom( new Address(), Address.class.getName() );
		assertEquals( inspect, XmlUtils.nodeToString( domInspect, false ) );
		internalTestInspection( domInspect.getOwnerDocument() );
	}

	private void internalTestInspection( Document document ) {

		assertEquals( "inspection-result", document.getFirstChild().getNodeName() );

		// Example Entity

		Element entity = (Element) document.getFirstChild().getFirstChild();
		assertEquals( ENTITY, entity.getNodeName() );
		assertEquals( Address.class.getName(), entity.getAttribute( TYPE ) );
		assertFalse( entity.hasAttribute( NAME ) );

		// Properties

		Element property = (Element) entity.getFirstChild();
		assertEquals( PROPERTY, property.getNodeName() );
		assertEquals( "city", property.getAttribute( NAME ) );

		property = (Element) property.getNextSibling();
		assertEquals( PROPERTY, property.getNodeName() );
		assertEquals( "owner", property.getAttribute( NAME ) );
		assertEquals( TRUE, property.getAttribute( HIDDEN ) );

		property = (Element) property.getNextSibling();
		assertEquals( PROPERTY, property.getNodeName() );
		assertEquals( "postcode", property.getAttribute( NAME ) );

		property = (Element) property.getNextSibling();
		assertEquals( PROPERTY, property.getNodeName() );
		assertEquals( "state", property.getAttribute( NAME ) );
		assertEquals( "Anytown,Cyberton,Lostville,Whereverton", property.getAttribute( LOOKUP ) );

		property = (Element) property.getNextSibling();
		assertEquals( PROPERTY, property.getNodeName() );
		assertEquals( "street", property.getAttribute( NAME ) );
	}

	public void testImaginaryEntity() {

		String xml = mInspector.inspect( new Foo(), Foo.class.getName() );
		Document document = XmlUtils.documentFromString( xml );
		assertEquals( "inspection-result", document.getFirstChild().getNodeName() );
		Element entity = (Element) document.getFirstChild().getFirstChild();
		assertEquals( ENTITY, entity.getNodeName() );
		assertEquals( Foo.class.getName(), entity.getAttribute( TYPE ) );

		Element property = (Element) entity.getFirstChild().getNextSibling();
		assertEquals( PROPERTY, property.getNodeName() );
		assertEquals( "string1", property.getAttribute( NAME ) );
		assertEquals( "bar", property.getAttribute( LABEL ) );
		assertEquals( "bar1", property.getAttribute( "foo1" ) );
		assertEquals( "bar2", property.getAttribute( "foo2" ) );
		assertEquals( TRUE, property.getAttribute( READ_ONLY ) );
		assertEquals( TRUE, property.getAttribute( HIDDEN ) );
		assertEquals( "Foo", property.getAttribute( SECTION ) );
		assertEquals( TRUE, property.getAttribute( MASKED ) );
		assertEquals( TRUE, property.getAttribute( DONT_EXPAND ) );
		assertEquals( TRUE, property.getAttribute( LARGE ) );
		assertEquals( "object1", property.getAttribute( COMES_AFTER ) );
		assertTrue( 11 == property.getAttributes().getLength() );

		Element action = (Element) property.getNextSibling();
		assertEquals( ACTION, action.getNodeName() );
		assertEquals( "doNothing", action.getAttribute( NAME ) );
		assertEquals( "Bar", action.getAttribute( SECTION ) );
		assertEquals( "string1", action.getAttribute( COMES_AFTER ) );
		assertTrue( 3 == action.getAttributes().getLength() );

		assertTrue( null == action.getNextSibling() );
	}

	public void testLookup() {

		MetawidgetAnnotationInspector inspector = new MetawidgetAnnotationInspector();
		Document document = XmlUtils.documentFromString( inspector.inspect( new Foo(), Foo.class.getName() ) );

		assertEquals( "inspection-result", document.getFirstChild().getNodeName() );

		// Entity

		Element entity = (Element) document.getFirstChild().getFirstChild();
		assertEquals( ENTITY, entity.getNodeName() );
		assertEquals( Foo.class.getName(), entity.getAttribute( TYPE ) );
		assertFalse( entity.hasAttribute( NAME ) );

		// Properties

		Element property = (Element) entity.getFirstChild();
		assertEquals( PROPERTY, property.getNodeName() );
		assertEquals( "object1", property.getAttribute( NAME ) );
		assertEquals( "foo\\,,bar", property.getAttribute( LOOKUP ) );
		assertEquals( TRUE, property.getAttribute( REQUIRED ) );
		assertEquals( TRUE, property.getAttribute( WIDE ) );
		assertTrue( property.getAttributes().getLength() == 4 );

		property = (Element) property.getNextSibling();
		assertEquals( "string1", property.getAttribute( NAME ) );
		assertEquals( "bar", property.getAttribute( LABEL ) );
		assertEquals( "bar1", property.getAttribute( "foo1" ) );
		assertEquals( "bar2", property.getAttribute( "foo2" ) );
		assertEquals( TRUE, property.getAttribute( HIDDEN ) );
		assertEquals( TRUE, property.getAttribute( READ_ONLY ) );
		assertEquals( TRUE, property.getAttribute( DONT_EXPAND ) );
		assertEquals( "Foo", property.getAttribute( SECTION ) );
		assertEquals( TRUE, property.getAttribute( MASKED ) );
		assertEquals( TRUE, property.getAttribute( LARGE ) );
		assertEquals( "object1", property.getAttribute( COMES_AFTER ) );

		assertTrue( property.getAttributes().getLength() == 11 );
	}

	public void testBadAction() {

		try {
			mInspector.inspect( new BadAction1(), BadAction1.class.getName() );
			assertTrue( false );
		} catch ( InspectorException e ) {
			assertEquals( "@UiAction public void org.metawidget.inspector.annotation.MetawidgetAnnotationInspectorTest$BadAction1.doNothing(java.lang.String) must not take any parameters", e.getMessage() );
		}
	}

	public void testInspectString() {

		// Should 'short circuit' and return null, as an optimization for CompositeInspector

		assertTrue( null == mInspector.inspect( "foo", String.class.getName() ) );

		// Should gather annotations from parent

		String xml = mInspector.inspect( new Foo(), Foo.class.getName(), "string1" );
		Document document = XmlUtils.documentFromString( xml );
		assertEquals( "inspection-result", document.getFirstChild().getNodeName() );
		Element entity = (Element) document.getFirstChild().getFirstChild();
		assertEquals( ENTITY, entity.getNodeName() );
		assertEquals( String.class.getName(), entity.getAttribute( TYPE ) );
		assertEquals( "string1", entity.getAttribute( NAME ) );
		assertEquals( "bar", entity.getAttribute( LABEL ) );
	}

	/**
	 * Test that parent properties <em>and</em> parent traits get merged in properly.
	 */

	public void testInspectParent() {

		MetawidgetAnnotationInspector inspector = new MetawidgetAnnotationInspector();
		Document document = XmlUtils.documentFromString( inspector.inspect( new PropertyAndTraitAnnotation(), PropertyAndTraitAnnotation.class.getName(), "foo" ) );

		assertEquals( "inspection-result", document.getFirstChild().getNodeName() );

		Element entity = (Element) document.getFirstChild().getFirstChild();
		assertEquals( ENTITY, entity.getNodeName() );
		assertEquals( String.class.getName(), entity.getAttribute( TYPE ) );
		assertEquals( "foo", entity.getAttribute( NAME ) );
		assertEquals( TRUE, entity.getAttribute( MASKED ) );
		assertEquals( "Foo", entity.getAttribute( LABEL ) );
		assertTrue( 4 == entity.getAttributes().getLength() );

		assertTrue( 0 == entity.getChildNodes().getLength() );

		// Test null parent doesn't throw NullPointerException

		assertTrue( null == inspector.inspect( null, PropertyAndTraitAnnotation.class.getName(), "foo" ) );
	}

	@SuppressWarnings( "unchecked" )
	public void testNullPropertyStyle()
		throws Exception {

		MetawidgetAnnotationInspector inspector = new MetawidgetAnnotationInspector();
		Method getPropertiesMethod = BaseObjectInspector.class.getDeclaredMethod( "getProperties", Class.class );
		getPropertiesMethod.setAccessible( true );

		// Should fail hard

		try {
			assertTrue( ( (Map<String, Property>) getPropertiesMethod.invoke( inspector, (Object) null ) ).isEmpty() );
			assertTrue( false );
		} catch ( InvocationTargetException e ) {
			assertTrue( e.getCause() instanceof NullPointerException );
		}

		// Should fail gracefully

		inspector = new MetawidgetAnnotationInspector( new BaseObjectInspectorConfig().setPropertyStyle( null ) );
		assertTrue( ( (Map<String, Property>) getPropertiesMethod.invoke( inspector, (Object) null ) ).isEmpty() );
	}

	@SuppressWarnings( "unchecked" )
	public void testNullActionStyle()
		throws Exception {

		MetawidgetAnnotationInspector inspector = new MetawidgetAnnotationInspector();
		Method getActionsMethod = BaseObjectInspector.class.getDeclaredMethod( "getActions", Class.class );
		getActionsMethod.setAccessible( true );

		// Should fail hard

		try {
			assertTrue( ( (Map<String, Property>) getActionsMethod.invoke( inspector, (Object) null ) ).isEmpty() );
			assertTrue( false );
		} catch ( InvocationTargetException e ) {
			assertTrue( e.getCause() instanceof NullPointerException );
		}

		// Should fail gracefully

		BaseObjectInspectorConfig config = new BaseObjectInspectorConfig();
		config.setActionStyle( null );

		inspector = new MetawidgetAnnotationInspector( config );
		assertTrue( ( (Map<String, Property>) getActionsMethod.invoke( inspector, (Object) null ) ).isEmpty() );
	}

	//
	// Inner class
	//

	public static class PropertyAndTraitAnnotation {

		@UiMasked
		@UiLabel( "Foo" )
		public String	foo;
	}

	public static class Foo {

		@UiRequired
		@UiLookup( value = { "foo,", "bar" } )
		@UiWide
		public Object	object1;

		@UiLabel( "bar" )
		@UiAttributes( { @UiAttribute( name = "foo1", value = "bar1" ), @UiAttribute( name = "foo2", value = "bar2" ) } )
		@UiHidden
		@UiReadOnly
		@UiDontExpand
		@UiSection( "Foo" )
		@UiMasked
		@UiComesAfter( "object1" )
		@UiLarge
		public String	string1;

		@UiAction
		@UiComesAfter( "string1" )
		@UiSection( "Bar" )
		public void doNothing() {

			// Do nothing
		}
	}

	public static class BadAction1 {

		/**
		 * @param foo
		 */

		@UiAction
		public void doNothing( String foo ) {

			// Do nothing
		}
	}
}