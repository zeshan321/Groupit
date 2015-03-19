class User < ActiveRecord::Base
  attr_accessor :remember_token

  has_and_belongs_to_many :groups

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
=begin
  def socket_connected
    active_groups = self.groups.pluck(:id)
    active_groups.each do |group_id|
      Group.increment_counter(:active_users_count,group_id)
    end
    active_groups
  end

  def socket_disconnected
    active_groups = self.groups.pluck(:id)
    active_groups.each do |group_id|
      Group.decrement_counter(:active_users_count,group_id)
    end
    active_groups
  end
=end
end
