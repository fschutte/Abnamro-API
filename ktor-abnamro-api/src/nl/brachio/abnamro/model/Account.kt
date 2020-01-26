package nl.brachio.abnamro.model

data class Account(
    val accountNumber: String,
    val accountHolderName: String,
    val currency: String,
    val amount: Number
)
