rjar - Recursive Jar Manipulation Tool
--------------------------------------

Utility that allows to manipulate Java Archive (jar) files recursively.
Full doc, bug reports, contrib, etc. at :

    http://rjar.googlecode.com


Installation
------------


Set the environment variable RJAR_HOME to the extracted rjar folder, e.g. :

  bash$ export RJAR_HOME=/Users/johndoe/tools/rjar


On unix, set execution permissions on the rjar script :

  bash$ chmod +x $RJAR_HOME/bin/rjar


Add rjar to your PATH environment variable :

  bash$ export PATH=$PATH:$RJAR_HOME/bin


You're ready to go ! To try your install, cd in any folder and type rjar :

  bash$ cd /tmp
  bash$ rjar


... you should see the usage message, which means everything should be ok !