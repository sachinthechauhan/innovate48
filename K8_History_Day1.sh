[root@vlmazjuly197 k8]# kubectl apply -f podenv.yml
pod/environments created
[root@vlmazjuly197 k8]# kubectl exec environments -- env
PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
HOSTNAME=environments
greet=hello
KUBERNETES_SERVICE_HOST=10.96.0.1
KUBERNETES_SERVICE_PORT=443
KUBERNETES_SERVICE_PORT_HTTPS=443
KUBERNETES_PORT=tcp://10.96.0.1:443
KUBERNETES_PORT_443_TCP=tcp://10.96.0.1:443
KUBERNETES_PORT_443_TCP_PROTO=tcp
KUBERNETES_PORT_443_TCP_PORT=443
KUBERNETES_PORT_443_TCP_ADDR=10.96.0.1
HOME=/root
[root@vlmazjuly197 k8]# cat podenv.yml
kind: Pod
apiVersion: v1
metadata:
  name: environments
spec:
  containers:
    - name: c00
      image: ubuntu
      command: ["/bin/bash", "-c", "while true; do echo hello; sleep 5 ; done"]
      env:                                   # List of environment variables to be used inside the pod
      - name: greet
        value: hello

[root@vlmazjuly197 k8]# clear
[root@vlmazjuly197 k8]# kubectl apply -f podlabels.yml
pod/labelspod created
[root@vlmazjuly197 k8]# cat podlabels.yml
kind: Pod
apiVersion: v1
metadata:
  name: labelspod
  labels:                                                    # Specifies the Label details under it
     app: test
     env: prd
spec:
    containers:
       - name: c00
         image: ubuntu
         command: ["/bin/bash", "-c", "while true; do echo Hello; sleep 5 ; done"]
[root@vlmazjuly197 k8]# kubectl get pods
NAME           READY   STATUS    RESTARTS   AGE
environments   1/1     Running   0          17m
labelspod      1/1     Running   0          34s
testpod        1/1     Running   0          51m
testpod2       1/1     Running   0          41m
testpod3       2/2     Running   0          38m
[root@vlmazjuly197 k8]# kubectl get pods --show-labels
NAME           READY   STATUS    RESTARTS   AGE   LABELS
environments   1/1     Running   0          17m   <none>
labelspod      1/1     Running   0          67s   app=test,env=prd
testpod        1/1     Running   0          51m   <none>
testpod2       1/1     Running   0          42m   <none>
testpod3       2/2     Running   0          38m   <none>
[root@vlmazjuly197 k8]# kubectl label pods labelspod version=1.0
pod/labelspod labeled
[root@vlmazjuly197 k8]# kubectl get pods --show-labels
NAME           READY   STATUS    RESTARTS   AGE    LABELS
environments   1/1     Running   0          18m    <none>
labelspod      1/1     Running   0          112s   app=test,env=prd,version=1.0
testpod        1/1     Running   0          52m    <none>
testpod2       1/1     Running   0          42m    <none>
testpod3       2/2     Running   0          39m    <none>
[root@vlmazjuly197 k8]# kubectl get pods -l env=prd
NAME        READY   STATUS    RESTARTS   AGE
labelspod   1/1     Running   0          2m11s
[root@vlmazjuly197 k8]# kubectl get pods -l  app=test,env=prd,version=1.0
NAME        READY   STATUS    RESTARTS   AGE
labelspod   1/1     Running   0          2m36s
[root@vlmazjuly197 k8]# kubectl label pods testpod version=1.0
pod/testpod labeled
[root@vlmazjuly197 k8]# kubectl get pods -l  app=test,env=prd,version=1.0
NAME        READY   STATUS    RESTARTS   AGE
labelspod   1/1     Running   0          4m9s
[root@vlmazjuly197 k8]# kubectl get pods -l  version=1.0
NAME        READY   STATUS    RESTARTS   AGE
labelspod   1/1     Running   0          4m25s
testpod     1/1     Running   0          55m
[root@vlmazjuly197 k8]# kubectl delete pods -l  version=1.0
pod "labelspod" deleted
pod "testpod" deleted
^C
[root@vlmazjuly197 k8]# kubectl get pods
NAME           READY   STATUS        RESTARTS   AGE
environments   1/1     Running       0          22m
labelspod      1/1     Terminating   0          6m11s
testpod        1/1     Terminating   0          57m
testpod2       1/1     Running       0          47m
testpod3       2/2     Running       0          43m
[root@vlmazjuly197 k8]# kubectl get pods
NAME           READY   STATUS    RESTARTS   AGE
environments   1/1     Running   0          23m
testpod2       1/1     Running   0          47m
testpod3       2/2     Running   0          44m
[root@vlmazjuly197 k8]# kubectl get pods -l 'env in (prd)'
No resources found in default namespace.
[root@vlmazjuly197 k8]# kubectl get pods
NAME           READY   STATUS    RESTARTS   AGE
environments   1/1     Running   0          32m
testpod2       1/1     Running   0          56m
testpod3       2/2     Running   0          53m
[root@vlmazjuly197 k8]# kubectl apply -f podlabels.yml
pod/labelspod created
[root@vlmazjuly197 k8]# kubectl get pods -l 'env in (prd)'
NAME        READY   STATUS    RESTARTS   AGE
labelspod   1/1     Running   0          3s
[root@vlmazjuly197 k8]# kubectl get pods --show-labels
NAME           READY   STATUS    RESTARTS   AGE   LABELS
environments   1/1     Running   0          33m   <none>
labelspod      1/1     Running   0          25s   app=test,env=prd
testpod2       1/1     Running   0          57m   <none>
testpod3       2/2     Running   0          54m   <none>
[root@vlmazjuly197 k8]# ls -lrt
total 32
-rw-r--r-- 1 root root 344 Nov 22 21:13 podannotate.yml
-rw-r--r-- 1 root root 313 Nov 22 21:18 podmulcont.yml
-rw-r--r-- 1 root root 338 Nov 22 21:30 podenv.yml
-rw-r--r-- 1 root root 330 Nov 22 21:44 podlabels.yml
-rw-r--r-- 1 root root 388 Nov 22 22:02 nodelabels.yml
-rw-r--r-- 1 root root 682 Nov 22 22:15 rc.yml
-rw-r--r-- 1 root root 861 Nov 22 22:27 rs.yml
-rw-r--r-- 1 root root 688 Nov 23 00:20 pod.yml
[root@vlmazjuly197 k8]# history
    1  yum update
    2  exit
    3  sudo yum remove docker                   docker-client                   docker-client-latest                   docker-common                   docker-latest                   docker-latest-logrotate                   docker-logrotate                   docker-engine
    4  sudo yum install -y yum-utils
    5  sudo yum-config-manager     --add-repo     https://download.docker.com/linux/centos/docker-ce.repo
    6  [root@vlmazjuly197 ~]# sudo yum install -y yum-utils
    7  Last metadata expiration check: 1:40:16 ago on Fri 13 Nov 2020 02:13:53 AM CST.
    8  Package yum-utils-4.0.8-4.el8_1.noarch is already installed.
    9  Dependencies resolved.
   10  Nothing to do.
   11  Complete!
   12  sudo yum install docker-ce docker-ce-cli containerd.io
   13   sudo yum-config-manager
   14  –add-repo
   15   sudo yum-config-manager –add-repo https://download.docker.com/linux/centos/docker-ce.repo
   16  pwd
   17  ls
   18  cd etc
   19  cd /etc/
   20  ls
   21  cd yum.repos.d
   22  ls
   23  vi docker-ce.repo
   24  ls
   25  cd..
   26  cd ..
   27  pwd
   28  sudo
   29  dzdo  su -
   30  sudo
   31  docker images
   32  docker run hello-world
   33  yum -y upgrade
   34  pwd
   35  ls
   36  docker images
   37  docker version
   38  docker images
   39  systemctl start docker
   40  docker images
   41  sudo systemctl start docker
   42   docker run hello-world
   43  ls
   44  pwd
   45  cd /home/e1075868
   46  ls
   47  cd httpd/
   48  ls
   49  clear
   50  docker build .
   51  docker images
   52  docker run -it -d b2740c2efd87
   53  curl 10.7.158.36:80
   54  docker ps
   55  docker stop 3211932881e5
   56  docker rm 3211932881e5
   57  docker -t 3211932881e5 myhttpd:1.0
   58  docker -t 3211932881e5 myhttpd
   59  docker tag 3211932881e5 myhttpd:1.0
   60  docker images
   61  docker tag b2740c2efd87 myhttpd:1.0
   62  docker images
   63  [root@vlmazjuly197 httpd]# docker images
   64  REPOSITORY          TAG                 IMAGE ID            CREATED             SIZE
   65  <none>              <none>              b2740c2efd87        11 minutes ago      254MB
   66  centos              latest              0d120b6ccaa8        3 months ago        215MB
   67  hello-world         latest              bf756fb1ae65        10 months ago       13.3kB
   68  [root@vlmazjuly197 httpd]# docker tag b2740c2efd87 myhttpd:1.0
   69  [root@vlmazjuly197 httpd]# docker images
   70  REPOSITORY          TAG                 IMAGE ID            CREATED             SIZE
   71  myhttpd             1.0                 b2740c2efd87        11 minutes ago      254MB
   72  centos              latest              0d120b6ccaa8        3 months ago        215MB
   73  hello-world         latest              bf756fb1ae65        10 months ago       13.3kB
   74  docker images
   75  docker run -d -it --name mywebserver -p 10.7.158.36:80:80 myhttpd
   76  docker run -d -it --name mywebserver -p 10.7.158.36:80:80 myhttpd:1.0
   77  curl 10.7.158.36:80
   78  docker images
   79  docker ps
   80  [root@vlmazjuly197 httpd]# docker images
   81  docker images
   82  docker ps
   83  docker stop dcbdb2c48a7a
   84  docker images
   85  docker ps
   86  docker rm dcbdb2c48a7a
   87  docker images
   88  docker build .
   89  docker -it -d --name mywebserver -p 10.7.158.36:80:80
   90  docker run -it -d --name mywebserver -p 10.7.158.36:80:80
   91  docker run -d -it --name mywebserver -p 10.7.158.36:80:80 myhttpd:2.0
   92  docker run -d -it --name mywebserver -p 10.7.158.36:80:80 myhttpd
   93  docker run -d -it --name mywebserver -p 10.7.158.36:80:80 myhttpd:1.0
   94  curl 10.7.158.36:80
   95  docker ps
   96  docker stop cc8663a4ef4c
   97  docker rm cc8663a4ef4c
   98  docker images
   99  docker  tag 7dd9da82fe70 httpd:2.0
  100  docker run -d -it --name mywebserver -p 10.7.158.36:80:80 myhttpd:2.0
  101  docker images
  102  docker run -d -it --name mywebserver1 -p 10.7.158.36:80:80 httpd:2.0
  103  curl 10.7.158.36:80
  104  docker ps
  105  docker stop 9df2e03fc94f
  106  docker ps
  107  docker rm 9df2e03fc94f
  108  docker ps
  109  docker images
  110  docker rm 7dd9da82fe70
  111  docker image rm 7dd9da82fe70
  112  docker images
  113  pwd
  114  ls
  115  cat index.html
  116  docker build .
  117  docker images
  118  docker tag b95cb7b9a133 myhttpd:2.0
  119  docker images
  120  docker run -d -it --name mywebserver1 -p 10.7.158.36:80:80 httpd:2.0
  121  docker run -d -it --name mywebserver1 -p 10.7.158.36:80:80 myhttpd:2.0
  122  curl 10.7.158.36:80
  123  docker ps
  124  docker build .
  125  cls
  126  clear
  127  docker ps
  128  docker stop 84bb0d83ff87
  129  docker rm 84bb0d83ff87
  130  docker images
  131  docker image rm b95cb7b9a133 b2740c2efd87
  132  docker images
  133  docker build .
  134  cd /etc/yum.repos.d/
  135  ls
  136  cat redhat.repo
  137  cat rh-cloud-rhel8-eus.repo
  138  clear
  139  cat rh-cloud-rhel8-eus.repo
  140  pwd
  141  cd /home/e1075868/httpd
  142  ls
  143  docker build .
  144  cd /etc/yum.repos.d
  145  ls
  146  yumm update
  147  yum update
  148  ls
  149  cat rh-cloud-rhel8-eus.repo
  150  rh-cloud-rhel8-eus.repo
  151  cat rh-cloud-rhel8-eus.repo
  152  clear
  153  rh-cloud-rhel8-eus.repo
  154  cat rh-cloud-rhel8-eus.repo
  155  pwd
  156  docker images
  157  docker image rm 0d120b6ccaa8 9f940c3ce656
  158  docker ps
  159  docker images
  160  docker image rm 9f940c3ce656
  161  docker container ls
  162  docker container ls -a
  163  docker build  /home/e1075868/httpd/
  164  docker images
  165  docker ls container -a
  166  docker container ls -a
  167  docker ps -a
  168  clear
  169  docker ps -a
  170  docker rm b36fc20d2869
  171  docker rm 0628d39345f3
  172  docker rm bdc848627486
  173  docker rm feba3b5e665d
  174  docker ps -a
  175  clear
  176  docker ps -a
  177  docker container ls -a
  178  cd /home/e1075868/httpd
  179  pwd
  180  doocker images -a
  181  doocker images
  182  docker images
  183  docker image rm 9f940c3ce656
  184  docker images
  185  docker images -a
  186  clear
  187  docker pull centos
  188  docker build .
  189  cd /etc/yum.repos.d/
  190  ls
  191  vi docker-ce.repo
  192  docker build  /home/e1075868/httpd/
  193  vi docker-ce.repo
  194  ls
  195  vi rh-cloud-rhel8-eus.repo
  196  docker build  /home/e1075868/httpd/
  197  cd /home/e1075868
  198  ls
  199  cd httpd/
  200  ls
  201  cat index.html
  202  clear
  203  docker build .
  204  sudo systemctl start docker
  205  docker build .
  206  cd ..
  207  cd nodejs-app/
  208  ls
  209  docker build .
  210  docker ps
  211  docker images
  212  docker tag 9ce318841e72 nodejs_app
  213  docker images
  214  docker tag 9ce318841e72 nodejs_app:1.0
  215  docker images
  216  docker run -d -it --name nodejs_app 10.7.158.36:9005:9005 nodejs_app:1.0
  217  docker run -d -it --name nodejs_app -p 10.7.158.36:9005:9005 nodejs_app:1.0
  218  docker ps
  219  docker stop 1ca56e183a63
  220  docker images
  221  docker run -d -it --name nodejs_app -p 10.7.158.36:9005:9005 nodejs_app:1.0
  222  docker ps
  223  docker ps -a
  224  docker rm 1ca56e183a63
  225  docker run -d -it --name nodejs_app -p 10.7.158.36:9005:9005 nodejs_app:1.0
  226  yum install vim
  227  cd /etc/yum.repos.d/
  228  vim docker-ce.repo
  229  cd /etc/yum.repos.d/docker info
  230  docker info
  231  systemctl docker start
  232  systemctl start docker
  233  docker info
  234  docker run -i -t ubuntu //bin/bash
  235  docker images
  236  docker ps
  237  docker container ls
  238  docker run -i -t ubuntu //bin/bash
  239  docker images
  240  docker container ps
  241  docker container ps -a
  242  docker run -f -i -t ubuntu //bin/bash
  243  docker run -i -t ubuntu //bin/bash -f
  244    docker run --name adam_new -d ubuntu //bin/sh -c "while
  245   true; do echo hello world; sleep 5; done“
  246  docker run --name adam_new -d ubuntu //bin/sh -c "while true; do echo hello world; sleep 5; done“
  247  docker images
  248  docker run –d -p 80:70 nginx:latest
  249  cd /home/e1075868
  250  docker run –d -p 80:70 nginx:latest
  251  docker run -p 80:70 nginx:latest
  252  docker containr ps -a
  253  docker containrs ps -a
  254  docker ps containrs -a
  255  docker ps containr -a
  256  docker ps -a
  257  clear
  258  curl -L https://github.com/docker/compose/releases/download/1.27.4/docker-compose-`uname -s`-`uname -m` -o /usr/local/bin/docker-compose
  259  docker-compose version
  260  chmod +x /usr/local/bin/docker-compose
  261  docker-compose version
  262  docker version
  263  systemctl docker start
  264  systemctl start docker
  265  docker version
  266  ls
  267  cd /home/e1075868/docker-compose/
  268  ls
  269  cd appa/
  270  ls
  271  docker-compose up --build
  272  ls
  273  docker-compose up --build
  274  ls -lrt
  275  ls
  276  ls -lrt
  277  vi docker-compose.yml
  278  ls
  279  docker-compose up --build
  280  ls
  281  cd ..
  282  ls
  283  clear
  284  cd todolist/
  285  ls
  286  cat pom.xml
  287  clear
  288  docker-compose up
  289  clear
  290  sudo apt-get install --reinstall ca-certificates
  291  sudo yum install --reinstall ca-certificates
  292  docker-compose up
  293  ls
  294  ls -lrt
  295  docker-compose up
  296  clear
  297  cat Dockerfile
  298  docker-compose up
  299  clear
  300  docker-compose up
  301  clear
  302  docker-compose up
  303  ls
  304  docker-compose up
  305  clear
  306  docker-compose up
  307  clear
  308  docker-compose up
  309  clear
  310  docker-compose up
  311  clear
  312  docker-compose up
  313  cls
  314  clear
  315  docker-compose up
  316  clear
  317  docker-compose up
  318  exit
  319  cd /home/e1075868/httpd/
  320  ls
  321  docker ps
  322  docker stop b0981c9b84c8
  323  cd /home/e1075868
  324  cd httpd/
  325  ls
  326  cat steps.txt
  327  docker run -d -it --name mywebserver -p 192.168.0.1:80:80 myhttpd
  328  ls
  329  docker build .
  330  docker -t myhttpd:7.0
  331  docker -t 09cc4b317740  myhttpd:7.0
  332  docker tag 09cc4b317740  myhttpd:7.0
  333  docker images
  334  docker run -d -it -p 10.7.158.36:80:80 myhttpd
  335  docker run -d -it -p 10.7.158.36:80:80 myhttpd:7.0
  336  ls
  337  cat Dockerfile
  338  curl 10.7.158.36:80
  339  cat index.html
  340  clear
  341  docker images
  342  docker history 09cc4b317740
  343  docker ps
  344  cd ..
  345  mkdir website
  346  cd website/
  347  touch index.html
  348  vi index.html
  349  ls
  350  docker containsr ls
  351  docker container ls
  352  docker stop 9e305b95d482
  353  docker rm 9e305b95d
  354  cd ..
  355  cd httpd/
  356  ls
  357  clear
  358  docker run -d -p 5000:5000 --restart=always --name registry registry1:2
  359  docker run -d -p 5000:5000 --restart=always --name registry registry:2
  360  docker ps
  361  docker stop e92c3bc7d94b
  362  docker rm e92c3bc7d94b
  363  docker container ps -a
  364  clear
  365  docker images
  366  docker rmi 2d4f4b5309b1
  367  docker images
  368  clear
  369  docker run -d -p 5000:5000 --restart=always --name registry registry:2
  370  docker images
  371  clear
  372  docker pull ubuntu:16.04
  373  docker images
  374  docker tag ubuntu:16.04 localhost:5000/my-ubuntu
  375  docker images
  376  docker push localhost:5000/my-ubuntu
  377  docker rmi localhost:5000/my-ubuntu
  378  docker images
  379  docker pull localhost:5000/my-ubuntu
  380  docker images
  381  curl -X GET http://registry:5000/v2/_catalog
  382  docker container ps
  383  http://vlmazjuly197.fisdev.local:5000/v2/_catalog
  384  curl http://vlmazjuly197.fisdev.local:5000/v2/_catalog
  385  curl -X DELETE http://vlmazjuly197.fisdev.local:5000/v2/_catalog
  386  systemctl start docker
  387  dnf provides tc
  388  vi /etc/yum.repos.d/docker-ce.repo
  389  yum update
  390  dnf provides tc
  391  swapoff -a
  392  dnf provides tc
  393  vi /etc/hosts
  394  dnf provides tc
  395  sudo dnf provides tc
  396  exit
  397  conntrack --version
  398  whereis conntrack
  399  sudo minikube start --driver=none
  400  whereis minicube
  401  sudo mv /usr/local/bin/minikube /usr/bin/
  402  ls .usr/bin
  403  cd /usr/bin
  404  ls
  405  ls minicube
  406  cd /usr/local/bin
  407  ls
  408  docker version
  409  cd..
  410  cd ..
  411  c d..
  412  dzdo su -
  413  dnf provides tc
  414  cat /var/log/dnf.log
  415  yum install conntrack
  416  yum repolist
  417  yum search conntrack
  418  conntrack --version
  419  sudo su -
  420  sudo apt install conntrack
  421  sudo yum install conntrack
  422  clear
  423  dnf install conntrack-tools
  424  cd /etc/yum.repos.d
  425  ls
  426  vi rh-cloud-rhel8-eus.repo
  427  cat rh-cloud-rhel8-eus.repo
  428  clear
  429  yum -update
  430  yum update
  431  kubectl api-resources
  432  kubectl get pods
  433  kubectl -version
  434  kubectl version
  435  kubectl
  436  cd /usr/bin
  437  ls
  438  ls -lrt
  439  dzdo su -
  440  cd /var/lib/rpm
  441  ls
  442  cd /
  443  cd root
  444  cat /var/log/dnf.log
  445  clear
  446  swapoff -a
  447  sudo mkdir /etc/docker
  448  cat <<EOF | sudo tee /etc/docker/daemon.json
  449  {
  450    "exec-opts": ["native.cgroupdriver=systemd"],
  451    "log-driver": "json-file",
  452    "log-opts": {
  453      "max-size": "100m"
  454    },
  455    "storage-driver": "overlay2",
  456    "storage-opts": [
  457      "overlay2.override_kernel_check=true"
  458    ]
  459  }
  460  EOF
  461  sudo mkdir -p /etc/systemd/system/docker.service.d
  462  sudo systemctl daemon-reload
  463  sudo systemctl restart docker
  464  sudo systemctl enable docker
  465  cat <<EOF | sudo tee /etc/yum.repos.d/kubernetes.repo
  466  [kubernetes]
  467  name=Kubernetes
  468  baseurl=https://packages.cloud.google.com/yum/repos/kubernetes-el7-\$basearch
  469  enabled=1
  470  gpgcheck=1
  471  repo_gpgcheck=1
  472  gpgkey=https://packages.cloud.google.com/yum/doc/yum-key.gpg https://packages.cloud.google.com/yum/doc/rpm-package-key.gpg
  473  exclude=kubelet kubeadm kubectl
  474  EOF
  475  sudo setenforce 0
  476  sudo sed -i 's/^SELINUX=enforcing$/SELINUX=permissive/' /etc/selinux/config
  477  sudo yum install -y kubelet kubeadm kubectl --disableexcludes=kubernetes
  478  yum install conntrack
  479  yum install conntrack --skip-broken
  480  yum update
  481  yum repolist
  482  dnf install conntrack -y
  483  dnf update -y
  484  dnf install conntrack -y
  485  curl -LO https://storage.googleapis.com/kubernetes-release/release/$(curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt)/bin/linux/amd64/kubectl
  486  chmod +x ./kubectl
  487  sudo mv ./kubectl /usr/local/bin/kubectl
  488  curl -Lo minikube https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
  489  chmod +x minikube
  490  sudo mv minikube /usr/local/bin/
  491  yum install conntrack
  492  minikube start --vm-driver=none
  493  yum repolist
  494  sudo subscription-manager repos --list-enabled
  495  cat
  496  cat /etc/dnf/plugins/subscription-manager.conf
  497  sudo yum update
  498  cd /etc/yum.repos.d
  499  ls
  500  cat rh-cloud-rhel8-eus.repo
  501  f
  502  cat rh-cloud-rhel8-eus.repo
  503  cd /
  504  cd root
  505  swapoff -a
  506  sudo dnf provides tc
  507  sudo dnf install iproute-tc
  508  yum install conntrack
  509  sudo yum install -y kubelet kubeadm kubectl --disableexcludes=kubernetes
  510  sudo systemctl enable --now kubelet
  511  kubectl version
  512  kubeadm join 10.7.158.33:6443 --token lg2t4s.cck3xtkmsf46psqu     --discovery-token-ca-cert-hash sha256:72adde93f0270171e6058571dc7a574e4de2f7848b678162e176846cdd15625a
  513  vi /etc/hosts
  514  kubectl get nodes
  515  clear
  516  kubectl get pods
  517  clear
  518  curl -Lo minikube https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
  519  chmod +x minikube
  520  sudo mv minikube /usr/local/bin/
  521  minikube start --vm-driver=none
  522  minucube status
  523  minicube status
  524  minikube status
  525  clear
  526  minikube start --vm-driver=none
  527  minikube status
  528  kubectl get pods
  529  kubectl get pods --all-namespaces
  530  kubectl proxy
  531  docker version
  532  kubectl api-resources -o wide
  533  cd /home/e1075868/
  534  ls
  535  mkdir k8
  536  cd k8
  537  vi podannotate.yml
  538  vi pod.yml
  539  kubectl create -f pod1.yml
  540  kubectl create -f pod.yml
  541  kubectl apply -f podannotate.yml
  542  kubectl get pods
  543  kubectl get pods -o wide
  544  kubectl get pods --all-namespaces
  545  kubectl describe pod testpod
  546  kubectl logs -f testpod
  547  kubectl logs -f testpod1
  548  kubectl logs -f testpod2
  549  kubectl get pods
  550  ll
  551  alias k=kubectl
  552  k get pods
  553  k describe testpod2
  554  k describe pod testpod2
  555  k logs -f testpod,testpod2
  556  k logs -c c00
  557  k logs -f testpod -c c00
  558  kubectl get pods --all-namespaces
  559  k exec testpod -- echo hi
  560  k logs testpod
  561  k logs -f testpod
  562  k logs testpod
  563  kubectl exec testpod2 -- hostname -i
  564  kubectl exec testpod2 -c c00 -- hostname -i
  565  k exec testpod -i -t -- /bin/bash
  566  k attach testpo -i
  567  k attach testpod -i
  568  k attach testpod -i -t
  569  k attach testpod -c c00 -i -t
  570  k attach testpod2 -c c00 -i -t
  571  k attach testpod2 -c c00 -it
  572  k attach -it testpod2 -c c00
  573  ls
  574  vi podannotate.yml
  575  k apply -f podannotate.yml
  576  k delete pods testpod2
  577  k describe pod testpod2
  578  k describe pod testpod2 -o --wide
  579  k describe pod testpod2 -o
  580  k describe pod -o testpod2
  581  vi podmulcont.yml
  582  kubectl apply -f podmulcont.xml
  583  kubectl apply -f podmulcont.yml
  584  k get logs -f testpod3 -c c00
  585  k logs -f testpod3 -c c00
  586  k logs -f testpod3 -c c01
  587  vi podenv.yml
  588  k apply -f podenv.yml
  589  vi podenv.yml
  590  k apply -f podenv.yml
  591  vi podenv.yml
  592  k apply -f podenv.yml
  593  vi podenv.yml
  594  k apply -f podenv.yml
  595  vi podenv.yml
  596  k apply -f podenv.yml
  597  k get pods
  598  k apply -f podenv.yml
  599  vi podenv.yml
  600  k apply -f podenv.yml
  601  k exec environments -- env
  602  k logs -f environments
  603  k exec environments echo set : grep | greet
  604  k exec environments -- echo set : grep | greet
  605  k exec environments -- echo set:greet
  606  k logs -f environments
  607  k exec environments -- env
  608  k logs -f environments
  609  k get pod environment -o yaml
  610  k get pod environments -o yaml
  611  k describe pod environments
  612  k delete pod  environments
  613  vi podlabels.yml
  614  kubectl apply -f podlabels.yml
  615  kubectl get pods --show-labels
  616  k label pods labelspod version=1.0
  617  kubectl get pods --show-labels
  618  kubectl get pods -l env=prd
  619  kubectl get pods -l env in prd
  620  kubectl get pods -l env in (prd)
  621  kubectl get pods -l 'env in (prd)
  622  kubectl get pods -l 'env' in (prd)
  623  kubectl get pods -l 'env in (prd)'
  624  kubectl get pods --show-labels
  625  kubectl get pods -l  app=test,env=prd,version=1.0
  626  nodelabels.yml
  627  kind: Pod
  628  apiVersion: v1
  629  metadata:
  630    name: nodelabels
  631    labels:
  632      env: development
  633  spec:
  634      containers:
  635         - name: c00
  636           image: ubuntu
  637           command: ["/bin/bash", "-c", "while true; do echo Hello; sleep 5 ; done"]
  638      nodeSelector:                                           # specifies which node to run the pod
  639  vi nodelabels.yml
  640  kubectl apply -f nodelabels.yml
  641  kubectl describe node t2-medium
  642* kubectl get pods[A
  643  l get nodes
  644  l get node
  645  k get node
  646  vi nodelabels.yml
  647  k get node
  648  k get pods
  649  vi nodelabels.yml
  650  l get nodes
  651  k apply -f nodelabels.yml
  652  k delete nodelabels
  653  k pods
  654  k get pods
  655  k delete pod nodelabels
  656  k apply -f nodelabels.yml
  657  k get pods
  658  k get nodes
  659  vi nodelabels.yml
  660  k get pods
  661  k delete nodelabels
  662  k delete pod nodelabels
  663  k apply -f nodelabels.yml
  664  kubectl label nodes minikube hardware=t2-medium
  665  kubectl label nodes minikubkubectl describe node e hardware=t2-medium
  666  kubectl describe node vlmazjuly197
  667  kubectl get pods
  668  kubectl describe pods nodelabels
  669  vi nodelabels.yml
  670  kubectl label nodes minikube hardware=t2-medium
  671  kubectl label nodes minikube hardware=t2-mediumvlmazjuly197
  672  kubectl label nodes vlmazjuly197 hardware=t2-mediumvlmazjuly197
  673  kubectl label nodes vlmazjuly197 hardware=t2-medium
  674  kubectl label nodes vlmazjuly197 hardware=t2-medium --overwrite true
  675  k get pods
  676  k delete nodelabels
  677  k delete pod nodelabels
  678  kubectl apply -f nodelabels.yml
  679  kubectl label nodes vlmazjuly197 hardware=t2-medium
  680  kubectl describe node vlmazjuly197
  681  kubectl get pods
  682  vi rc.yml
  683  kubectl apply -f rc.yml
  684  kubectl get pods --show-labels
  685  kubectl get pods -l myname=Adam
  686  kubectl get rc
  687  kubectl delete pods Adam
  688  kubectl delete pods testpod6
  689  kubectl delete pod testpod6
  690  k get pods
  691  kubectl delete pods replicationcontroller-cdbrh
  692  kubectl describe rc replicationcontroller
  693  k ge pods
  694  k get pods
  695  kubectl scale --replicas=2 rc -l myname=Adam
  696  kubectl scale --replicas=3 rc -l myname=Adam
  697  k get pods
  698  kubectl delete rc replicationcontroller
  699  k get pods
  700  vi rc.yml
  701  vi rs.yml
  702  kubectl apply -f rs.yml
  703  kubectl get rs
  704  kubectl describe rs myreplicaset
  705  k get pods
  706  kubectl scale --replicas=1  rs/myreplicaset
  707  k get pods
  708  kubectl scale --replicas=2 rs -l myname=Adam
  709  k get pods
  710  kubectl scale --replicas=2 rs -l myname=Adam
  711  kubectl scale --replicas=2 rs/myreplicaset -l myname=Adam
  712  kubectl scale --replicas=2 rs -l myname=Adam
  713  kubectl scale --replicas=2 rs/myreplicaset -l myname=Adam
  714  kubectl scale --replicas=1  rs/myreplicaset
  715  kubectl scale --replicas=2  rs/myreplicaset
  716  k get pods
  717  kubectl delete rs myreplicaset
  718  minikube status
  719  clear
  720  minikube status
  721  k version
  722  alias k kubectl
  723  k get nodes
  724  k get pods
  725  k get pods --all-namespaces
  726  cd /etc
  727  kubectl get pods --all-namespaces
  728  kubectl describe node vlmazjuly197
  729  kubectl get nodes
  730  clear
  731  cd/
  732  cd /home
  733  cd e1075868/
  734  ls
  735  cd k8
  736  ls
  737  cat pod.yml
  738  clear
  739  cat pod.yml
  740  kubectl create -f pod.yml
  741  k get pods
  742  k delete pods testpod
  743  k get pods
  744  k delete pods testpod3
  745  k delete pods labelspod
  746  k delete pods nodelabels
  747  k get pods
  748  clear
  749  kubectl create -f pod.yml
  750  kubectl get pods
  751  kubectl get pods -o wide
  752  kubectl describe pod testpod
  753  kubectl logs -f testpod
  754  kubectl logs -f testpod -c c00
  755  k get pods
  756  cat pod.yml
  757  ls
  758  k delete pods testpod
  759  vi pod1.yml
  760  ls
  761  vi pod.yml
  762  kubectl create -f pod.yml
  763  vi pod.yml
  764  kubectl create -f pod.yml
  765  k get pods
  766  kubectl create -f podannotate.yml
  767  k describe pod testpod2
  768  k describe pod testpod
  769  clear
  770  kubectl apply -f podmulcont.yml
  771  k get pods
  772  kubectl logs -f testpod3 -c c00
  773  kubectl logs -f testpod3 -c c01
  774  kubectl exec testpod2 -- hostname -i
  775  kubectl exec testpod3 -- hostname -i
  776  kubectl exec testpod3 -- hostname -i -c c01
  777  kubectl exec testpod3 -c c01 -- hostname -i
  778  kubectl exec testpod3 -c c00 -- hostname -i
  779  kubectl exec testpod3 -c c00 -- ls -i
  780  clear
  781  kubectl apply -f podenv.yml
  782  kubectl exec environments -- env
  783  cat podenv.yml
  784  clear
  785  kubectl apply -f podlabels.yml
  786  cat podlabels.yml
  787  kubectl get pods
  788  kubectl get pods --show-labels
  789  kubectl label pods labelspod version=1.0
  790  kubectl get pods --show-labels
  791  kubectl get pods -l env=prd
  792  kubectl get pods -l  app=test,env=prd,version=1.0
  793  kubectl label pods testpod version=1.0
  794  kubectl get pods -l  app=test,env=prd,version=1.0
  795  kubectl get pods -l  version=1.0
  796  kubectl delete pods -l  version=1.0
  797  kubectl get pods
  798  kubectl get pods -l 'env in (prd)'
  799  kubectl get pods
  800  kubectl apply -f podlabels.yml
  801  kubectl get pods -l 'env in (prd)'
  802  kubectl get pods --show-labels
  803  ls -lrt
  804  history
[root@vlmazjuly197 k8]# ls -lrt
total 32
-rw-r--r-- 1 root root 344 Nov 22 21:13 podannotate.yml
-rw-r--r-- 1 root root 313 Nov 22 21:18 podmulcont.yml
-rw-r--r-- 1 root root 338 Nov 22 21:30 podenv.yml
-rw-r--r-- 1 root root 330 Nov 22 21:44 podlabels.yml
-rw-r--r-- 1 root root 388 Nov 22 22:02 nodelabels.yml
-rw-r--r-- 1 root root 682 Nov 22 22:15 rc.yml
-rw-r--r-- 1 root root 861 Nov 22 22:27 rs.yml
-rw-r--r-- 1 root root 688 Nov 23 00:20 pod.yml
[root@vlmazjuly197 k8]# cd /etc/yum.repos.d/
[root@vlmazjuly197 yum.repos.d]# vi docker-ce.repo
[root@vlmazjuly197 yum.repos.d]#
