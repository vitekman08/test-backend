package mobi.sevenwinds.app.budget

data class CreateAuthorRequest(val fullName: String)

data class AuthorResponse(
    val id: Int,
    val fullName: String,
    val createdAt: String
)