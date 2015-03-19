class AddImageDimensionsToMessages < ActiveRecord::Migration
  def change
    add_column :messages, :image_width, :integer
    add_column :messages, :image_height, :integer
  end
end
