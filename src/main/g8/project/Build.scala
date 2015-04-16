import java.io.{File, FileWriter}
import java.util.Properties

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.model.{CannedAccessControlList, PutObjectRequest}
import com.amazonaws.services.s3.{AmazonS3, AmazonS3Client}
import org.apache.commons.io.FileUtils
import sbt._
import sbt.complete.DefaultParsers.spaceDelimited

object ViewBuild extends Build {

  object ViewFiles {
    def unapply(path: String): Option[(File, File, File)] = {
      Some(file(s"./src/main/webapp/WEB-INF/view/$path.html"),
        new File(s"./src/main/webapp/less/$path.less"),
        new File(s"./src/main/webapp/coffee/$path.coffee"))
    }
  }

  def writeContentToFile(file: File, content: String): Unit = {
    file.getParentFile.mkdirs
    file.createNewFile
    val writer = new FileWriter(file)
    writer.write(content)
    writer.flush
    writer.close
  }

  def uploadToS3(s3: AmazonS3, file: File, bucket: String, path: String): Unit = {
    if(file.isFile){
      var uploaded = false
      while(!uploaded){
        try{
          val putRequest: PutObjectRequest = new PutObjectRequest(bucket, path, file)
          putRequest.setCannedAcl(CannedAccessControlList.PublicRead);
          s3.putObject(putRequest);
          uploaded = true;
          println(s"${file.getPath} Uploaded to S3 ${bucket}/${path}")
        }catch{
          case ex: Exception => {
            println(ex.getMessage)
            println(s"Upload to S3 Fail retry ${file.getPath}")
            Thread.sleep(1000);
          }
        }
      }
    }
  }

  def uploadResourceToS3(s3: AmazonS3, baseDir: File, file: File, bucket: String, appName: String): Unit = uploadToS3(s3, file, bucket, s"$appName/${sbt.IO.relativize(baseDir, file).get}")

  def findAllChildFiles(base: File): PathFinder = (base) ** "*.*"

  def parseVersion(): (Int, Int, Int, String) = {
    val buildSbtContent = FileUtils.readFileToString(new File("./build.sbt"), "UTF-8")
    val pattern = "(?m)^\\s*version\\s*\\:\\=\\s*\"(\\d+)\\.(\\d+)\\.(\\d+)(\\S*)\"".r
    val Some(m) = pattern.findFirstMatchIn(buildSbtContent)
    (m.group(1).toInt, m.group(2).toInt, m.group(3).toInt, m.group(4))
  }

  def updateVersion(version: String) = {
    writeContentToFile(new File("./src/main/resources/version.properties"), s"version=${version}")
    val buildSbtFile = new File("./build.sbt")
    val buildSbtContent = FileUtils.readFileToString(buildSbtFile, "UTF-8")
    val pattern = "(?m)^\\s*version\\s*\\:\\=\\s*\"\\S*\"".r
    val newBuildSbtContent = pattern.replaceFirstIn(buildSbtContent, "\nversion := \"" + version + "\"")
    writeContentToFile(buildSbtFile, newBuildSbtContent)
  }

  val addView = inputKey[Unit]("Add View task.")
  val removeView = inputKey[Unit]("Remove View task.")
  val changeConfig = inputKey[Unit]("Change Config task.")
  val deploy = inputKey[Unit]("Deploy task.")
  val setVersion = inputKey[Unit]("Set Version.")
  val incFirstVersion = taskKey[Unit]("Increments First Version.")
  val incMiddleVersion = taskKey[Unit]("Increments Middle Version.")
  val incLastVersion = taskKey[Unit]("Increments Last Version.")
  val htmlTemplate = """<!DOCTYPE html>
<html layout:decorator="template/mb" lang="zh-tw" xmlns="http://www.w3.org/1999/xhtml" xmlns:th="http://www.thymeleaf.org" xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout">
<body><div layout:fragment="content" th:remove="tag">

</div></body>
</html>""";

  lazy val root =
    sbt.Project(id = "scala_web", base = file(".")).settings(
      addView := {
        val args: Seq[String] = spaceDelimited("<arg>").parsed
        args.foreach{(path) =>
          val ViewFiles(htmlFile, lessFile, coffeeFile) = path
          writeContentToFile(htmlFile, htmlTemplate)
          writeContentToFile(lessFile, "")
          writeContentToFile(coffeeFile, """$ = require 'jquery'""")
        }
      },
      removeView := {
        val args: Seq[String] = spaceDelimited("<arg>").parsed
        args.foreach{(path) =>
          val ViewFiles(htmlFile, lessFile, coffeeFile) = path
          htmlFile.delete
          lessFile.delete
          coffeeFile.delete
        }
      },
      changeConfig := {
        val args: Seq[String] = spaceDelimited("<arg>").parsed
        val configDirName = if(args.size > 0) args(0) else "default"
        val defaultDir = new File("./src/config/default")
        val configDir = new File(s"./src/config/$configDirName")
        val targetDir = new File("./src/main")
        IO.copyDirectory(defaultDir, targetDir, true)
        IO.copyDirectory(configDir, targetDir, true)
      },
      deploy := {
        val args: Seq[String] = spaceDelimited("<arg>").parsed
        val otherName = if(args.size > 0) args(0) else ""
        val properties = new Properties()
        IO.load(properties, new File("./src/main/resources/version.properties"))
        IO.load(properties, new File("./src/main/resources/aws.properties"))
        val s3: AmazonS3 = new AmazonS3Client(new BasicAWSCredentials(properties.getProperty("aws.accessKey"), properties.getProperty("aws.secretKey")));
        val baseDir = new File("./src/main/webapp/dist")
        s3.setEndpoint(properties.getProperty("aws.s3.endpoint"))
        val resourceBucket = properties.getProperty("asw.s3.web.resource.bucket")
        val appName = Keys.name.value
        val version = properties.getProperty("version")
        findAllChildFiles(new File(s"./src/main/webapp/dist/${version}")).get.par.foreach((file) => uploadResourceToS3(s3, baseDir, file, resourceBucket, appName))
        val scalaVersion = Keys.scalaBinaryVersion.value
        val war = new File(s"./target/scala-${scalaVersion}/${appName}_${scalaVersion}-${Keys.version.value}.war")
        val warBucket = properties.getProperty("asw.s3.web.war.bucket")
        uploadToS3(s3, war, warBucket, s"${appName}/${version}.war")
        if(otherName != "")
          uploadToS3(s3, war, warBucket, s"${appName}/${otherName}.war")
      },
      setVersion := {
        val args: Seq[String] = spaceDelimited("<arg>").parsed
        val newVersion = if(args.size > 0) args(0) else "0.0.1"
        updateVersion(newVersion)
      },
      incFirstVersion := {
        var (first, middle, last, other) = parseVersion()
        updateVersion(s"${first + 1}.0.0${other}")
      },
      incMiddleVersion := {
        var (first, middle, last, other) = parseVersion()
        updateVersion(s"${first}.${middle + 1}.0${other}")
      },
      incLastVersion := {
        var (first, middle, last, other) = parseVersion()
        updateVersion(s"${first}.${middle}.${last + 1}${other}")
      }
    )

}