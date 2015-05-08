{
    lang : "en_US",
    noun : {
        author : "Author",
        message : "Message"
    },
    "/index" : {
        title : "Index",
        send : "Send",
        success : self.send + " Success",
        fail : {
            empty : "is empty",
            authorEmpty : $.noun.author + " " + self.empty,
            messageEmpty : $.noun.message + " " + self.empty,
            sendError : $["/index"].send + " fail"
        }
    },
    "/author/list" : {
        title : "Author List",
        fail : "Get " + self.title + " fail"
    },
    "/author/messageList" : self["/author/list"]{
        title : "Author Message List"
    }
}