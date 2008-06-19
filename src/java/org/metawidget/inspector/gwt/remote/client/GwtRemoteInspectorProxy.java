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

package org.metawidget.inspector.gwt.remote.client;

import java.io.Serializable;

import org.metawidget.inspector.gwt.remote.iface.GwtRemoteInspector;
import org.metawidget.inspector.gwt.remote.iface.GwtRemoteInspectorAsync;
import org.metawidget.inspector.iface.Inspector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * GwtInspector that sends its objects to the server for inspection.
 * <p>
 * Leveraging Metawidget's separated inspection architecture, GwtRemoteInspectorProxy uses AJAX to
 * pass objects to the server where the full power of Java introspection and reflection can be used
 * to inspect them. This includes inspecting their annotations.
 *
 * @author Richard Kennard
 */

public class GwtRemoteInspectorProxy
	implements Inspector
{
	//
	//
	// Private members
	//
	//

	private GwtRemoteInspectorAsync	mInspector;

	//
	//
	// Constructor
	//
	//

	public GwtRemoteInspectorProxy()
	{
		mInspector = (GwtRemoteInspectorAsync) GWT.create( GwtRemoteInspector.class );
	}

	//
	//
	// Public methods
	//
	//

	public String inspect( Object toInspect, String type, String... names )
	{
		throw new UnsupportedOperationException( "Use async inspection instead" );
	}

	public void inspect( Object toInspect, String type, String[] names, final AsyncCallback<String> callback )
	{
		if ( !( toInspect instanceof Serializable ) )
			throw new RuntimeException( "Objects passed to GwtRemoteInspector must be Serializable" );

		mInspector.inspect( (Serializable) toInspect, type, names, new AsyncCallback<String>()
		{
			public void onFailure( Throwable caught )
			{
				callback.onFailure( caught );
			}

			public void onSuccess( String xml )
			{
				callback.onSuccess( xml );
			}
		} );

	}
}