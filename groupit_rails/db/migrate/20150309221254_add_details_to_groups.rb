class AddDetailsToGroups < ActiveRecord::Migration
  def change
  	add_column :groups, :name, :string
  	add_column :groups, :public, :boolean, {:default => true, :null => false}
  	add_column :groups, :password_digest, :string
  end
end