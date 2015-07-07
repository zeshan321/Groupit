class Member < ActiveRecord::Base
  has_one :user
  validates :email, presence: true, format: /.+@.+\..+/i, uniqueness:true
  has_secure_password
end
