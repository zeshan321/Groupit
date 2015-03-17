module GroupsHelper
  def user_allow_access? group
    if user_signed_in?
      if group.public_group
        return true
      else
        return group.users.exists?(id:current_user.id)
      end
    else
      return false
    end
  end

  def user_join group
    unless group.users.exists?(id:current_user.id)
      group.users << current_user
    end
  end

  def authenticate_access group
    unless user_allow_access? group
      redirect_to join_group_path(group.id)
    end
  end
end
