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

package org.metawidget.test.inspector.jbpm;

import static org.metawidget.inspector.InspectionResultConstants.*;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import org.metawidget.inspector.iface.Inspector;
import org.metawidget.inspector.iface.InspectorException;
import org.metawidget.inspector.jbpm.JbpmInspector;
import org.metawidget.inspector.jbpm.JbpmInspectorConfig;
import org.metawidget.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Richard Kennard
 */

public class JbpmInspectorTest
	extends TestCase
{
	//
	//
	// Private members
	//
	//

	private Inspector	mInspector;

	//
	//
	// Public methods
	//
	//

	@Override
	public void setUp()
	{
		JbpmInspectorConfig config = new JbpmInspectorConfig();
		config.setFile( "org/metawidget/test/inspector/jbpm/test-components.xml" );
		mInspector = new JbpmInspector( config );
	}

	public void testMissingFile()
	{
		try
		{
			new JbpmInspector();
			assertTrue( false );
		}
		catch( InspectorException e )
		{
			assertTrue( "Unable to locate components.xml".equals( e.getMessage() ));
		}

		try
		{
			JbpmInspectorConfig config = new JbpmInspectorConfig();
			config.setInputStream( new ByteArrayInputStream( "<foo></foo>".getBytes() ) );
			new JbpmInspector( config );
			assertTrue( false );
		}
		catch( InspectorException e )
		{
			assertTrue( "Expected an XML document starting with 'components' or 'pageflow-definition', but got 'foo'".equals( e.getMessage() ));
		}
	}

	public void testProperties()
	{
		String xml = mInspector.inspect( null, "account" );
		System.out.println( xml );
		Document document = XmlUtils.documentFromString( xml );

		assertTrue( "inspection-result".equals( document.getFirstChild().getNodeName() ) );

		// Entity

		Element entity = (Element) document.getFirstChild().getFirstChild();
		assertTrue( ENTITY.equals( entity.getNodeName() ) );
		assertTrue( !entity.hasAttribute( NAME ) );
		assertTrue( "account".equals( entity.getAttribute( TYPE ) ));

		// Properties

		Element property = (Element) entity.getFirstChild();
		assertTrue( ACTION.equals( property.getNodeName() ) );
		assertTrue( "next".equals( property.getAttribute( NAME ) ) );
		assertTrue( TRUE.equals( property.getAttribute( HIDDEN ) ) );

		assertTrue( property.getNextSibling() == null );
	}

	//
	//
	// Constructor
	//
	//

	/**
	 * JUnit 3.7 constructor.
	 */

	public JbpmInspectorTest( String name )
	{
		super( name );
	}
}