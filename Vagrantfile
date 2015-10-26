# -*- mode: ruby -*-
# vi: set ft=ruby :

VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.vm.define "ubuntu" do |ubuntu|
    ubuntu.vm.box = "precise64"
    ubuntu.vm.synced_folder "samples", "/home/vagrant/samples"
    ubuntu.vm.provision :shell, :path => 'provision.sh'
  end
  config.vm.define "centos" do |centos|
    centos.vm.box = "nrel/CentOS-6.5-x86_64"
    centos.vm.synced_folder "samples", "/home/vagrant/samples"
    centos.vm.provision :shell, :path => 'provision-centos.sh'
  end
end
