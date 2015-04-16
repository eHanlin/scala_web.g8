var gulp = require('gulp');

var browserify = require('gulp-browserify');
var less = require('gulp-less');
var livereload = require('gulp-livereload');
var rename = require('gulp-rename');

var del = require('del');
var fs = require('fs');

var es = require('event-stream');
var gutil = require('gulp-util');
var extend = require('lodash.assign');
var path = require('path');

var nodeModulesDir = 'node_modules/';
var dir = 'src/main/webapp/';
var resourceDir = dir+'resource';
var coffeeDir = 'src/main/webapp/coffee/';
var lessDir = 'src/main/webapp/less/';
var javaI18nDir = 'src/main/resources/i18n/';
var javaI18nFileName = 'messages_#{file}.properties';
var javaI18nFileNameRegex = /#{file}/g;

var versionRegex = /^version=(\S+)/m;
var version = fs.readFileSync('src/main/resources/version.properties', 'utf8').toString().match(versionRegex)[1];

var packageJson = JSON.parse(fs.readFileSync('package.json', 'utf8').toString());

var libNodeModules = [];
for(var i=0 ; i<packageJson['web-dependencies'].length ; i++){
    libNodeModules.push(nodeModulesDir + packageJson['web-dependencies'][i] + '/**/*');
}

var paths = {
    environment: 'src/main/resources/environment.properties',
    version: 'src/main/resources/version.properties',
    cdn: 'src/main/resources/cdn.properties',
    libNodeModules: libNodeModules,
    resource: 'src/main/webapp/resource/**/*',
    coffee: dir+'coffee/**/*.coffee',
    i18n: dir+'i18n/**/*.json',
    i18ned: dir+'dist/'+version+'/i18n/**/*.json',
    js: dir+'dist/'+version+'/js/**/*.js',
    less: dir+'less/**/*.less',
    css: dir+'dist/'+version+'/css/**/*.css',
    html: dir+'WEB-INF/view/**/*.html'
};


var concatMainLess = function() {
    var reg = /\//g;
    return es.map(function(file, cb){
        var prePath = "./";
        var relPath = path.relative(file.base, path.dirname(file.path));
        if(relPath !== ''){
            var matchs = relPath.match(reg);
            var matchsLength = 0;
            if(matchs != null){
                matchsLength = matchs.length;
            }
            for(var i=0 ; i < matchsLength+1 ; i++){
                prePath += "../"
            }
        }
        file.contents = Buffer.concat([
            new Buffer('@import "' + prePath + 'main.less";'),
            file.contents
        ]);
        cb(null, file);
    });
};

var marginJson = function(defaultJson, targetJson) {
  for(var p in defaultJson){
      switch(typeof targetJson[p]){
          case 'undefined':
              targetJson[p] = defaultJson[p];
              break;
          case 'object':
              if(targetJson[p] !== null && (typeof defaultJson[p]) !== 'undefined' && defaultJson[p] !== null){
                  marginJson(defaultJson[p], targetJson[p]);
              }
              break;
      }
  }
};

var marginI18n = function() {
    var defaultPath = "default.json";
    return es.map(function(file, cb){
        if(path.basename(file.path) !== defaultPath){
            var defaultJson = JSON.parse(fs.readFileSync(path.dirname(file.path) + "/" + defaultPath, 'utf8').toString());
            var i18nJson = JSON.parse(file.contents.toString());
            marginJson(defaultJson, i18nJson);
            file.contents = new Buffer("scalaWebI18n = "+JSON.stringify(i18nJson)+";");
        }
        cb(null, file);
    });
};

var jsonToProperties = function(json, prefix) {
    var str = '';
    for(var p in json){
        if(typeof json[p] === 'object' && json[p] !== null){
            if(Array.isArray(json[p])){
                var arr = json[p];
                for(var i=0 ; i<arr.length ; i++){
                    if(typeof arr[i] === 'object' && arr[i] !== null){
                        str += jsonToProperties(arr[i], prefix+p+'.'+i+'.');
                    }else{
                        str += prefix+p+'.'+i+'='+arr[i]+"\n";
                    }
                }
            }else{
                str += jsonToProperties(json[p], prefix+p+'.');
            }
        }else{
            str += prefix+p+'='+json[p]+"\n";
        }
    }
    return str;
};

var buildJavaI18n = function() {
    var defaultPath = "default.json";
    return es.map(function(file, cb){
        var defaultJson = JSON.parse(fs.readFileSync(path.dirname(file.path) + '/' + defaultPath, 'utf8').toString());
        var i18nJson = JSON.parse(file.contents.toString());
        marginJson(defaultJson, i18nJson);
        var resutl = jsonToProperties(i18nJson, '');
        file.contents = new Buffer(resutl);
        var fileName = path.basename(file.path, '.json');
        file.path = path.join(file.base, javaI18nFileName.replace(javaI18nFileNameRegex, fileName));
        cb(null, file);
    });
};

var cdnRegex = /^cdn=(.*)$/m;
var buildCoffeeCdn = function() {
    return es.map(function(file, cb){
        var cdn = file.contents.toString().match(cdnRegex)[1].trim();
        file.contents = new Buffer('module.exports = "'+cdn+'/'+version+'"');
        cb(null, file);
    });
}
var buildLessCdn = function() {
    return es.map(function(file, cb){
       var cdn = file.contents.toString().match(cdnRegex)[1].trim();
       file.contents = new Buffer('@cdn: "'+cdn+'/'+version+'";');
       cb(null, file);
    });
};

var versionRegex = /^version=(.*)$/m;
var buildCoffeeVersion = function() {
    return es.map(function(file, cb){
        var version = file.contents.toString().match(versionRegex)[1].trim();
        file.contents = new Buffer('module.exports = "'+version+'"');
        cb(null, file);
    });
}
var buildLessVersion = function() {
    return es.map(function(file, cb){
        var version = file.contents.toString().match(versionRegex)[1].trim();
        file.contents = new Buffer('@version: "'+version+'";');
        cb(null, file);
    });
};


gulp.task('clean', function(cb){ del([
    dir+'/lib',
    dir+'/dist',
    coffeeDir+'_cdn.coffee',
    lessDir+'_cdn.less',
    coffeeDir+'_version.coffee',
    lessDir+'_version.less',
    javaI18nDir], cb); });

gulp.task('copy_node_modules', function(){
   return gulp.src(paths.libNodeModules, {base:nodeModulesDir})
        .pipe(gulp.dest(dir+'dist/'+version+'/'))
        .pipe(gulp.dest(dir+'lib/'))
});

gulp.task('copy_resource', function(){
   return gulp.src(paths.resource, {base:resourceDir})
       .pipe(gulp.dest(dir+'dist/'+version+'/'))
});

var coffeeCdnTask = function(paths){
    return gulp.src(paths)
        .pipe(buildCoffeeCdn())
        .pipe(rename({basename:'_cdn', extname:'.coffee'}))
        .pipe(gulp.dest(coffeeDir))
};

gulp.task('coffeeCdn', function(){ return coffeeCdnTask([paths.cdn]); });

var lessCdnTask = function(paths){
  return gulp.src(paths)
      .pipe(buildLessCdn())
      .pipe(rename({basename:'_cdn', extname:'.less'}))
      .pipe(gulp.dest(lessDir))
};

gulp.task('lessCdn', function(){ return lessCdnTask([paths.cdn]); });

var coffeeVersionTask = function(paths){
    return gulp.src(paths)
        .pipe(buildCoffeeVersion())
        .pipe(rename({basename:'_version', extname:'.coffee'}))
        .pipe(gulp.dest(coffeeDir))
};

gulp.task('coffeeVersion', function(){ return coffeeVersionTask([paths.version]); });

var lessVersionTask = function(paths){
    return gulp.src(paths)
        .pipe(buildLessVersion())
        .pipe(rename({basename:'_version', extname:'.less'}))
        .pipe(gulp.dest(lessDir))
};

gulp.task('lessVersion', function(){ return lessVersionTask([paths.version]); });


var javaI18nTask = function(paths){
    return gulp.src(paths)
        .pipe(buildJavaI18n())
        .pipe(rename({extname:'.properties'}))
        .pipe(gulp.dest(javaI18nDir));
};

gulp.task('javaI18n', function(){ return javaI18nTask([paths.i18n]) });

var i18nTask = function(paths){
    return gulp.src(paths)
        .pipe(marginI18n())
        .pipe(rename({extname:'.js'}))
        .pipe(gulp.dest(dir+'dist/'+version+'/i18n/'));
};

gulp.task('i18n', function(){ return i18nTask([paths.i18n]); });

var coffeeTask = function(paths){
    return gulp.src(paths, {read: false})
        .pipe(browserify({transform: ['coffeeify'], extensions: ['.coffee','.js','json']}))
        .pipe(rename({extname:'.js'}))
        .pipe(gulp.dest(dir+'dist/'+version+'/js/'));
};

gulp.task('coffee', ['coffeeVersion', 'coffeeCdn', 'copy_resource'], function(){ return coffeeTask([paths.coffee]); });

var lessTask = function(paths){
    return gulp.src(paths)
        .pipe(less())
        .pipe(gulp.dest(dir+'dist/'+version+'/css/'));
};

gulp.task('less', ['lessVersion', 'lessCdn', 'copy_node_modules', 'copy_resource'], function(){ return lessTask([paths.less]); });

var watchTask = function(){
    livereload.listen();
    gulp.watch([paths.i18n], function(event){ javaI18nTask([event.path]); i18nTask([event.path]); coffeeTask([paths.coffee]);});
    gulp.watch([paths.i18ned]).on('change', livereload.changed);
    gulp.watch([paths.coffee], function(event){ coffeeTask([paths.coffee]); });
    gulp.watch([paths.js]).on('change', livereload.changed);
    gulp.watch([paths.less], function(event){ lessTask([paths.less]); });
    gulp.watch([paths.css]).on('change', livereload.changed);
    gulp.watch([paths.html]).on('change', livereload.changed);
};

gulp.task('watch', ['coffee', 'less', 'javaI18n', 'i18n'], function(){ watchTask(); });

gulp.task('build', ['coffee', 'less', 'javaI18n', 'i18n']);

gulp.task('default', ['coffee', 'less', 'javaI18n', 'i18n']);