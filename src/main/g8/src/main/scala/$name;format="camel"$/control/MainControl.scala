package $name;format="camel"$.control

import java.util.Optional

import $name;format="camel"$.service.MainService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation._

@Controller
class MainControl {

  @Autowired
  val service: MainService = null

  @RequestMapping(value = Array("/Msg"), method = Array(RequestMethod.POST))
  @ResponseBody
  def appendMsg(
    @RequestParam author: String,
    @RequestParam message: String): Object =
  {
    service.appendMsg(author, message)
    java.lang.Boolean.TRUE
  }

  @RequestMapping(value = Array("/Msg"), method = Array(RequestMethod.GET))
  @ResponseBody
  def findMsg(
    @RequestParam author: Optional[String]): Object = if(author.isPresent)
  {
    service.findFormattedMsg(author.get())
  }else{
    service.findFormattedMsg()
  }

  @RequestMapping(value = Array("/Msg/author"), method = Array(RequestMethod.GET))
  @ResponseBody
  def findAuthor(): Object = service.findAuthor()

}
