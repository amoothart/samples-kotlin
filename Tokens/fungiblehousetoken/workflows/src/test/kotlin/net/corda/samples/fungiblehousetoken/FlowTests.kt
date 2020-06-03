package net.corda.samples.fungiblehousetoken

import net.corda.core.utilities.getOrThrow
import net.corda.samples.fungiblehousetoken.flows.CreateHouseTokenFlow
import net.corda.samples.fungiblehousetoken.flows.IssueHouseTokenFlow
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.core.singleIdentity
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.StartedMockNode
import net.corda.testing.node.TestCordapp
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class FlowTests {
    private lateinit var network: MockNetwork
    private lateinit var a: StartedMockNode
    private lateinit var b: StartedMockNode
    private val symbol = "BigHouse"
    private val quantity = 100L

    @Before
    fun setup() {
        network = MockNetwork(
                MockNetworkParameters(
                        cordappsForAllNodes = listOf(
                                TestCordapp.findCordapp("net.corda.samples.fungiblehousetoken.contracts"),
                                TestCordapp.findCordapp("net.corda.samples.fungiblehousetoken.flows"),
                                TestCordapp.findCordapp("com.r3.corda.lib.tokens.contracts"),
                                TestCordapp.findCordapp("com.r3.corda.lib.tokens.workflows")
                        ),
                        networkParameters = testNetworkParameters(minimumPlatformVersion = 6)
                )
        )
        a = network.createNode()
        b = network.createNode()

//        init {
//            listOf(a, b).forEach {
//                it.registerInitiatedFlow(Responder::class.java)
//            }
//        }
        network.runNetwork()
    }


    @After
    fun tearDown() = network.stopNodes()

    @Test
    fun `Simultaneously issue tokens and a record of position`() {
        val createTokenFlow = CreateHouseTokenFlow(BigDecimal.valueOf(100000.00), symbol)
        a.startFlow(createTokenFlow)
        network.runNetwork()

        val flow = IssueHouseTokenFlow(symbol, quantity, b.info.singleIdentity())
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTransaction = future.getOrThrow()
        signedTransaction.verifyRequiredSignatures()
    }
}