package $name;format="camel"$.view

import javax.servlet.http.HttpServletRequest

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod}
import org.springframework.web.servlet.{ModelAndView, View}
import org.springframework.web.servlet.view.RedirectView

@Controller
class MainView {

  val pageRegex = """^/(.+)\.html$""".r

  @RequestMapping(value = Array("**/*.html"), method = Array(RequestMethod.GET))
  def html(request: HttpServletRequest): ModelAndView = {
    val pageRegex(pagePath) = request.getServletPath
    val model = new java.util.HashMap[String, Object]()
    model.put("pagePath", s"/${pagePath}")
    new ModelAndView(pagePath, model)
  }

  @RequestMapping(value = Array("/"))
  def rootIndex: View = new RedirectView("/index.html")

}
