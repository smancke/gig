
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
