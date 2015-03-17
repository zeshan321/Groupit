class CreateMembers < ActiveRecord::Migration
  def change
    create_table :members do |t|
      t.string :email, null: false, index: true
      t.string :password_digest, null: false

      t.timestamps null: false
    end
  end
end
