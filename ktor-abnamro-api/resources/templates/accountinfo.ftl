<#-- @ftlvariable name="account" type="nl.brachio.abnamro.model.Account" -->
<#-- @ftlvariable name="transactionList" type="nl.brachio.abnamro.model.TransactionList" -->
<html>
    <head>
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm" crossorigin="anonymous">
        <title>
            ABNAMRO Account Info
        </title>
    </head>
    <body>
        <div class="container">
            <h1>Account Info</h1>
            <ul>
                <li>IBAN: ${account.accountNumber}</li>
                <li>Account holder: ${account.accountHolderName}</li>
                <li>Balance: ${account.currency} ${account.amount}</li>
            </ul>
            <h2>Transactions</h2>
            <table class="table table-striped">
                <tr>
                    <th>Book Date</th>
                    <th>Account Number</th>
                    <th>Account Holder</th>
                    <th>Amount</th>
                </tr>
                <#list transactionList.transactions as tx>
                    <tr>
                        <td>${tx.bookDate}</td>
                        <td>${tx.counterPartyAccountNumber!""}</td>
                        <td>${tx.counterPartyName!""}</td>
                        <td align="right">${tx.amountAsString()}</td>
                    </tr>
                    <tr>
                        <td></td>
                        <td colspan="3">
                            <#list tx.descriptionLines as line>
                                ${line}<br/>
                            </#list>
                        </td>
                    </tr>
                </#list>
            </table>
        </div>
    </body>
</html>
