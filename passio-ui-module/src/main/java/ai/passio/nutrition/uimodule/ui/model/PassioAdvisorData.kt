package ai.passio.nutrition.uimodule.ui.model

import ai.passio.passiosdk.passiofood.data.model.PassioAdvisorResponse
import android.graphics.Bitmap


class PassioAdvisorData {
    var passioAdvisorResponse: PassioAdvisorResponse? = null
    val selectedFoodIndexes: MutableList<Int> = mutableListOf()
    var isLogged: Boolean = false
    var dataItemType: Int = TYPE_SENDER_TEXT
    var passioAdvisorSender: PassioAdvisorSender? = null

    fun isFindFoodEnabled(): Boolean {
        return passioAdvisorResponse?.tools?.contains("SearchIngredientMatches") ?: false
    }

    fun getIngredientContent(): String {
        return if (isLogged) {
            CONTENT_FOOD_LOGGED
        } else {
            passioAdvisorResponse?.markupContent ?: ""
        }
    }

    companion object {

        internal const val TYPE_PROCESSING = 1
        internal const val TYPE_WELCOME_INSTRUCTION = 2
        internal const val TYPE_SENDER_TEXT = 3
        internal const val TYPE_SENDER_IMAGES = 4
        internal const val TYPE_RECEIVER_TEXT = 5
        internal const val TYPE_RECEIVER_INGREDIENT = 6

        private const val INTRO_MESSAGE = "Welcome! I am your AI Nutrition Advisor!\n" +
                " \n" +
                "**You can ask me things like:**  \n" +
                "  • How many calories are in a yogurt?  \n" +
                "  • Create me a recipe for dinner?  \n" +
                "  • How can I adjust my diet for heart health?\n" +
                "\n" +
                "Let's chat!"

        private const val CONTENT_FOOD_LOGGED = "I’ve added the items below to your log:"
        private const val CONTENT_TEXT_RESPONSE = "Here are the items from the recipe above:"
        private const val CONTENT_IMAGE_RESPONSE =
            "Based on the image you took, I’ve recognized the following items. Please select the items you want and log them."

        fun createInstruction(): PassioAdvisorData {
            val passioAdvisorData = PassioAdvisorData()
            passioAdvisorData.dataItemType = TYPE_WELCOME_INSTRUCTION
            passioAdvisorData.passioAdvisorResponse = PassioAdvisorResponse(
                threadId = "0",
                messageId = "0",
                markupContent = INTRO_MESSAGE,
                rawContent = INTRO_MESSAGE,
                tools = null,
                extractedIngredients = null
            )
            return passioAdvisorData
        }

        fun createSender(textMessage: String): PassioAdvisorData {
            val passioAdvisorData = PassioAdvisorData()
            passioAdvisorData.dataItemType = TYPE_SENDER_TEXT
            val passioAdvisorSender = PassioAdvisorSender()
            passioAdvisorSender.createdAt = System.currentTimeMillis()
            passioAdvisorSender.textMessage = textMessage
            passioAdvisorData.passioAdvisorSender = passioAdvisorSender
            return passioAdvisorData
        }

        fun createSender(bitmaps: List<Bitmap>): PassioAdvisorData {
            val passioAdvisorData = PassioAdvisorData()
            passioAdvisorData.dataItemType = TYPE_SENDER_IMAGES
            val passioAdvisorSender = PassioAdvisorSender()
            passioAdvisorSender.createdAt = System.currentTimeMillis()
            passioAdvisorSender.bitmaps.clear()
            passioAdvisorSender.bitmaps.addAll(bitmaps)
            passioAdvisorData.passioAdvisorSender = passioAdvisorSender
            return passioAdvisorData
        }

        fun createProcessing(): PassioAdvisorData {
            val passioAdvisorData = PassioAdvisorData()
            passioAdvisorData.dataItemType = TYPE_PROCESSING
            return passioAdvisorData
        }

        fun createTextResponse(passioAdvisorResponse: PassioAdvisorResponse): PassioAdvisorData {
            val passioAdvisorData = PassioAdvisorData()
            passioAdvisorData.dataItemType = TYPE_RECEIVER_TEXT
            passioAdvisorData.passioAdvisorResponse = passioAdvisorResponse

            return passioAdvisorData
        }

        fun createIngredientsResponse(
            passioAdvisorResponse: PassioAdvisorResponse,
            questionType: Int
        ): PassioAdvisorData {
            val passioAdvisorData = PassioAdvisorData()
            passioAdvisorData.dataItemType = TYPE_RECEIVER_INGREDIENT
            if (questionType == TYPE_SENDER_IMAGES) {
                passioAdvisorData.passioAdvisorResponse =
                    passioAdvisorResponse.copy(
                        markupContent = CONTENT_IMAGE_RESPONSE,
                        rawContent = CONTENT_IMAGE_RESPONSE
                    )
            } else {
                passioAdvisorData.passioAdvisorResponse = passioAdvisorResponse.copy(
                    markupContent = CONTENT_TEXT_RESPONSE,
                    rawContent = CONTENT_TEXT_RESPONSE
                )
            }
            val size = passioAdvisorData.passioAdvisorResponse?.extractedIngredients?.size ?: 0
            if (size > 0) {
                passioAdvisorData.selectedFoodIndexes.addAll(0 until size)
            }
            return passioAdvisorData
        }
    }

}
