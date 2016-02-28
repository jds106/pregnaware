package user.entities

case class AddUserRequest(displayName: String, email: String, passwordHash: String)
