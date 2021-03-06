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

package org.metawidget.jsp.tagext.layout;

import java.lang.reflect.Field;

import junit.framework.TestCase;

import org.metawidget.jsp.tagext.StubTag;
import org.metawidget.jsp.tagext.html.HtmlMetawidgetTag;
import org.metawidget.jsp.tagext.html.HtmlStubTag;
import org.metawidget.jsp.tagext.html.layout.HeadingTagLayoutDecorator;
import org.metawidget.jsp.tagext.html.layout.HeadingTagLayoutDecoratorConfig;
import org.metawidget.jsp.tagext.html.layout.HtmlTableLayout;

/**
 * @author Richard Kennard
 */

public class JspFlatSectionLayoutDecoratorTest
	extends TestCase {

	//
	// Public methods
	//

	public void testEmptyStub()
		throws Exception {

		HeadingTagLayoutDecorator layoutDecorator = new HeadingTagLayoutDecorator( new HeadingTagLayoutDecoratorConfig().setLayout( new HtmlTableLayout() ) );
		assertTrue( false == layoutDecorator.isEmptyStub( null ) );
		assertTrue( false == layoutDecorator.isEmptyStub( new HtmlMetawidgetTag() ) );

		StubTag stub = new HtmlStubTag();
		assertTrue( true == layoutDecorator.isEmptyStub( stub ) );

		Field field = StubTag.class.getDeclaredField( "mSavedBodyContent" );
		field.setAccessible( true );
		field.set( stub, "Foo" );

		assertTrue( false == layoutDecorator.isEmptyStub( stub ) );
	}
}
