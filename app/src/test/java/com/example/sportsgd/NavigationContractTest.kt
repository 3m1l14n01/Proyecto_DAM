package com.example.sportsgd

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationContractTest {

    @Test
    fun graphContainsEveryRequiredFlowDestination() {
        val graph = File("src/main/res/navigation/nav_graph.xml")
        assertTrue("El grafo de navegación debe existir", graph.isFile)
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(graph)
        val xml = document.documentElement.textContent + graph.readText()

        listOf(
            "loginFragment",
            "recoverAccessFragment",
            "checkEmailFragment",
            "dashboardFragment",
            "playerListFragment",
            "playerFormFragment",
            "playerRegisteredFragment",
            "playerDetailFragment",
            "calendarFragment",
            "activityDetailFragment",
            "goalsFragment",
            "routineActiveFragment",
            "routineCompletedFragment",
            "notificationsFragment",
            "menuFragment",
            "profileFragment",
        ).forEach { destination ->
            assertTrue("Falta el destino $destination", xml.contains(destination))
        }
    }
}
