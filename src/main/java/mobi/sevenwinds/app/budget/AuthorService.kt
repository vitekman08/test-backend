package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction

object AuthorService {
    suspend fun createAuthor(request: AuthorResponse): AuthorResponse = withContext(Dispatchers.IO) {
        transaction {
            val author = AuthorTable.AuthorEntity.new {
                fullName = request.fullName
            }

            AuthorResponse(
                id = author.id.value,
                fullName = author.fullName,
                createdAt = author.createdAt.toString()
            )
        }
    }
}
