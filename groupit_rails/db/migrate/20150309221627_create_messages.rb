class CreateMessages < ActiveRecord::Migration
  def change
    create_table :messages do |t|
      t.string :content
			t.belongs_to :user
			t.belongs_to :group
      t.timestamps null: false
    end
  end
end
