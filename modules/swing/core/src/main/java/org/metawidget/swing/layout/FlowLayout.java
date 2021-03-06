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

package org.metawidget.swing.layout;

import java.util.Map;

import javax.swing.JComponent;

import org.metawidget.layout.iface.AdvancedLayout;
import org.metawidget.swing.Stub;
import org.metawidget.swing.SwingMetawidget;

/**
 * Layout to simply output components one after another, with no labels and no structure, using
 * <code>java.awt.FlowLayout</code>.
 * <p>
 * This is like <code>BoxLayout</code>, except it does not fill width. It can be useful for button
 * bars.
 *
 * @author Richard Kennard
 */

public class FlowLayout
	implements AdvancedLayout<JComponent, JComponent, SwingMetawidget> {

	//
	// Public methods
	//

	public void onStartBuild( SwingMetawidget metawidget ) {

		// Do nothing
	}

	public void startContainerLayout( JComponent container, SwingMetawidget metawidget ) {

		container.setLayout( new java.awt.FlowLayout() );
	}

	public void layoutWidget( JComponent component, String elementName, Map<String, String> attributes, JComponent container, SwingMetawidget metawidget ) {

		// Do not render empty stubs

		if ( component instanceof Stub && ( (Stub) component ).getComponentCount() == 0 ) {
			return;
		}

		// Add to the Metawidget

		container.add( component );
	}

	public void endContainerLayout( JComponent container, SwingMetawidget metawidget ) {

		// Do nothing
	}

	public void onEndBuild( SwingMetawidget metawidget ) {

		// Do nothing
	}
}
