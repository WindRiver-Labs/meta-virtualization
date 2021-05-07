require openvswitch.inc

DEPENDS += "virtual/kernel"

PACKAGE_ARCH = "${MACHINE_ARCH}"

RDEPENDS_${PN}-ptest += "\
	python3-logging python3-syslog python3-io python3-core \
	python3-fcntl python3-shell python3-xml python3-math \
	python3-datetime python3-netclient python3 sed \
	ldd perl-module-socket perl-module-carp perl-module-exporter \
	perl-module-xsloader python3-netserver python3-threading \
	python3-resource findutils which diffutils \
	"

S = "${WORKDIR}/git"
PV = "2.15+${SRCPV}"
CVE_VERSION = "2.13.0"

FILESEXTRAPATHS_append := "${THISDIR}/${PN}-git:"

SRCREV = "8dc1733eaea866dce033b3c44853e1b09bf59fc7"
SRC_URI += "git://github.com/openvswitch/ovs.git;protocol=git;branch=branch-2.15 \
            file://openvswitch-add-ptest-71d553b995d0bd527d3ab1e9fbaf5a2ae34de2f3.patch \
            file://run-ptest \
            file://disable_m4_check.patch \
            file://kernel_module.patch \
            file://systemd-update-tool-paths.patch \
            file://systemd-create-runtime-dirs.patch \
            file://0001-ovs-use-run-instead-of-var-run-for-in-systemd-units.patch \
            file://0001-openvswitch-fix-do_configure-with-DPDK-19.11-error.patch \
            file://0001-openvswitch-fix-netdev-dpdk-compile-error.patch \
           "

LIC_FILES_CHKSUM = "file://LICENSE;md5=1ce5d23a6429dff345518758f13aaeab"

DPDK_INSTALL_DIR ?= "/usr/share/dpdk"

PACKAGECONFIG ?= "libcap-ng"
PACKAGECONFIG[dpdk] = "--with-dpdk=static,,dpdk,dpdk"
PACKAGECONFIG[libcap-ng] = "--enable-libcapng,--disable-libcapng,libcap-ng,"
PACKAGECONFIG[ssl] = ",--disable-ssl,openssl,"

# Don't compile kernel modules by default since it heavily depends on
# kernel version. Use the in-kernel module for now.
# distro layers can enable with EXTRA_OECONF_pn_openvswitch += ""
# EXTRA_OECONF += "--with-linux=${STAGING_KERNEL_BUILDDIR} --with-linux-source=${STAGING_KERNEL_DIR} KARCH=${TARGET_ARCH}"

# silence a warning
FILES_${PN} += "/lib/modules"

inherit ptest

EXTRA_OEMAKE += "TEST_DEST=${D}${PTEST_PATH} TEST_ROOT=${PTEST_PATH}"

do_configure() {
	if [ ${PREFERRED_VERSION_dpdk} = "19.11.%" ]; then
		export DPDK_LIBS="-L${STAGING_DIR_TARGET}${DPDK_INSTALL_DIR}/${TARGET_ARCH}-native-linuxapp-gcc/lib -ldpdk"
		export DPDK_CFLAGS="-I${STAGING_DIR_TARGET}${DPDK_INSTALL_DIR}/${TARGET_ARCH}-native-linuxapp-gcc/include"
	fi

	autotools_do_configure
}

do_install_ptest() {
	oe_runmake test-install
}

do_install_append() {
	oe_runmake modules_install INSTALL_MOD_PATH=${D}
}
