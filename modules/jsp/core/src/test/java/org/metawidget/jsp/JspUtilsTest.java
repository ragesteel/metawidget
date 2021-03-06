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

package org.metawidget.jsp;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.IterationTag;
import javax.servlet.jsp.tagext.Tag;

import junit.framework.TestCase;

import org.metawidget.jsp.JspUtils.BodyPreparer;

/**
 * @author Richard Kennard
 */

public class JspUtilsTest
	extends TestCase {

	//
	// Package-level statics
	//

	static final String	NEWLINE	= System.getProperty( "line.separator" );

	//
	// Public methods
	//

	public void testBodyTag()
		throws Exception {

		Tag testTag = new Tag() {

			public int doEndTag() {

				return Tag.EVAL_PAGE;
			}

			public int doStartTag() {

				return Tag.EVAL_BODY_INCLUDE;
			}

			public Tag getParent() {

				return null;
			}

			public void release() {

				// Do nothing
			}

			public void setPageContext( PageContext context ) {

				// Do nothing
			}

			public void setParent( Tag parent ) {

				// Do nothing
			}
		};

		final DummyPageContext dummyPageContext = new DummyPageContext();

		JspUtils.writeTag( dummyPageContext, testTag, null, new BodyPreparer() {

			public void prepareBody( PageContext delegateContext )
				throws IOException {

				JspWriter writer = delegateContext.getOut();

				// Write

				writer.newLine();
				writer.print( true );
				writer.print( 'a' );
				writer.print( Integer.MAX_VALUE );
				writer.print( Long.MAX_VALUE );
				writer.print( Float.MAX_VALUE );
				writer.print( Double.MAX_VALUE );
				writer.print( new char[] { 'b', 'c' } );
				Object object1 = new Object();
				writer.print( object1 );
				writer.println();
				writer.println( false );
				writer.println( 'b' );
				writer.println( Integer.MIN_VALUE );
				writer.println( Long.MIN_VALUE );
				writer.println( Float.MIN_VALUE );
				writer.println( Double.MIN_VALUE );
				writer.println( new char[] { 'c', 'd' } );
				Object object2 = new Object();
				writer.println( object2 );
				writer.println( "END" );
				writer.flush();
				writer.close();

				// Hit

				try {
					delegateContext.forward( null );
					delegateContext.getException();
					delegateContext.getPage();
					delegateContext.getRequest();
					delegateContext.getResponse();
					delegateContext.getServletConfig();
					delegateContext.getServletContext();
					delegateContext.getSession();
					delegateContext.handlePageException( (Exception) null );
					delegateContext.handlePageException( (Throwable) null );
					delegateContext.include( null );
					delegateContext.include( null, false );
					delegateContext.initialize( null, null, null, null, false, 0, false );
					delegateContext.release();
					delegateContext.findAttribute( null );
					delegateContext.getAttribute( null );
					delegateContext.getAttribute( null, 0 );
					delegateContext.getAttributeNamesInScope( 0 );
					delegateContext.getAttributesScope( null );
					delegateContext.getExpressionEvaluator();
					delegateContext.getOut();
					delegateContext.getVariableResolver();
					delegateContext.removeAttribute( null );
					delegateContext.removeAttribute( null, 0 );
					delegateContext.setAttribute( null, null );
					delegateContext.setAttribute( null, null, 0 );
				} catch ( Exception e ) {
					throw new IOException( e.getMessage() );
				}

				assertTrue( dummyPageContext.getPageContextHits() == 25 );

				// Verify

				String verify = NEWLINE + "truea" + Integer.MAX_VALUE + Long.MAX_VALUE + Float.MAX_VALUE + Double.MAX_VALUE + "bc" + object1;
				verify += NEWLINE + "false" + NEWLINE;
				verify += "b" + NEWLINE;
				verify += Integer.MIN_VALUE + NEWLINE;
				verify += Long.MIN_VALUE + NEWLINE;
				verify += Float.MIN_VALUE + NEWLINE;
				verify += Double.MIN_VALUE + NEWLINE;
				verify += "cd" + NEWLINE;
				verify += object2 + NEWLINE;
				verify += "END" + NEWLINE;

				assertEquals( verify, writer.toString() );
			}
		} );
	}

	public void testSkipBody()
		throws Exception {

		Tag testTag = new Tag() {

			public int doEndTag() {

				return Tag.EVAL_PAGE;
			}

			public int doStartTag() {

				return Tag.SKIP_BODY;
			}

			public Tag getParent() {

				return null;
			}

			public void release() {

				// Do nothing
			}

			public void setPageContext( PageContext context ) {

				// Do nothing
			}

			public void setParent( Tag parent ) {

				// Do nothing
			}
		};

		final DummyPageContext dummyPageContext = new DummyPageContext();

		JspUtils.writeTag( dummyPageContext, testTag, null, new BodyPreparer() {

			public void prepareBody( PageContext delegateContext ) {

				assertTrue( false );
			}
		} );
	}

	int	mRepeat;

	public void testRepeatBody()
		throws Exception {

		BodyTag testTag = new BodyTag() {

			public int doEndTag() {

				return Tag.EVAL_PAGE;
			}

			public int doStartTag() {

				return Tag.EVAL_BODY_INCLUDE;
			}

			public Tag getParent() {

				return null;
			}

			public void release() {

				// Do nothing
			}

			public void setPageContext( PageContext context ) {

				// Do nothing
			}

			public void setParent( Tag parent ) {

				// Do nothing
			}

			public void doInitBody() {

				// Do nothing
			}

			public void setBodyContent( BodyContent bodycontent ) {

				// Do nothing
			}

			public int doAfterBody() {

				mRepeat++;

				if ( mRepeat < 5 ) {
					return IterationTag.EVAL_BODY_AGAIN;
				}

				return Tag.EVAL_PAGE;
			}
		};

		mRepeat = 0;

		JspUtils.writeTag( null, testTag, null, null );

		assertTrue( mRepeat == 5 );
	}

	//
	// Inner class
	//

	public static class DummyPageContext
		extends PageContext {

		//
		// Private members
		//

		private int	mPageContextHits;

		//
		// Public methods
		//

		public int getPageContextHits() {

			return mPageContextHits;
		}

		@Override
		public void forward( String relativeUrlPath ) {

			mPageContextHits++;
		}

		@Override
		public Exception getException() {

			mPageContextHits++;
			return null;
		}

		@Override
		public Object getPage() {

			mPageContextHits++;
			return null;
		}

		@Override
		public ServletRequest getRequest() {

			mPageContextHits++;
			return null;
		}

		@Override
		public ServletResponse getResponse() {

			mPageContextHits++;
			return null;
		}

		@Override
		public ServletConfig getServletConfig() {

			mPageContextHits++;
			return null;
		}

		@Override
		public ServletContext getServletContext() {

			mPageContextHits++;
			return new ServletContext() {

				public String getInitParameter( String arg0 ) {

					return null;
				}

				//
				// Unsupported methods
				//

				public Object getAttribute( String arg0 ) {

					throw new UnsupportedOperationException();
				}

				public Enumeration<?> getAttributeNames() {

					throw new UnsupportedOperationException();
				}

				public ServletContext getContext( String arg0 ) {

					throw new UnsupportedOperationException();
				}

				public Enumeration<?> getInitParameterNames() {

					throw new UnsupportedOperationException();
				}

				public int getMajorVersion() {

					throw new UnsupportedOperationException();
				}

				public String getMimeType( String arg0 ) {

					throw new UnsupportedOperationException();
				}

				public int getMinorVersion() {

					throw new UnsupportedOperationException();
				}

				public RequestDispatcher getNamedDispatcher( String arg0 ) {

					throw new UnsupportedOperationException();
				}

				public String getRealPath( String arg0 ) {

					throw new UnsupportedOperationException();
				}

				public RequestDispatcher getRequestDispatcher( String arg0 ) {

					throw new UnsupportedOperationException();
				}

				public URL getResource( String arg0 ) {

					throw new UnsupportedOperationException();
				}

				public InputStream getResourceAsStream( String arg0 ) {

					throw new UnsupportedOperationException();
				}

				public Set<?> getResourcePaths( String arg0 ) {

					throw new UnsupportedOperationException();
				}

				public String getServerInfo() {

					throw new UnsupportedOperationException();
				}

				@SuppressWarnings( "deprecation" )
				public Servlet getServlet( String arg0 ) {

					throw new UnsupportedOperationException();
				}

				public String getServletContextName() {

					throw new UnsupportedOperationException();
				}

				@SuppressWarnings( "deprecation" )
				public Enumeration<?> getServletNames() {

					throw new UnsupportedOperationException();
				}

				@SuppressWarnings( "deprecation" )
				public Enumeration<?> getServlets() {

					throw new UnsupportedOperationException();
				}

				public void log( String arg0 ) {

					throw new UnsupportedOperationException();
				}

				@SuppressWarnings( "deprecation" )
				public void log( Exception arg0, String arg1 ) {

					throw new UnsupportedOperationException();
				}

				public void log( String arg0, Throwable arg1 ) {

					throw new UnsupportedOperationException();
				}

				public void removeAttribute( String arg0 ) {

					throw new UnsupportedOperationException();
				}

				public void setAttribute( String arg0, Object arg1 ) {

					throw new UnsupportedOperationException();
				}
			};
		}

		@Override
		public HttpSession getSession() {

			mPageContextHits++;
			return null;
		}

		@Override
		public void handlePageException( Exception exception ) {

			mPageContextHits++;
		}

		@Override
		public void handlePageException( Throwable throwable ) {

			mPageContextHits++;
		}

		@Override
		public void include( String relativeUrlPath ) {

			mPageContextHits++;
		}

		@Override
		public void include( String relativeUrlPath, boolean flush ) {

			mPageContextHits++;
		}

		@Override
		public void initialize( Servlet servlet, ServletRequest request, ServletResponse response, String errorPageUrl, boolean needsSession, int bufferSize, boolean autoFlush ) {

			mPageContextHits++;
		}

		@Override
		public void release() {

			mPageContextHits++;
		}

		@Override
		public Object findAttribute( String name ) {

			mPageContextHits++;
			return null;
		}

		@Override
		public Object getAttribute( String name ) {

			mPageContextHits++;
			return null;
		}

		@Override
		public Object getAttribute( String name, int scope ) {

			mPageContextHits++;
			return null;
		}

		@Override
		public Enumeration<?> getAttributeNamesInScope( int scope ) {

			mPageContextHits++;
			return null;
		}

		@Override
		public int getAttributesScope( String name ) {

			mPageContextHits++;
			return 0;
		}

		@Override
		public ExpressionEvaluator getExpressionEvaluator() {

			mPageContextHits++;
			return null;
		}

		@Override
		public JspWriter getOut() {

			assertTrue( false );
			return null;
		}

		@Override
		public VariableResolver getVariableResolver() {

			mPageContextHits++;
			return null;
		}

		@Override
		public void removeAttribute( String name ) {

			mPageContextHits++;
		}

		@Override
		public void removeAttribute( String name, int scope ) {

			mPageContextHits++;
		}

		@Override
		public void setAttribute( String name, Object value ) {

			mPageContextHits++;
		}

		@Override
		public void setAttribute( String name, Object value, int scope ) {

			mPageContextHits++;
		}
	}
}
