$ = require 'jquery'
page = require '../util/page.coffee'

$.get "#{page.root}/Msg/author" , (data) ->
  if data.success
    tbody = $('table.author > tbody')
    for author in data.result
      tbody.append("""<tr><td><a href="#{page.root}/author/messageList.html?author=#{endecodeURIComponent(author)}">#{author}</a></td></tr>""")
  else
    alert(page.i18n.error)