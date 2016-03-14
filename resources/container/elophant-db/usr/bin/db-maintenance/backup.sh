#!/bin/bash
source /etc/envvars

if [ ! -z "${ELOPHANT_AWS_KEY_BACKUP_CREATE}" ] && [ ! -z "${ELOPHANT_AWS_SECRET_BACKUP_CREATE}" ]; then
  LAST_RUN="$(cat ./lastrun)"
  CURRENT_TIME="$(date +'%s')"
  DELTA=$((CURRENT_TIME - LAST_RUN))
  DAYS=$((DELTA / 86400))
  if ((DAYS >= ELOPHANT_DB_BACKUP_FREQUENCY_DAYS)); then
    export AWS_ACCESS_KEY_ID=${ELOPHANT_AWS_KEY_BACKUP_CREATE}
    export AWS_SECRET_ACCESS_KEY=${ELOPHANT_AWS_SECRET_BACKUP_CREATE}

    BACKUP_NAME="backup-$(hostname)-$(date +'%F-%s')"
    rm -f backup-*
    pg_dump -U postgres elophant > "${BACKUP_NAME}"
    gzip ${BACKUP_NAME}
    aws s3 mv ${BACKUP_NAME}.gz s3://elophant-db-backup/${BACKUP_NAME}.gz

    export AWS_ACCESS_KEY_ID=""
    export AWS_SECRET_ACCESS_KEY=""
    echo "$(date +'%s')" > ./lastrun
  fi
else
  if [ ! -z "${ELOPHANT_DB_BACKUP_FREQUENCY_DAYS}" ]; then
    echo "A backup frequency was set, but no AWS credentials were provided"
  fi
fi
