package user.entities

case class ModifyUserRequest(userId: Int, displayName: String, email: String, passwordHash: String)
