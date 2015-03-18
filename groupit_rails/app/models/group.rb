class Group < ActiveRecord::Base
  #include ActiveModel::Validations
  #require NameValidator
  attr_accessor :password

  has_many :messages
  has_and_belongs_to_many :users

  validates :name, presence: true, uniqueness: {:case_sensitive => false}, length: { minimum: 3, maximum: 64}
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
    token = SecureRandom.urlsafe_base64(8) + self.id.to_s(36)
    update_attribute :join_token, token
  end

  def quick_join_url(path)
    'http://'+ path + '/join/' + self.join_token
  end

  def generate_qr_image(path)
    qr = RQRCode::QRCode.new(quick_join_url(path), :size => 6, :level => :h )
    png = qr.to_img.resize(200,200).to_data_url
  end

  def self.search(keywords,limit=30)
    if keywords.length > 0
      query = '( name LIKE ? )'
      (keywords.length - 1).times do
        query << ' OR ( name LIKE ? )'
      end
      search_params = [query] + keywords.map{ |k| k = "%#{sanitize_sql_like(k)}%" }
      self.where(search_params).limit(limit).pluck(:id, :name, :public_group)
    else
      none
    end
  end
end
