package org.researchstack.backbone.utils

fun isOmronTask(taskKey: String?): Boolean {
    return taskKey == "b7c29473-96ec-44ae-ac8c-c71f9c8df954" // AM - BP Measurements task
            || taskKey == "b47c87bc-b42b-4fac-8e27-d92962e38ff7" // PM - BP Measurements task
            || taskKey == "0c0fabf5-505d-4a68-ae3b-153eb83a7afa"
}