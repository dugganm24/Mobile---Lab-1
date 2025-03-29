__Learning Notes__

Throughout the course of this portion of the lab, the group was able to learn a great deal about general best practices regarding android development, and important tools that are useful in Android applications. Some of the most important things we learned include Model-View-ViewModel (MVVM) architecture, Compose, Room, Hilt, Kotlin Coroutines and Flow, Gradle, Logcat, and how to generally navigate a large Android project.

This project displayed a practical application of MVVM architecture. It was very interesting to see how the Model (data) layer interacts with Room, how the View (UI) layer is built in particular with Compose for this case, and how ViewModel acts as the intermediary by managing UI states and handling the backend logic. We were also able to learn how StateFlow is used to handle the UI state from ViewModel and how viewModelScope manages coroutines for asynchronous operations, like database operations. We were also able to learn about how the repository abstracts data sources and provides an entry point for the data.

This project was also a good demo on how to use Compose as a declarative approach to UI development. We were able to learn how to create composable functions, manage UI state, and use modifiers to customize the layout of pages. We were also able to get a better understanding of some in-built Compose components for layout options like Column, Row, and Scaffold. We were also able to learn how composables react to state changes.

This project utilized Room for the database instance, and provided us a deeper understanding of how to define databases and DAOs. This was able to give us an understanding of how Room simplifies database operations. Along with Room, we also were able to observe the use of Hilt for dependency injection to better understand how to annotate classes for injection. We also learned how to use Hilt to provide ViewModels to composables. 

The use of Kotlin Coroutines and Flow in this project helped us to understand their importance in asynchronous operations. We learned how to use viewModelScope to manage coroutines in ViewModesl and how to use Flow to get data streams. We also saw how to use StateFlow to manage UI state and operators like combine to transform and combine data flows. 

The use of Gradle in this project highlighted its importance in ensuring consistent builds across development environments. We got a thorough understanding of how the wrapper ensures the correct Gradle versoin is used for thie project regardless of the local installation. 

The group was also able to add Timber tags throughout the project and view their output in Logcat, which provided us valuable experience in debugging an Android project.

Perhaps the most valuable information learned in this project was regarding the navgiation of a large Android project. This was the group's first time navigating any Android project, let alone one of this size, and doing so allowed us to get a thorough understanding of one type of Android architecture and what the essential components to investigate are. 

Overall, the group was able to learn a great deal of information throughout this exercise that will allow for future success in this course and beyond. 