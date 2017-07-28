SUMMARY = "Pixhawk firmware binary"

LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://${THISDIR}/files/COPYING.txt;md5=d32239bcb673463ab874e80d47fae504"

firmwaredir = "/firmware"
FILES_${PN} += "${firmwaredir}/"

REPO_NAME = "ardupilot-solo"
REPO_TAG = "solo-${PV}"
FILE_EXT = "px4"
FILE_SRC = "ArduCopter-v2.${FILE_EXT}"
FILE_DST = "ArduCopter-${PV}.${FILE_EXT}"

do_fetch () {
    #
    # look up a release by tag name via the github api,
    # extract the url for the build artifact that we're interested in (*.FILE_EXT),
    # and download it.
    #
    # requires jq 1.4 or later: https://stedolan.github.io/jq/
    #

    # NB: this relies on the fact that tag names in the repo are of a specific form
    #     such that we can derive the tag name from the bitbake PV variable, which is itself
    #     derived from the name of this file.

    # There must be a github "personal access token" in the file ~/.ssh/github_token
    # https://help.github.com/articles/creating-an-access-token-for-command-line-use/
    TOKEN=$(cat ~/.ssh/github_token)

    SRC_URL="https://api.github.com/repos/3drobotics/${REPO_NAME}/releases/tags/${REPO_TAG}"

    BIN_URL=$(curl -s -H "Authorization: token ${TOKEN}" -H "Accept: application/json" ${SRC_URL} | jq -r '.assets[] | select(.name | endswith(".${FILE_EXT}")) | .url')

    # NB: supply github access token as url param because if we supply it as a header,
    #     once github redirects us to s3, it gets included in that request as well
    #     and amazon complains that 2 forms of auth have been provided and quits.
    curl -v -L -H "Accept: application/octet-stream" ${BIN_URL}?access_token=${TOKEN} -o ${WORKDIR}/${FILE_SRC}
}

do_install () {
    install -d ${D}${firmwaredir}
    install -m 0644 ${WORKDIR}/${FILE_SRC} ${D}${firmwaredir}/${FILE_DST}
}
