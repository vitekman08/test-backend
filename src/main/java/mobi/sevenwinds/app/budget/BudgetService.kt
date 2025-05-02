package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like

object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = BudgetTable.BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.author = body.authorId?.let { AuthorTable.AuthorEntity.findById(it) }
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val query = BudgetTable
                .join(AuthorTable, JoinType.LEFT, onColumn = BudgetTable.authorId, otherColumn = AuthorTable.id)

            // Базовое условие - год
            val conditions = mutableListOf<Op<Boolean>>(BudgetTable.year eq param.year)

            // Добавляем условие фильтрации по автору, если оно задано
            param.authorFilter?.let { filter ->
                conditions.add(AuthorTable.fullName.lowerCase() like "%${filter.lowercase()}%")
            }

            // Объединяем все условия
            val whereCondition = conditions.reduce { acc, op -> acc and op }

            // Получаем все записи для года (для подсчета статистики)
            val allForYear = query.select(whereCondition)

            val total = allForYear.count()
            val totalByType = allForYear.groupBy { it[BudgetTable.type] }
                .mapValues { (_, rows) -> rows.sumOf { it[BudgetTable.amount] } }
                .mapKeys { it.key.name }

            // Получаем пагинированные данные
            val paginated = query
                .select(whereCondition)
                .orderBy(
                    BudgetTable.month to SortOrder.ASC,
                    BudgetTable.amount to SortOrder.DESC
                )
                .limit(param.limit, param.offset)

            val data = paginated.map { row ->
                BudgetRecord(
                    year = row[BudgetTable.year],
                    month = row[BudgetTable.month],
                    amount = row[BudgetTable.amount],
                    type = row[BudgetTable.type],
                    authorName = row.getOrNull(AuthorTable.fullName),
                    authorCreatedAt = row.getOrNull(AuthorTable.createdAt)?.toString(),
                    authorId = row.getOrNull(BudgetTable.authorId)?.value
                )
            }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = totalByType,
                items = data
            )
        }
    }
}