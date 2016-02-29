package pregnaware.utils

import java.lang.management.ManagementFactory

/** Wraps access to the current process idenitifier */
object SysUtils {
  lazy val pid = ManagementFactory.getRuntimeMXBean.getName.split('@')(0)
}
