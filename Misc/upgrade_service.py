#!/usr/bin/python3

import sh
import os
import tarfile
import shutil
import argparse
import tempfile

parser = argparse.ArgumentParser(description="Upgrades a ANNIS service.")
parser.add_argument("dir", help="The directory containing the ANNIS service.")
parser.add_argument("archive", help="The archive file containing the new ANNIS version.")
#parser.add_argument("-b", "--backup", help="Perform a backup of already deployed ANNIS instances. This parameter defines also the prefix to use to name the folders."
args = parser.parse_args()

tmp = tempfile.mkdtemp(prefix="annisservice-upgrade-")

print("Extracting the distribution archive to " + tmp)
shutil.unpack_archive(args.archive, tmp)

# find the actual toplevel directory
extracted = tmp
for root, dirs, files in os.walk(tmp):
	if extracted == tmp:
		for d in dirs:
			if d.startswith("annis-service"):
				extracted = os.path.join(root, d)
				break

origconf = os.path.join(args.dir, "conf")
newconf = os.path.join(extracted, "conf")

print("Copying the config files.")

shutil.copy2(os.path.join(origconf, "database.properties"), os.path.join(newconf, "database.properties"))
shutil.copy2(os.path.join(origconf, "annis-service.properties"), os.path.join(newconf, "annis-service.properties"))

origenv = os.environ.copy();
origenv["ANNIS_HOME"] = args.dir;

print("Stopping old service.")
origcmd = sh.Command(os.path.join(args.dir, "bin", "annis-service.sh"))
print(origcmd("stop", _env=origenv))

print("Removing old installation files.")
shutil.rmtree(args.dir)

print("Moving new version to old location.")
shutil.copytree(extracted, args.dir)


#from sh import ls
#print(ls())
