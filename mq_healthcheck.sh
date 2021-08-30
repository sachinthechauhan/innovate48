#!/bin/sh

source /opt/mqm/bin/setmqenv -s
qmgr_name=$(grep QMGR_NAME /var/mqm/config/mq-config.sh | cut -d ":" -f 2)
qmgr_state=$(dspmq -m $qmgr_name |  sed -e 's/.*STATUS(\(.*\))/\1/g')

if [[ $qmgr_state != 'Running' ]]; then
    echo "QMGR $qmgr_name is not running! Current status is $qmgr_state"
    exit 1
fi
