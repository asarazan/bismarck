Pod::Spec.new do |spec|
  spec.name         = 'bismarck'
  spec.version      = '0.0.1'
  spec.license      = { :type => "MIT", :file => "LICENSE.txt" }
  spec.homepage     = 'https://github.com/asarazan/bismarck'
  spec.authors      = { 
                       'Aaron Sarazan' => 'aaron@sarazan.net',
                       'Tre Murillo' => 'treelzebub@gmail.com'
  }
  spec.summary      = "A WIP caching library for kotlin"
  spec.source       = { :git => 'https://github.com/asarazan/bismarck.git', :tag => 'master' }
  spec.platform     = :ios, "9.0"
  spec.vendored_frameworks    = 'outputs/bismarck.framework'
end
