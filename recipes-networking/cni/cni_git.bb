# This cni version is supposed to track latest upstream.
require cni.inc

SRCREV_cni = "8c6c47d1c7fcf51c8d0c939d2af06dd108f876da"
SRCREV_plugins = "2d6d4b260a98973a206fde18bdde4f2511c72c60"
SRC_URI = "\
	git://github.com/containernetworking/cni.git;nobranch=1;name=cni \
        git://github.com/containernetworking/plugins.git;nobranch=1;destsuffix=plugins;name=plugins \
	"
PV = "0.7.1+git${SRCREV_cni}"
