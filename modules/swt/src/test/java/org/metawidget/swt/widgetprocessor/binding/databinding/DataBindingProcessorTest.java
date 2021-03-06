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

package org.metawidget.swt.widgetprocessor.binding.databinding;

import junit.framework.TestCase;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Text;
import org.metawidget.inspector.annotation.UiSection;
import org.metawidget.swt.SwtMetawidget;
import org.metawidget.swt.SwtMetawidgetTests;
import org.metawidget.swt.layout.GridLayout;
import org.metawidget.swt.layout.TabFolderLayoutDecorator;
import org.metawidget.swt.layout.TabFolderLayoutDecoratorConfig;
import org.metawidget.swt.widgetbuilder.SwtWidgetBuilder;
import org.metawidget.swt.widgetprocessor.binding.databinding.DataBindingProcessor.ConvertFromTo;
import org.metawidget.util.MetawidgetTestUtils;

/**
 * @author Richard Kennard
 */

public class DataBindingProcessorTest
	extends TestCase {

	//
	// Public methods
	//

	public void testConfig() {

		MetawidgetTestUtils.testEqualsAndHashcode( DataBindingProcessorConfig.class, new DataBindingProcessorConfig() {
			// subclass
		} );
		MetawidgetTestUtils.testEqualsAndHashcode( new ConvertFromTo( Integer.class, String.class ), new ConvertFromTo( Integer.class, String.class ), null );
	}

	@SuppressWarnings( "cast" )
	public void testNestedMetawidget() {

		PersonalContact contact = new PersonalContact();
		Shell shell = new Shell( SwtMetawidgetTests.TEST_DISPLAY, SWT.NONE );
		SwtMetawidget metawidget = new SwtMetawidget( shell, SWT.NONE );
		metawidget.addWidgetProcessor( new DataBindingProcessor() );
		metawidget.setToInspect( contact );

		// Just GridBagLayout

		assertEquals( null, contact.getFirstname() );
		assertEquals( null, contact.getAddress().getStreet() );
		( (Text) metawidget.getControl( "firstname" ) ).setText( "Foo" );
		( (Text) metawidget.getControl( "address", "street" ) ).setText( "Bar" );
		assertEquals( metawidget, ((Control) metawidget.getControl( "address", "street" )).getParent().getParent() );
		metawidget.getWidgetProcessor( DataBindingProcessor.class ).save( metawidget );
		assertEquals( "Foo", contact.getFirstname() );
		assertEquals( "Bar", contact.getAddress().getStreet() );

		// With TabbedPaneLayoutDecorator

		metawidget.setMetawidgetLayout( new TabFolderLayoutDecorator( new TabFolderLayoutDecoratorConfig().setLayout( new GridLayout() ) ) );
		( (Text) metawidget.getControl( "firstname" ) ).setText( "Foo1" );
		( (Text) ((Control) metawidget.getControl( "address", "street" ) )).setText( "Bar1" );
		assertTrue( ((Control) metawidget.getControl( "address", "street" )).getParent().getParent().getParent() instanceof TabFolder );
		metawidget.getWidgetProcessor( DataBindingProcessor.class ).save( metawidget );
		assertEquals( "Foo1", contact.getFirstname() );
		assertEquals( "Bar1", contact.getAddress().getStreet() );
	}

	public void testMissingReadOnlyWidgetBuilder() {

		Foo foo = new Foo();
		foo.setBar( 35 );

		Shell shell = new Shell( SwtMetawidgetTests.TEST_DISPLAY, SWT.NONE );
		SwtMetawidget metawidget = new SwtMetawidget( shell, SWT.NONE );
		metawidget.addWidgetProcessor( new DataBindingProcessor() );
		metawidget.setWidgetBuilder( new SwtWidgetBuilder() );
		metawidget.setToInspect( foo );

		assertEquals( 35, ( (Spinner) metawidget.getControl( "bar" ) ).getSelection() );
		( (Spinner) metawidget.getControl( "bar" ) ).setSelection( 36 );
		metawidget.getWidgetProcessor( DataBindingProcessor.class ).save( metawidget );
		assertEquals( 36l, foo.getBar() );
		metawidget.setReadOnly( true );
		assertEquals( 36l, ( (Spinner) metawidget.getControl( "bar" ) ).getSelection() );
	}

	//
	// Inner class
	//

	public static class Foo {

		//
		// Private members
		//

		private int	mBar;

		//
		// Public methods
		//

		public int getBar() {

			return mBar;
		}

		public void setBar( int bar ) {

			mBar = bar;
		}
	}

	public static class PersonalContact {

		//
		// Private members
		//

		private String	mFirstname;

		private Address	mAddress = new Address();

		//
		// Public methods
		//

		public String getFirstname() {

			return mFirstname;
		}

		public void setFirstname( String firstname ) {

			mFirstname = firstname;
		}

		@UiSection( "Contact Details" )
		public Address getAddress() {

			return mAddress;
		}

		public void setAddress( Address address ) {

			mAddress = address;
		}
	}

	public static class Address {

		//
		// Private members
		//

		private String	mStreet;

		//
		// Public methods
		//

		public String getStreet() {

			return mStreet;
		}

		public void setStreet( String street ) {

			mStreet = street;
		}
	}
}
