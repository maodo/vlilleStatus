package utils

import org.joda.time.DateTime

object DateImplicits {
      implicit def dateTimeToMillis(dt: DateTime) = dt.getMillis
}
