$ = require 'jquery'
page = require './util/page.coffee'

$('form.message').submit () ->
  thiz = $(@)
  data = {}

  for parameter in ['author', 'message']
    v = thiz.find("""input[name="#{parameter}"]""").val()
    if v? != ''
      data[parameter] = v
    else
      alert(page.i18n.fail["#{parameter}Empty"])
      return false

  $.post "#{page.root}/Msg", data, (data) ->
    if(data.success)
      alert(page.i18n.success)
    else
      alert(page.i18n.fail.sendError)
  false