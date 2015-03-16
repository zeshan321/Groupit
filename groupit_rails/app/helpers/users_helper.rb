module UsersHelper
  def login_user(user)
    session[:user_id] = user.id
    @current_user = user
  end

  def current_user
    if !@current_user.nil?
      @current_user

    elsif user_id = session[:user_id]
      @current_user = User.find_by(:id => user_id)
      if @current_user.nil?
        session[:user_id] = nil
      end
      @current_user

    elsif user_id = cookies.signed[:user_id]
      if user = User.find_by(:id => user_id)
        if user.authenticated? cookies[:remember_token]
          session[:user_id] = user_id
          @current_user = login_user(user)
        end
      end
    end
  end

  def user_signed_in?
    !current_user.nil?
  end

  def logout_user
    @current_user = nil
    session[:user_id] = nil
  end

  def authenticate_user
    unless user_signed_in?
      #flash[:notice] = "You need a display name to join groups."
      session[:back_url] = request.fullpath
      redirect_to new_user_path
    end
  end
end
