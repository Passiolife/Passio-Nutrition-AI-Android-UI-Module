# Passio-Nutrition-AI-Android-UI-Module

Welcome to Nutrition UI Module! 🍎🥗

Welcome to the Nutrition UI Module repository! This Android library provides a comprehensive set of UI components for building an engaging nutrition tracking experience. The module includes advanced features like AI-powered photo recognition of foods, making it easier for users to log their meals and monitor their nutritional intake.

## Import Nutrition UI Module into your app

1. Download the .aar from the [latest release](https://github.com/Passiolife/Passio-Nutrition-AI-Android-UI-Module/releases)
2. Import the .aar into your project
3. Add the dependencies located in the [build.gradle](https://github.com/Passiolife/Passio-Nutrition-AI-Android-UI-Module/blob/main/passio-ui-module/build.gradle) file of library module. Be sure to check the [repositories](https://github.com/Passiolife/Passio-Nutrition-AI-Android-UI-Module/blob/main/build.gradle) needed to pull the dependencies
4. Sync files with gradle and make sure you can access the Passio classes
5. Before starting the UI Module make sure to configure the SDK using `PassioSDK.instance.configure()`. Use the SDK provided by the [Passio Platform](https://www.passio.ai/). The configure must return `IS_READY_FOR_DETECTION` for the UI Module to work properly.
6. Start the UI Module by calling:
```kotlin
NutritionUIModule.launch(this) //Starts the activity of the UI Module
```

## PassioConnector

The `NutritionUIModule.launch` also accepts a `PassioConnector` interface to control the data flow of the UI Module. Create an implementation of the interface by providing functions that will store and retrieve data needed by the Module. Then pass the implementation to the launch functions and all of the data flow will use that specific connector.

## Color customisation

Any of the color defined in the [.xml](https://github.com/Passiolife/Passio-Nutrition-AI-Android-UI-Module/blob/main/passio-ui-module/src/main/res/values/colors.xml) file can be overridden in the app to change the appearance of the module. 

Example of ids that control the navigation bar and toolbar:
```
 <color name="passio_bottom_navigation_background">#FFFFFF</color>
 <color name="passio_bottom_navigation_selected">@color/passio_primary</color>
 <color name="passio_bottom_navigation_deselected">@color/passio_gray300</color>
 <color name="passio_toolbar_background">#FFFFFF</color>
 <color name="passio_toolbar_title">#000000</color>
 ```