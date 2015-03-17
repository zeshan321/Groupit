class Message < ActiveRecord::Base
  validates :text, presence: true
  belongs_to :group, required: true
  belongs_to :user, required: true
end
