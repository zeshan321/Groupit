class CreateGroups < ActiveRecord::Migration
  def change
    create_table :groups do |t|
      t.string :name, null: false, index: true

      t.boolean :public_group, default: true, null: false
      t.string :password_digest

      t.string :join_token, index: true
      t.timestamps null: false
    end

    create_table :groups_users, id: false do |t|
      t.belongs_to :groups, index: true
      t.belongs_to :users, index: true
    end

  end


end
