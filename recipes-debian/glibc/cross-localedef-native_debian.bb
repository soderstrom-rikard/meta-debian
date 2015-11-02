SUMMARY = "Cross locale generation tool for glibc"
HOMEPAGE = "http://www.gnu.org/software/libc/libc.html"

inherit debian-package
PR = "0"
DPN = "glibc"

LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://LICENSES;md5=e9a558e243b36d3209f380deb394b213 \
      file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263 \
      file://posix/rxspencer/COPYRIGHT;md5=dc5485bb394a13b2332ec1c785f5d83a \
      file://COPYING.LIB;md5=4fbd65380cdd255951079008b364516c"

FILESEXTRAPATHS =. "${FILE_DIRNAME}/${PN}:"

SRC_URI += "\
           git://github.com/kraj/localedef;protocol=${DEBIAN_GIT_PROTOCOL};\
branch=master;name=localedef;destsuffix=git/localedef \
           ${EGLIBCPATCHES} \
          "
EGLIBCPATCHES = "\
           file://eglibc.patch \
           file://option-groups.patch \
           file://fix-not-found-tls.patch \
           file://fix-not-found-mempcpy.patch \
           file://use-options-group-for-argp-help.patch \
           file://use-options-group-for-argp-fmtstream.patch \
           file://add-support-uint-32.patch \
          "

inherit native
inherit autotools

# Makes for a rather long rev (22 characters), but...
#
SRCREV_FORMAT = "glibc_localedef"

EXTRA_OECONF = "--with-glibc=${S}"
CFLAGS += "-fgnu89-inline -std=gnu99 -DIS_IN\(x\)='0'"

do_configure () {
	${S}/localedef/configure ${EXTRA_OECONF}
}

do_install() {
	install -d ${D}${bindir}
	install -m 0755 ${B}/localedef ${D}${bindir}/cross-localedef
}
