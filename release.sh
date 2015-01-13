#!/bin/bash

version=$1
if [ "x$version" == "x" ]; then
    echo "usage: $0 <version>"
    exit 1
fi


gradle clean shadowJar
mkdir gig_$version
cp build/libs/gig-all.jar gig_$version/gig-$version.jar
cat > gig_$version/gig <<EOF
#!/bin/bash

java -version  2>&1 | grep 1.8 > /dev/null
if [ \$? != 0 ]; then
    echo "WARNING: java should be an 1.8er Java"
fi

DIR=\$( cd "\$( dirname "\${BASH_SOURCE[0]}" )" && pwd )
java -jar \$DIR/gig-$version.jar "\$@"
EOF

chmod a+x gig_$version/gig
tar -c gig_$version | gzip > gig_$version.tar.gz

git tag -a $version -m "released version $version"

