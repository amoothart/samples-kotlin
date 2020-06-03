package net.corda.samples.fungiblehousetoken.states

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.samples.fungiblehousetoken.contracts.PositionContract

@BelongsToContract(PositionContract::class)
data class PositionState(
        val quantity: Long,
        override val participants: List<AbstractParty>,
        override val linearId: UniqueIdentifier = UniqueIdentifier()
): LinearState {

}