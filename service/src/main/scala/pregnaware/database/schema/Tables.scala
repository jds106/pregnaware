package pregnaware.database.schema
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
  lazy val schema: profile.SchemaDescription = Array(Babyname.schema, Friend.schema, Namestat.schema, Namestatbycountry.schema, Namestatbymonth.schema, Namestatbyregion.schema, Session.schema, User.schema, Userstate.schema).reduceLeft(_ ++ _)
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table Babyname
   *  @param id Database column Id SqlType(INT), AutoInc, PrimaryKey
   *  @param userid Database column UserId SqlType(INT)
   *  @param name Database column Name SqlType(VARCHAR), Length(45,true)
   *  @param isboy Database column IsBoy SqlType(BIT)
   *  @param suggestedby Database column SuggestedBy SqlType(INT)
   *  @param date Database column Date SqlType(DATE) */
  case class BabynameRow(id: Int, userid: Int, name: String, isboy: Boolean, suggestedby: Int, date: java.sql.Date)
  /** GetResult implicit for fetching BabynameRow objects using plain SQL queries */
  implicit def GetResultBabynameRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Boolean], e3: GR[java.sql.Date]): GR[BabynameRow] = GR{
    prs => import prs._
    BabynameRow.tupled((<<[Int], <<[Int], <<[String], <<[Boolean], <<[Int], <<[java.sql.Date]))
  }
  /** Table description of table BabyName. Objects of this class serve as prototypes for rows in queries. */
  class Babyname(_tableTag: Tag) extends Table[BabynameRow](_tableTag, "BabyName") {
    def * = (id, userid, name, isboy, suggestedby, date) <> (BabynameRow.tupled, BabynameRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(userid), Rep.Some(name), Rep.Some(isboy), Rep.Some(suggestedby), Rep.Some(date)).shaped.<>({r=>import r._; _1.map(_=> BabynameRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column Id SqlType(INT), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("Id", O.AutoInc, O.PrimaryKey)
    /** Database column UserId SqlType(INT) */
    val userid: Rep[Int] = column[Int]("UserId")
    /** Database column Name SqlType(VARCHAR), Length(45,true) */
    val name: Rep[String] = column[String]("Name", O.Length(45,varying=true))
    /** Database column IsBoy SqlType(BIT) */
    val isboy: Rep[Boolean] = column[Boolean]("IsBoy")
    /** Database column SuggestedBy SqlType(INT) */
    val suggestedby: Rep[Int] = column[Int]("SuggestedBy")
    /** Database column Date SqlType(DATE) */
    val date: Rep[java.sql.Date] = column[java.sql.Date]("Date")

    /** Foreign key referencing User (database name BabyName_User_SuggestedBy) */
    lazy val userFk1 = foreignKey("BabyName_User_SuggestedBy", suggestedby, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
    /** Foreign key referencing User (database name BabyName_User_UserId) */
    lazy val userFk2 = foreignKey("BabyName_User_UserId", userid, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table Babyname */
  lazy val Babyname = new TableQuery(tag => new Babyname(tag))

  /** Entity class storing rows of table Friend
   *  @param id Database column Id SqlType(INT), AutoInc, PrimaryKey
   *  @param senderid Database column SenderId SqlType(INT)
   *  @param receiverid Database column ReceiverId SqlType(INT)
   *  @param isconfirmed Database column IsConfirmed SqlType(BIT)
   *  @param isblocked Database column IsBlocked SqlType(BIT)
   *  @param date Database column Date SqlType(DATE) */
  case class FriendRow(id: Int, senderid: Int, receiverid: Int, isconfirmed: Boolean, isblocked: Boolean, date: java.sql.Date)
  /** GetResult implicit for fetching FriendRow objects using plain SQL queries */
  implicit def GetResultFriendRow(implicit e0: GR[Int], e1: GR[Boolean], e2: GR[java.sql.Date]): GR[FriendRow] = GR{
    prs => import prs._
    FriendRow.tupled((<<[Int], <<[Int], <<[Int], <<[Boolean], <<[Boolean], <<[java.sql.Date]))
  }
  /** Table description of table Friend. Objects of this class serve as prototypes for rows in queries. */
  class Friend(_tableTag: Tag) extends Table[FriendRow](_tableTag, "Friend") {
    def * = (id, senderid, receiverid, isconfirmed, isblocked, date) <> (FriendRow.tupled, FriendRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(senderid), Rep.Some(receiverid), Rep.Some(isconfirmed), Rep.Some(isblocked), Rep.Some(date)).shaped.<>({r=>import r._; _1.map(_=> FriendRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column Id SqlType(INT), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("Id", O.AutoInc, O.PrimaryKey)
    /** Database column SenderId SqlType(INT) */
    val senderid: Rep[Int] = column[Int]("SenderId")
    /** Database column ReceiverId SqlType(INT) */
    val receiverid: Rep[Int] = column[Int]("ReceiverId")
    /** Database column IsConfirmed SqlType(BIT) */
    val isconfirmed: Rep[Boolean] = column[Boolean]("IsConfirmed")
    /** Database column IsBlocked SqlType(BIT) */
    val isblocked: Rep[Boolean] = column[Boolean]("IsBlocked")
    /** Database column Date SqlType(DATE) */
    val date: Rep[java.sql.Date] = column[java.sql.Date]("Date")

    /** Foreign key referencing User (database name Friend_User_UserId1) */
    lazy val userFk1 = foreignKey("Friend_User_UserId1", senderid, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
    /** Foreign key referencing User (database name Friend_User_UserId2) */
    lazy val userFk2 = foreignKey("Friend_User_UserId2", receiverid, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table Friend */
  lazy val Friend = new TableQuery(tag => new Friend(tag))

  /** Entity class storing rows of table Namestat
   *  @param name Database column Name SqlType(VARCHAR), Length(50,true)
   *  @param gender Database column Gender SqlType(VARCHAR), Length(5,true)
   *  @param year Database column Year SqlType(INT)
   *  @param count Database column Count SqlType(INT) */
  case class NamestatRow(name: String, gender: String, year: Int, count: Int)
  /** GetResult implicit for fetching NamestatRow objects using plain SQL queries */
  implicit def GetResultNamestatRow(implicit e0: GR[String], e1: GR[Int]): GR[NamestatRow] = GR{
    prs => import prs._
    NamestatRow.tupled((<<[String], <<[String], <<[Int], <<[Int]))
  }
  /** Table description of table NameStat. Objects of this class serve as prototypes for rows in queries. */
  class Namestat(_tableTag: Tag) extends Table[NamestatRow](_tableTag, "NameStat") {
    def * = (name, gender, year, count) <> (NamestatRow.tupled, NamestatRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(name), Rep.Some(gender), Rep.Some(year), Rep.Some(count)).shaped.<>({r=>import r._; _1.map(_=> NamestatRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column Name SqlType(VARCHAR), Length(50,true) */
    val name: Rep[String] = column[String]("Name", O.Length(50,varying=true))
    /** Database column Gender SqlType(VARCHAR), Length(5,true) */
    val gender: Rep[String] = column[String]("Gender", O.Length(5,varying=true))
    /** Database column Year SqlType(INT) */
    val year: Rep[Int] = column[Int]("Year")
    /** Database column Count SqlType(INT) */
    val count: Rep[Int] = column[Int]("Count")

    /** Primary key of Namestat (database name NameStat_PK) */
    val pk = primaryKey("NameStat_PK", (name, gender, year))
  }
  /** Collection-like TableQuery object for table Namestat */
  lazy val Namestat = new TableQuery(tag => new Namestat(tag))

  /** Entity class storing rows of table Namestatbycountry
   *  @param name Database column Name SqlType(VARCHAR), Length(50,true)
   *  @param gender Database column Gender SqlType(VARCHAR), Length(5,true)
   *  @param year Database column Year SqlType(INT)
   *  @param country Database column Country SqlType(VARCHAR), Length(50,true)
   *  @param count Database column Count SqlType(INT) */
  case class NamestatbycountryRow(name: String, gender: String, year: Int, country: String, count: Int)
  /** GetResult implicit for fetching NamestatbycountryRow objects using plain SQL queries */
  implicit def GetResultNamestatbycountryRow(implicit e0: GR[String], e1: GR[Int]): GR[NamestatbycountryRow] = GR{
    prs => import prs._
    NamestatbycountryRow.tupled((<<[String], <<[String], <<[Int], <<[String], <<[Int]))
  }
  /** Table description of table NameStatByCountry. Objects of this class serve as prototypes for rows in queries. */
  class Namestatbycountry(_tableTag: Tag) extends Table[NamestatbycountryRow](_tableTag, "NameStatByCountry") {
    def * = (name, gender, year, country, count) <> (NamestatbycountryRow.tupled, NamestatbycountryRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(name), Rep.Some(gender), Rep.Some(year), Rep.Some(country), Rep.Some(count)).shaped.<>({r=>import r._; _1.map(_=> NamestatbycountryRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column Name SqlType(VARCHAR), Length(50,true) */
    val name: Rep[String] = column[String]("Name", O.Length(50,varying=true))
    /** Database column Gender SqlType(VARCHAR), Length(5,true) */
    val gender: Rep[String] = column[String]("Gender", O.Length(5,varying=true))
    /** Database column Year SqlType(INT) */
    val year: Rep[Int] = column[Int]("Year")
    /** Database column Country SqlType(VARCHAR), Length(50,true) */
    val country: Rep[String] = column[String]("Country", O.Length(50,varying=true))
    /** Database column Count SqlType(INT) */
    val count: Rep[Int] = column[Int]("Count")

    /** Primary key of Namestatbycountry (database name NameStatByCountry_PK) */
    val pk = primaryKey("NameStatByCountry_PK", (name, gender, year, country))
  }
  /** Collection-like TableQuery object for table Namestatbycountry */
  lazy val Namestatbycountry = new TableQuery(tag => new Namestatbycountry(tag))

  /** Entity class storing rows of table Namestatbymonth
   *  @param name Database column Name SqlType(VARCHAR), Length(50,true)
   *  @param gender Database column Gender SqlType(VARCHAR), Length(5,true)
   *  @param year Database column Year SqlType(INT)
   *  @param month Database column Month SqlType(VARCHAR), Length(3,true)
   *  @param count Database column Count SqlType(INT) */
  case class NamestatbymonthRow(name: String, gender: String, year: Int, month: String, count: Int)
  /** GetResult implicit for fetching NamestatbymonthRow objects using plain SQL queries */
  implicit def GetResultNamestatbymonthRow(implicit e0: GR[String], e1: GR[Int]): GR[NamestatbymonthRow] = GR{
    prs => import prs._
    NamestatbymonthRow.tupled((<<[String], <<[String], <<[Int], <<[String], <<[Int]))
  }
  /** Table description of table NameStatByMonth. Objects of this class serve as prototypes for rows in queries. */
  class Namestatbymonth(_tableTag: Tag) extends Table[NamestatbymonthRow](_tableTag, "NameStatByMonth") {
    def * = (name, gender, year, month, count) <> (NamestatbymonthRow.tupled, NamestatbymonthRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(name), Rep.Some(gender), Rep.Some(year), Rep.Some(month), Rep.Some(count)).shaped.<>({r=>import r._; _1.map(_=> NamestatbymonthRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column Name SqlType(VARCHAR), Length(50,true) */
    val name: Rep[String] = column[String]("Name", O.Length(50,varying=true))
    /** Database column Gender SqlType(VARCHAR), Length(5,true) */
    val gender: Rep[String] = column[String]("Gender", O.Length(5,varying=true))
    /** Database column Year SqlType(INT) */
    val year: Rep[Int] = column[Int]("Year")
    /** Database column Month SqlType(VARCHAR), Length(3,true) */
    val month: Rep[String] = column[String]("Month", O.Length(3,varying=true))
    /** Database column Count SqlType(INT) */
    val count: Rep[Int] = column[Int]("Count")

    /** Primary key of Namestatbymonth (database name NameStatByMonth_PK) */
    val pk = primaryKey("NameStatByMonth_PK", (name, gender, year, month))
  }
  /** Collection-like TableQuery object for table Namestatbymonth */
  lazy val Namestatbymonth = new TableQuery(tag => new Namestatbymonth(tag))

  /** Entity class storing rows of table Namestatbyregion
   *  @param name Database column Name SqlType(VARCHAR), Length(50,true)
   *  @param gender Database column Gender SqlType(VARCHAR), Length(5,true)
   *  @param year Database column Year SqlType(INT)
   *  @param region Database column Region SqlType(VARCHAR), Length(100,true)
   *  @param count Database column Count SqlType(INT) */
  case class NamestatbyregionRow(name: String, gender: String, year: Int, region: String, count: Int)
  /** GetResult implicit for fetching NamestatbyregionRow objects using plain SQL queries */
  implicit def GetResultNamestatbyregionRow(implicit e0: GR[String], e1: GR[Int]): GR[NamestatbyregionRow] = GR{
    prs => import prs._
    NamestatbyregionRow.tupled((<<[String], <<[String], <<[Int], <<[String], <<[Int]))
  }
  /** Table description of table NameStatByRegion. Objects of this class serve as prototypes for rows in queries. */
  class Namestatbyregion(_tableTag: Tag) extends Table[NamestatbyregionRow](_tableTag, "NameStatByRegion") {
    def * = (name, gender, year, region, count) <> (NamestatbyregionRow.tupled, NamestatbyregionRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(name), Rep.Some(gender), Rep.Some(year), Rep.Some(region), Rep.Some(count)).shaped.<>({r=>import r._; _1.map(_=> NamestatbyregionRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column Name SqlType(VARCHAR), Length(50,true) */
    val name: Rep[String] = column[String]("Name", O.Length(50,varying=true))
    /** Database column Gender SqlType(VARCHAR), Length(5,true) */
    val gender: Rep[String] = column[String]("Gender", O.Length(5,varying=true))
    /** Database column Year SqlType(INT) */
    val year: Rep[Int] = column[Int]("Year")
    /** Database column Region SqlType(VARCHAR), Length(100,true) */
    val region: Rep[String] = column[String]("Region", O.Length(100,varying=true))
    /** Database column Count SqlType(INT) */
    val count: Rep[Int] = column[Int]("Count")

    /** Primary key of Namestatbyregion (database name NameStatByRegion_PK) */
    val pk = primaryKey("NameStatByRegion_PK", (name, gender, year, region))
  }
  /** Collection-like TableQuery object for table Namestatbyregion */
  lazy val Namestatbyregion = new TableQuery(tag => new Namestatbyregion(tag))

  /** Entity class storing rows of table Session
   *  @param id Database column Id SqlType(VARCHAR), PrimaryKey, Length(150,true)
   *  @param userid Database column UserId SqlType(INT)
   *  @param accesstime Database column AccessTime SqlType(BIGINT) */
  case class SessionRow(id: String, userid: Int, accesstime: Long)
  /** GetResult implicit for fetching SessionRow objects using plain SQL queries */
  implicit def GetResultSessionRow(implicit e0: GR[String], e1: GR[Int], e2: GR[Long]): GR[SessionRow] = GR{
    prs => import prs._
    SessionRow.tupled((<<[String], <<[Int], <<[Long]))
  }
  /** Table description of table Session. Objects of this class serve as prototypes for rows in queries. */
  class Session(_tableTag: Tag) extends Table[SessionRow](_tableTag, "Session") {
    def * = (id, userid, accesstime) <> (SessionRow.tupled, SessionRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(userid), Rep.Some(accesstime)).shaped.<>({r=>import r._; _1.map(_=> SessionRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column Id SqlType(VARCHAR), PrimaryKey, Length(150,true) */
    val id: Rep[String] = column[String]("Id", O.PrimaryKey, O.Length(150,varying=true))
    /** Database column UserId SqlType(INT) */
    val userid: Rep[Int] = column[Int]("UserId")
    /** Database column AccessTime SqlType(BIGINT) */
    val accesstime: Rep[Long] = column[Long]("AccessTime")

    /** Foreign key referencing User (database name Session_User_UserId) */
    lazy val userFk = foreignKey("Session_User_UserId", userid, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table Session */
  lazy val Session = new TableQuery(tag => new Session(tag))

  /** Entity class storing rows of table User
   *  @param id Database column Id SqlType(INT), AutoInc, PrimaryKey
   *  @param displayname Database column DisplayName SqlType(VARCHAR), Length(100,true)
   *  @param email Database column Email SqlType(VARCHAR), Length(100,true)
   *  @param passwordhash Database column PasswordHash SqlType(VARCHAR), Length(200,true)
   *  @param duedate Database column DueDate SqlType(DATE), Default(None)
   *  @param joindate Database column JoinDate SqlType(DATE) */
  case class UserRow(id: Int, displayname: String, email: String, passwordhash: String, duedate: Option[java.sql.Date] = None, joindate: java.sql.Date)
  /** GetResult implicit for fetching UserRow objects using plain SQL queries */
  implicit def GetResultUserRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Option[java.sql.Date]], e3: GR[java.sql.Date]): GR[UserRow] = GR{
    prs => import prs._
    UserRow.tupled((<<[Int], <<[String], <<[String], <<[String], <<?[java.sql.Date], <<[java.sql.Date]))
  }
  /** Table description of table User. Objects of this class serve as prototypes for rows in queries. */
  class User(_tableTag: Tag) extends Table[UserRow](_tableTag, "User") {
    def * = (id, displayname, email, passwordhash, duedate, joindate) <> (UserRow.tupled, UserRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(displayname), Rep.Some(email), Rep.Some(passwordhash), duedate, Rep.Some(joindate)).shaped.<>({r=>import r._; _1.map(_=> UserRow.tupled((_1.get, _2.get, _3.get, _4.get, _5, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column Id SqlType(INT), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("Id", O.AutoInc, O.PrimaryKey)
    /** Database column DisplayName SqlType(VARCHAR), Length(100,true) */
    val displayname: Rep[String] = column[String]("DisplayName", O.Length(100,varying=true))
    /** Database column Email SqlType(VARCHAR), Length(100,true) */
    val email: Rep[String] = column[String]("Email", O.Length(100,varying=true))
    /** Database column PasswordHash SqlType(VARCHAR), Length(200,true) */
    val passwordhash: Rep[String] = column[String]("PasswordHash", O.Length(200,varying=true))
    /** Database column DueDate SqlType(DATE), Default(None) */
    val duedate: Rep[Option[java.sql.Date]] = column[Option[java.sql.Date]]("DueDate", O.Default(None))
    /** Database column JoinDate SqlType(DATE) */
    val joindate: Rep[java.sql.Date] = column[java.sql.Date]("JoinDate")

    /** Uniqueness Index over (email) (database name User_Email_Unique) */
    val index1 = index("User_Email_Unique", email, unique=true)
  }
  /** Collection-like TableQuery object for table User */
  lazy val User = new TableQuery(tag => new User(tag))

  /** Entity class storing rows of table Userstate
   *  @param userid Database column UserId SqlType(INT), PrimaryKey
   *  @param state Database column State SqlType(VARCHAR), Length(1000,true) */
  case class UserstateRow(userid: Int, state: String)
  /** GetResult implicit for fetching UserstateRow objects using plain SQL queries */
  implicit def GetResultUserstateRow(implicit e0: GR[Int], e1: GR[String]): GR[UserstateRow] = GR{
    prs => import prs._
    UserstateRow.tupled((<<[Int], <<[String]))
  }
  /** Table description of table UserState. Objects of this class serve as prototypes for rows in queries. */
  class Userstate(_tableTag: Tag) extends Table[UserstateRow](_tableTag, "UserState") {
    def * = (userid, state) <> (UserstateRow.tupled, UserstateRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(userid), Rep.Some(state)).shaped.<>({r=>import r._; _1.map(_=> UserstateRow.tupled((_1.get, _2.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column UserId SqlType(INT), PrimaryKey */
    val userid: Rep[Int] = column[Int]("UserId", O.PrimaryKey)
    /** Database column State SqlType(VARCHAR), Length(1000,true) */
    val state: Rep[String] = column[String]("State", O.Length(1000,varying=true))

    /** Foreign key referencing User (database name UserLayout_User_UserId) */
    lazy val userFk = foreignKey("UserLayout_User_UserId", userid, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table Userstate */
  lazy val Userstate = new TableQuery(tag => new Userstate(tag))
}
