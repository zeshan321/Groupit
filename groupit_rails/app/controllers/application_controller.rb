class ApplicationController < ActionController::Base
  # Prevent CSRF attacks by raising an exception.
  # For APIs, you may want to use :null_session instead.
  protect_from_forgery with: :exception

  include UsersHelper

  def page_not_found
    render :file => "#{Rails.root}/public/404.html",  :status => 404
  end

  def page_unauthorized
    render :file => "#{Rails.root}/public/401.html",  :status => 401
  end
end
