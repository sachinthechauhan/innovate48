#!/bin/sh

export MQ_QMGR_NAME=$(grep QMGR_NAME /var/mqm/config/mq-config.sh | cut -d ":" -f 2)
echo "Starting queue manager $MQ_QMGR_NAME"
/mq.sh
