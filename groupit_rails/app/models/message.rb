class Message < ActiveRecord::Base
  validates :text, presence: true, unless: :has_image
  validates :image_height, presence: true, if: :has_image
  validates :image_width, presence: true, if: :has_image
  belongs_to :group, required: true
  belongs_to :user, required: true

  mount_uploader :image, ImagesUploader

  def has_image
    self.image.present?
  end

  def limit_img_tag_size(max_length)
    ratio = self.image_width.to_f/self.image_height.to_f
    new_width = self.image_width
    new_height = self.image_height

    if self.image_width >= self.image_height
      if self.image_width >  max_length
        new_width = max_length
        new_height = (new_width/ratio).to_i
      end
    else
      if self.image_height >  max_length
        new_height = max_length
        new_width = (new_height*ratio).to_i
      end
    end
    [new_width,new_height]
  end
end
