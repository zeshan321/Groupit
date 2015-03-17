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
    if @user.id != params[:id].to_i
      page_unauthorized
    end
  end

  def update
    @user = current_user
    old_name = @user.name
    @user.name = params.require(:user)[:name]
    if @user.save
      redirect_to root_path
    else
      @user.name = old_name
      render :edit
    end
  end
end
