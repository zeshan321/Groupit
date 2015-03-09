class CreateRelationsBetweenGroupsAndUsers < ActiveRecord::Migration
  def change
    create_table :groups_users, id: false do |t|
    	t.belongs_to :groups, index: true
    	t.belongs_to :users, index: true
    end
  end
end
