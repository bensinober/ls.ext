###########
# KOHA SPECIFIC CONFIG
###########

apache2:
  service.dead

/var/migration_workdir:
  file.directory:
    - user: root
    - group: root
    - dir_mode: 755
    - file_mode: 644
    - recurse:
      - user
      - group
      - mode

###########
# Services specific config
###########

services_gradle_build_docker_image:
  cmd.run:
    - name: ./gradlew -PdockerUrl={{pillar['dockerUrl']}} dockerBuildImage
    - cwd: /vagrant/redef/services
    - user: vagrant

##########
# Elasticsearch specifig config
##########

/etc/elasticsearch.yml:
  file.managed:
    - source: salt://elasticsearch/config/elasticsearch.yml
    - user: root
    - group: root
    - file_mode: 644

##########
# Overview specifig config
##########

/var/www:
  file.directory

/var/www/overview:
  file.directory:
    - user: root
    - group: root
    - dir_mode: 755
    - file_mode: 644
    - recurse:
      - user
      - group
      - mode
    - require:
      - file: /var/www

/var/www/overview/index.html:
  file.managed:
    - source: salt://overview/files/index.template.html
    - template: jinja
    - require:
      - file: /var/www/overview

/var/www/overview/logo.png:
  file.managed:
    - source: salt://overview/files/logo.png
    - template: jinja
    - require:
      - file: /var/www/overview

docker_compose_up:
  cmd.run:
    - name: docker-compose up -d
    - cwd: /vagrant/docker-compose
    - require:
      - cmd: services_gradle_build_docker_image

