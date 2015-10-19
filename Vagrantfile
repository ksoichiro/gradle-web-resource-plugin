# -*- mode: ruby -*-
# vi: set ft=ruby :

VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.vm.box = "precise64"
  config.vm.synced_folder "samples", "/home/vagrant/samples"
  config.vm.provision :shell, :path => 'provision.sh'
end
