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

package org.metawidget.android.widget;

import static org.metawidget.inspector.InspectionResultConstants.*;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Set;

import org.metawidget.android.AndroidConfigReader;
import org.metawidget.android.widget.layout.TableLayout;
import org.metawidget.android.widget.layout.TextViewLayoutDecorator;
import org.metawidget.android.widget.layout.TextViewLayoutDecoratorConfig;
import org.metawidget.android.widget.widgetbuilder.AndroidWidgetBuilder;
import org.metawidget.android.widget.widgetbuilder.OverriddenWidgetBuilder;
import org.metawidget.android.widget.widgetbuilder.ReadOnlyWidgetBuilder;
import org.metawidget.config.ConfigReader;
import org.metawidget.iface.MetawidgetException;
import org.metawidget.inspectionresultprocessor.iface.InspectionResultProcessor;
import org.metawidget.inspectionresultprocessor.sort.ComesAfterInspectionResultProcessor;
import org.metawidget.inspector.composite.CompositeInspector;
import org.metawidget.inspector.composite.CompositeInspectorConfig;
import org.metawidget.inspector.iface.Inspector;
import org.metawidget.inspector.propertytype.PropertyTypeInspector;
import org.metawidget.layout.iface.Layout;
import org.metawidget.pipeline.w3c.W3CPipeline;
import org.metawidget.util.ArrayUtils;
import org.metawidget.util.ClassUtils;
import org.metawidget.util.CollectionUtils;
import org.metawidget.util.simple.PathUtils;
import org.metawidget.util.simple.PathUtils.TypeAndNames;
import org.metawidget.util.simple.StringUtils;
import org.metawidget.widgetbuilder.composite.CompositeWidgetBuilder;
import org.metawidget.widgetbuilder.composite.CompositeWidgetBuilderConfig;
import org.metawidget.widgetbuilder.iface.WidgetBuilder;
import org.w3c.dom.Element;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Metawidget for Android environments.
 * <p>
 * Note: this class extends <code>LinearLayout</code> rather than <code>FrameLayout</code>, because
 * <code>FrameLayout</code> would <em>always</em> need to have another <code>Layout</code> embedded
 * within it, whereas <code>LinearLayout</code> is occasionally useful directly.
 *
 * @author Richard Kennard
 */

public class AndroidMetawidget
	extends LinearLayout {

	//
	// Private statics
	//

	private static Inspector									DEFAULT_INSPECTOR;

	private static InspectionResultProcessor<AndroidMetawidget>	DEFAULT_INSPECTIONRESULTPROCESSOR;

	private static WidgetBuilder<View, AndroidMetawidget>		DEFAULT_WIDGETBUILDER;

	private static Layout<View, ViewGroup, AndroidMetawidget>	DEFAULT_LAYOUT;

	private static ConfigReader									CONFIG_READER;

	//
	// Private members
	//

	private Object												mToInspect;

	private String												mPath;

	private int													mConfig;

	private Class<?>											mBundle;

	private boolean												mNeedToBuildWidgets;

	Element														mLastInspection;

	private boolean												mIgnoreAddRemove;

	private Set<View>											mExistingViews;

	private Set<View>											mExistingUnusedViews;

	private Map<String, Facet>									mFacets;

	private Map<Object, Object>									mClientProperties;

	private Pipeline											mPipeline;

	//
	// Constructor
	//

	public AndroidMetawidget( Context context ) {

		super( context );
		mPipeline = newPipeline();

		setOrientation( LinearLayout.VERTICAL );
	}

	public AndroidMetawidget( Context context, AttributeSet attributes ) {

		super( context, attributes );
		mPipeline = newPipeline();

		setOrientation( LinearLayout.VERTICAL );

		// Support overriding config in the XML

		mConfig = attributes.getAttributeResourceValue( null, "config", 0 );

		if ( mConfig != 0 ) {
			mPipeline.setNeedsConfiguring();
		}

		// Support readOnly in the XML

		String readOnly = attributes.getAttributeValue( null, "readOnly" );

		if ( readOnly != null && !"".equals( readOnly ) ) {
			mPipeline.setReadOnly( Boolean.parseBoolean( readOnly ) );
		}
	}

	//
	// Public methods
	//

	/**
	 * Sets the Object to inspect.
	 * <p>
	 * If <code>setPath</code> has not been set, or points to a previous <code>setToInspect</code>,
	 * sets it to point to the given Object.
	 */

	public void setToInspect( Object toInspect ) {

		if ( mToInspect == null ) {
			if ( mPath == null && toInspect != null ) {
				mPath = toInspect.getClass().getName();
			}
		} else if ( mToInspect.getClass().getName().equals( mPath ) ) {
			if ( toInspect == null ) {
				mPath = null;
			} else {
				mPath = toInspect.getClass().getName();
			}
		}

		mToInspect = toInspect;
		invalidateInspection();
	}

	/**
	 * Sets the path to be inspected.
	 * <p>
	 * Note <code>setPath</code> is quite different to <code>setTag</code>. <code>setPath</code> is
	 * always in relation to <code>setToInspect</code>, so must include the type name and any
	 * subsequent sub-names (eg. type/name/name). Conversely, <code>setTag</code> is a single name
	 * relative to our immediate parent.
	 */

	public void setPath( String path ) {

		mPath = path;
		invalidateInspection();
	}

	public String getPath() {

		return mPath;
	}

	/**
	 * Provides an id for the inspector configuration.
	 * <p>
	 * Typically, the id will be retrieved by <code>R.raw.inspector</code>
	 */

	public void setConfig( int config ) {

		mConfig = config;
		mPipeline.setNeedsConfiguring();
		invalidateInspection();
	}

	public void setInspector( Inspector inspector ) {

		mPipeline.setInspector( inspector );
		invalidateInspection();
	}

	/**
	 * Useful for WidgetBuilders to perform nested inspections (eg. for Collections).
	 */

	public String inspect( Object toInspect, String type, String... names ) {

		return mPipeline.inspect( toInspect, type, names );
	}

	public void setWidgetBuilder( WidgetBuilder<View, AndroidMetawidget> widgetBuilder ) {

		mPipeline.setWidgetBuilder( widgetBuilder );
		invalidateInspection();
	}

	public void setLayout( Layout<View, ViewGroup, AndroidMetawidget> layout ) {

		mPipeline.setLayout( layout );
		invalidateInspection();
	}

	public void setBundle( Class<?> bundle ) {

		mBundle = bundle;
		invalidateWidgets();
	}

	public String getLabelString( Map<String, String> attributes ) {

		if ( attributes == null ) {
			return "";
		}

		// Explicit label

		String label = attributes.get( LABEL );

		if ( label != null ) {
			// (may be forced blank)

			if ( "".equals( label ) ) {
				return null;
			}

			// (localize if possible)

			String localized = getLocalizedKey( StringUtils.camelCase( label ) );

			if ( localized != null ) {
				return localized.trim();
			}

			return label.trim();
		}

		// Default name

		String name = attributes.get( NAME );

		if ( name != null ) {
			// (localize if possible)

			String localized = getLocalizedKey( name );

			if ( localized != null ) {
				return localized.trim();
			}

			return StringUtils.uncamelCase( name );
		}

		return "";
	}

	/**
	 * Looks up the given key in the given bundle using
	 * <code>getContext().getResources().getText()</code>.
	 *
	 * @return null if no bundle, ???key??? if bundle is missing a key
	 */

	public String getLocalizedKey( String key ) {

		if ( mBundle == null ) {
			return null;
		}

		try {
			int id = (Integer) mBundle.getField( key ).get( null );
			return getContext().getResources().getText( id ).toString();
		} catch ( Exception e ) {
			return StringUtils.RESOURCE_KEY_NOT_FOUND_PREFIX + key + StringUtils.RESOURCE_KEY_NOT_FOUND_SUFFIX;
		}
	}

	public boolean isReadOnly() {

		return mPipeline.isReadOnly();
	}

	public void setReadOnly( boolean readOnly ) {

		if ( mPipeline.isReadOnly() == readOnly ) {
			return;
		}

		mPipeline.setReadOnly( readOnly );
		invalidateWidgets();
	}

	public int getMaximumInspectionDepth() {

		return mPipeline.getMaximumInspectionDepth();
	}

	public void setMaximumInspectionDepth( int maximumInspectionDepth ) {

		mPipeline.setMaximumInspectionDepth( maximumInspectionDepth );
		invalidateWidgets();
	}

	/**
	 * Storage area for WidgetProcessors, Layouts, and other stateless clients.
	 */

	public void putClientProperty( Object key, Object value ) {

		if ( mClientProperties == null ) {
			mClientProperties = CollectionUtils.newHashMap();
		}

		mClientProperties.put( key, value );
	}

	/**
	 * Storage area for WidgetProcessors, Layouts, and other stateless clients.
	 */

	@SuppressWarnings( "unchecked" )
	public <T> T getClientProperty( Object key ) {

		if ( mClientProperties == null ) {
			return null;
		}

		return (T) mClientProperties.get( key );
	}

	//
	// The following methods all kick off a buildWidgets()
	//

	@Override
	public int getChildCount() {

		buildWidgets();
		return super.getChildCount();
	}

	public void addView( View child, LayoutParams params ) {

		if ( !mIgnoreAddRemove ) {
			invalidateWidgets();
		}

		super.addView( child, params );
	}

	@Override
	public void addView( View child ) {

		if ( !mIgnoreAddRemove ) {
			invalidateWidgets();
		}

		super.addView( child );
	}

	@Override
	public void removeAllViews() {

		if ( !mIgnoreAddRemove ) {
			invalidateWidgets();
		}

		super.removeAllViews();
	}

	@Override
	public void removeView( View view ) {

		if ( !mIgnoreAddRemove ) {
			invalidateWidgets();
		}

		super.removeView( view );
	}

	@Override
	public View getChildAt( int index ) {

		buildWidgets();
		return super.getChildAt( index );
	}

	// Cannot override findViewWithTag( Object tag ), as is marked final!

	/**
	 * Gets the value from the View with the given name.
	 * <p>
	 * The value is returned as it is stored in the View (eg. Editable for EditText) so may need
	 * some conversion before being reapplied to the object being inspected. This obviously requires
	 * knowledge of which View AndroidMetawidget created, which is not ideal.
	 *
	 * @return the value from the View. Note this return type uses generics, so as to not require a
	 *         cast by the caller (eg. <code>String s = getValue(names)</code>)
	 */

	@SuppressWarnings( "unchecked" )
	public <T> T getValue( String... names ) {

		if ( names == null || names.length == 0 ) {
			throw MetawidgetException.newException( "No names specified" );
		}

		View view = findViewWithTags( names );

		if ( view == null ) {
			throw MetawidgetException.newException( "No View with tag " + ArrayUtils.toString( names ) );
		}

		return (T) getValue( view, mPipeline.getWidgetBuilder() );
	}

	/**
	 * Sets the value of the View with the given name.
	 * <p>
	 * Clients must ensure the value is of the correct type to suit the View (eg. String for
	 * EditText). This obviously requires knowledge of which View AndroidMetawidget created, which
	 * is not ideal.
	 */

	public void setValue( Object value, String... names ) {

		if ( names == null || names.length == 0 ) {
			throw MetawidgetException.newException( "No names specified" );
		}

		View view = findViewWithTags( names );

		if ( view == null ) {
			throw MetawidgetException.newException( "No View with tag " + ArrayUtils.toString( names ) );
		}

		if ( !setValue( value, view, mPipeline.getWidgetBuilder() ) ) {
			throw MetawidgetException.newException( "Don't know how to setValue of a " + view.getClass().getName() );
		}
	}

	public Facet getFacet( String name ) {

		buildWidgets();

		return mFacets.get( name );
	}

	/**
	 * Fetch a list of <code>Views</code> that were added manually, and have so far not been used.
	 * <p>
	 * <strong>This is an internal API exposed for OverriddenWidgetBuilder. Clients should not call
	 * it directly.</strong>
	 */

	public Set<View> fetchExistingUnusedViews() {

		return mExistingUnusedViews;
	}

	//
	// Protected methods
	//

	/**
	 * Instantiate the Pipeline used by this Metawidget.
	 * <p>
	 * Subclasses wishing to use their own Pipeline should override this method to instantiate their
	 * version.
	 */

	protected Pipeline newPipeline() {

		return new Pipeline();
	}

	@Override
	protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec ) {

		buildWidgets();
		super.onMeasure( widthMeasureSpec, heightMeasureSpec );
	}

	/**
	 * Invalidates the current inspection result (if any) <em>and</em> invalidates the widgets.
	 */

	protected void invalidateInspection() {

		mLastInspection = null;
		invalidateWidgets();
	}

	/**
	 * Invalidates the widgets.
	 */

	protected void invalidateWidgets() {

		if ( mNeedToBuildWidgets ) {
			return;
		}

		mNeedToBuildWidgets = true;

		postInvalidate();
	}

	protected void configure() {

		try {
			if ( mConfig != 0 ) {
				if ( CONFIG_READER == null ) {
					CONFIG_READER = new AndroidConfigReader( getContext() );
				}

				CONFIG_READER.configure( getContext().getResources().openRawResource( mConfig ), this );
			}

			// Sensible defaults
			//
			// Unlike the other Metawidgets, we don't handle these via ConfigReader because we
			// couldn't figure out how to read a metawidget-android-default.xml file from the JAR

			if ( mPipeline.getInspector() == null ) {
				if ( DEFAULT_INSPECTOR == null ) {
					// Relax the dependancy on MetawidgetAnnotationInspector (if the class is
					// hard-coded, rather than using Class.forName, Dalvik seems to pick
					// the dependancy up even if we never come down this codepath)

					Inspector annotationInspector = (Inspector) Class.forName( "org.metawidget.inspector.annotation.MetawidgetAnnotationInspector" ).newInstance();
					DEFAULT_INSPECTOR = new CompositeInspector( new CompositeInspectorConfig().setInspectors( new PropertyTypeInspector(), annotationInspector ) );
				}

				mPipeline.setInspector( DEFAULT_INSPECTOR );
			}

			if ( mPipeline.getInspectionResultProcessors() == null ) {
				if ( DEFAULT_INSPECTIONRESULTPROCESSOR == null ) {
					DEFAULT_INSPECTIONRESULTPROCESSOR = new ComesAfterInspectionResultProcessor<AndroidMetawidget>();
				}

				mPipeline.addInspectionResultProcessor( DEFAULT_INSPECTIONRESULTPROCESSOR );
			}

			if ( mPipeline.getWidgetBuilder() == null ) {
				if ( DEFAULT_WIDGETBUILDER == null ) {
					@SuppressWarnings( "unchecked" )
					CompositeWidgetBuilderConfig<View, AndroidMetawidget> config = new CompositeWidgetBuilderConfig<View, AndroidMetawidget>().setWidgetBuilders( new OverriddenWidgetBuilder(), new ReadOnlyWidgetBuilder(), new AndroidWidgetBuilder() );
					DEFAULT_WIDGETBUILDER = new CompositeWidgetBuilder<View, AndroidMetawidget>( config );
				}

				mPipeline.setWidgetBuilder( DEFAULT_WIDGETBUILDER );
			}

			if ( mPipeline.getLayout() == null ) {
				if ( DEFAULT_LAYOUT == null ) {
					DEFAULT_LAYOUT = new TextViewLayoutDecorator( new TextViewLayoutDecoratorConfig().setLayout( new TableLayout() ) );
				}

				mPipeline.setLayout( DEFAULT_LAYOUT );
			}
		} catch ( Exception e ) {
			throw MetawidgetException.newException( e );
		}
	}

	protected void buildWidgets() {

		// No need to build?

		if ( !mNeedToBuildWidgets ) {
			return;
		}

		mPipeline.configureOnce();

		mNeedToBuildWidgets = false;
		mIgnoreAddRemove = true;

		try {
			if ( mLastInspection == null ) {
				mLastInspection = inspect();
			}

			mPipeline.buildWidgets( mLastInspection );
		} catch ( Exception e ) {
			throw MetawidgetException.newException( e );
		} finally {
			mIgnoreAddRemove = false;
		}
	}

	protected void startBuild() {

		if ( mExistingViews == null ) {
			mExistingViews = CollectionUtils.newHashSet();
			mFacets = CollectionUtils.newHashMap();

			for ( int loop = 0, length = getChildCount(); loop < length; loop++ ) {
				View view = getChildAt( loop );

				if ( view instanceof Facet ) {
					Facet facet = (Facet) view;

					mFacets.put( facet.getName(), facet );
					continue;
				}

				mExistingViews.add( view );
			}
		}

		removeAllViews();

		mExistingUnusedViews = CollectionUtils.newHashSet( mExistingViews );
	}

	/**
	 * @param view
	 *            the widget to layout. Never null
	 * @param elementName
	 *            XML node name of the business field. Typically 'entity', 'property' or 'action'.
	 *            Never null
	 * @param attributes
	 *            attributes of the widget to layout. Never null
	 */

	protected void layoutWidget( View view, String elementName, Map<String, String> attributes ) {

		String childName = attributes.get( NAME );
		view.setTag( childName );

		// Remove, then re-add to layout (to re-order the component)

		if ( mPipeline.getLayout() != null ) {
			if ( view.getParent() != null ) {
				( (ViewGroup) view.getParent() ).removeView( view );
			}
		}

		// BasePipeline will call .layoutWidget
	}

	protected void endBuild() {

		// End layout

		Layout<View, ViewGroup, AndroidMetawidget> layout = mPipeline.getLayout();

		if ( layout != null ) {
			Map<String, String> noAttributes = CollectionUtils.newHashMap();

			for ( View viewExisting : mExistingUnusedViews ) {
				// In case View has been moved into a nested Layout during last build

				if ( viewExisting.getParent() != null ) {
					( (ViewGroup) viewExisting.getParent() ).removeView( viewExisting );
				}

				layout.layoutWidget( viewExisting, PROPERTY, noAttributes, this, this );
			}
		}
	}

	protected void initNestedMetawidget( AndroidMetawidget nestedMetawidget, Map<String, String> attributes ) {

		mPipeline.initNestedPipeline( nestedMetawidget.mPipeline, attributes );
		nestedMetawidget.setPath( mPath + StringUtils.SEPARATOR_FORWARD_SLASH_CHAR + attributes.get( NAME ) );
		nestedMetawidget.setBundle( mBundle );
		nestedMetawidget.setToInspect( mToInspect );
	}

	//
	// Private methods
	//

	private Element inspect() {

		if ( mPath == null ) {
			return null;
		}

		TypeAndNames typeAndNames = PathUtils.parsePath( mPath );
		return mPipeline.inspectAsDom( mToInspect, typeAndNames.getType(), typeAndNames.getNamesAsArray() );
	}

	private View findViewWithTags( String... tags ) {

		if ( tags == null ) {
			return null;
		}

		ViewGroup viewgroup = this;

		for ( int tagsLoop = 0, tagsLength = tags.length; tagsLoop < tagsLength; tagsLoop++ ) {
			Object tag = tags[tagsLoop];

			// buildWidgets just-in-time

			if ( viewgroup instanceof AndroidMetawidget ) {
				( (AndroidMetawidget) viewgroup ).buildWidgets();
			}

			// Use our own findViewWithTag, not View.findViewWithTag!

			View match = findViewWithTag( viewgroup, tag );

			// Not found

			if ( match == null ) {
				return null;
			}

			// Found

			if ( tagsLoop == tagsLength - 1 ) {
				return match;
			}

			// Keep traversing

			if ( !( match instanceof ViewGroup ) ) {
				return null;
			}

			viewgroup = (ViewGroup) match;
		}

		// Not found

		return null;
	}

	/**
	 * Version of <code>View.findViewWithTag</code> that only traverses child Views if they don't
	 * define a tag of their own.
	 * <p>
	 * This stops us incorrectly finding arbitrary bits of tag in arbitrary bits of the heirarchy.
	 */

	private View findViewWithTag( ViewGroup viewgroup, Object tag ) {

		for ( int childLoop = 0, childLength = viewgroup.getChildCount(); childLoop < childLength; childLoop++ ) {
			View child = viewgroup.getChildAt( childLoop );
			Object childTag = child.getTag();

			// Only recurse if child does not define a tag (eg. something like
			// an embedded Layout or a TableRow)

			if ( childTag == null && child instanceof ViewGroup ) {
				View view = findViewWithTag( (ViewGroup) child, tag );

				if ( view != null ) {
					return view;
				}

				continue;
			}

			// Keep looking

			if ( !tag.equals( child.getTag() ) ) {
				continue;
			}

			// Found

			return child;
		}

		// Not found

		return null;
	}

	private Object getValue( View widget, WidgetBuilder<View, AndroidMetawidget> widgetBuilder ) {

		// Recurse into CompositeWidgetBuilders

		if ( widgetBuilder instanceof CompositeWidgetBuilder<?, ?> ) {
			for ( WidgetBuilder<View, AndroidMetawidget> widgetBuilderChild : ( (CompositeWidgetBuilder<View, AndroidMetawidget>) widgetBuilder ).getWidgetBuilders() ) {
				Object value = getValue( widget, widgetBuilderChild );

				if ( value != null ) {
					return value;
				}
			}

			return null;
		}

		// Interrogate AndroidValueAccessors

		if ( widgetBuilder instanceof AndroidValueAccessor ) {
			return ( (AndroidValueAccessor) widgetBuilder ).getValue( widget );
		}

		return null;
	}

	private boolean setValue( Object value, View widget, WidgetBuilder<View, AndroidMetawidget> widgetBuilder ) {

		// Recurse into CompositeWidgetBuilders

		if ( widgetBuilder instanceof CompositeWidgetBuilder<?, ?> ) {
			for ( WidgetBuilder<View, AndroidMetawidget> widgetBuilderChild : ( (CompositeWidgetBuilder<View, AndroidMetawidget>) widgetBuilder ).getWidgetBuilders() ) {
				if ( setValue( value, widget, widgetBuilderChild ) ) {
					return true;
				}
			}

			return false;
		}

		// Interrogate AndroidValueAccessors

		if ( widgetBuilder instanceof AndroidValueAccessor ) {
			return ( (AndroidValueAccessor) widgetBuilder ).setValue( value, widget );
		}

		return false;
	}

	//
	// Inner class
	//

	protected class Pipeline
		extends W3CPipeline<View, ViewGroup, AndroidMetawidget> {

		//
		// Protected methods
		//

		@Override
		protected void configure() {

			AndroidMetawidget.this.configure();
		}

		@Override
		protected void startBuild() {

			super.startBuild();
			AndroidMetawidget.this.startBuild();
		}

		@Override
		protected void layoutWidget( View view, String elementName, Map<String, String> attributes ) {

			AndroidMetawidget.this.layoutWidget( view, elementName, attributes );
			super.layoutWidget( view, elementName, attributes );
		}

		@Override
		protected Map<String, String> getAdditionalAttributes( View view ) {

			if ( view instanceof Stub ) {
				return ( (Stub) view ).getAttributes();
			}

			return null;
		}

		@Override
		public AndroidMetawidget buildNestedMetawidget( Map<String, String> attributes )
			throws Exception {

			Constructor<?> constructor = AndroidMetawidget.this.getClass().getConstructor( Context.class );
			AndroidMetawidget nestedMetawidget = (AndroidMetawidget) constructor.newInstance( getContext() );
			AndroidMetawidget.this.initNestedMetawidget( nestedMetawidget, attributes );

			return nestedMetawidget;
		}

		@Override
		protected void endBuild() {

			AndroidMetawidget.this.endBuild();
			super.endBuild();
		}

		@Override
		protected AndroidMetawidget getPipelineOwner() {

			return AndroidMetawidget.this;
		}
	}
}
