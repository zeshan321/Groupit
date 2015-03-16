class UsersController < ApplicationController
  def new
    @user = User.new
  end

  def create
    @user = User.new params.require(:user).permit(:name)
    if @user.save
      @user.generate_remember_token
      cookies.permanent[:remember_token] = @user.generate_remember_token
      cookies.permanent.signed[:user_id] = @user.id
      login_user @user
      if session[:back_url].nil?
        redirect_to root_path
      else
        back_url = session[:back_url]
        session[:back_url] = nil
        redirect_to back_url
      end
    else
      render :new
    end
  end

  def edit
    @user = current_user
    if @user.id != params[:id]
      page_unauthorized
    end
  end

  def update
    @user = current_user
    if @user.update_attributes(:name => params.require(:user)[name])
    end
  end
end
