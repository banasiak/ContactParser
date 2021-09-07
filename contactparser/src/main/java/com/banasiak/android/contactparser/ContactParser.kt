package com.banasiak.android.contactparser

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import java.lang.ref.WeakReference

class ContactParser private constructor(context: Context) {

  private val weakContext: WeakReference<Context> = WeakReference(context)

  companion object {
    private const val TAG = "ContactParser"
    private const val IDX_ID = ContactsContract.Contacts._ID
    private const val IDX_NAME = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
    private const val IDX_EMAIL_ID = ContactsContract.CommonDataKinds.Email.CONTACT_ID
    private const val IDX_EMAIL_ADDRESS = ContactsContract.CommonDataKinds.Email.ADDRESS
    private const val IDX_EMAIL_TYPE = ContactsContract.CommonDataKinds.Email.TYPE
    private const val IDX_EMAIL_LABEL = ContactsContract.CommonDataKinds.Email.LABEL
    private const val IDX_PHONE_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID
    private const val IDX_PHONE_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER
    private const val IDX_PHONE_TYPE = ContactsContract.CommonDataKinds.Phone.TYPE
    private const val IDX_PHONE_LABEL = ContactsContract.CommonDataKinds.Phone.LABEL

    private lateinit var instance: ContactParser
    fun getInstance(context: Context): ContactParser {
      if (!this::instance.isInitialized) {
        instance = ContactParser(context.applicationContext)
      }
      return instance
    }
  }

  fun parseContact(uri: Uri): Contact? {
    val contactCursor = createContactCursor(uri) ?: return null
    val idIndex = contactCursor.getColumnIndex(IDX_ID)
    val id = contactCursor.getString(idIndex)
    val builder = Contact.Builder()
    builder.name = getName(contactCursor)
    builder.emails = getEmails(id)
    builder.phones = getPhones(id)

    destroyCursor(contactCursor)

    return builder.build()
  }

  private fun getName(cursor: Cursor): String {
    val index = cursor.getColumnIndex(IDX_NAME)
    return cursor.getString(index)
  }

  private fun getEmails(id: String): MutableList<Contact.Email> {
    val emails = mutableListOf<Contact.Email>()
    val emailCursor = createEmailCursor(id) ?: return emails

    while (!emailCursor.isAfterLast) {
      try {
        val addressIndex = emailCursor.getColumnIndex(IDX_EMAIL_ADDRESS)
        val address = emailCursor.getString(addressIndex)
        val type = getEmailType(emailCursor)
        emails.add(Contact.Email(address = address, type = type))
      } catch (e: Exception) {
        Log.w(TAG, "Unable to read email address for contact", e)
      }
      emailCursor.moveToNext()
    }

    destroyCursor(emailCursor)
    return emails
  }

  private fun getEmailType(emailCursor: Cursor): String {
    val typeIndex = emailCursor.getColumnIndex(IDX_EMAIL_TYPE)
    val labelIndex = emailCursor.getColumnIndex(IDX_EMAIL_LABEL)
    val type = emailCursor.getInt(typeIndex)
    val label = if (labelIndex != -1) emailCursor.getString(labelIndex) else ""
    return ContactsContract.CommonDataKinds.Email.getTypeLabel(
      weakContext.get()?.resources,
      type,
      label
    ) as String
  }

  private fun getPhones(id: String): MutableList<Contact.Phone> {
    val phones = mutableListOf<Contact.Phone>()
    val phoneCursor = createPhoneCursor(id) ?: return phones

    while (!phoneCursor.isAfterLast) {
      try {
        val numberIndex = phoneCursor.getColumnIndex(IDX_PHONE_NUMBER)
        val number = phoneCursor.getString(numberIndex)
        val type = getPhoneType(phoneCursor)
        phones.add(Contact.Phone(number = number, type = type))
      } catch (e: Exception) {
        Log.w(TAG, "Unable to read phone number for contact", e)
      }
      phoneCursor.moveToNext()
    }

    destroyCursor(phoneCursor)
    return phones
  }

  private fun getPhoneType(phoneCursor: Cursor): String {
    val typeIndex = phoneCursor.getColumnIndex(IDX_PHONE_TYPE)
    val labelIndex = phoneCursor.getColumnIndex(IDX_PHONE_LABEL)
    val type = phoneCursor.getInt(typeIndex)
    val label = if (labelIndex != -1) phoneCursor.getString(labelIndex) else ""
    return ContactsContract.CommonDataKinds.Phone.getTypeLabel(
      weakContext.get()?.resources,
      type,
      label
    ) as String
  }

  private fun createContactCursor(uri: Uri): Cursor? {
    val projection = arrayOf(
      IDX_ID,
      IDX_NAME
    )
    val cursor = weakContext.get()?.contentResolver?.query(
      uri,
      projection,
      null,
      null,
      null,
      null
    )
    cursor?.moveToFirst()
    return cursor
  }

  private fun createEmailCursor(id: String): Cursor? {
    val projection = arrayOf(
      IDX_EMAIL_ID,
      IDX_EMAIL_ADDRESS,
      IDX_EMAIL_TYPE,
      IDX_EMAIL_LABEL
    )
    val cursor = weakContext.get()?.contentResolver?.query(
      ContactsContract.CommonDataKinds.Email.CONTENT_URI,
      projection,
      "$IDX_EMAIL_ID = ?",
      arrayOf(id),
      null
    )
    cursor?.moveToFirst()
    return cursor
  }

  private fun createPhoneCursor(id: String): Cursor? {
    val projection = arrayOf(
      IDX_PHONE_ID,
      IDX_PHONE_NUMBER,
      IDX_PHONE_TYPE,
      IDX_PHONE_LABEL
    )
    val cursor = weakContext.get()?.contentResolver?.query(
      ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
      projection,
      "$IDX_PHONE_ID = ?",
      arrayOf(id),
      null
    )
    cursor?.moveToFirst()
    return cursor
  }

  private fun destroyCursor(cursor: Cursor) {
    if (!cursor.isClosed) {
      cursor.close()
    }
  }
}