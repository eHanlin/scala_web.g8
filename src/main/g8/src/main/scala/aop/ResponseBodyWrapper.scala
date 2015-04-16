package aop

import com.mongodb.DBCursor
import com.mongodb.casbah.Imports._

import collection.JavaConversions._
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.{Around, Aspect}
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.ResponseBody

@Aspect
@Order (0)
@Service
class ResponseBodyWrapper {

  @Around(value = "@annotation(responseBody)")
  def wrapper(pjp: ProceedingJoinPoint, responseBody: ResponseBody): Object = {
    try {
      mapAsJavaMap(Map("success" -> true, "result" -> scalaToJava(pjp.proceed)))
    }
    catch {
      case throwable: Throwable => mapAsJavaMap(Map("success" -> false, "errMsg" -> throwable.getMessage))
    }
  }

  def scalaToJava(obj: Any): Object = obj match {
    case null => null
    case None => null
    case Some(result) => scalaToJava(result)
    case result: DBCursor => result.toArray
    case result: MongoCursor => result.toArray
    case result: Iterator[_] => if(result.isEmpty) Array() else result.map(scalaToJava(_)).toArray
    case result: Seq[_] => if(result.isEmpty) Array() else result.map(scalaToJava(_)).toArray
    case result: Map[_, _] => mapAsJavaMap(result.mapValues(scalaToJava(_)))
    case _ => obj.asInstanceOf[Object]
  }

}
