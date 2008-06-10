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

package org.metawidget.inspector.impl.propertystyle.javabean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

import org.metawidget.inspector.InspectorException;
import org.metawidget.inspector.impl.propertystyle.BaseProperty;
import org.metawidget.inspector.impl.propertystyle.Property;
import org.metawidget.inspector.impl.propertystyle.PropertyStyle;
import org.metawidget.util.ArrayUtils;
import org.metawidget.util.ClassUtils;
import org.metawidget.util.CollectionUtils;
import org.metawidget.util.simple.StringUtils;

/**
 * PropertyStyle for JavaBean-style properties.
 * <p>
 * This PropertyStyle recognizes both getters and setters and public member fields.
 *
 * @author Richard Kennard
 */

public class JavaBeanPropertyStyle
	implements PropertyStyle
{
	//
	//
	// Private members
	//
	//

	/**
	 * Cache of property lookups.
	 * <p>
	 * Property lookups are potentially expensive, so we cache them. The cache itself is a member
	 * variable, not a static, because we rely on <code>BasePropertyInspector</code> to only
	 * create one instance of <code>JavaBeanPropertyStyle</code> for all <code>Inspectors</code>.
	 */

	private Map<Class<?>, Map<String, Property>>	mPropertiesCache	= CollectionUtils.newHashMap();

	private String[]								mExcludeNames;

	private Class<?>[]								mExcludeTypes;

	//
	//
	// Constructor
	//
	//

	public JavaBeanPropertyStyle()
	{
		mExcludeNames = getExcludeNames();
		mExcludeTypes = getExcludeTypes();
	}

	//
	//
	// Public methods
	//
	//

	/**
	 * Returns properties sorted by name.
	 */

	public Map<String, Property> getProperties( Class<?> clazz )
	{
		synchronized ( mPropertiesCache )
		{
			Map<String, Property> properties = mPropertiesCache.get( clazz );

			if ( properties == null )
			{
				// TreeMap so that returns alphabetically sorted properties

				properties = CollectionUtils.newTreeMap();

				// Public fields

				for ( Field field : clazz.getFields() )
				{
					// Ignore static public fields

					if ( Modifier.isStatic( field.getModifiers() ) )
						continue;

					String propertyName = field.getName();
					properties.put( propertyName, new FieldProperty( propertyName, field ) );
				}

				// Getter methods

				for ( Method methodRead : clazz.getMethods() )
				{
					Class<?>[] parameters = methodRead.getParameterTypes();

					if ( parameters.length != 0 )
						continue;

					Class<?> toReturn = methodRead.getReturnType();

					if ( void.class.equals( toReturn ) )
						continue;

					// Ignore certain types

					if ( ArrayUtils.contains( mExcludeTypes, toReturn ) )
						continue;

					String methodName = methodRead.getName();
					String propertyName = null;

					if ( methodName.startsWith( ClassUtils.JAVABEAN_GET_PREFIX ) )
						propertyName = methodName.substring( ClassUtils.JAVABEAN_GET_PREFIX.length() );
					else if ( methodName.startsWith( ClassUtils.JAVABEAN_IS_PREFIX ) )
						propertyName = methodName.substring( ClassUtils.JAVABEAN_IS_PREFIX.length() );
					else
						continue;

					if ( !StringUtils.isFirstLetterUppercase( propertyName ) )
						continue;

					// Ignore certain names

					String lowercasedPropertyName = StringUtils.lowercaseFirstLetter( propertyName );

					if ( ArrayUtils.contains( mExcludeNames, lowercasedPropertyName ) )
						continue;

					// Already found (via its field)?

					Property propertyExisting = properties.get( lowercasedPropertyName );

					if ( propertyExisting != null )
					{
						if ( !( propertyExisting instanceof JavaBeanProperty ) )
							continue;

						// Beware covariant return types: always prefer the
						// subclass

						if ( toReturn.isAssignableFrom( propertyExisting.getType() ) )
							continue;
					}

					// Try to find a matching setter

					Method methodWrite = null;

					try
					{
						methodWrite = clazz.getMethod( ClassUtils.JAVABEAN_SET_PREFIX + propertyName, toReturn );
					}
					catch ( NoSuchMethodException e )
					{
						// May not be one
					}

					properties.put( lowercasedPropertyName, new JavaBeanProperty( lowercasedPropertyName, toReturn, methodRead, methodWrite ) );
				}

				// Setter methods (for those without getters)

				for ( Method methodWrite : clazz.getMethods() )
				{
					Class<?>[] parameters = methodWrite.getParameterTypes();

					if ( parameters.length != 1 )
						continue;

					Class<?> toSet = parameters[0];

					// Ignore certain types

					if ( ArrayUtils.contains( mExcludeTypes, toSet ) )
						continue;

					String methodName = methodWrite.getName();

					if ( !methodName.startsWith( ClassUtils.JAVABEAN_SET_PREFIX ) )
						continue;

					String propertyName = methodName.substring( ClassUtils.JAVABEAN_SET_PREFIX.length() );

					if ( !StringUtils.isFirstLetterUppercase( propertyName ) )
						continue;

					// Ignore certain names

					String lowercasedPropertyName = StringUtils.lowercaseFirstLetter( propertyName );

					if ( ArrayUtils.contains( mExcludeNames, lowercasedPropertyName ) )
						continue;

					// Already found (via its field/getter)?

					if ( properties.containsKey( lowercasedPropertyName ) )
						continue;

					properties.put( lowercasedPropertyName, new JavaBeanProperty( lowercasedPropertyName, toSet, null, methodWrite ) );
				}

				mPropertiesCache.put( clazz, Collections.unmodifiableMap( properties ) );
			}

			return properties;
		}
	}

	//
	//
	// Protected methods
	//
	//

	protected String[] getExcludeNames()
	{
		return new String[] { "propertyChangeListeners", "vetoableChangeListeners" };
	}

	protected Class<?>[] getExcludeTypes()
	{
		return new Class<?>[] { Class.class };
	}

	//
	//
	// Inner classes
	//
	//

	/**
	 * Public member field-based property.
	 */

	private static class FieldProperty
		extends BaseProperty
	{
		//
		//
		// Private methods
		//
		//

		private Field	mField;

		//
		//
		// Constructor
		//
		//

		public FieldProperty( String name, Field field )
		{
			super( name, field.getType() );

			mField = field;
		}

		//
		//
		// Public methods
		//
		//

		public boolean isReadable()
		{
			return true;
		}

		public Object read( Object obj )
		{
			try
			{
				return mField.get( obj );
			}
			catch ( Exception e )
			{
				throw InspectorException.newException( e );
			}
		}

		public boolean isWritable()
		{
			return true;
		}

		public <T extends Annotation> T getAnnotation( Class<T> annotation )
		{
			return mField.getAnnotation( annotation );
		}

		public Type getGenericType()
		{
			return mField.getGenericType();
		}
	}

	/**
	 * JavaBean-convention-based property.
	 */

	private static class JavaBeanProperty
		extends BaseProperty
	{
		//
		//
		// Private methods
		//
		//

		private Method	mReadMethod;

		private Method	mWriteMethod;

		//
		//
		// Constructor
		//
		//

		public JavaBeanProperty( String name, Class<?> clazz, Method readMethod, Method writeMethod )
		{
			super( name, clazz );

			mReadMethod = readMethod;
			mWriteMethod = writeMethod;
		}

		//
		//
		// Public methods
		//
		//

		public boolean isReadable()
		{
			return ( mReadMethod != null );
		}

		public Object read( Object obj )
		{
			try
			{
				return mReadMethod.invoke( obj );
			}
			catch ( Exception e )
			{
				throw InspectorException.newException( e );
			}
		}

		public boolean isWritable()
		{
			return ( mWriteMethod != null );
		}

		public <T extends Annotation> T getAnnotation( Class<T> annotationClass )
		{
			if ( mReadMethod != null )
			{
				T annotation = mReadMethod.getAnnotation( annotationClass );

				if ( annotation != null )
					return annotation;
			}

			if ( mWriteMethod != null )
			{
				T annotation = mWriteMethod.getAnnotation( annotationClass );

				if ( annotation != null )
					return annotation;

				return null;
			}
			else if ( mReadMethod != null )
				return null;

			throw InspectorException.newException( "Don't know how to getAnnotation from " + getName() );
		}

		public Type getGenericType()
		{
			if ( mReadMethod != null )
				return mReadMethod.getGenericReturnType();

			if ( mWriteMethod != null )
				return mWriteMethod.getGenericParameterTypes()[0];

			throw InspectorException.newException( "Don't know how to getGenericType from " + getName() );
		}
	}
}