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

package org.metawidget.faces.component;

import java.util.List;
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.component.ActionSource;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.event.ActionListener;

import org.metawidget.util.CollectionUtils;

/**
 * Stub for Java Server Faces environments.
 * <p>
 * A UIStub takes a <code>value</code> binding or an <code>action</code> binding but does nothing
 * with them. Stubs are used to 'stub out' what Metawidget would normally create - either to
 * suppress widget creation entirely or to create child widgets with a different name.
 * <p>
 * Note there is generally no need to use UIStub to replace a component with another, single
 * component. Doing...
 * <p>
 * <code>
 * &lt;m:stub value="#{foo.bar}"&gt;<br/>
 * &lt;h:inputText value="#{foo.bar}"&gt;<br/>
 * &lt;/m:stub&gt;
 * </code>
 * <p>
 * ...is equivalent to simply...
 * <p>
 * <code>
 * &lt;h:inputText value="#{foo.bar}"&gt;
 * </code>
 * <p>
 * UIStub is only required when replacing a component with either zero components (ie. to hide it
 * completely)...
 * <p>
 * <code>
 * &lt;m:stub value="#{foo.bar}"/&gt;
 * </code>
 * <p>
 * ...or with multiple components...
 * <p>
 * <code>
 * &lt;m:stub value="#{foo.bar}"&gt;<br/>
 * &lt;h:outputText value="#{foo.bar}"&gt;<br/>
 * &lt;h:inputHidden value="#{foo.bar}"&gt;<br/>
 * &lt;/m:stub&gt;
 * </code>
 * <p>
 * ...or with components of a different value binding...
 * <p>
 * <code>
 * &lt;m:stub value="#{foo.bar}"&gt;<br/>
 * &lt;h:outputText value="#{foo.myBar}"&gt;<br/>
 * &lt;/m:stub&gt;
 * </code>
 * <p>
 * As an exception to this rule, wrapping a single component in a UIStub can be useful to suppress
 * processing by <code>WidgetProcessors</code>, who will typically ignore Stubs. For example, you can
 * wrap a component in a UIStub and supply your own Validators, and
 * <code>StandardValidatorProcessor</code> will pass over it.
 *
 * @author Richard Kennard
 */

@SuppressWarnings( "deprecation" )
public class UIStub
	extends UIComponentBase
	implements ActionSource {

	//
	// Private members
	//

	private String			mStubAttributes;

	private MethodBinding	mAction;

	//
	// Public methods
	//

	@Override
	public String getFamily() {

		return "org.metawidget";
	}

	@Override
	public String getRendererType() {

		return "org.metawidget.Stub";
	}

	/**
	 * Gets the stub attributes as a Map.
	 * <p>
	 * Called <code>getStubAttributesAsMap</code>, not just <code>getStubAttributes</code>, because
	 * while JSF 2 Facelets does support write-only setters (ie. <code>setStubAttributes</code>), it
	 * does not seem to support write-only setters if there is also a getter with a different return
	 * type (ie. a Map).
	 */

	public Map<String, String> getStubAttributesAsMap() {

		// Static attributes

		String stubAttributes = mStubAttributes;

		// Dynamic attributes (take precedence if set)

		ValueBinding bindingStubAttributes = getValueBinding( "attributes" );

		if ( bindingStubAttributes != null ) {
			stubAttributes = (String) bindingStubAttributes.getValue( getFacesContext() );
		}

		if ( stubAttributes == null ) {
			return null;
		}

		// Parse attributes

		Map<String, String> attributes = CollectionUtils.newHashMap();

		for ( String nameAndValue : CollectionUtils.fromString( stubAttributes, ';' ) ) {
			// (use .length(), not .isEmpty(), so that we're 1.4 compatible)

			if ( nameAndValue.length() == 0 ) {
				continue;
			}

			List<String> nameAndValueList = CollectionUtils.fromString( nameAndValue, ':' );

			if ( nameAndValueList.size() != 2 || nameAndValueList.get( 1 ).length() == 0 ) {
				throw new FacesException( "Unrecognized value '" + nameAndValue + "'" );
			}

			attributes.put( nameAndValueList.get( 0 ), nameAndValueList.get( 1 ) );
		}

		return attributes;
	}

	public void setStubAttributes( String stubAttributes ) {

		mStubAttributes = stubAttributes;
	}

	@Override
	public Object saveState( FacesContext context ) {

		Object values[] = new Object[2];
		values[0] = super.saveState( context );
		values[1] = mStubAttributes;

		return values;
	}

	@Override
	public void restoreState( FacesContext context, Object state ) {

		Object values[] = (Object[]) state;
		super.restoreState( context, values[0] );

		mStubAttributes = (String) values[1];
	}

	public MethodBinding getAction() {

		return mAction;
	}

	public void setAction( MethodBinding action ) {

		mAction = action;
	}

	public MethodBinding getActionListener() {

		// Do nothing: UIStub is just a stub

		return null;
	}

	public void setActionListener( MethodBinding arg0 ) {

		// Do nothing: UIStub is just a stub
	}

	public ActionListener[] getActionListeners() {

		// Do nothing: UIStub is just a stub

		return null;
	}

	public void addActionListener( ActionListener arg0 ) {

		// Do nothing: UIStub is just a stub
	}

	public void removeActionListener( ActionListener arg0 ) {

		// Do nothing: UIStub is just a stub
	}

	public void setImmediate( boolean arg0 ) {

		// Do nothing: UIStub is just a stub
	}

	public boolean isImmediate() {

		// Do nothing: UIStub is just a stub

		return false;
	}
}