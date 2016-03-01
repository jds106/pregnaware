package pregnaware.user.entities

import java.time.LocalDate

case class WrappedFriendToBe(
  userId: Int,
  displayName: String,
  email: String,
  requestDate: LocalDate)
