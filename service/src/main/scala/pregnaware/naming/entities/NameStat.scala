package pregnaware.naming.entities

case class NameStat(name: String, isBoy: Boolean, year: Int, count: Int)
case class NameStatByCountry(name: String, isBoy: Boolean, year: Int, country: String, count: Int)
case class NameStatByMonth(name: String, isBoy: Boolean, year: Int, month: String, count: Int)
case class NameStatByRegion(name: String, isBoy: Boolean, year: Int, region: String, count: Int)

case class NameSummaryStat(year: Int, isBoy: Boolean, count: Int)
