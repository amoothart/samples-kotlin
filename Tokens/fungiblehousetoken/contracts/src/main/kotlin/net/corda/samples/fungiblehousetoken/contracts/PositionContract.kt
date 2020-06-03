package net.corda.samples.fungiblehousetoken.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction


class PositionContract : Contract {
    override fun verify(tx: LedgerTransaction) {

    }

    companion object {
        @JvmStatic
        val ID = "net.corda.samples.fungiblehousetoken.contracts"
    }

    interface Commands : CommandData {
        class Create : Commands
    }
}