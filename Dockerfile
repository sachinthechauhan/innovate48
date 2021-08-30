FROM docker.clear2pay.com/c2p-base/base-oracle:12.1.0.2-ee-55

USER root

RUN mkdir -p ~/deploy/scripts/ ~/deploy/sql/

COPY oracle/scripts/* /home/oracle/deploy/scripts/
COPY delivery/database/Database.zip /home/oracle/deploy/sql/

RUN chown -R oracle:oinstall /home/oracle/deploy

#TimeZone Change
USER root
ENV TZ="Asia/Bangkok"
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

USER oracle

RUN dos2unix -o ~/deploy/scripts/start-and-update.sh && \
    chmod u+x ~/deploy/scripts/*.sh

RUN unzip -d ~/deploy/sql/ ~/deploy/sql/Database.zip && \
    rm -f ~/deploy/sql/Database.zip && \
    find ~/deploy/sql/ -name "*.sql" -exec dos2unix -o {} \; && \
    find ~/deploy/sql/ -name "*.sh" -exec dos2unix -o {} \; && \
    find ~/deploy/sql/ -name "*.sh" -exec chmod u+x {} \;

ENV DB_TARGET siamph
ENV DB_USER_PREFIX BPH

ARG migrationFlag
RUN echo "[SIAMCC-2440] Dockerfile migration flag: $migrationFlag"

RUN ~/deploy/scripts/start-and-update.sh "${migrationFlag}"
# cat $(find -name "~/deploy/scripts/sql/*.log" -type f -print0 | xargs -r0 stat -c %y\ %n | sort | cut -d " " -f4) >> /dev/tty

HEALTHCHECK --interval=5s --timeout=5s --retries=120 CMD /home/oracle/oracle-base/bash/oracle_healthcheck.sh > /dev/null 2>&1