package user.entities

import java.time.LocalDate

import naming.entities.WrappedBabyName

case class WrappedUser(
  userId: Int,
  displayName: String,
  email: String,
  dueDate: Option[LocalDate],
  babyNames: Seq[WrappedBabyName],
  passwordHash: String,
  friends: Seq[WrappedFriend])
