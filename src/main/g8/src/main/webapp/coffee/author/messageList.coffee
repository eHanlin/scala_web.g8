$ = require 'jquery'
page = require '../util/page.coffee'

msgHandle = (data) ->
  if data.success
    tbody = $('table.message > tbody')
    for message in data.result
      tbody.append("""<tr><td>#{message}</td></tr>""")
  else
    alert(page.i18n.fail)

if page.search.author
  $.get("#{page.root}/Msg", {author:decodeURIComponent(page.search.author)}, msgHandle)
else
  $.get("#{page.root}/Msg", msgHandle)
