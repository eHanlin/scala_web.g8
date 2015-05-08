{
    lang : "zh_TW",
    langs : ["zh_TW", "en_US"],
    noun : {
        author : "發言者",
        message : "訊息"
    },
    "/index" : {
        title : "首頁",
        send : "送出訊息",
        success : self.send + "成功",
        fail : {
            empty : "不能是空白",
            authorEmpty : $.noun.author + self.empty,
            messageEmpty : $.noun.message + self.empty,
            sendError : $["/index"].send + "失敗"
        }
    },
    "/author/list" : {
        title : "發言者清單",
        fail : "取得" + self.title + "失敗"
    },
    "/author/messageList" : self["/author/list"]{
        title : "發言清單"
    }
}