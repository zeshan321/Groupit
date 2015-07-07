class CreateMessages < ActiveRecord::Migration
  def change
    create_table :messages do |t|
      t.string :text, null: false
      t.belongs_to :group, null: false, index: true
      t.belongs_to :user

      t.timestamps null: false
    end
  end
end
