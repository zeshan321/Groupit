class FixGroupsUsersColumnsName < ActiveRecord::Migration
  def change
    change_table :groups_users do |t|
      t.rename :groups_id, :group_id
      t.rename :users_id, :user_id
    end
  end
end
