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

package org.metawidget.jsp.tagext.html.widgetbuilder.spring;

import static org.metawidget.inspector.InspectionResultConstants.*;
import static org.metawidget.inspector.spring.SpringInspectionResultConstants.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.metawidget.jsp.JspUtils;
import org.metawidget.jsp.JspUtils.BodyPreparer;
import org.metawidget.jsp.tagext.LiteralTag;
import org.metawidget.jsp.tagext.MetawidgetTag;
import org.metawidget.jsp.tagext.html.BaseHtmlMetawidgetTag;
import org.metawidget.jsp.tagext.html.HtmlStubTag;
import org.metawidget.util.ClassUtils;
import org.metawidget.util.CollectionUtils;
import org.metawidget.util.WidgetBuilderUtils;
import org.metawidget.widgetbuilder.iface.WidgetBuilder;
import org.metawidget.widgetbuilder.iface.WidgetBuilderException;
import org.springframework.web.servlet.tags.form.AbstractDataBoundFormElementTag;
import org.springframework.web.servlet.tags.form.AbstractHtmlElementTag;
import org.springframework.web.servlet.tags.form.CheckboxTag;
import org.springframework.web.servlet.tags.form.InputTag;
import org.springframework.web.servlet.tags.form.OptionTag;
import org.springframework.web.servlet.tags.form.OptionsTag;
import org.springframework.web.servlet.tags.form.PasswordInputTag;
import org.springframework.web.servlet.tags.form.SelectTag;
import org.springframework.web.servlet.tags.form.TextareaTag;

/**
 * WidgetBuilder for Spring environments.
 * <p>
 * Creates native Spring form tags, such as <code>&lt;form:input&gt;</code> and
 * <code>&lt;form:select&gt;</code>, to suit the inspected fields.
 *
 * @author Richard Kennard
 */

public class SpringWidgetBuilder
	implements WidgetBuilder<Tag, MetawidgetTag> {

	//
	// Private statics
	//

	private static final List<Boolean>	LIST_BOOLEAN_VALUES	= CollectionUtils.unmodifiableList( Boolean.TRUE, Boolean.FALSE );

	//
	// Public methods
	//

	public Tag buildWidget( String elementName, Map<String, String> attributes, MetawidgetTag metawidget ) {

		// Hidden?

		if ( TRUE.equals( attributes.get( HIDDEN ) ) ) {
			attributes.put( MetawidgetTag.ATTRIBUTE_NEEDS_HIDDEN_FIELD, TRUE );
			return new HtmlStubTag();
		}

		// Actions (ignored)

		if ( ACTION.equals( elementName ) ) {
			return new HtmlStubTag();
		}

		String type = WidgetBuilderUtils.getActualClassOrType( attributes );

		// If no type, assume a String

		if ( type == null ) {
			type = String.class.getName();
		}

		// Lookup the Class

		Class<?> clazz = ClassUtils.niceForName( type );

		// Support mandatory Booleans (can be rendered as a checkbox, even though they have a
		// Lookup)

		if ( Boolean.class.equals( clazz ) && TRUE.equals( attributes.get( REQUIRED ) ) ) {
			return initSpringTag( new CheckboxTag(), attributes, metawidget );
		}

		// Spring Lookups

		String springLookup = attributes.get( SPRING_LOOKUP );

		if ( springLookup != null && !"".equals( springLookup ) ) {
			return createSelectTag( springLookup, attributes, metawidget );
		}

		// String Lookups

		String lookup = attributes.get( LOOKUP );

		if ( lookup != null && !"".equals( lookup ) ) {
			return createSelectTag( CollectionUtils.fromString( lookup ), CollectionUtils.fromString( attributes.get( LOOKUP_LABELS ) ), attributes, metawidget );
		}

		if ( clazz != null ) {
			// Primitives

			if ( clazz.isPrimitive() ) {
				if ( boolean.class.equals( clazz ) ) {
					return initSpringTag( new CheckboxTag(), attributes, metawidget );
				}

				return initSpringTag( new InputTag(), attributes, metawidget );
			}

			// Strings

			if ( String.class.equals( clazz ) ) {
				if ( TRUE.equals( attributes.get( MASKED ) ) ) {
					return initSpringTag( new PasswordInputTag(), attributes, metawidget );
				}

				if ( TRUE.equals( attributes.get( LARGE ) ) ) {
					return initSpringTag( new TextareaTag(), attributes, metawidget );
				}

				return initSpringTag( new InputTag(), attributes, metawidget );
			}

			// Character

			if ( Character.class.equals( clazz ) ) {
				return initSpringTag( new InputTag(), attributes, metawidget );
			}

			// Dates

			if ( Date.class.equals( clazz ) ) {
				return initSpringTag( new InputTag(), attributes, metawidget );
			}

			// Booleans (are tri-state)

			if ( Boolean.class.equals( clazz ) ) {
				return createSelectTag( LIST_BOOLEAN_VALUES, null, attributes, metawidget );
			}

			// Numbers

			if ( Number.class.isAssignableFrom( clazz ) ) {
				return initSpringTag( new InputTag(), attributes, metawidget );
			}

			// Collections

			if ( Collection.class.isAssignableFrom( clazz ) ) {
				return new HtmlStubTag();
			}
		}

		// Not simple, but don't expand

		if ( TRUE.equals( attributes.get( DONT_EXPAND ) ) ) {
			return initSpringTag( new InputTag(), attributes, metawidget );
		}

		// Nested Metawidget

		return null;
	}

	//
	// Private methods
	//

	/**
	 * Initialize the Spring Tag with various attributes, CSS settings etc.
	 */

	private Tag initSpringTag( Tag tag, Map<String, String> attributes, MetawidgetTag metawidget ) {

		// Path

		String path = attributes.get( NAME );

		if ( metawidget.getPathPrefix() != null ) {
			path = metawidget.getPathPrefix() + path;
		}

		if ( tag instanceof AbstractDataBoundFormElementTag ) {
			( (AbstractDataBoundFormElementTag) tag ).setPath( path );
		}

		// Maxlength

		if ( tag instanceof InputTag ) {
			String actualType = WidgetBuilderUtils.getActualClassOrType( attributes );

			if ( "char".equals( actualType ) || Character.class.getName().equals( actualType )) {
				( (InputTag) tag ).setMaxlength( "1" );
			} else {
				String maximumLength = attributes.get( MAXIMUM_LENGTH );

				if ( maximumLength != null ) {
					( (InputTag) tag ).setMaxlength( maximumLength );
				}
			}
		}

		// CSS

		if ( tag instanceof AbstractHtmlElementTag ) {
			AbstractHtmlElementTag tagAbstractHtmlElement = (AbstractHtmlElementTag) tag;
			BaseHtmlMetawidgetTag htmlMetawidgetTag = (BaseHtmlMetawidgetTag) metawidget;
			tagAbstractHtmlElement.setCssStyle( htmlMetawidgetTag.getStyle() );
			tagAbstractHtmlElement.setCssClass( htmlMetawidgetTag.getStyleClass() );
		}

		return tag;
	}

	private Tag createSelectTag( final String expression, final Map<String, String> attributes, MetawidgetTag metawidget ) {

		// Write the SELECT tag

		final SelectTag tagSelect = new SelectTag();
		initSpringTag( tagSelect, attributes, metawidget );

		try {
			String literal = JspUtils.writeTag( metawidget.getPageContext(), tagSelect, metawidget, new BodyPreparer() {

				// Within the SELECT tag, write the OPTION tags

				public void prepareBody( PageContext delgateContext )
					throws JspException, IOException {

					// Empty option

					if ( WidgetBuilderUtils.needsEmptyLookupItem( attributes ) ) {
						OptionTag tagOptionEmpty = new OptionTag();
						tagOptionEmpty.setValue( "" );
						delgateContext.getOut().write( JspUtils.writeTag( delgateContext, tagOptionEmpty, tagSelect, null ) );
					}

					// Options tag

					OptionsTag tagOptions = new OptionsTag();
					tagOptions.setItems( expression );

					// Optional itemValue and itemLabel

					String itemValue = attributes.get( SPRING_LOOKUP_ITEM_VALUE );

					if ( itemValue != null ) {
						tagOptions.setItemValue( itemValue );
					}

					String itemLabel = attributes.get( SPRING_LOOKUP_ITEM_LABEL );

					if ( itemLabel != null ) {
						tagOptions.setItemLabel( itemLabel );
					}

					delgateContext.getOut().write( JspUtils.writeTag( delgateContext, tagOptions, tagSelect, null ) );
				}
			} );

			return new LiteralTag( literal );
		} catch ( JspException e ) {
			throw WidgetBuilderException.newException( e );
		}
	}

	private Tag createSelectTag( final List<?> values, final List<String> labels, final Map<String, String> attributes, MetawidgetTag metawidget ) {

		// Write the SELECT tag

		final SelectTag tagSelect = new SelectTag();
		initSpringTag( tagSelect, attributes, metawidget );

		try {
			String literal = JspUtils.writeTag( metawidget.getPageContext(), tagSelect, metawidget, new BodyPreparer() {

				// Within the SELECT tag, write the OPTION tags

				public void prepareBody( PageContext delgateContext )
					throws JspException, IOException {

					// See if we're using labels

					if ( labels != null && !labels.isEmpty() && labels.size() != values.size() ) {
						throw WidgetBuilderException.newException( "Labels list must be same size as values list" );
					}

					// Empty option

					if ( WidgetBuilderUtils.needsEmptyLookupItem( attributes ) ) {
						OptionTag tagOptionEmpty = new OptionTag();
						tagOptionEmpty.setValue( "" );
						delgateContext.getOut().write( JspUtils.writeTag( delgateContext, tagOptionEmpty, tagSelect, null ) );
					}

					// Add the options

					for ( int loop = 0, length = values.size(); loop < length; loop++ ) {
						final OptionTag tagOption = new OptionTag();
						tagOption.setValue( values.get( loop ) );

						if ( labels != null && !labels.isEmpty() ) {
							tagOption.setLabel( labels.get( loop ) );
						}

						delgateContext.getOut().write( JspUtils.writeTag( delgateContext, tagOption, tagSelect, null ) );
					}
				}
			} );

			return new LiteralTag( literal );
		} catch ( JspException e ) {
			throw WidgetBuilderException.newException( e );
		}
	}
}
