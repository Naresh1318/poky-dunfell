SUMMARY = "Socket-based service activation daemon"
HOMEPAGE = "https://github.com/xinetd-org/xinetd"
DESCRIPTION = "xinetd is a powerful replacement for inetd, xinetd has access control mechanisms, extensive logging capabilities, the ability to make services available based on time, can place limits on the number of servers that can be started, and has deployable defence mechanisms to protect against port scanners, among other things."

# xinetd is a BSD-like license
# Apple and Gentoo say BSD here.
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://COPYRIGHT;md5=8ad8615198542444f84d28a6cf226dd8"

DEPENDS += "libtirpc"
PR = "r2"

# Blacklist a bogus tag in upstream check
UPSTREAM_CHECK_GITTAGREGEX = "xinetd-(?P<pver>(?!20030122).+)"

SRC_URI = "git://github.com/xinetd-org/xinetd.git;protocol=https \
      file://xinetd.init \
      file://xinetd.conf \
      file://xinetd.default \
      file://Various-fixes-from-the-previous-maintainer.patch \
      file://Disable-services-from-inetd.conf-if-a-service-with-t.patch \
      file://xinetd-should-be-able-to-listen-on-IPv6-even-in-ine.patch \
      file://xinetd-CVE-2013-4342.patch \
      file://0001-configure-Use-HAVE_SYS_RESOURCE_H-to-guard-sys-resou.patch \
      file://xinetd.service \
      "
UPSTREAM_VERSION_UNKNOWN = "1"

SRCREV = "68bb9ab9e9f214ad8a2322f28ac1d6733e70bc24"

S = "${WORKDIR}/git"

inherit autotools update-rc.d systemd

SYSTEMD_SERVICE_${PN} = "xinetd.service"

INITSCRIPT_NAME = "xinetd"
INITSCRIPT_PARAMS = "defaults"

EXTRA_OECONF="--disable-nls"

PACKAGECONFIG ??= "tcp-wrappers"
PACKAGECONFIG[tcp-wrappers] = "--with-libwrap,,tcp-wrappers"

CFLAGS += "-I${STAGING_INCDIR}/tirpc"
LDFLAGS += "-ltirpc"

do_configure() {
	# Looks like configure.in is broken, so we are skipping
	# rebuilding configure and are just using the shipped one
	( cd ${S}; gnu-configize --force )
	oe_runconf
}

do_install() {
	# Same here, the Makefile does some really stupid things,
	# but since we only want two files why not override
	# do_install from autotools and doing it ourselfs?
	install -d "${D}${sbindir}"
	install -d "${D}${sysconfdir}/init.d"
	install -d "${D}${sysconfdir}/xinetd.d"
	install -d "${D}${sysconfdir}/default"
	install -m 644 "${WORKDIR}/xinetd.conf" "${D}${sysconfdir}"
	install -m 755 "${WORKDIR}/xinetd.init" "${D}${sysconfdir}/init.d/xinetd"
	install -m 644 "${WORKDIR}/xinetd.default" "${D}${sysconfdir}/default/xinetd"
	install -m 755 "${B}/xinetd/xinetd" "${D}${sbindir}"
	install -m 755 "${B}/xinetd/itox" "${D}${sbindir}"
	install -m 664 ${S}/contrib/xinetd.d/* ${D}${sysconfdir}/xinetd.d

	# Install systemd unit files
	install -d ${D}${systemd_unitdir}/system
	install -m 0644 ${WORKDIR}/xinetd.service ${D}${systemd_unitdir}/system
	sed -i -e 's,@BASE_BINDIR@,${base_bindir},g' \
	       -e 's,@SBINDIR@,${sbindir},g' \
	       ${D}${systemd_unitdir}/system/xinetd.service
}

CONFFILES_${PN} = "${sysconfdir}/xinetd.conf"
