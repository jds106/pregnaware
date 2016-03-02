package pregnaware.user.entities

import java.time.{Instant, LocalDate}

import pregnaware.naming.entities.WrappedBabyName

case class WrappedUser(
  userId: Int,
  displayName: String,
  email: String,
  dueDate: Option[LocalDate],
  joinedDate: LocalDate,
  lastAccessedTime: Instant,
  babyNames: Seq[WrappedBabyName],
  passwordHash: String,
  friends: Seq[WrappedFriend],
  friendRequestsSent: Seq[WrappedFriend],
  friendRequestsReceived: Seq[WrappedFriend])

