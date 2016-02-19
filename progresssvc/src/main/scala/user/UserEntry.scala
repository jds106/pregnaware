package user

case class UserEntry(
  userId: Int,
  displayName: String,
  email: String,
  passwordHash: String)
