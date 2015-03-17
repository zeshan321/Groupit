class CreateUsers < ActiveRecord::Migration
  def change
    create_table :users do |t|
      t.string :name, null: false
      t.string :remember_digest
      t.belongs_to :member, index: true

      t.timestamps null: false
    end
  end
end
