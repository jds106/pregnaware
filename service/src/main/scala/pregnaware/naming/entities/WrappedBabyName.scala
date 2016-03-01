package pregnaware.naming.entities

import java.time.LocalDate

case class WrappedBabyName(
  nameId: Int, userId: Int,
  suggestedBy: Int, suggestedByName: String, suggestedDate: LocalDate,
  name: String, isBoy: Boolean)
