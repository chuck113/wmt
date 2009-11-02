/**
 * When one branch iteration of a longjevity test fails, copy the files for
 * that branch inot a separate recored path test and run this to remove
 * the prefix number
 */
recordedHtmlsFolder = "../htmls/"
recordingFolderToUpdate = recordedHtmlsFolder+"bakerloo-QueensParkAndNorthSidingsError"

new File(recordingFolderToUpdate).eachFile {
  it.name.indexOf("-")
  println "renaming ${it} to ${recordingFolderToUpdate+"/"+it.name.substring(it.name.indexOf("-")+1, it.name.length())}"
  it.renameTo(new File(recordingFolderToUpdate+"/"+it.name.substring(it.name.indexOf("-")+1, it.name.length())));
}