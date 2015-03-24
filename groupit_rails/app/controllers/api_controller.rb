class ApiController < ApplicationController
  skip_before_action :verify_authenticity_token

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

  def init_session
    
  end
end
