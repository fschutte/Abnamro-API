package nl.brachio.abnamro.model

import java.text.DecimalFormat

data class Transaction(
    val bookDate: String,
    val amount: Number,
    val counterPartyAccountNumber: String?,
    val counterPartyName: String?,
    val descriptionLines: Array<String>?
) {
    fun amountAsString(): String = DecimalFormat("#,##0.00").format(amount)
}
