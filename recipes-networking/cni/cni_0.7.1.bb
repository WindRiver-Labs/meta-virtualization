# This cni version is here to ensure that basic k8s setup with flannel could work.
require cni.inc

# 0.7.1
SRCREV_cni = "4cfb7b568922a3c79a23e438dc52fe537fc9687e"
# 0.7.6
SRCREV_plugins = "9f96827c7cabb03f21d86326000c00f61e181f6a"
SRC_URI = "\
	git://github.com/containernetworking/cni.git;branch=master;name=cni \
        git://github.com/containernetworking/plugins.git;branch=v0.7;destsuffix=plugins;name=plugins \
	"
PV = "0.7.1"
COMPATIBLE_HOST_mips = "null"
