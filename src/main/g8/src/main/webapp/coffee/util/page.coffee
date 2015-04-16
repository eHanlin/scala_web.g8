uri = require 'URIjs'
webapp = require './webapp.coffee'
i18n = require './i18n.coffee'

pageUri = uri(window.location.href)
path = pageUri.pathname().substring(webapp.root.length)

module.exports =
  root : webapp.root
  path : path
  search : pageUri.search(true)
  i18n : i18n[path.substring(0, path.lastIndexOf('.'))]

