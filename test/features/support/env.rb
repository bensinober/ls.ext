# encoding: utf-8

require 'rspec'
require 'yaml'

if !File.exists? '/srv/pillar/koha/admin.sls'
  puts "missing koha minion admin.sls; aborting"
  Cucumber.wants_to_quit = true
else
  SETTINGS = YAML.load_file('/srv/pillar/koha/admin.sls')
end

# Extending World object
# Methods are inherited by all steps
require_relative 'paths.rb'
require_relative 'featurestack.rb'
World(Paths,FeatureStack)