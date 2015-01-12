
# read additional configurations
if test -f "${gig_project_name}.profile"; then
    echo "reading configuration from: ${gig_project_name}.profile"
    . "${gig_project_name}.profile"
else
    if test -f "/etc/${gig_project_name}/${gig_project_name}.profile"; then
        echo "reading configuration from: /etc/${gig_project_name}/${gig_project_name}.profile"
        . "/etc/${gig_project_name}/${gig_project_name}.profile"
    fi
fi

# which services to handle?
if [[ "x$2" == "x" ]]; then
    services="${all_gig_services[@]}"
else
    services="${@:2}"
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
            printf "%-30s" "restart existing $1 "
            docker start $1 > /dev/null
        else 
            printf "%-30s" "start new $1 "
            id=$(docker run --restart=always -d --name="$1" "${!service_parameters}")
        fi
        sleep 1
        if isRunning $1; then
            echo "up $id"
        else
            echo "error !!!"
            returnCode=1
        fi
    fi
}


stop () {
    if isExisting $1; then                
        if isRunning $1 || isRestarting $1; then
            printf "%-30s" "stopping $1 "
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

pull () {
    imageVar="$1_image"
    docker pull ${!imageVar}
}

rm () {
    if isExisting $1; then                
        if isRunning $1 || isRestarting $1; then
            echo "stopping $1"
            docker stop $1 > /dev/null
        fi
        printf "%-30s" "removing $1 "
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
    printf "%-25s" "$1"
    if isRunning $1; then
        echo "running"
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

ps () {
    docker ps -a | grep -w $1
}

case "$1" in
  start|rm|status|ps)
        for s in $services; do
            $1 $s
        done;
        ;;

  stop)
        servicesReverted=`echo -n "${services[@]} " | tac -s ' '`
        for s in $servicesReverted; do
            $1 $s
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

 help|-h|'')
    cat<<EOF 

Usage: gig COMMAND [service..]
Calling comands without arguments means all services.

    start             start the existing containers, if they are not already up (image must exist.)
    stop              stop containers
    restart           stop containers and then start them
    rollout           Pull and start/restart containers, if needed
    rm                Remove the containers
    ps                execute ps for the containers
    status            shows the running status of each container
    help              Print the list of commands

EOF
    ;;
    
    * )
        returnCode=6
        ;;
    
esac

exit $returnCode
