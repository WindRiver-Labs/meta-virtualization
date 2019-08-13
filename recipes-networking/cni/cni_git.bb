# This cni version is supposed to track latest upstream.
require cni.inc

SRCREV_cni = "dc71cd2ba60c452c56a0a259f2a23d2afe42b688"
SRCREV_plugins = "0eddc554c0747200b7b112ce5322dcfa525298cf"
SRC_URI = "\
	git://github.com/containernetworking/cni.git;nobranch=1;name=cni \
        git://github.com/containernetworking/plugins.git;nobranch=1;destsuffix=plugins;name=plugins \
	"
PV = "0.7.0+git${SRCREV_cni}"
