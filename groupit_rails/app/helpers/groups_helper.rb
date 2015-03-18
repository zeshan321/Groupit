module GroupsHelper
  def user_allow_access? group
    if user_signed_in?
      return group.users.exists?(id:current_user.id)
    else
      return false
    end
  end

  def user_join group
    unless user_allow_access? group
      group.users << current_user
    end
  end

  def authenticate_access group
    unless user_allow_access? group
      if group.public_group and user_signed_in?
        user_join group
      else
        redirect_to join_group_path(group.id)
      end
    end
  end
end
