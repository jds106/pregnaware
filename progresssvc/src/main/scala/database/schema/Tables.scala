package database.schema
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = slick.driver.MySQLDriver
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: slick.driver.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = Array(Babyname.schema, Friend.schema, Progress.schema, Session.schema, User.schema, Userlayout.schema).reduceLeft(_ ++ _)
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table Babyname
   *  @param id Database column Id SqlType(INT), PrimaryKey
   *  @param userid Database column UserId SqlType(INT)
   *  @param name Database column Name SqlType(VARCHAR), Length(45,true)
   *  @param isboy Database column IsBoy SqlType(BIT)
   *  @param suggestedby Database column SuggestedBy SqlType(INT) */
  case class BabynameRow(id: Int, userid: Int, name: String, isboy: Boolean, suggestedby: Int)
  /** GetResult implicit for fetching BabynameRow objects using plain SQL queries */
  implicit def GetResultBabynameRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Boolean]): GR[BabynameRow] = GR{
    prs => import prs._
    BabynameRow.tupled((<<[Int], <<[Int], <<[String], <<[Boolean], <<[Int]))
  }
  /** Table description of table BabyName. Objects of this class serve as prototypes for rows in queries. */
  class Babyname(_tableTag: Tag) extends Table[BabynameRow](_tableTag, "BabyName") {
    def * = (id, userid, name, isboy, suggestedby) <> (BabynameRow.tupled, BabynameRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(userid), Rep.Some(name), Rep.Some(isboy), Rep.Some(suggestedby)).shaped.<>({r=>import r._; _1.map(_=> BabynameRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column Id SqlType(INT), PrimaryKey */
    val id: Rep[Int] = column[Int]("Id", O.PrimaryKey)
    /** Database column UserId SqlType(INT) */
    val userid: Rep[Int] = column[Int]("UserId")
    /** Database column Name SqlType(VARCHAR), Length(45,true) */
    val name: Rep[String] = column[String]("Name", O.Length(45,varying=true))
    /** Database column IsBoy SqlType(BIT) */
    val isboy: Rep[Boolean] = column[Boolean]("IsBoy")
    /** Database column SuggestedBy SqlType(INT) */
    val suggestedby: Rep[Int] = column[Int]("SuggestedBy")

    /** Foreign key referencing User (database name BabyName_User_SuggestedBy) */
    lazy val userFk1 = foreignKey("BabyName_User_SuggestedBy", suggestedby, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
    /** Foreign key referencing User (database name BabyName_User_UserId) */
    lazy val userFk2 = foreignKey("BabyName_User_UserId", userid, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table Babyname */
  lazy val Babyname = new TableQuery(tag => new Babyname(tag))

  /** Entity class storing rows of table Friend
   *  @param id Database column Id SqlType(INT), PrimaryKey
   *  @param userid1 Database column UserId1 SqlType(INT)
   *  @param userid2 Database column UserId2 SqlType(INT) */
  case class FriendRow(id: Int, userid1: Int, userid2: Int)
  /** GetResult implicit for fetching FriendRow objects using plain SQL queries */
  implicit def GetResultFriendRow(implicit e0: GR[Int]): GR[FriendRow] = GR{
    prs => import prs._
    FriendRow.tupled((<<[Int], <<[Int], <<[Int]))
  }
  /** Table description of table Friend. Objects of this class serve as prototypes for rows in queries. */
  class Friend(_tableTag: Tag) extends Table[FriendRow](_tableTag, "Friend") {
    def * = (id, userid1, userid2) <> (FriendRow.tupled, FriendRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(userid1), Rep.Some(userid2)).shaped.<>({r=>import r._; _1.map(_=> FriendRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column Id SqlType(INT), PrimaryKey */
    val id: Rep[Int] = column[Int]("Id", O.PrimaryKey)
    /** Database column UserId1 SqlType(INT) */
    val userid1: Rep[Int] = column[Int]("UserId1")
    /** Database column UserId2 SqlType(INT) */
    val userid2: Rep[Int] = column[Int]("UserId2")

    /** Foreign key referencing User (database name Friend_User_UserId1) */
    lazy val userFk1 = foreignKey("Friend_User_UserId1", userid1, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
    /** Foreign key referencing User (database name Friend_User_UserId2) */
    lazy val userFk2 = foreignKey("Friend_User_UserId2", userid2, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table Friend */
  lazy val Friend = new TableQuery(tag => new Friend(tag))

  /** Entity class storing rows of table Progress
   *  @param userid Database column UserId SqlType(INT), PrimaryKey
   *  @param duedate Database column DueDate SqlType(DATE) */
  case class ProgressRow(userid: Int, duedate: java.sql.Date)
  /** GetResult implicit for fetching ProgressRow objects using plain SQL queries */
  implicit def GetResultProgressRow(implicit e0: GR[Int], e1: GR[java.sql.Date]): GR[ProgressRow] = GR{
    prs => import prs._
    ProgressRow.tupled((<<[Int], <<[java.sql.Date]))
  }
  /** Table description of table Progress. Objects of this class serve as prototypes for rows in queries. */
  class Progress(_tableTag: Tag) extends Table[ProgressRow](_tableTag, "Progress") {
    def * = (userid, duedate) <> (ProgressRow.tupled, ProgressRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(userid), Rep.Some(duedate)).shaped.<>({r=>import r._; _1.map(_=> ProgressRow.tupled((_1.get, _2.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column UserId SqlType(INT), PrimaryKey */
    val userid: Rep[Int] = column[Int]("UserId", O.PrimaryKey)
    /** Database column DueDate SqlType(DATE) */
    val duedate: Rep[java.sql.Date] = column[java.sql.Date]("DueDate")

    /** Foreign key referencing User (database name Progress_User_UserId) */
    lazy val userFk = foreignKey("Progress_User_UserId", userid, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table Progress */
  lazy val Progress = new TableQuery(tag => new Progress(tag))

  /** Entity class storing rows of table Session
   *  @param id Database column Id SqlType(VARCHAR), PrimaryKey, Length(150,true)
   *  @param userid Database column UserId SqlType(INT) */
  case class SessionRow(id: String, userid: Int)
  /** GetResult implicit for fetching SessionRow objects using plain SQL queries */
  implicit def GetResultSessionRow(implicit e0: GR[String], e1: GR[Int]): GR[SessionRow] = GR{
    prs => import prs._
    SessionRow.tupled((<<[String], <<[Int]))
  }
  /** Table description of table Session. Objects of this class serve as prototypes for rows in queries. */
  class Session(_tableTag: Tag) extends Table[SessionRow](_tableTag, "Session") {
    def * = (id, userid) <> (SessionRow.tupled, SessionRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(userid)).shaped.<>({r=>import r._; _1.map(_=> SessionRow.tupled((_1.get, _2.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column Id SqlType(VARCHAR), PrimaryKey, Length(150,true) */
    val id: Rep[String] = column[String]("Id", O.PrimaryKey, O.Length(150,varying=true))
    /** Database column UserId SqlType(INT) */
    val userid: Rep[Int] = column[Int]("UserId")

    /** Foreign key referencing User (database name Session_User_UserId) */
    lazy val userFk = foreignKey("Session_User_UserId", userid, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table Session */
  lazy val Session = new TableQuery(tag => new Session(tag))

  /** Entity class storing rows of table User
   *  @param id Database column Id SqlType(INT), AutoInc, PrimaryKey
   *  @param displayname Database column DisplayName SqlType(VARCHAR), Length(100,true)
   *  @param email Database column Email SqlType(VARCHAR), Length(100,true)
   *  @param passwordhash Database column PasswordHash SqlType(VARCHAR), Length(200,true) */
  case class UserRow(id: Int, displayname: String, email: String, passwordhash: String)
  /** GetResult implicit for fetching UserRow objects using plain SQL queries */
  implicit def GetResultUserRow(implicit e0: GR[Int], e1: GR[String]): GR[UserRow] = GR{
    prs => import prs._
    UserRow.tupled((<<[Int], <<[String], <<[String], <<[String]))
  }
  /** Table description of table User. Objects of this class serve as prototypes for rows in queries. */
  class User(_tableTag: Tag) extends Table[UserRow](_tableTag, "User") {
    def * = (id, displayname, email, passwordhash) <> (UserRow.tupled, UserRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(displayname), Rep.Some(email), Rep.Some(passwordhash)).shaped.<>({r=>import r._; _1.map(_=> UserRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column Id SqlType(INT), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("Id", O.AutoInc, O.PrimaryKey)
    /** Database column DisplayName SqlType(VARCHAR), Length(100,true) */
    val displayname: Rep[String] = column[String]("DisplayName", O.Length(100,varying=true))
    /** Database column Email SqlType(VARCHAR), Length(100,true) */
    val email: Rep[String] = column[String]("Email", O.Length(100,varying=true))
    /** Database column PasswordHash SqlType(VARCHAR), Length(200,true) */
    val passwordhash: Rep[String] = column[String]("PasswordHash", O.Length(200,varying=true))
  }
  /** Collection-like TableQuery object for table User */
  lazy val User = new TableQuery(tag => new User(tag))

  /** Entity class storing rows of table Userlayout
   *  @param userid Database column UserId SqlType(INT), PrimaryKey
   *  @param layout Database column Layout SqlType(VARCHAR), Length(1000,true) */
  case class UserlayoutRow(userid: Int, layout: String)
  /** GetResult implicit for fetching UserlayoutRow objects using plain SQL queries */
  implicit def GetResultUserlayoutRow(implicit e0: GR[Int], e1: GR[String]): GR[UserlayoutRow] = GR{
    prs => import prs._
    UserlayoutRow.tupled((<<[Int], <<[String]))
  }
  /** Table description of table UserLayout. Objects of this class serve as prototypes for rows in queries. */
  class Userlayout(_tableTag: Tag) extends Table[UserlayoutRow](_tableTag, "UserLayout") {
    def * = (userid, layout) <> (UserlayoutRow.tupled, UserlayoutRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(userid), Rep.Some(layout)).shaped.<>({r=>import r._; _1.map(_=> UserlayoutRow.tupled((_1.get, _2.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column UserId SqlType(INT), PrimaryKey */
    val userid: Rep[Int] = column[Int]("UserId", O.PrimaryKey)
    /** Database column Layout SqlType(VARCHAR), Length(1000,true) */
    val layout: Rep[String] = column[String]("Layout", O.Length(1000,varying=true))

    /** Foreign key referencing User (database name UserLayout_User_UserId) */
    lazy val userFk = foreignKey("UserLayout_User_UserId", userid, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table Userlayout */
  lazy val Userlayout = new TableQuery(tag => new Userlayout(tag))
}
