package $name;format="camel"$.service

import java.util.Date

import com.mongodb.casbah.Imports._
import $name;format="camel"$.repository.Implicit._
import $name;format="camel"$.repository.MainRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class MainService {

  @Autowired
  val repository: MainRepository = null

  def appendMsg(author: String, msg: String) = repository.insert(MongoDBObject("author" -> author, "msg" -> msg, "date" -> new Date()))

  def findAuthor(): Seq[String] = repository.distance("author")

  def findMsgList(): MongoCursor = repository.find(MongoDBObject()).sort(MongoDBObject("date" -> 1))

  def findMsgList(author: String) : MongoCursor = repository.find("author" $eq author).sort(MongoDBObject("date" -> 1))

  def findFormattedMsg() = findMsgList().map(formatMsgFn)

  def findFormattedMsg(author: String) = findMsgList(author).map(formatMsgFn)

  def formatMsgFn = (msg: DBObject) => msg.date.getTime+"@"+msg.author+":"+msg.msg

  def echo(msg: String): String = s"Service : ${msg}"
}