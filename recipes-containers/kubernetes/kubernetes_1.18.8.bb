HOMEPAGE = "git://github.com/kubernetes/kubernetes"
SUMMARY = "Production-Grade Container Scheduling and Management"
DESCRIPTION = "Kubernetes is an open source system for managing containerized \
applications across multiple hosts, providing basic mechanisms for deployment, \
maintenance, and scaling of applications. \
"

SRCREV_kubernetes = "9f2892aab98fe339f3bd70e3c470144299398ace"
SRCREV_kubernetes-release = "c600d12013979543fcfab24c977ffae158405a21"

SRC_URI = "git://github.com/kubernetes/kubernetes.git;branch=release-1.18;name=kubernetes \
           git://github.com/kubernetes/release;branch=master;name=kubernetes-release;destsuffix=git/release \
           file://0001-hack-lib-golang.sh-use-CC-from-environment.patch \
           file://0001-cross-don-t-build-tests-by-default.patch \
           file://0001-Makefile.generated_files-Fix-race-issue-for-installi.patch \
          "

SRC_URI_append_arm = " file://0001-fix-arm-node-failed-to-join-master-node.patch"

DEPENDS += "rsync-native \
            coreutils-native \
           "

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://src/import/LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"
S = "${WORKDIR}/git"

GO_IMPORT = "import"

inherit systemd
inherit go
inherit goarch

COMPATIBLE_HOST = '(x86_64.*|arm.*|aarch64.*)-linux'

do_compile() {
	# link fixups for compilation
	rm -f ${S}/src/import/vendor/src
	ln -sf ./ ${S}/src/import/vendor/src

	export GOPATH="${S}/src/import/.gopath:${S}/src/import/vendor:${STAGING_DIR_TARGET}/${prefix}/local/go"
	cd ${S}/src/import

	# Build the host tools first, using the host compiler
	export GOARCH="${BUILD_GOARCH}"
	# Pass the needed cflags/ldflags so that cgo can find the needed headers files and libraries
	export CGO_ENABLED="1"
	export CFLAGS="${BUILD_CFLAGS}"
	export LDFLAGS="${BUILD_LDFLAGS}"
	export CGO_CFLAGS="${BUILD_CFLAGS}"
	export CGO_LDFLAGS="${BUILD_LDFLAGS}"
	export CC="${BUILD_CC}"
	export LD="${BUILD_LD}"

	make generated_files KUBE_BUILD_PLATFORMS="${HOST_GOOS}/${BUILD_GOARCH}"

	# Build the target binaries
	export GOARCH="${TARGET_GOARCH}"
	# Pass the needed cflags/ldflags so that cgo can find the needed headers files and libraries
	export CGO_ENABLED="1"
	export CGO_CFLAGS="${CFLAGS} --sysroot=${STAGING_DIR_TARGET}"
	export CGO_LDFLAGS="${LDFLAGS} --sysroot=${STAGING_DIR_TARGET}"
	export CFLAGS=""
	export LDFLAGS=""
	export CC="${CC}"
	export LD="${LD}"

	# to limit what is built, use 'WHAT', i.e. make WHAT=cmd/kubelet
	make cross KUBE_BUILD_PLATFORMS=${GOOS}/${GOARCH} GOLDFLAGS=""
}

do_install() {
    install -d ${D}${bindir}
    install -d ${D}${systemd_unitdir}/system/
    install -d ${D}${systemd_unitdir}/system/kubelet.service.d/

    install -d ${D}${sysconfdir}/kubernetes/manifests/

    install -m 755 -D ${S}/src/import/_output/local/bin/${TARGET_GOOS}/${TARGET_GOARCH}/* ${D}/${bindir}

    install -m 0644 ${WORKDIR}/git/release/cmd/kubepkg/templates/latest/deb/kubelet/lib/systemd/system/kubelet.service ${D}${systemd_unitdir}/system/
    install -m 0644 ${WORKDIR}/git/release/cmd/kubepkg/templates/latest/deb/kubeadm/10-kubeadm.conf  ${D}${systemd_unitdir}/system/kubelet.service.d/
}

PACKAGES =+ "kubeadm kubectl kubelet kube-proxy ${PN}-misc"

ALLOW_EMPTY_${PN} = "1"
INSANE_SKIP_${PN} += "ldflags already-stripped"
INSANE_SKIP_${PN}-misc += "ldflags already-stripped"

# Note: we are explicitly *not* adding docker to the rdepends, since we allow
#       backends like cri-o to be used.
RDEPENDS_${PN} += "kubeadm \
                   kubectl \
                   kubelet \
                   cni \
                   conntrack-tools \
                  "

RDEPENDS_kubeadm = "kubelet kubectl"
FILES_kubeadm = "${bindir}/kubeadm ${systemd_unitdir}/system/kubelet.service.d/*"

RDEPENDS_kubelet = "iptables socat util-linux ethtool iproute2 ebtables iproute2-tc"
FILES_kubelet = "${bindir}/kubelet ${systemd_unitdir}/system/kubelet.service ${sysconfdir}/kubernetes/manifests/"

SYSTEMD_PACKAGES = "${@bb.utils.contains('DISTRO_FEATURES','systemd','kubelet','',d)}"
SYSTEMD_SERVICE_kubelet = "${@bb.utils.contains('DISTRO_FEATURES','systemd','kubelet.service','',d)}"
SYSTEMD_AUTO_ENABLE_kubelet = "enable"

FILES_kubectl = "${bindir}/kubectl"
FILES_kube-proxy = "${bindir}/kube-proxy"
FILES_${PN}-misc = "${bindir}"


deltask compile_ptest_base
