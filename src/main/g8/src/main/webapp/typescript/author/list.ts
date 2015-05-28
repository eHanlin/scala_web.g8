import $ = require('jquery');
import page = require('../util/page');

var tbody = $('table.author > tbody');

interface IdInterface {
    id : String
}

class Author implements IdInterface {
    id : String;
    constructor(public name : String){
        this.id = "id is " + name;
    }
    toDom(){
        return $(`<tr><td><a href="${page.root}/author/messageList.html?author=${decodeURIComponent(this.name)}">${this.name}</a></td></tr>`)
    }
}

function appendAuthor(author : Author) {
    tbody.append(author.toDom());
}

$.get(`${page.root}/Msg/author`, function(data){
   if(data.success){
       for(var index in data.result){
           appendAuthor(new Author(data.result[index]));
       }
   }else
       alert(page.i18n.error)
});