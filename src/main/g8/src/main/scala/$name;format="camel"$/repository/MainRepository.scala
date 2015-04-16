package $name;format="camel"$.repository

import com.mongodb.casbah.Imports._
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Repository

@Repository
class MainRepository @Autowired()(
  @Value("${mongodb.host}") host: String,
  @Value("${mongodb.port}") port: Integer,
  @Value("${mongodb.db}") dbName: String )
{
  val coll = MongoClient(host, port)(dbName)("Msg")

  def insert(msg: DBObject) = coll.insert(msg)

  def find(msg: DBObject): MongoCursor = coll.find(msg)

  def findOne(msg: DBObject): Option[DBObject] = coll.findOne(msg)

  def distance(key: String): Seq[String] = coll.distinct(key).map(_.toString)
}
