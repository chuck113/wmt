recordedHtmlsFolder = "../htmls/"
recordingToCopy = "victoria-happy"
recordedHtmlFolder = recordedHtmlsFolder+recordingToCopy
convertedRecordedHtmlFolder = recordedHtmlsFolder+recordingToCopy

outFolder = new File(recordedHtmlFolder+"-HTML");
outFolder.mkdir();

new File(recordedHtmlFolder).eachFile {
  def outFile = recordedHtmlsFolder + (outFolder.name +"/"+it.name.substring(0, it.name.length() - "txt".length()))+"html"
  println "out is ${outFile}"
  new File(outFile).createNewFile()
  def fw = new FileWriter(outFile)
  def fr = new FileReader(it);

  fr.each {fw.write(it)}
  fw.flush();
  fw.close();
}