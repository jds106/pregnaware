package database.entities

case class WrappedUser(userId: Int, displayName: String, email: String, friends: Seq[WrappedFriend])

