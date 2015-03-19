class AddActiveUsersCountToGroups < ActiveRecord::Migration
  def change
    add_column :groups, :active_users_count, :integer, default: 0, null: false
  end
end
