:: ==========================================================
:: Install script to install Jawk jars into local maven repo
:: Whitehead, 2013
:: https://github.com/nickman/javax-scripting
:: Note: Run this in the javax-scripting\3rdParty\jawk dir
:: ==========================================================
set VERSION=1_02
echo "Installing Jawk %VERSION% Lib Jar, Source Jar and JavaDoc Jar Artifacts"
mvn install:install-file -DgroupId=org.jawk -DartifactId=jawk -Dversion=%VERSION% -Dpackaging=jar -Dfile=jawk.%VERSION%.jar -Dsources=jawk_src.%VERSION%.jar -Djavadoc=jawk_doc.%VERSION%.jar
