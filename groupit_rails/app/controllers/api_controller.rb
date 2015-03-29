class ApiController < ApplicationController
  include ApiHelper
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
      render json:['OK', user.id,user.remember_token]
    else
      render json:['Error', user.errors.as_json(full_messages: true)]
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
    	render json: ["OK"]
    else
    	render json: ["ERROR"]
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
			render json: ["OK", @group.id, @group.join_token]
		else
			render json: ["ERROR", @group.errors.as_json(full_messages: true)]
		end
  end

  def init_session
    session[:api_key] = 1
    render plain:"OK"
  end

  def list_groups
    list = Group.limit(30).pluck(:id,:name,:public_group)
    render json: list
  end

  def join_group
  	@group = Group.find(params[:id])
    password = params[:password]

    if user_allow_access? @group
    	render plain:"ALLOWED"
    elsif @group.public_group
      user_join @group
    	render plain:"AUTHORIZED"
    elsif password.nil?
    	render plain:"REQUIRE PASSWORD"
    elsif  @group.authenticated?(password)
      user_join @group
    	render plain:"AUTHORIZED"
    else
    	render plain:"WRONG PASSWORD"
    end
  end
end
