

## argument handling
arg_command=$1
shift

if [[ "$HOST" =~ ^user.* ]]; then
    echo "yes"
fi

while [[ "$1" =~ ^-.* ]]; do
    case $1 in
        -t | --tag )            shift
                                arg_tag=$1
                                ;;
        -d | --dir )            shift
                                arg_dir=$1
                                ;;
        * )                     echo "wrong option $1"
                                exit 1
    esac
    shift
done

# which services to handle?
if [[ "x$1" == "x" ]]; then
    services="${all_gig_services[@]}"
else
    services="${@:1}"
fi


returnCode=0

isRunning () {
    docker ps -a --filter 'status=running' | awk '{print $NF}' | grep -w $1 > /dev/null
    return $?
}

isRestarting () {
    docker ps -a --filter 'status=restarting' | awk '{print $NF}' | grep -w $1 > /dev/null
    return $?
}

isExited () {
    docker ps -a --filter 'status=exited' | awk '{print $NF}' | grep -w $1 > /dev/null
    return $?
}

isExisting () {
    docker ps -a | awk '{print $NF}' | grep -w $1 > /dev/null
    return $?
}

start () {
    service_parameters="$1[@]"
    if isRunning $1; then
        echo "already running $1"
    else
        if isExited $1; then
            printf "%-40s" "restart existing $1 "
            docker start $1 > /dev/null
        else 
            printf "%-40s" "start new $1 "
            result=$(docker run --restart=always -d --name="$1" "${!service_parameters}")
        fi
        sleep 1
        if isRunning $1; then
            echo "up $result"
        else
            echo "error !!! $result"
            returnCode=1
        fi
    fi
}


stop () {
    if isExisting $1; then                
        if isRunning $1 || isRestarting $1; then
            printf "%-40s" "stopping $1 "
            docker stop $1 > /dev/null
            if isRunning $1;  then
                echo "error !!!"
                returnCode=2
            else
                echo "stopped"
            fi
        else
            echo "not running: $1"
        fi
    else
        echo "not existing: $1"
    fi
}

save-logs () {
    if [ "x$arg_dir" == "x" ] || [ ! -d $arg_dir ]; then
        echo "target directory '$arg_dir' does not exist"
        exit 1;
    fi;
    printf "%-40s" "writing $1 "
    file="$arg_dir/$1_`date +%Y-%m-%d_%H-%M-%S`.log"
    docker logs -t "$1" &> "$file"
    if [ $? -ne 0 ]; then
        echo "error !!!"
        returnCode=2
    else
        echo "$file"
    fi
}

pull () {
    imageVar="$1_image"
    docker pull ${!imageVar}
}

push () {
    imageVar="$1_image"
    docker push ${!imageVar}      docker logs -t "$1" > "$file"
  docker logs -t "$1" > "$file"

}

tag () {
    imageVar="$1_image"
    theTag=$( echo ${!imageVar} | sed -e s/:.*/:$arg_tag/ )
    echo "tagging: ${!imageVar} $theTag"
    docker tag -f ${!imageVar} $theTag
}

rm () {
    if isExisting $1; then                
        if isRunning $1 || isRestarting $1; then
            echo "stopping $1"
            docker stop $1 > /dev/null
        fi
        printf "%-40s" "removing $1 "
        docker rm $1 > /dev/null
        if isExisting $1; then                
            echo "error !!!"
            returnCode=3
        else
            echo "removed"
        fi
    else
        echo "not existing: $1"
    fi
}

status () {
    printf "%-30s" "$1"
    if isRunning $1; then
        echo "up"
        return
    fi
    if isRestarting $1; then
        echo "restarting"
        return
    fi
    if isExited $1; then
        echo "exited"
        return
    fi
    if ! isExisting $1; then
        echo "absent"
        return
    fi
    echo "???"
    returnCode=4
}

versions () {
    echo
    image=$(docker ps -a | grep -w $1 | awk '{print $2}')
    imageRepo=$(echo $image | sed -e s/:.*//)
    imageTag=$(echo $image | sed -e s/.*://)
    imageHash=$(docker images | grep -w "$imageRepo" | grep -w "$imageTag" | awk '{print $3}')
    versionFiles=$(docker exec $1 bash -c 'cat /*.version' | sed -e 's/^/    "/'  | sed -e 's/$/"/' | sed -e ':a;N;$!ba;s/\n/,\n/g')
    
    echo "\"$1\": {"
    echo "  \"image\": \"$image\","
    echo "  \"imageHash\": \"$imageHash\","
    echo "  \"versionFiles\": ["
    echo "$versionFiles"
    echo "  ]"
    echo -n "}"
}

ps () {
    docker ps -a | grep -w $1
}

printHelpCommands() {
    cat<<EOF 
    start               start the existing containers, if they are not already up (image must exist.)
    stop                stop containers
    restart             stop containers and then start them
    restartrm           stop containers, remove them and then start them again
    rollout             pull and start/restart containers, if needed
    rm                  remove the containers
    ps                  execute ps for the containers
    status|ls           shows the running status of each container
    versions            shows json of the image versions and *.version files in containers '/'
    tag -t <tag>        tags the images with the supplied version
    pull                pulls the images from the registry
    push                push the images to the registry
    save-logs -d <dir>  the images to the registry
    help                print the list of commands
EOF
}

case "$arg_command" in
  start|rm|status|ps|pull|push|tag|save-logs)
        for s in $services; do
            $arg_command $s
        done;
        ;;

  status|ls)
        for s in $services; do
            status $s
        done;
        ;;

  stop)
        servicesReverted=`echo -n "${services[@]} " | tac -s ' '`
        for s in $servicesReverted; do
            $arg_command $s
        done;
        ;;

  rollout)
        for s in $services; do
            pull $s
        done;
        # todo: only restart if the image was updated"
        servicesReverted=`echo -n "${services[@]} " | tac -s ' '`
        for s in $servicesReverted; do
            rm $s
        done;
        for s in $services; do
            start $s
        done;
        ;;

  restart)
        servicesReverted=`echo -n "${services[@]} " | tac -s ' '`
        for s in $servicesReverted; do
            stop $s
        done;
        for s in $services; do
            start $s
        done;
        ;;
  restartrm)
        servicesReverted=`echo -n "${services[@]} " | tac -s ' '`
        for s in $servicesReverted; do
            rm $s
        done;
        for s in $services; do
            start $s
        done;
        ;;
  versions)
        echo -n "{"
        separator=""
        for s in $services; do
            echo $separator
            separator=","
            $arg_command $s
        done;
        echo -n "}"
        ;;

 help|-h|'')
    cat<<EOF 

Usage: gig COMMAND [service..]
Calling comands without arguments means all services.

EOF

        printHelpCommands
    ;;
    
 help-commands)
        printHelpCommands
    ;;
    
    * )
        echo "wrong command $arg_command, try 'gig help'"
        returnCode=6
        ;;
    
esac

exit $returnCode
