module GroupsHelper
  def user_allow_access? group
    if group.public_group
      return true
    else
      return group.users.exists?(current_user)
    end
  end

  def user_join group
    unless group.users.exists?(current_user)
      group.users << current_user
    end
  end

  def authenticate_access group
    unless user_allow_access? group
      redirect_to join_group_path(group.id)
    end
  end
end
