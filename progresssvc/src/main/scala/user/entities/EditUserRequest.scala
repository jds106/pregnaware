package user.entities

case class EditUserRequest(displayName: String, email: String, passwordHash: String)
