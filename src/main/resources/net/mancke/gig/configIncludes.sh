
BASE_DIR=`pwd`

# read additional configurations
if test -f "gig.profile"; then
    echo "reading configuration from: gig.profile" >/dev/stderr
    . "gig.profile"
fi
