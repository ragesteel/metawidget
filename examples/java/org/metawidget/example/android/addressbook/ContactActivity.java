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

package org.metawidget.example.android.addressbook;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import org.metawidget.android.AndroidUtils.ResourcelessArrayAdapter;
import org.metawidget.android.widget.AndroidMetawidget;
import org.metawidget.example.shared.addressbook.model.BusinessContact;
import org.metawidget.example.shared.addressbook.model.Communication;
import org.metawidget.example.shared.addressbook.model.Contact;
import org.metawidget.example.shared.addressbook.model.Gender;
import org.metawidget.example.shared.addressbook.model.PersonalContact;
import org.metawidget.util.simple.StringUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Activity for Address Book Contacts.
 * <p>
 * Note: for brevity, this example is not optimized to reuse the Metawidget. For an example showing
 * reuse, see <code>org.metawidget.example.gwt.addressbook.client.ui.ContactDialog</code>.
 *
 * @author Richard Kennard
 */

// LOW: Actions added as menu items

public class ContactActivity
	extends Activity
{
	//
	//
	// Private statics
	//
	//

	private final static DateFormat	FORMAT				= DateFormat.getDateInstance( DateFormat.SHORT );

	static
	{
		FORMAT.setLenient( false );
	}

	private final static int		EDIT				= 0;

	private final static int		SAVE				= 1;

	private final static int		DELETE				= 2;

	private final static int		ADD_COMMUNICATION	= 3;

	//
	//
	// Protected members
	//
	//

	protected Contact				mContact;

	//
	//
	// Public methods
	//
	//

	@Override
	public void onCreate( Bundle bundle )
	{
		super.onCreate( bundle );

		// Layout from resource

		setContentView( R.layout.contact );

		final AddressBookApplication application = (AddressBookApplication) getApplication();
		StringBuilder builderTitle = new StringBuilder();

		// Load contact

		Intent intent = getIntent();

		long id = intent.getLongExtra( "contactId", -1 );

		if ( id != -1 )
		{
			mContact = application.getContactsController().load( id );
			builderTitle.append( mContact.getFullname() );
			builderTitle.append( " - " );
		}

		// Create contact

		else
		{
			String contactType = intent.getStringExtra( "contactType" );

			if ( "business".equals( contactType ) )
				mContact = new BusinessContact();
			else
				mContact = new PersonalContact();
		}

		// Set title

		if ( mContact instanceof PersonalContact )
			builderTitle.append( getString( R.string.personalContact ) );
		else
			builderTitle.append( getString( R.string.businessContact ) );

		setTitle( builderTitle );

		// Communications

		final ListView communicationsView = (ListView) findViewById( R.id.communications );
		communicationsView.setTag( "communications" );
		Set<Communication> communications = mContact.getCommunications();

		// If no communications, go with an emptySet. This is because we still need a ArrayAdpater
		// or Android will not scroll the screen correctly

		if ( communications == null )
			communications = Collections.emptySet();

		communicationsView.setAdapter( new ResourcelessArrayAdapter<Communication>( this, communications ) );

		communicationsView.setOnItemClickListener( new ListView.OnItemClickListener()
		{
			@SuppressWarnings( "unchecked" )
			public void onItemClick( final AdapterView viewAdapter, View view, int position, long itemId )
			{
				Communication communication = (Communication) viewAdapter.getSelectedItem();

				CommunicationDialog.show( ContactActivity.this, mContact, communication, new DialogInterface.OnClickListener()
				{
					public void onClick( DialogInterface dialog, int button )
					{
						communicationsView.setAdapter( new ResourcelessArrayAdapter<Communication>( ContactActivity.this, mContact.getCommunications() ) );
					}
				} );
			}
		} );

		// Metawidget

		final AndroidMetawidget metawidget = (AndroidMetawidget) findViewById( R.id.metawidget );
		metawidget.setToInspect( mContact );

		if ( id == -1 )
			metawidget.setReadOnly( false );

		// Manual mapping

		refresh();

		if ( !metawidget.isReadOnly() )
		{
			View view = metawidget.findViewWithTag( "title" );
			view.setFocusable( true );
			view.setFocusableInTouchMode( true );
			view.requestFocus();
		}
	}

	@Override
	public boolean onPrepareOptionsMenu( Menu menu )
	{
		super.onPrepareOptionsMenu( menu );
		menu.clear();

		AndroidMetawidget metawidget = (AndroidMetawidget) findViewById( R.id.metawidget );

		if ( metawidget.isReadOnly() )
		{
			menu.add( 0, EDIT, getString( R.string.edit ) );
		}
		else
		{
			menu.add( 0, ADD_COMMUNICATION, getString( R.string.addCommunication ) );
			menu.add( 0, SAVE, getString( R.string.save ) );

			if ( mContact.getId() != -1 )
				menu.add( 0, DELETE, getString( R.string.delete ) );
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected( Menu.Item item )
	{
		final AddressBookApplication application = (AddressBookApplication) getApplication();
		final AndroidMetawidget metawidget = (AndroidMetawidget) findViewById( R.id.metawidget );

		switch ( item.getId() )
		{
			case EDIT:
				metawidget.setReadOnly( false );
				refresh();
				View view = metawidget.findViewWithTag( "title" );
				view.setFocusable( true );
				view.setFocusableInTouchMode( true );
				view.requestFocus();
				break;

			case SAVE:
				if ( !save() )
					return false;

				try
				{
					application.getContactsController().save( mContact );
					finish();
				}
				catch ( Exception e )
				{
					new AlertDialog.Builder( getCurrentFocus().getContext() ).setTitle( getString( R.string.saveError ) ).setMessage( e.getMessage() ).setPositiveButton( getString( R.string.ok ), null ).show();
				}
				break;

			case DELETE:
				ConfirmDialog.show( ContactActivity.this, getString( R.string.deleteContact ), getString( R.string.confirmDeleteContact ), new DialogInterface.OnClickListener()
				{
					public void onClick( DialogInterface dialog, int button )
					{
						if ( button == DialogInterface.BUTTON1 )
						{
							application.getContactsController().delete( mContact );
							finish();
						}
					}
				} );
				break;

			case ADD_COMMUNICATION:
				CommunicationDialog.show( ContactActivity.this, mContact, null, new DialogInterface.OnClickListener()
				{
					public void onClick( DialogInterface dialog, int button )
					{
						ListView communicationsView = (ListView) findViewById( R.id.communications );
						communicationsView.setAdapter( new ResourcelessArrayAdapter<Communication>( ContactActivity.this, mContact.getCommunications() ) );
					}
				} );
				break;
		}

		return false;
	}

	//
	//
	// Protected methods
	//
	//

	/**
	 * Transfers data from the Contact to Metawidget
	 *
	 * @return true if the data was successfully transferred
	 */

	protected void refresh()
	{
		AndroidMetawidget metawidget = (AndroidMetawidget) findViewById( R.id.metawidget );

		metawidget.setValue( mContact.getTitle(), "title" );
		metawidget.setValue( mContact.getFirstnames(), "firstnames" );
		metawidget.setValue( mContact.getSurname(), "surname" );

		Gender gender = mContact.getGender();

		if ( gender == null )
			metawidget.setValue( null, "gender" );
		else if ( metawidget.findViewWithTag( "gender" ) instanceof TextView )
			metawidget.setValue( gender, "gender" );
		else
			metawidget.setValue( gender.name(), "gender" );

		metawidget.setValue( mContact.getAddress().getStreet(), "address", "street" );
		metawidget.setValue( mContact.getAddress().getCity(), "address", "city" );
		metawidget.setValue( mContact.getAddress().getState(), "address", "state" );
		metawidget.setValue( mContact.getAddress().getPostcode(), "address", "postcode" );
		metawidget.setValue( mContact.getNotes(), "notes" );

		if ( mContact instanceof PersonalContact )
		{
			Date ofBirth = ( (PersonalContact) mContact ).getDateOfBirth();

			if ( ofBirth != null )
			{
				synchronized ( FORMAT )
				{
					metawidget.setValue( FORMAT.format( ( (PersonalContact) mContact ).getDateOfBirth() ), "dateOfBirth" );
				}
			}
		}
		else
		{
			metawidget.setValue( ( (BusinessContact) mContact ).getCompany(), "company" );
			metawidget.setValue( StringUtils.quietValueOf( ( (BusinessContact) mContact ).getNumberOfStaff() ), "numberOfStaff" );
		}
	}

	/**
	 * Transfers data from Metawidget to the Contact
	 *
	 * @return true if the data was successfully transferred
	 */

	protected boolean save()
	{
		AndroidMetawidget metawidget = (AndroidMetawidget) findViewById( R.id.metawidget );

		mContact.setTitle( (String) metawidget.getValue( "title" ) );
		mContact.setFirstnames( (String) metawidget.getValue( "firstnames" ) );
		mContact.setSurname( (String) metawidget.getValue( "surname" ) );

		String gender = (String) metawidget.getValue( "gender" );

		if ( gender == null || "".equals( gender ) )
			mContact.setGender( null );
		else
			mContact.setGender( Gender.valueOf( gender ) );

		mContact.getAddress().setStreet( (String) metawidget.getValue( "address", "street" ) );
		mContact.getAddress().setCity( (String) metawidget.getValue( "address", "city" ) );
		mContact.getAddress().setState( (String) metawidget.getValue( "address", "state" ) );
		mContact.getAddress().setPostcode( (String) metawidget.getValue( "address", "postcode" ) );
		mContact.setNotes( (String) metawidget.getValue( "notes" ) );

		if ( mContact instanceof PersonalContact )
		{
			String ofBirth = (String) metawidget.getValue( "dateOfBirth" );

			if ( ofBirth == null || "".equals( ofBirth ) )
			{
				( (PersonalContact) mContact ).setDateOfBirth( null );
			}
			else
			{
				try
				{
					synchronized ( FORMAT )
					{
						( (PersonalContact) mContact ).setDateOfBirth( FORMAT.parse( ofBirth ) );
					}
				}
				catch ( ParseException e )
				{
					new AlertDialog.Builder( getCurrentFocus().getContext() ).setTitle( getString( R.string.dateError ) ).setMessage( "Unable to recognize date '" + ofBirth + "'\n\nPlease re-enter the date" ).setPositiveButton( "OK", null ).show();

					return false;
				}
			}
		}
		else
		{
			( (BusinessContact) mContact ).setCompany( (String) metawidget.getValue( "company" ) );
			String numberOfStaff = (String) metawidget.getValue( "numberOfStaff" );

			if ( numberOfStaff == null || "".equals( numberOfStaff ) )
			{
				( (BusinessContact) mContact ).setNumberOfStaff( 0 );
			}
			else
			{
				( (BusinessContact) mContact ).setNumberOfStaff( Integer.parseInt( numberOfStaff ) );
			}
		}

		return true;
	}
}