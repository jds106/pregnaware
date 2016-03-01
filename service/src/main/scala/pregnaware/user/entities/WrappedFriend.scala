package pregnaware.user.entities

import java.time.LocalDate

import pregnaware.naming.entities.WrappedBabyName

case class WrappedFriend(
  userId: Int,
  displayName: String,
  email: String,
  dueDate: Option[LocalDate],
  babyNames: Seq[WrappedBabyName],
  friendDate: LocalDate)
