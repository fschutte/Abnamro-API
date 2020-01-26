package nl.brachio.abnamro.service

import nl.brachio.abnamro.model.Account
import nl.brachio.abnamro.model.Transaction
import nl.brachio.abnamro.model.TransactionList

class AccountInformationService(private val abnamroRequestService: AbnamroRequestService) {

    suspend fun getAccount(accessToken: String, iban: String): Account {
        val accountDetails = callAccountDetails(accessToken, iban)
        val accountBalances =  callAccountBalances(accessToken, iban)

        return Account(
            accountDetails.accountNumber,
            accountDetails.accountHolderName,
            accountDetails.currency,
            accountBalances.amount)
    }

    suspend fun getTransactions(accessToken: String, iban: String): TransactionList {
        val aabTransactions = callTransactions(accessToken, iban)
        val transactions = aabTransactions.transactions.map { tx ->
            Transaction(tx.bookDate, tx.amount, tx.counterPartyAccountNumber, tx.counterPartyName, tx.descriptionLines)
        }
        return TransactionList(transactions)
    }

    private suspend fun callAccountDetails(accessToken: String, iban: String): AabAccountDetailsResponse =
        abnamroRequestService.doGetCall(accessToken, "/v1/accounts/$iban/details")

    private suspend fun callAccountBalances(accessToken: String, iban: String): AabAccountBalancesResponse =
        abnamroRequestService.doGetCall(accessToken, "/v1/accounts/$iban/balances")

    private suspend fun callTransactions(accessToken: String, iban: String): AabTransactionsResponse =
        abnamroRequestService.doGetCall(accessToken, "/v1/accounts/$iban/transactions")

}


data class AabAccountDetailsResponse(
    val accountNumber: String,
    val currency: String,
    val accountHolderName: String
)

data class AabAccountBalancesResponse(
    val accountNumber: String,
    val balanceType: String,
    val amount: Number,
    val currency: String
)

data class AabTransactionsResponse(
    val transactions: List<AabTransaction>
)

data class AabTransaction(
    val bookDate: String,
    val amount: Number,
    val counterPartyAccountNumber: String,
    val counterPartyName: String,
    val descriptionLines: Array<String>
)