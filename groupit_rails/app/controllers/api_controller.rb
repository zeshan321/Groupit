class ApiController < ApplicationController
  skip_before_action :verify_authenticity_token
  before_action :authenticate_api, except: [:init_session]

  def create_user
    name = params[:name]
    allow_cookies = (params[:allow_cookies] == 'false')? false : true

    user = User.new name: name
    if user.save
      user.generate_remember_token
      if allow_cookies
        cookies.permanent[:remember_token] = user.generate_remember_token
        cookies.permanent.signed[:user_id] = user.id
      end
      login_user user
      flag = true
      render json:['OK',user.id,user.remember_token], status:202

    else

      render json:['Error'], status:422

    end
  end
  
  def login_user
  	user_id = params[:user_id]
  	remember_token = params[:remember_token]
  	if user = User.find_by(:id => user_id)
        if user.authenticated? remember_token
          session[:user_id] = user_id
          @current_user = login_user(user)
          render plain: "OK"
  end

  def init_session
    session[:api_key] = 1 
  end
  
  private
  def authenticate_api
  	if session[:api_key].nil?
  		render nothing: true
  	end
  end
end
