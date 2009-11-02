/**
 * When one branch iteration of a longjevity test fails, copy the files for
 * that branch inot a separate recored path test and run this to remove
 * the prefix number
 */
recordedHtmlsFolder = "../htmls/"

new File(recordedHtmlsFolder).eachFile {
  if(it.isDirectory()){
    renameFilesInFolder(it)    
  }
}

def renameFilesInFolder(folder){
  folder.eachFile{
    println "it is ${it}"
    if(it.isFile() && it.name.endsWith(".txt")){
      def newFile = new File(it.getParentFile(), it.name.substring(0, (it.name.length() - ".txt".length())) + ".html");
      println "renaming ${it} to  ${newFile}"
      it.renameTo(newFile)
    }
  }
}