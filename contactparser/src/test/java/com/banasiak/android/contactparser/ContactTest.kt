package com.banasiak.android.contactparser

import junit.framework.TestCase.assertEquals
import org.junit.Test

class ContactTest {

  private val name = "Richard Banasiak"
  private val emails = listOf(
    Contact.Email(address = "richard@banasiak.com", type = "Home"),
    Contact.Email(address = "banasiak@gmail.com", type = "Other")
  )
  private val phones = listOf(
    Contact.Phone(number = "(210) 827-0127", type = "Mobile"),
    Contact.Phone(number = "(316) 833-1528", type = "Work")
  )

  @Test
  fun `builder functions`() {
    val additionalEmail = Contact.Email("test@test.test", "test")
    val additionalPhone = Contact.Phone("5555555555", "test")

    val builder = Contact.Builder()
    builder.name = name
    builder.addEmail(additionalEmail)
    builder.addPhone(additionalPhone)

    val contact = builder.build()

    assertEquals(contact.name, name)
    assertEquals(contact.emails, listOf(additionalEmail))
    assertEquals(contact.phones, listOf(additionalPhone))
  }

  @Test
  fun `vcf string formatted correctly`() {
    val validVcf =
      "BEGIN:VCARD\n" +
        "VERSION:3.0\n" +
        "FN:Richard Banasiak\n" +
        "EMAIL;TYPE=Home:richard@banasiak.com\n" +
        "EMAIL;TYPE=Other:banasiak@gmail.com\n" +
        "TEL;TYPE=VOICE;TYPE=Mobile:(210) 827-0127\n" +
        "TEL;TYPE=VOICE;TYPE=Work:(316) 833-1528\n" +
        "END:VCARD\n"

    val contact = Contact.Builder().apply {
      this.name = this@ContactTest.name
      this.emails = this@ContactTest.emails.toMutableList()
      this.phones = this@ContactTest.phones.toMutableList()
    }.build()

    assertEquals(contact.toVcf(), validVcf)
  }
}