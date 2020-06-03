package net.corda.samples.fungiblehousetoken.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.utilities.heldBy
import com.r3.corda.lib.tokens.contracts.utilities.issuedBy
import com.r3.corda.lib.tokens.contracts.utilities.of
import com.r3.corda.lib.tokens.workflows.flows.issue.addIssueTokens
import com.r3.corda.lib.tokens.workflows.utilities.addTokenTypeJar
import com.r3.corda.lib.tokens.workflows.utilities.getPreferredNotary
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.samples.fungiblehousetoken.contracts.PositionContract
import net.corda.samples.fungiblehousetoken.states.FungibleHouseTokenState
import net.corda.samples.fungiblehousetoken.states.PositionState

@StartableByRPC
@InitiatingFlow
class IssueHouseTokenFlow(val symbol: String,
                          val quantity: Long,
                          val holder:Party) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {

        //get house states on ledger with uuid as input tokenId
        val stateAndRef = serviceHub.vaultService.queryBy<FungibleHouseTokenState>()
                .states.filter { it.state.data.symbol.equals(symbol) }.first()

        //get the RealEstateEvolvableTokenType object
        val houseTokenType = stateAndRef.state.data

        //get the pointer pointer to the house
        val tokenPointer = houseTokenType.toPointer<FungibleHouseTokenState>()

        //assign the issuer to the house type who will be issuing the tokens
        val fungibleTokens  = quantity of tokenPointer issuedBy ourIdentity heldBy holder

        val txnBuilder = TransactionBuilder(notary = getPreferredNotary(serviceHub))
        addIssueTokens(txnBuilder, fungibleTokens)

        val position = PositionState(quantity, listOf(holder))
        txnBuilder.addOutputState(position)
        txnBuilder.addCommand(PositionContract.Commands.Create(), position.participants.map { it.owningKey })

        val ptx = serviceHub.signInitialTransaction(txnBuilder)

        val sessions = fungibleTokens.participants.union(position.participants).minus(ourIdentity).map { initiateFlow(it) }
        val stx = subFlow(CollectSignaturesFlow(ptx, sessions))

        return subFlow(FinalityFlow(stx, sessions))
    }
}

@InitiatedBy(IssueHouseTokenFlow::class)
class IssueHouseTokenFlowResponder(val otherSession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val signTransactionFlow = object : SignTransactionFlow(otherSession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                //no requirements
            }
        }
        val txId = subFlow(signTransactionFlow).id

        return subFlow(ReceiveFinalityFlow(otherSession, expectedTxId = txId))
    }

}
