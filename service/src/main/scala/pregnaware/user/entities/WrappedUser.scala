package pregnaware.user.entities

import java.time.LocalDate

import pregnaware.naming.entities.WrappedBabyName

case class WrappedUser(
  userId: Int,
  displayName: String,
  email: String,
  dueDate: Option[LocalDate],
  joinedDate: LocalDate,
  babyNames: Seq[WrappedBabyName],
  passwordHash: String,
  friends: Seq[WrappedFriend],
  friendRequestsSent: Seq[WrappedFriendToBe],
  friendRequestsReceived: Seq[WrappedFriendToBe])

