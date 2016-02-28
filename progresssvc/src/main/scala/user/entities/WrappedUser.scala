package user.entities

case class WrappedUser(
  userId: Int,
  displayName: String,
  email: String,
  passwordHash: String,
  friends: Seq[WrappedFriend])

