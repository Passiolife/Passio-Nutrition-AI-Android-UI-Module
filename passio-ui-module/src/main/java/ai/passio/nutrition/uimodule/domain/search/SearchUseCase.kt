package ai.passio.nutrition.uimodule.domain.search

import ai.passio.nutrition.uimodule.data.Repository
import ai.passio.passiosdk.passiofood.PassioFoodDataInfo

object SearchUseCase {

    private val repository = Repository.getInstance()

    suspend fun fetchSearchResults(query: String): Pair<List<PassioFoodDataInfo>, List<String>> {
        return repository.fetchSearchResults(query)
    }
}