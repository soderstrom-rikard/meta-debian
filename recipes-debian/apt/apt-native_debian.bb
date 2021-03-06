#
# base recipe: meta/recipes-devtools/apt/apt-native_0.9.9.4.bb
# base branch: daisy
#

require apt.inc

PR = "r0"

inherit native

DEPENDS += "dpkg-native gettext-native db-native"

PACKAGES = ""
USE_NLS = "yes"

# Exclude inappropriate patch which for old version 
# db_linking_hack.patch
# Resolve conflict for noconfigure.patch and no-curl.patch
# Note that no-curl.patch make apt does not support https methods
# Patch file no-ko-translation_debian.patch should not be apply
# since all available translations should be mentioned.
SRC_URI += " \
	file://noconfigure_debian.patch \
	file://no-curl_debian.patch \
"

# Skip build test for native package
SRC_URI += " \
	file://gtest-skip-fix.patch \
"

EXTRA_OECONF = " --with-cpus=1 --with-procs=1 --with-proc-multiply=1"

do_configure_prepend() {
	sed -i -e "s#AC_PATH_PROG(XSLTPROC,xsltproc)#\
		AC_PATH_PROG(XSLTPROC,xsltproc,[], [${STAGING_BINDIR_NATIVE}])#" \
		${S}/configure.ac
}

python do_install () {
    bb.build.exec_func('do_install_base', d)
    bb.build.exec_func('do_install_config', d)
}

python do_install_config () {
    indir = os.path.dirname(d.getVar('FILE',1))
    infile = file(oe.path.join(indir, 'files', 'apt.conf'), 'r')
    data = infile.read()
    infile.close()

    data = d.expand(data)

    outdir = oe.path.join(d.getVar('D', True), d.getVar('sysconfdir', True), 'apt')
    if not os.path.exists(outdir):
        os.makedirs(outdir)

    outpath = oe.path.join(outdir, 'apt.conf.sample')
    if not os.path.exists(outpath):
        outfile = file(outpath, 'w')
        outfile.write(data)
        outfile.close()
}

do_install_base () {
	install -d ${D}${bindir}
	install -m 0755 bin/apt-cdrom ${D}${bindir}/
	install -m 0755 bin/apt-get ${D}${bindir}/
	install -m 0755 bin/apt-config ${D}${bindir}/
	install -m 0755 bin/apt-cache ${D}${bindir}/
	install -m 0755 bin/apt-sortpkgs ${D}${bindir}/
	install -m 0755 bin/apt-extracttemplates ${D}${bindir}/
	install -m 0755 bin/apt-ftparchive ${D}${bindir}/

	oe_libinstall -so -C bin libapt-private ${D}${libdir}/

	eval `cat environment.mak | grep ^GLIBC_VER | sed -e's, = ,=,'`
	eval `cat environment.mak | grep ^LIBSTDCPP_VER | sed -e's, = ,=,'`
	oe_libinstall -so -C bin libapt-pkg$GLIBC_VER$LIBSTDCPP_VER ${D}${libdir}/
	ln -sf libapt-pkg$GLIBC_VER$LIBSTDCPP_VER.so ${D}${libdir}/libapt-pkg.so
	oe_libinstall -so -C bin libapt-inst$GLIBC_VER$LIBSTDCPP_VER ${D}${libdir}/
	ln -sf libapt-inst$GLIBC_VER$LIBSTDCPP_VER.so ${D}${libdir}/libapt-inst.so

	install -d ${D}${libdir}/apt/methods
	install -m 0755 bin/methods/* ${D}${libdir}/apt/methods/

	install -d ${D}${libdir}/dpkg/methods/apt
	install -m 0644 ${S}/dselect/desc.apt ${D}${libdir}/dpkg/methods/apt/
	install -m 0644 ${S}/dselect/names ${D}${libdir}/dpkg/methods/apt/
	install -m 0755 ${S}/dselect/install ${D}${libdir}/dpkg/methods/apt/
	install -m 0755 ${S}/dselect/setup ${D}${libdir}/dpkg/methods/apt/
	install -m 0755 ${S}/dselect/update ${D}${libdir}/dpkg/methods/apt/

	install -d ${D}${sysconfdir}/apt
	install -d ${D}${sysconfdir}/apt/apt.conf.d
	install -d ${D}${sysconfdir}/apt/preferences.d
	install -d ${D}${localstatedir}/lib/apt/lists/partial
	install -d ${D}${localstatedir}/cache/apt/archives/partial

	install -d ${D}${localstatedir}/log/apt/
}
