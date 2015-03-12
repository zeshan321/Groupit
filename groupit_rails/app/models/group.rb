class Group < ActiveRecord::Base
	has_secure_password validations: false
	has_and_belongs_to_many :users
	has_many :messages
end
