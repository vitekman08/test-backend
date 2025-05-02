package mobi.sevenwinds.app.budget

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.CurrentDateTime

object AuthorTable : IntIdTable("author") {
    val fullName = varchar("full_name", 255)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime())

    class AuthorEntity(id: EntityID<Int>) : IntEntity(id) {
        companion object : IntEntityClass<AuthorEntity>(AuthorTable)

        var fullName by AuthorTable.fullName
        var createdAt by AuthorTable.createdAt
    }
}
