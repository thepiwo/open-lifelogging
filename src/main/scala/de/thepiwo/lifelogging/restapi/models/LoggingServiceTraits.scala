package de.thepiwo.lifelogging.restapi.models

sealed trait TimeSectionType

case object Hour extends TimeSectionType {
  override def toString = "hour";
}

case object Day extends TimeSectionType {
  override def toString = "day";
}

trait LimitType

object LimitType extends LimitType {
  def fromString(string: Option[String], default: LimitType): LimitType = {
    string match {
      case Some("unlimited") => Unlimited
      case Some("limited") => LimitModId
      case Some("timesections;day") => LimitTimeSections(Day)
      case Some("timesections;hour") => LimitTimeSections(Hour)
      case _ => default
    }
  }
}

case object Unlimited extends LimitType

case object LimitModId extends LimitType

case class LimitTimeSections(timeSectionType: TimeSectionType) extends LimitType
