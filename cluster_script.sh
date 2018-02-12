
#FIRST WE NEED TO SET CONFIG FILE FOR EASE CONNECTION

#home node
scp -i pk_First.pem pk_First.pem nnode:~/.ssh/ 
scp -i pk_First.pem config nnode:~/.ssh/ 
scp -i pk_First.pem pk_First.pem  dnode1:~/.ssh/
scp -i pk_First.pem pk_First.pem  dnode2:~/.ssh/
scp -i pk_First.pem pk_First.pem  dnode3:~/.ssh/
scp -i pk_First.pem pk_First.pem  dnode4:~/.ssh/
scp -i pk_First.pem pk_First.pem  dnode5:~/.ssh/
scp -i pk_First.pem pk_First.pem  dnode6:~/.ssh/
scp -i pk_First.pem pk_First.pem  dnode7:~/.ssh/
scp -i pk_First.pem config  dnode1:~/.ssh/
scp -i pk_First.pem config  dnode2:~/.ssh/
scp -i pk_First.pem config  dnode3:~/.ssh/
scp -i pk_First.pem config  dnode4:~/.ssh/
scp -i pk_First.pem config  dnode5:~/.ssh/
scp -i pk_First.pem config  dnode6:~/.ssh/
scp -i pk_First.pem config  dnode7:~/.ssh/

ssh nnode

ssh-keygen -f ~/.ssh/id_rsa -t rsa -P ""
cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys

ssh dnode1 'cat >> ~/.ssh/authorized_keys' < ~/.ssh/id_rsa.pub
ssh dnode2 'cat >> ~/.ssh/authorized_keys' < ~/.ssh/id_rsa.pub
ssh dnode3 'cat >> ~/.ssh/authorized_keys' < ~/.ssh/id_rsa.pub
ssh dnode4 'cat >> ~/.ssh/authorized_keys' < ~/.ssh/id_rsa.pub
ssh dnode5 'cat >> ~/.ssh/authorized_keys' < ~/.ssh/id_rsa.pub
ssh dnode6 'cat >> ~/.ssh/authorized_keys' < ~/.ssh/id_rsa.pub
ssh dnode7 'cat >> ~/.ssh/authorized_keys' < ~/.ssh/id_rsa.pub

ssh dnode1

exit

