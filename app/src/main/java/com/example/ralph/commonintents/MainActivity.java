package com.example.ralph.commonintents;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_SELECT_CONTACT = 1;
    static final int REQUEST_SELECT_PHONE_NUMBER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void locateAddress(View view) {
        EditText addressEditText = (EditText) findViewById(R.id.editText2);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("geo:0,0?q=" + addressEditText.getText().toString()));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void searchName(View view) {
        EditText searchEditText = (EditText) findViewById(R.id.editText);
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.putExtra(SearchManager.QUERY, searchEditText.getText().toString());
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void callPhone(View view) {
        EditText phoneEditText = (EditText) findViewById(R.id.editText3);
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneEditText.getText().toString()));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void sendText(View view) {
        EditText phoneEditText = (EditText) findViewById(R.id.editText3);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setData(Uri.parse("smsto:" + phoneEditText.getText().toString()));  // This ensures only SMS apps respond
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void sendEmail(View view) {
        EditText emailEditText = (EditText) findViewById(R.id.editText4);
        String[] recipients = {emailEditText.getText().toString()};
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, recipients);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void contactName(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_SELECT_CONTACT);
        }
    }

    // http://www.higherpass.com/android/tutorials/working-with-android-contacts/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // We've got a contact from the user
        if (requestCode == REQUEST_SELECT_CONTACT && resultCode == RESULT_OK) {
            Uri contactUri = data.getData();
            ContentResolver cr = getContentResolver();
            Cursor cur = cr.query(contactUri, null, null, null, null);
            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {

                    // Get the Contact ID and Display Name
                    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    Log.d("MyActivity", "id: " + id + ", name: " + name);
                    // Write Display Name to the View
                    EditText nameEditText = (EditText) findViewById(R.id.editText);
                    nameEditText.setText(name);

                    //Get the first phone number
                    if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                        if (pCur.moveToNext()) {
                            int numberIndex = pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                            String number = pCur.getString(numberIndex);
                            Log.d("MyActivity", "number: " + number);
                            // Write Phone Number to the View
                            EditText phoneEditText = (EditText) findViewById(R.id.editText3);
                            phoneEditText.setText(number);
                            pCur.close();
                        }
                    }

                    // Get the first email address
                    Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[]{id}, null);
                    if (emailCur.moveToNext()) {
                        String emailAddress = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                        // String emailType = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
                        emailCur.close();
                        Log.d("MyActivity", "email: " + emailAddress);
                        // Write Email Address to the View
                        EditText emailEditText = (EditText) findViewById(R.id.editText4);
                        emailEditText.setText(emailAddress);
                    }

                    // Get the first Street Address
                    String addrWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
                    String[] addrWhereParams = new String[]{id, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE};
                    Cursor addrCur = cr.query(ContactsContract.Data.CONTENT_URI, null, addrWhere, addrWhereParams, null);
                    if (addrCur.moveToNext()) {
                        String poBox = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POBOX));
                        Log.d("MyActivity", "poBox: " + poBox);
                        String street = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
                        Log.d("MyActivity", "street: " + street);
                        String city = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
                        Log.d("MyActivity", "city: " + city);
                        String state = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION));
                        Log.d("MyActivity", "state: " + state);
                        String postalCode = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));
                        Log.d("MyActivity", "postalCode: " + postalCode);
                        String country = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));
                        Log.d("MyActivity", "country: " + country);
                        String type = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));
                        Log.d("MyActivity", "type: " + type);
                        addrCur.close();
                        // Write the Street Address to the View
                        EditText addressEditText = (EditText) findViewById(R.id.editText2);
                        addressEditText.setText(street + ", " + city + ", " + state + " " + postalCode);
                    }
                }
            }
        }
    }
}

