package pregnaware

import org.scalatest.Tag

/** DB Tests should only be run locally (arguably not a unit test) */
object DbTest extends Tag("DbTest")

/** Tests which take >1s to run */
object SlowTest extends Tag("SlowTest")
