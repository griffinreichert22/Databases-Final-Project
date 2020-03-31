all:
	clear
	javac fgr221/*.java
	mv fgr221/*.class .
	mv fgr221/Manifest.txt .
	jar cfmv fgr221.jar Manifest.txt *.class
	mv Manifest.txt fgr221
	mv *.class fgr221
run:
	java -jar fgr221.jar
clean:
	rm fgr221.jar
	rm fgr221/*.class
	clear




