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
  
  def create_user_session
  	user_id = params[:user_id]
  	remember_token = params[:remember_token]
  	flag = false
  	if user = User.find_by(:id => user_id)
        if user.authenticated? remember_token
          session[:user_id] = user_id
          @current_user = login_user(user)
          flag = true
        end
    end
    if flag
    	render plain: "OK"
    else
    	render plain: "ERROR"
    end
    
  end
  
  def create_group
  	name = params[:name]
  	password = params[:password]
		@group = Group.new(:name => name)
		if params[:public] == 'false'
			@group.public_group = false
			@group.password = password
		end
		
		if @group.save
			render plain:"OK"
		else
			render plain:"ERROR"
		end
  end

  def init_session
    session[:api_key] = 1 
    render plain:"OK"
  end
  
  private
  def authenticate_api
  	if session[:api_key].nil?
  		render nothing: true
  	end
  end
end
