package com.phone.tracker.data.api.model

data class LoginResposeModel(
    val login: List<LoginModel> = emptyList(),
    val message: String = "",
    val status: Int = 0
)
data class LoginModel(
    val userId: String,
    val roleId: String,
    val userName: String,
    val empCode: String,
    val empEmail: String,
    val profileImage: String?
)