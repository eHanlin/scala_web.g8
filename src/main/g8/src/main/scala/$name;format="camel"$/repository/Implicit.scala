package $name;format="camel"$.repository

import java.util.Date

import com.mongodb.casbah.Imports._

object Implicit {

  case class DateField(
    start: Date,
    end: Date,
    date: Date)

  implicit def dbObjectToDateFiled(m: DBObject): DateField = DateField(
    if(m.containsField("start")) m.get("start").asInstanceOf[Date] else null,
    if(m.containsField("end")) m.get("end").asInstanceOf[Date] else null,
    if(m.containsField("date")) m.get("date").asInstanceOf[Date] else null
  )


  case class StringField(
    _id: String,
    name: String,
    author: String,
    msg: String)

  implicit def dbObjectToStringField(m: DBObject): StringField = StringField(
    if(m.containsField("_id")) m.get("_id").toString else null,
    if(m.containsField("name")) m.get("name").toString else null,
    if(m.containsField("author")) m.get("author").toString else null,
    if(m.containsField("msg")) m.get("msg").toString else null
  )

}
