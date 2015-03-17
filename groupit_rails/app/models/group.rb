class Group < ActiveRecord::Base
  #include ActiveModel::Validations
  #require NameValidator
  attr_accessor :password

  has_many :messages
  has_and_belongs_to_many :users

  validates :name, presence: true, uniqueness: true, length: { minimum: 3, maximum: 64}
  validates_with NameValidator

  validates :password, presence: true, unless: :public_group

  def password= (new_password)
    if new_password.nil? or new_password.empty?
      self.password_digest = nil
    else
      @password = new_password
      cost = ActiveModel::SecurePassword.min_cost ? BCrypt::Engine::MIN_COST : BCrypt::Engine.cost
      self.password_digest = BCrypt::Password.create(new_password, cost: cost)
    end
  end

  def authenticated?(password)
    BCrypt::Password.new(password_digest).is_password?(password)
  end

  def generate_join_token
    token = random_token
    i = 0
    #Retry until the unique join_token is found
    while Group.exists?(:join_token => token)
      i += 1
      length = (0.05*i*i + 6).floor
      token = random_token(length)
    end
    update_attribute :join_token, token
  end

  def quick_join_url(path)
    'http://'+ path + '/join/' + self.join_token
  end

  def generate_qr_image(path)
    qr = RQRCode::QRCode.new(quick_join_url(path), :size => 4, :level => :h )
    png = qr.to_img.resize(200,200).to_data_url
  end

  private
  def random_token(length=6)
    chars = 'abcdefghkmnpqrstwxyz23456789'
    token = ''
    length.times { token << chars[rand(chars.size)] }
    token
  end
end
