gig - simple docker starter
===========================

gig is a simple program to start docker containers
in development and production. It is inspired by docker fig,
but more focussed on simple robust processes in production.

gig ist licensed under the MIT license. Suggestions and contributions are welcome.

Basic Usage
===========================

Define your services
------------------------------------------

You have to define your services in a fig.yml file, as in the following example.
The configuration format is staight forward and alinged on the options of the docker run command.
See above for a detailed specification of the format.

```yaml
webserver:
  image: nginx
  ports:
    - "80:80"
    - "443:443"
  volumes:
    - ./my_web_root:/usr/share/nginx/html
```

Start you services
------------------------------------------
Try 
```shell
$> gig start
start new gig_nginx          up 2835cb79e5c475a97364e7f5087afbb26730fe7b56d2c06b588019107ca58acb

$> gig stop
stopping gig_nginx           stopped

$> gig remove
removing gig_nginx           removed
```

All gig docker commands
------------------------------------------
gig supports the following commands to manage docker containers.
Without arguments, to commands will be applied to all your services within the
fig.yml. But you can also specify the services to manage.
```
service commands: command [service..]
    start             start the existing containers, if they are not already up (image must exist.)
    stop              stop containers
    restart           stop containers and then start them
    restartrm         stop containers, remove them and then start them again
    rollout           Pull and start/restart containers, if needed
    rm                Remove the containers
    ps                execute ps for the containers
    status|ls         shows the running status of each container
    help              Print the list of commands
```

Differences between original fig.yml format
-------------------------------------------
- You can use shell variables in all places, which will be expanded while execution


