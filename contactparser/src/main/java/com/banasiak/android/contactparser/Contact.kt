package com.banasiak.android.contactparser

class Contact private constructor(
  val name: String,
  val phones: List<Phone>,
  val emails: List<Email>
) {

  data class Phone(val number: String, val type: String)
  data class Email(val address: String, val type: String)

  data class Builder(
    var name: String? = null,
    var phones: MutableList<Phone> = mutableListOf(),
    var emails: MutableList<Email> = mutableListOf()
  ) {
    fun addPhone(phone: Phone) = this.phones.add(phone)
    fun addEmail(email: Email) = this.emails.add(email)
    fun build() = Contact(requireNotNull(this.name), this.phones.toList(), this.emails.toList())
  }

  fun toVcf(): String {
    val builder = StringBuilder()
    builder.appendLine("BEGIN:VCARD")
    builder.appendLine("VERSION:3.0")
    builder.appendLine("FN:$name")
    for (email in emails) {
      builder.appendLine("EMAIL;TYPE=${email.type}:${email.address}")
    }
    for (phone in phones) {
      builder.appendLine("TEL;TYPE=VOICE;TYPE=${phone.type}:${phone.number}")
    }
    builder.appendLine("END:VCARD")
    return builder.toString()
  }
}