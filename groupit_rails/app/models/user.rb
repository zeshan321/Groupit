class User < ActiveRecord::Base
  attr_accessor :remember_token

  has_many :messages

  validates :name, presence: true, length: { minimum: 3, maximum: 32}
  validates_with NameValidator

  def generate_remember_token
    self.remember_token = SecureRandom.urlsafe_base64

    cost = ActiveModel::SecurePassword.min_cost ? BCrypt::Engine::MIN_COST : BCrypt::Engine.cost

    update_attribute(:remember_digest, BCrypt::Password.create(self.remember_token, cost: cost))

    self.remember_token
  end

  def authenticated?(remember_token)
    BCrypt::Password.new(remember_digest).is_password?(remember_token)
  end
end
